package com.scimanager.core.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scimanager.core.entity.User;
import com.scimanager.core.entity.UserInterest;
import com.scimanager.core.repository.UserInterestRepository;
import com.scimanager.core.repository.UserRepository;
import com.scimanager.core.service.UserInterestsService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 科研兴趣画像服务实现
 *
 * <p>基于用户文献关键词，通过 DeepSeek AI 进行语义聚合和权重计算，
 * 生成 Top 10 研究方向画像（JSON 格式）。</p>
 *
 * <p><b>执行流程：</b></p>
 * <ol>
 *   <li>收集用户所有文献的关键词</li>
 *   <li>统计词频并去重（大小写归一化）</li>
 *   <li>构建提示词发送给 DeepSeek API</li>
 *   <li>AI 返回 JSON 数组（含 name、value、description）</li>
 *   <li>缓存到 user_interests 表</li>
 * </ol>
 *
 * <p><b>触发时机：</b></p>
 * <ul>
 *   <li>上传新文献后（异步）</li>
 *   <li>删除文献后（事务提交后异步）</li>
 *   <li>更新文献关键词后（事务提交后异步）</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class UserInterestsServiceImpl implements UserInterestsService {

	final private UserRepository userRepository;
	final private UserInterestRepository userInterestRepository;

	/**
	 * OkHttp 客户端（连接超时 30s、写入 30s、读取 60s）
	 */
	private final OkHttpClient httpClient = new OkHttpClient.Builder()
			.connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
			.writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
			.readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
			.build();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Value("${deepseek.api-key}")
	private String apiKey;
	private static final String API_URL = "https://api.deepseek.com/chat/completions";

	/**
	 * 【异步】生成/更新用户的科研兴趣画像
	 *
	 * <p>从用户的所有论文中收集关键词，调用 DeepSeek AI 进行语义分析，
	 * 将结果缓存到 user_interests 表。</p>
	 *
	 * @param userId 需要生成画像的用户 ID
	 */
	@Async
	@Override
	@Transactional
	public void makeUserInterestProfile(String userId) {
		User user = userRepository.findById(userId).orElseThrow();
		int currentPaperCount = user.getPapers().size();
		UserInterest cachedInterest = userInterestRepository.findById(userId).orElse(null);

		// 收集用户所有文献的关键词（展平 List<List<String>> → List<String>）
		List<String> allKeywords = user.getPapers().stream()
				.flatMap(paper -> paper.getKeyWords().stream())
				.collect(Collectors.toList());

		try {
			String newAnalysis = this.analyzeUserInterests(allKeywords);
			if (cachedInterest == null) {
				cachedInterest = new UserInterest();
				cachedInterest.setUserId(userId);
			}
			cachedInterest.setAnalysisResult(newAnalysis);
			cachedInterest.setPaperCountSnapshot(currentPaperCount);
			cachedInterest.setLastUpdateTime(LocalDateTime.now());
			userInterestRepository.save(cachedInterest);
		} catch (Exception e) {
			System.err.println("！！！AI分析环节出错！！！");
			e.printStackTrace();
		}
	}

	/**
	 * 分析用户兴趣（核心 AI 逻辑）
	 *
	 * <p><b>分析步骤：</b></p>
	 * <ol>
	 *   <li>统计每个关键词的出现频次（大小写归一化）</li>
	 *   <li>构建提示词，包含词频统计</li>
	 *   <li>调用 DeepSeek API 进行语义聚合和权重计算</li>
	 *   <li>返回 Top 10 兴趣画像的 JSON 字符串</li>
	 * </ol>
	 *
	 * @param allKeywords 用户所有文献的关键词列表
	 * @return JSON 字符串，如 [{name, value, description}, ...]
	 * @throws Exception AI 调用或解析失败时抛出
	 */
	public String analyzeUserInterests(List<String> allKeywords) throws Exception {
		if (allKeywords == null || allKeywords.isEmpty()) {
			return "暂无足够数据分析用户兴趣。";
		}

		// 统计词频（大小写不敏感）
		Map<String, Long> wordCounts = allKeywords.stream()
				.collect(Collectors.groupingBy(s -> s.trim().toLowerCase(), Collectors.counting()));

		// 构建词频描述字符串
		StringBuilder context = new StringBuilder();
		wordCounts.forEach((word, count) -> context.append(word).append("(").append(count).append("次); "));

		String prompt = """
				你是一个学术导师助手。以下是一个科研人员最近研究论文的关键词及其出现频次统计：
				---
				%s
				---

				任务要求：
				1. 语义聚合：将含义相同或高度相关的关键词合并（如：'CNN'与'卷积神经网络'）。
				2. 计算权重：结合关键词出现的频次以及该领域的重要性，计算该用户的"关注权重"（0.0 - 1.0）。
				3. 提取 Top 10：仅输出最核心的 10 个关键词画像，不足 10 个时自动添加较为边缘的关键词，超过 10 个时自动剔除过于边缘的关键词。
				4. **严格限制输出格式为 JSON 数组**，数组元素包含 'name' (关键词), 'value' (权重), 'description' (简要评价)。

				    示例输出：
				    [
				      {"name": "代数理论", "value": 0.95, "description": "核心研究方向..."},
				      ...
				    ]

				    **只输出 JSON，不要任何开场白或 Markdown 代码块标记（如 ```json）,使用英文半角符号与空格。**
				""".formatted(context.toString());

		String systemPrompt = "你是一个精通学术大数据分析的专家，擅长通过关键词勾勒研究者画像。";
		return callDeepSeek(systemPrompt, prompt);
	}

	/**
	 * 调用 DeepSeek API（通用封装）
	 *
	 * <p>使用 OkHttp 发送 POST 请求，通过 Jackson 构建/解析 JSON。</p>
	 *
	 * @param systemContent 系统提示词（定义 AI 的角色和行为）
	 * @param userContent   用户提示词（具体任务描述）
	 * @return AI 返回的文本内容（已 trim）
	 * @throws Exception 网络异常或 API 返回错误码时抛出
	 */
	private String callDeepSeek(String systemContent, String userContent) throws Exception {
		// 构建请求体 JSON
		String jsonBody = objectMapper.createObjectNode()
				.put("model", "deepseek-chat")
				.put("temperature", 0.0) // 温度=0，确保输出确定性
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

		// 发送请求并解析响应
		try (Response response = httpClient.newCall(request).execute()) {
			if (!response.isSuccessful())
				throw new IOException("DeepSeek API 响应失败: " + response);
			JsonNode root = objectMapper.readTree(response.body().string());
			// choices[0].message.content 即为 AI 返回的文本
			return root.path("choices").get(0).path("message").path("content").asText().trim();
		}
	}

	/**
	 * 获取缓存的用户兴趣画像
	 *
	 * @param userId 用户 ID
	 * @return JSON 字符串（如果已生成），否则返回提示信息
	 */
	@Override
	public String getUserInterestProfile(String userId) {
		return userInterestRepository.findById(userId)
				.map(UserInterest::getAnalysisResult)
				.orElse("您的科研可视化图像正在生成中，请稍后刷新查看...");
	}
}
