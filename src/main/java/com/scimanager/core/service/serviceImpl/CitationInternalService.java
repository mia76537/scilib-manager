package com.scimanager.core.service.serviceImpl;

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
import com.scimanager.core.repository.PaperRepository;
import com.scimanager.core.service.UserInterestsService;

import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 内部文献分析服务 负责解析 PDF 文本、与 DeepSeek API 交互以及格式化数据
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
	 * 1. 提取 PDF 内容 (前十页，限 10000 字)
	 */
	public String extractPdfText(String filePath) throws IOException {
		try (PDDocument document = PDDocument.load(new File(filePath))) {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setStartPage(1);
			stripper.setEndPage(Math.min(10, document.getNumberOfPages()));
			String text = stripper.getText(document);

			// DOI 预扫描 (10.xxxx/xxxx)
			Pattern doiPattern = Pattern.compile("10\\.\\d{4,9}/[-._;()/:A-Z0-9]+", Pattern.CASE_INSENSITIVE);
			Matcher matcher = doiPattern.matcher(text);
			String doiPrefix = matcher.find() ? "检测到的DOI参考: " + matcher.group(0) + "\n" : "";

			return doiPrefix + (text.length() > 20000 ? text.substring(0, 20000) : text);
		}
	}

	/**
	 * 2. 生成 GB/T 7714 引文
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
		return cleanCitation(rawResponse);
	}

	/**
	 * 3. 提取论文关键词 (新增功能)
	 */
	public List<String> getKeywordsFromAi(String paperText) throws Exception {
		String prompt = """
				    你是一个学术论文分析专家。请阅读以下论文片段，并提取出 3-5 个最能代表本文研究内容的关键词。

				   任务要求：
				1. 语义合并：如果存在意义相近或范畴重叠的词（例如“人工智能”与“AI”、“卷积神经网络”与“CNN”），请仅保留一个最正式、最通用的学术标准名词。
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

		// 将 AI 返回的字符串按中文逗号或英文逗号分割成列表
		return Arrays.asList(rawResponse.replace(" ", "").split("[,，]"));
	}

	/**
	 * 4. 核心：封装 API 调用逻辑 (OkHttp + Jackson)
	 */
	private String callDeepSeek(String systemContent, String userContent) throws Exception {
		String jsonBody = objectMapper.createObjectNode().put("model", "deepseek-chat").put("temperature", 0.0) // 核心：设为
																												// 0
																												// 以获得最稳定的结果
				.set("messages", objectMapper.createArrayNode()
						.add(objectMapper.createObjectNode().put("role", "system").put("content", systemContent))
						.add(objectMapper.createObjectNode().put("role", "user").put("content", userContent)))
				.toString();

		RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
		Request request = new Request.Builder().url(API_URL).addHeader("Authorization", "Bearer " + apiKey).post(body)
				.build();

		try (Response response = httpClient.newCall(request).execute()) {
			if (!response.isSuccessful())
				throw new IOException("DeepSeek API 响应失败: " + response);
			JsonNode root = objectMapper.readTree(response.body().string());
			return root.path("choices").get(0).path("message").path("content").asText().trim();
		}
	}

	/**
	 * 5. 引文清洗与格式化
	 */
	public String cleanCitation(String aiResponse) {
		// 匹配引文核心和编号 (支持 [1] 格式)
		Pattern pattern = Pattern.compile("(\\[\\d+\\])?\\s*([^。\\n]+?\\\\[[A-Z/]+\\\\][^。\\n]+?\\\\d{4}.+)");
		Matcher matcher = pattern.matcher(aiResponse);

		if (!matcher.find())
			return aiResponse; // 若匹配失败则返回原文

		String refNum = matcher.group(1) != null ? matcher.group(1) : "";
		String citation = matcher.group(2).trim();

		// 标点标准化与紧凑化 (全角 -> 半角)
		citation = citation.replace("，", ",").replace("：", ":").replace("。", ".").replace("；", ";").replace("（", "(")
				.replace("）", ")");

		citation = citation.replaceAll("[*_]", "") // 去掉 Markdown
				.replaceAll("\\s*([,.:;?+])\\s*", "$1") // 标点去空
				.replaceAll("\\s*(\\[[A-Z/]+\\])\\s*", "$1"); // 修正 [J] 等标记

		return refNum + citation.replaceAll("\\.+$", "") + ".";
	}

	@Async
	public void processMetadataAsync(Long paperId, String pdfPath, PaperRepository repository, String paperOwnerId) {
		try (PDDocument document = PDDocument.load(new File(pdfPath))) {
			String text = extractPdfText(pdfPath); // 解析 PDF
			String citation = getAiCitation(text); // 调 AI
			List<String> keywords = getKeywordsFromAi(text); // 提关键词

			// 回写数据库
			repository.findById(paperId).ifPresent(paper -> {
				paper.setPaperCitation(citation);
				paper.setKeyWords(keywords);
				repository.save(paper);
				System.out.println("关键词生成完了，准备make");
				userInterestsService.makeUserInterestProfile(paperOwnerId);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}