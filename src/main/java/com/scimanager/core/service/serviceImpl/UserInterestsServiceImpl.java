package com.scimanager.core.service.serviceImpl;

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
import com.scimanager.core.model.User;
import com.scimanager.core.model.UserInterest;
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

@Service
@RequiredArgsConstructor
public class UserInterestsServiceImpl implements UserInterestsService {

	final private UserRepository userRepository;
	final private UserInterestRepository userInterestRepository;
	private final OkHttpClient httpClient = new OkHttpClient.Builder()
			.connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 连接超时
			.writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 写入超时
			.readTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // 读取超时
			.build();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Value("${deepseek.api-key}")
	private String apiKey;
	private static final String API_URL = "https://api.deepseek.com/chat/completions";

	@Async
	@Override
	@Transactional
	public void makeUserInterestProfile(String userId) {
		System.out.printf("开始强制生成用户画像，用户ID: %s\n", userId);

		System.out.printf("开始make");
		// 1. 获取用户信息及当前论文总数
		User user = userRepository.findById(userId).orElseThrow();
		int currentPaperCount = user.getPapers().size();
		System.out.printf("获取用户信息及当前论文总数了");

		// 2. 检查缓存记录
		UserInterest cachedInterest = userInterestRepository.findById(userId).orElse(null);
		System.out.printf("检查缓存记录了");
		{

			// 提取关键词并调用 AI
			List<String> allKeywords = user.getPapers().stream().flatMap(paper -> paper.getKeyWords().stream())
					.collect(Collectors.toList());
			System.out.printf("关键词是" + allKeywords);
			System.out.printf("提取关键词并调用 AI了");

			try {
				String newAnalysis = this.analyzeUserInterests(allKeywords);
				System.out.println("AI 分析结果成功获取: " + newAnalysis);
				// 4. 更新或创建缓存记录
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
				e.printStackTrace(); // 打印具体的错误堆栈，比如是不是网络断了？

			}
		}
	}

	/**
	 * 分析用户的研究兴趣画像
	 * 
	 * @param allKeywords 用户所有论文关键词的扁平化列表
	 * @return AI 生成的兴趣权重报告（JSON）
	 */
	public String analyzeUserInterests(List<String> allKeywords) throws Exception {
		if (allKeywords == null || allKeywords.isEmpty()) {
			System.out.printf("没有关键词了");
			return "暂无足够数据分析用户兴趣。";
		}
		// 1.统计原始词频
		Map<String, Long> wordCounts = allKeywords.stream()
				.collect(Collectors.groupingBy(s -> s.trim().toLowerCase(), Collectors.counting()));
		System.out.printf("关键词是" + wordCounts);
		// 2. 构建发送给 AI 的上下文
		StringBuilder context = new StringBuilder();
		wordCounts.forEach((word, count) -> context.append(word).append("(").append(count).append("次); "));
		String prompt = """
				你是一个学术导师助手。以下是一个科研人员最近研究论文的关键词及其出现频次统计：
				---
				%s
				---

				任务要求：
				1. 语义聚合：将含义相同或高度相关的关键词合并（如：'CNN'与'卷积神经网络'）。
				2. 计算权重：结合关键词出现的频次以及该领域的重要性，计算该用户的“关注权重”（0.0 - 1.0）。
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
		// 调用 callDeepSeek 方法
		return callDeepSeek(systemPrompt, prompt);
	}

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

	@Override
	public String getUserInterestProfile(String userId) {
		return userInterestRepository.findById(userId).map(UserInterest::getAnalysisResult) // 如果存在记录，返回结果
				.orElse("您的科研画像正在生成中，请稍后刷新查看..."); // 如果不存在，返回友好提示
	}
}
