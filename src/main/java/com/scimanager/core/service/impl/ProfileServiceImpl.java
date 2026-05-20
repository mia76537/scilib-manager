package com.scimanager.core.service.impl;

import org.springframework.stereotype.Service;

import com.scimanager.core.entity.User;
import com.scimanager.core.dto.user.PasswordUpdateDTO;
import com.scimanager.core.dto.user.UserProfileDTO;
import com.scimanager.core.mapper.UserMapper;
import com.scimanager.core.repository.UserRepository;
import com.scimanager.core.service.ProfileService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * 个人资料服务实现
 *
 * <p>提供当前登录用户的个人信息查看、修改及密码更改功能。<br>
 * 使用 MapStruct {@link UserMapper} 实现 Entity ↔ DTO 的转换。</p>
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	/**
	 * 获取个人资料 DTO
	 *
	 * <p>查询用户实体，通过 Mapper 转换为不含密码的 DTO 返回。</p>
	 *
	 * @param userId 当前用户 ID
	 * @return 用户个人信息 DTO（userId, userName, role, mentorId）
	 * @throws RuntimeException 如果用户不存在（将被全局异常处理器捕获，返回 HTTP 400）
	 */
	@Override
	public UserProfileDTO getProfile(String userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("该用户不存在"));
		return userMapper.toProfileDto(user);
	}

	/**
	 * 更新个人资料
	 *
	 * <p>仅更新 DTO 中非空的字段（由 UserMapper 控制），userId、password、role 等敏感字段不会被覆盖。</p>
	 *
	 * @param userId 当前用户 ID
	 * @param dto    包含待更新字段的 DTO（仅 userName 实际生效）
	 */
	@Override
	@Transactional
	public void updateProfile(String userId, UserProfileDTO dto) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户身份验证失败"));
		// 将 DTO 的值合并到实体（忽略 userId、password、role 等字段）
		userMapper.updateEntityFromDto(dto, user);
		userRepository.save(user);
	}

	/**
	 * 修改密码
	 *
	 * <p><b>执行流程：</b></p>
	 * <ol>
	 *   <li>校验用户是否存在</li>
	 *   <li>校验原密码是否正确（当前使用明文比对）</li>
	 *   <li>更新为新密码</li>
	 *   <li>持久化到数据库</li>
	 * </ol>
	 *
	 * @param userId 当前用户 ID
	 * @param dto    含 oldPassword 和 newPassword 的 DTO
	 * @throws RuntimeException 如果原密码不匹配
	 */
	@Override
	@Transactional
	public void changePassword(String userId, PasswordUpdateDTO dto) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));

		// 校验原密码（当前为明文比对，预留 BCrypt 迁移方案）
		if (!user.getPassword().equals(dto.getOldPassword())) {
			throw new RuntimeException("原密码校验失败，请重试"); // 被全局处理器捕获为 HTTP 400
		}

		user.setPassword(dto.getNewPassword());
		userRepository.save(user);
	}
}
