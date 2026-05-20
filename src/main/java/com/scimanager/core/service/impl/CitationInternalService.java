package com.scimanager.core.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scimanager.core.entity.Paper;
import com.scimanager.core.repository.PaperRepository;
import com.scimanager.core.service.UserInterestsService;

import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 内部文献分析服务
 *
 * <p>负责 PDF 文本解析、与 DeepSeek AI 交互实现元数据提取、关键词生成及 GB/T 7714 引文格式化。<br>
 * 所有 AI 调用均为异步执行，不阻塞文献上传主流程。</p>
 *
 * <p><b>核心能力：</b></p>
 * <ul>
 *   <li>PDF 文本提取（Apache PDFBox，限前 10 页 20000 字符）</li>
 *   <li>元数据提取（标题、作者、来源、年份、DOI）→ JSON 解析 → 填充 Paper 实体</li>
 *   <li>学术关键词提取（语义合并、规范化）</li>
 *   <li>GB/T 7714-2015 格式引文生成</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CitationInternalService {

	private final UserInterestsService userInterestsService;

	@Value("${deepseek.api-key}")
	private String apiKey;

	private static final String API_URL = "https://api.deepseek.com/chat/completions";
	private final OkHttpClient httpClient = new OkHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 将 JsonNode 中的元数据字段映射到 Paper 实体
	 *
	 * @param node  从 AI 返回的 JSON 中解析得到的节点
	 * @param paper 待填充的目标 Paper 实体
	 */
	private void mapJsonToPaper(JsonNode node, Paper paper) {
		paper.setPaperTitle(node.path("title").asText(null));
		paper.setPaperAuthors(node.path("authors").asText(null));
		paper.setPaperSourcePublications(node.path("source").asText(null));
		paper.setPaperPublicationYear(node.path("year").asText(null));
		paper.setPaperDoi(node.path("doi").asText(null));
	}

	/**
	 * 提取 PDF 文本内容
	 *
	 * <p>使用 Apache PDFBox 解析 PDF，提取前 10 页文本，最大 20000 字符。<br>
	 * 额外检测文本中的 DOI 号并附加到结果前缀。</p>
	 *
	 * @param filePath PDF 文件的绝对路径
	 * @return 提取的文本内容（含 DOI 前缀）
	 * @throws IOException 文件读取或解析失败时抛出
	 */
	public String extractPdfText(String filePath) throws IOException {
		try (PDDocument document = PDDocument.load(new File(filePath))) {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setStartPage(1);
			stripper.setEndPage(Math.min(10, document.getNumberOfPages())); // 最多解析 10 页
			String text = stripper.getText(document);

			// 检测 DOI 号（正则匹配 "10.xxxx/xxxx" 模式）
			Pattern doiPattern = Pattern.compile("10\\.\\d{4,9}/[-._;()/:A-Z0-9]+", Pattern.CASE_INSENSITIVE);
			Matcher matcher = doiPattern.matcher(text);
			String doiPrefix = matcher.find() ? "检测到的DOI参考: " + matcher.group(0) + "\n" : "";

			// 截断过长的文本（限制 20000 字符）
			return doiPrefix + (text.length() > 20000 ? text.substring(0, 20000) : text);
		}
	}

	/**
	 * 通过 AI 生成 GB/T 7714-2015 格式引文
	 *
	 * @param paperText 论文文本内容
	 * @return 格式化后的引文字符串
	 * @throws Exception AI 调用或解析失败时抛出
	 */
	public String getAiCitation(String paperText) throws Exception {
		String prompt = """
				你是一个专业的学术助手。请从给定的论文文本中提取关键元数据。

				任务要求：
				1. 提取：题目、作者、期刊名/会议名、年份、卷号、期号。
				2. 特别寻找 DOI 号：它通常以 '10.' 开头，可能隐藏在页脚或摘要附近。
				3. 按照中国国家标准 GB/T 7714-2015 格式生成一条规范的参考文献。
				4. 如果存在 DOI，必须在引文末尾追加 "DOI: [具体号码]"。
				5. 直接输出引文条目，不要开场白，不要解释。

				论文文本内容：
				---
				%s
				---
				""".formatted(paperText);

		String systemPrompt = "你是一个严格遵守 GB/T 7714-2015 标准的学术引文生成器。";
		String rawResponse = callDeepSeek(systemPrompt, prompt);
		return cleanCitation(rawResponse); // 后处理清洗
	}

	/**
	 * 通过 AI 提取论文关键词（3-5 个）
	 *
	 * <p>语义合并同义词，规范化术语，返回关键词列表。</p>
	 *
	 * @param paperText 论文文本内容
	 * @return 关键词列表（逗号分隔解析）
	 * @throws Exception AI 调用失败时抛出
	 */
	public List<String> getKeywordsFromAi(String paperText) throws Exception {
		String prompt = """
				    你是一个学术论文分析专家。请阅读以下论文片段，并提取出 3-5 个最能代表本文研究内容的关键词。

				   任务要求：
				1. 语义合并：如果存在意义相近或范畴重叠的词（例如"人工智能"与"AI"、"卷积神经网络"与"CNN"），请仅保留一个最正式、最通用的学术标准名词。
				2. 规范化：优先使用该领域的公认术语，避免使用口语化或过于细碎的描述。
				3. 覆盖维度：关键词应涵盖研究领域、核心技术及研究对象。
				4. 输出格式：直接输出关键词，多个关键词之间仅使用中文逗号（，）分隔，不要包含任何解释、前导词或编号。

				    论文文本内容：
				    ---
				    %s
				    ---
				    """.formatted(paperText);

		String systemPrompt = "你是一个精通学术本体论和术语规范化的 AI 助手。你的任务是提取具有高代表性且无冗余的学术关键词。";
		String rawResponse = callDeepSeek(systemPrompt, prompt);
		// 按中文或英文逗号分割
		return Arrays.asList(rawResponse.replace(" ", "").split("[,，]"));
	}

	/**
	 * 从论文文本中提取元数据并填充到 Paper 实体
	 *
	 * <p>调用 AI 提取标题、作者、来源、年份、DOI，解析返回的 JSON 并映射到 Paper 实体。<br>
	 * 包含 JSON 解析容错：如果直接解析失败，尝试去掉 Markdown 代码块标记后重试。</p>
	 *
	 * @param paperText 论文文本内容
	 * @param paper     待填充的 Paper 实体
	 * @throws Exception JSON 解析失败时抛出
	 */
	public void extractAndPopulateMetadata(String paperText, Paper paper) throws Exception {
		String prompt = """
				你是一个学术元数据提取专家。请从给定的论文文本中提取以下字段，并以严格的 JSON 格式输出。

				字段说明：
				1. title: 论文标题
				2. authors: 作者列表，多人用逗号分隔
				3. source: 来源期刊或会议名称
				4. year: 出版年份 (仅数字)
				5. doi: DOI号 (如有)
				6. abstract: 论文摘要 (尽可能提取完整)

				任务要求：
				- 如果某个字段无法识别，请填入 null。
				- 只输出 JSON 代码块，不要有任何解释文字。
				- JSON 格式必须合法。

				论文文本：
				---
				%s
				---
				""".formatted(paperText);

		String systemPrompt = "你是一个专门负责解析学术论文元数据的机器人。你只输出 JSON。";

		String rawJson = callDeepSeek(systemPrompt, prompt);

		// 尝试直接解析 JSON
		try {
			JsonNode node = objectMapper.readTree(rawJson);
			mapJsonToPaper(node, paper);
		} catch (Exception e) {
			// 容错：去掉 Markdown 代码块标记后重试
			String cleanedJson = rawJson.replaceAll("(?s)```json|```", "").trim();
			try {
				JsonNode node = objectMapper.readTree(cleanedJson);
				mapJsonToPaper(node, paper);
			} catch (Exception secondEx) {
				System.err.println("解析 AI 返回的 JSON 失败。原始文本: " + rawJson);
				throw new RuntimeException("无法解析论文元数据 JSON 格式", secondEx);
			}
		}
	}

	/**
	 * 调用 DeepSeek API（通用封装）
	 *
	 * <p>使用 OkHttp 发送 POST 请求，Jackson 构建/解析 JSON。</p>
	 *
	 * @param systemContent 系统提示词
	 * @param userContent   用户提示词
	 * @return AI 回复的文本内容
	 * @throws Exception 网络或 API 异常时抛出
	 */
	private String callDeepSeek(String systemContent, String userContent) throws Exception {
		String jsonBody = objectMapper.createObjectNode()
				.put("model", "deepseek-chat")
				.put("temperature", 0.0)
				.set("messages", objectMapper.createArrayNode()
						.add(objectMapper.createObjectNode().put("role", "system").put("content", systemContent))
						.add(objectMapper.createObjectNode().put("role", "user").put("content", userContent)))
				.toString();

		RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
		Request request = new Request.Builder()
				.url(API_URL)
				.addHeader("Authorization", "Bearer " + apiKey)
				.post(body)
				.build();

		try (Response response = httpClient.newCall(request).execute()) {
			if (!response.isSuccessful())
				throw new IOException("DeepSeek API 响应失败: " + response);
			JsonNode root = objectMapper.readTree(response.body().string());
			return root.path("choices").get(0).path("message").path("content").asText().trim();
		}
	}

	/**
	 * 引文清洗与格式化
	 *
	 * <p>对 AI 返回的原始引文进行后处理：</p>
	 * <ul>
	 *   <li>匹配引文主体（含期刊标识和年份的部分）</li>
	 *   <li>全角符号 → 半角符号（逗号、冒号、句号、分号、括号）</li>
	 *   <li>去除 Markdown 标记（*、_）</li>
	 *   <li>规范化标点前后空格</li>
	 *   <li>确保以句号结尾</li>
	 * </ul>
	 *
	 * @param aiResponse AI 返回的原始引文字符串
	 * @return 清洗后的引文字符串
	 */
	public String cleanCitation(String aiResponse) {
		// 匹配模式：可选引用编号 + 引文主体（含期刊标识符如 [J] 和年份）
		Pattern pattern = Pattern.compile("(\\[\\d+\\])?\\s*([^。\\n]+?\\\\[[A-Z/]+\\\\][^。\\n]+?\\\\d{4}.+)");
		Matcher matcher = pattern.matcher(aiResponse);
		if (!matcher.find())
			return aiResponse;

		String refNum = matcher.group(1) != null ? matcher.group(1) : "";
		String citation = matcher.group(2).trim();

		// 全角 → 半角
		citation = citation.replace("，", ",").replace("：", ":").replace("。", ".")
				.replace("；", ";").replace("（", "(").replace("）", ")");

		// 去除 Markdown 标记，规范化标点空格
		citation = citation.replaceAll("[*_]", "")
				.replaceAll("\\s*([,.:;?+])\\s*", "$1")
				.replaceAll("\\s*(\\[[A-Z/]+\\])\\s*", "$1");

		return refNum + citation.replaceAll("\\.+$", "") + ".";
	}

	/**
	 * 【异步】处理文献元数据（入口方法）
	 *
	 * <p>在文献上传后异步执行：</p>
	 * <ol>
	 *   <li>提取 PDF 文本</li>
	 *   <li>调用 AI 提取元数据并填充 Paper</li>
	 *   <li>调用 AI 生成 GB/T 7714 引文</li>
	 *   <li>调用 AI 提取关键词</li>
	 *   <li>保存更新后的 Paper 到数据库</li>
	 *   <li>刷新用户的兴趣画像</li>
	 * </ol>
	 *
	 * @param paperId       文献 ID
	 * @param pdfPath       PDF 文件绝对路径
	 * @param repository    PaperRepository（通过参数注入避免循环依赖）
	 * @param paperOwnerId  文献所有者 ID（用于刷新兴趣画像）
	 */
	@Async
	public void processMetadataAsync(Long paperId, String pdfPath, PaperRepository repository, String paperOwnerId) {
		try {
			// 步骤 1：提取 PDF 文本
			String text = extractPdfText(pdfPath);

			// 步骤 2-5：解析并填充元数据（在 repository.findById 回调中执行）
			repository.findById(paperId).ifPresent(paper -> {
				try {
					// 提取元数据并填充 Paper 实体
					extractAndPopulateMetadata(text, paper);
					// 生成 GB/T 7714 引文
					String citation = getAiCitation(text);
					paper.setPaperCitation(citation);
					// 提取关键词
					List<String> keywords = getKeywordsFromAi(text);
					paper.setKeyWords(keywords);
					// 保存到数据库
					repository.save(paper);
					// 刷新用户的兴趣画像（异步）
					userInterestsService.makeUserInterestProfile(paperOwnerId);
				} catch (Exception e) {
					System.err.println("解析论文 ID " + paperId + " 时出错: " + e.getMessage());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
