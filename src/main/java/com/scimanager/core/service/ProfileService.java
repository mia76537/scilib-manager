package com.scimanager.core.service;

import org.springframework.stereotype.Service;

import com.scimanager.core.model.User;
import com.scimanager.core.model.dto.userdto.PasswordUpdateDTO;
import com.scimanager.core.model.dto.userdto.UserProfileDTO;
import com.scimanager.core.repository.UserRepository;
import com.scimanager.core.service.mapper.UserMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	public UserProfileDTO getProfile(String userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("该用户不存在")); // 触发 400 错误
		return userMapper.toProfileDto(user);
	}

	@Transactional
	public void updateProfile(String userId, UserProfileDTO dto) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户身份验证失败"));
		System.out.println(dto);
		userMapper.updateEntityFromDto(dto, user);
		userRepository.save(user);
	}

	@Transactional
	public void changePassword(String userId, PasswordUpdateDTO dto) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
		System.out.println(dto);

		// 业务校验失败直接抛异常
		if (!user.getPassword().equals(dto.getOldPassword())) {
			throw new RuntimeException("原密码校验失败，请重试"); // 会被全局处理器捕获
		}

		user.setPassword(dto.getNewPassword());
		userRepository.save(user);
	}
}