package com.scimanager.core.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scimanager.core.common.Result;
import com.scimanager.core.model.User;
import com.scimanager.core.repository.UserRepository;
import com.scimanager.core.service.UserInterestsService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserInterestsController {

	private final UserInterestsService userInterestsService;
	private final UserRepository userRepository;

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
		// 注意到这里的analysisResult是一串JSON字符串
	}

	@GetMapping("/interests/{targetUserId}")
	public Result<String> getTargetUserInterests(@PathVariable String targetUserId, HttpServletRequest request) {
		// 从 request 属性中获取当前登录者的 ID 和 角色
		String currentUserId = (String) request.getAttribute("userId");
		String currentUserRole = (String) request.getAttribute("role");
		// 权限判定
		// 如果是管理员，直接放行
		if ("ADMIN".equals(currentUserRole)) {
			return Result.success(userInterestsService.getUserInterestProfile(targetUserId));
		}
		// 如果是导师，检查目标学生是否归属于自己
		if ("MENTOR".equals(currentUserRole)) {
			// 你需要通过 userRepository 找到目标学生的信息
			// 建议在 UserInterestsService 中封装一个权限检查方法，或者直接在这里调用 repository
			User targetUser = userRepository.findById(targetUserId).orElse(null);
			if (targetUser == null) {
				return Result.error(404, "目标用户不存在");
			}
			// 核心校验：检查学生的 mentorId 是否指向当前登录的导师
			if (currentUserId.equals(targetUser.getMentorId())) {
				String analysisResult = userInterestsService.getUserInterestProfile(targetUserId);
				return Result.success(analysisResult);
			} else {
				return Result.error(403, "权限不足：您只能查看自己学生的可视化画像");
			}
		}
		// 其他角色（如学生想看别人的）直接拒绝
		return Result.error(403, "权限不足，无法查看他人画像");
	}

}