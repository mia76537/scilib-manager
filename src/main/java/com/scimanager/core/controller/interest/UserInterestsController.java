package com.scimanager.core.controller.interest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scimanager.core.common.Result;
import com.scimanager.core.entity.User;
import com.scimanager.core.repository.UserRepository;
import com.scimanager.core.service.UserInterestsService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * 科研兴趣画像控制器
 *
 * <p>提供用户科研兴趣画像的查看接口。画像由 DeepSeek AI 基于用户文献关键词分析生成。</p>
 *
 * <p><b>权限控制：</b></p>
 * <ul>
 *   <li>用户自己 — 可查看自己的画像</li>
 *   <li>管理员 — 可查看任意用户画像</li>
 *   <li>导师 — 仅可查看自己名下学生的画像</li>
 *   <li>其他 — 无权查看</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserInterestsController {

	private final UserInterestsService userInterestsService;
	private final UserRepository userRepository;

	/**
	 * 【GET /api/users/interests】获取当前登录用户的科研兴趣画像
	 *
	 * <p>从缓存中读取 AI 分析结果，若尚未生成则返回提示信息。</p>
	 *
	 * @param request 用于从 attribute 中提取 userId（由 JwtInterceptor 注入）
	 * @return JSON 字符串形式的分析结果（如 {@code [{"name":"...","value":0.9,...}]}）
	 */
	@GetMapping("/interests")
	public Result<String> getUserInterests(HttpServletRequest request) {
		String userId = (String) request.getAttribute("userId");

		if (userId == null) {
			throw new RuntimeException("用户未登录或 Token 无效");
		}

		String analysisResult = userInterestsService.getUserInterestProfile(userId);
		return Result.success(analysisResult);
	}

	/**
	 * 【GET /api/users/interests/{targetUserId}】获取指定用户的科研兴趣画像（管理员/导师）
	 *
	 * <p>权限校验逻辑：</p>
	 * <ol>
	 *   <li>管理员 — 直接放行</li>
	 *   <li>导师 — 校验目标用户是否为名下学生</li>
	 *   <li>其他角色 — 拒绝访问</li>
	 * </ol>
	 *
	 * @param targetUserId 目标用户的 ID
	 * @param request      用于从 attribute 中提取当前用户 ID 和角色
	 * @return JSON 字符串形式的分析结果，或错误信息
	 */
	@GetMapping("/interests/{targetUserId}")
	public Result<String> getTargetUserInterests(@PathVariable String targetUserId, HttpServletRequest request) {
		String currentUserId = (String) request.getAttribute("userId");
		String currentUserRole = (String) request.getAttribute("role");

		// 管理员：直接放行
		if ("ADMIN".equals(currentUserRole)) {
			return Result.success(userInterestsService.getUserInterestProfile(targetUserId));
		}

		// 导师：需校验目标用户是否为自己名下的学生
		if ("MENTOR".equals(currentUserRole)) {
			User targetUser = userRepository.findById(targetUserId).orElse(null);
			if (targetUser == null) {
				return Result.error(404, "目标用户不存在");
			}
			if (currentUserId.equals(targetUser.getMentorId())) {
				String analysisResult = userInterestsService.getUserInterestProfile(targetUserId);
				return Result.success(analysisResult);
			} else {
				return Result.error(403, "权限不足：您只能查看自己学生的可视化画像");
			}
		}

		return Result.error(403, "权限不足，无法查看他人画像");
	}

}
