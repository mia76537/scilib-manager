package com.scimanager.core.controller.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scimanager.core.common.Result;
import com.scimanager.core.model.dto.userdto.PasswordUpdateDTO;
import com.scimanager.core.model.dto.userdto.UserProfileDTO;
import com.scimanager.core.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

	private final ProfileService profileService;

	/**
	 * 展示个人信息 返回 Result<UserProfileDTO>
	 */
	@GetMapping
	public Result<UserProfileDTO> getMyProfile(@RequestAttribute("userId") String currentUserId) {
		UserProfileDTO profile = profileService.getProfile(currentUserId);
		return Result.success(profile);
	}

	/**
	 * 修改个人信息 成功后返回 Result<Void> 或简单的成功消息
	 */
	@PutMapping
	public Result<String> updateMyProfile(@RequestAttribute("userId") String currentUserId,
			@RequestBody UserProfileDTO profileDto) {

		profileService.updateProfile(currentUserId, profileDto);
		return Result.success("个人信息更新成功");
	}

	/**
	 * 修改密码
	 */
	@PostMapping("/password")
	public Result<String> updatePassword(@RequestAttribute("userId") String currentUserId,
			@RequestBody PasswordUpdateDTO passwordDto) {

		profileService.changePassword(currentUserId, passwordDto);
		return Result.success("密码修改成功");
	}
}