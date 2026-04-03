package com.scimanager.core.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scimanager.core.common.Result;
import com.scimanager.core.service.UserInterestsService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserInterestsController {

	private final UserInterestsService userInterestsService;

	/**
	 * 获取当前登录用户的科研兴趣画像 路径: GET /api/users/interests
	 */
	@GetMapping("/interests")
	public Result<String> getUserInterests(HttpServletRequest request) {
		// JwtInterceptor 已经将解析后的 userId 存入了 request 属性中
		// 如果没有，通常从 Token 中解析获取
		String userId = (String) request.getAttribute("userId");

		if (userId == null) {
			throw new RuntimeException("用户未登录或 Token 无效");
		}

		String analysisResult = userInterestsService.getUserInterestProfile(userId);
		return Result.success(analysisResult);
	}
}