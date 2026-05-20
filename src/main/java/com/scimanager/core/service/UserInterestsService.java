package com.scimanager.core.service;

/**
 * 用户科研兴趣画像服务接口
 *
 * <p>基于用户文献关键词，通过 DeepSeek AI 分析并缓存科研兴趣画像。<br>
 * 实现类为 {@link com.scimanager.core.service.impl.UserInterestsServiceImpl}。</p>
 */
public interface UserInterestsService {

	/**
	 * 获取缓存的用户兴趣画像（JSON 字符串）
	 *
	 * @param userId 用户 ID
	 * @return AI 分析结果 JSON，或提示信息
	 */
	String getUserInterestProfile(String userId);

	/**
	 * 【异步】生成/更新用户的科研兴趣画像
	 *
	 * @param userId 需要分析的用户 ID
	 */
	void makeUserInterestProfile(String userId);

}
