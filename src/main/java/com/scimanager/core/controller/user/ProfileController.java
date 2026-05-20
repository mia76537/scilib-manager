package com.scimanager.core.controller.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scimanager.core.common.Result;
import com.scimanager.core.dto.user.PasswordUpdateDTO;
import com.scimanager.core.dto.user.UserProfileDTO;
import com.scimanager.core.service.ProfileService;

import lombok.RequiredArgsConstructor;

/**
 * 个人资料管理控制器
 *
 * <p>当前登录用户管理自己的个人信息和密码。<br>
 * 区别于 {@link UserController} 的管理员操作用户，此控制器聚焦于"个人视角"的操作。</p>
 *
 * <p>统一使用 {@link Result} 返回格式。</p>
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

	private final ProfileService profileService;

	/**
	 * 【GET /api/profile】获取当前登录用户的个人信息
	 *
	 * <p>返回脱敏的个人信息 DTO（不含密码）。</p>
	 *
	 * @param currentUserId 从 JWT Token 中提取的用户 ID
	 * @return 用户个人信息 DTO（userId, userName, role, mentorId）
	 */
	@GetMapping
	public Result<UserProfileDTO> getMyProfile(@RequestAttribute("userId") String currentUserId) {
		UserProfileDTO profile = profileService.getProfile(currentUserId);
		return Result.success(profile);
	}

	/**
	 * 【PUT /api/profile】修改个人信息
	 *
	 * <p>可修改字段仅为 {@code userName}；userId、role、mentorId 为只读字段，不允许个人修改。</p>
	 *
	 * @param currentUserId 当前登录用户 ID
	 * @param profileDto    包含待更新字段的 DTO（仅 userName 生效）
	 * @return 操作成功提示
	 */
	@PutMapping
	public Result<String> updateMyProfile(@RequestAttribute("userId") String currentUserId,
			@RequestBody UserProfileDTO profileDto) {

		profileService.updateProfile(currentUserId, profileDto);
		return Result.success("个人信息更新成功");
	}

	/**
	 * 【POST /api/profile/password】修改密码
	 *
	 * <p>需要验证原密码是否正确，验证通过后更新为新密码。</p>
	 *
	 * @param currentUserId 当前登录用户 ID
	 * @param passwordDto   含旧密码和新密码的 DTO
	 * @return 操作成功提示
	 */
	@PostMapping("/password")
	public Result<String> updatePassword(@RequestAttribute("userId") String currentUserId,
			@RequestBody PasswordUpdateDTO passwordDto) {

		profileService.changePassword(currentUserId, passwordDto);
		return Result.success("密码修改成功");
	}
}