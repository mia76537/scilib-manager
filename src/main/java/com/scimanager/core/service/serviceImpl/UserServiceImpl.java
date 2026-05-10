package com.scimanager.core.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scimanager.core.model.User;
import com.scimanager.core.repository.UserRepository;
import com.scimanager.core.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	/**
	 * 权限校验：确保当前操作者具备管理员权限 * @param operatorId 操作者的唯一标识 ID
	 * 
	 * @throws RuntimeException 当操作员不存在或角色非 ADMIN 时抛出异常
	 */
	private void checkAdminPermission(String operatorId) {
		User operator = userRepository.findById(operatorId).orElseThrow(() -> new RuntimeException("操作员不存在"));
		if (!"ADMIN".equalsIgnoreCase(operator.getRole())) {
			throw new RuntimeException("权限不足：仅限管理员操作");
		}
	}

	/**
	 * 保存新用户 * @param newUser 待保存的用户实体信息
	 * 
	 * @param operatorId 执行此操作的管理员 ID
	 * @return 保存成功后的用户对象
	 */
	@Override
	@Transactional
	public User saveUser(User newUser, String operatorId) {
		checkAdminPermission(operatorId);
		if (userRepository.existsById(newUser.getUserId())) {
			throw new RuntimeException("用户ID已存在");
		}
		return userRepository.save(newUser);
	}

	/**
	 * 更新指定用户信息 * @param id 目标用户的 ID
	 * 
	 * @param details    包含更新内容的实体（用户名、密码、导师ID、角色）
	 * @param operatorId 执行此操作的管理员 ID
	 */
	@Override
	@Transactional
	public void updateUser(String id, User details, String operatorId) {
		checkAdminPermission(operatorId);
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("目标用户不存在"));

		user.setUserName(details.getUserName());
		user.setMentorId(details.getMentorId());
		user.setRole(details.getRole());

		// 只有当前端传了非空密码时，才更新密码
		if (details.getPassword() != null && !details.getPassword().isEmpty()) {
			user.setPassword(details.getPassword());
		}
		userRepository.save(user);
	}

	/**
	 * 删除指定用户 * @param id 要删除的用户 ID
	 * 
	 * @param operatorId 执行此操作的管理员 ID
	 */
	@Override
	@Transactional
	public void deleteUser(String id, String operatorId) {
		checkAdminPermission(operatorId);

		if (!userRepository.existsById(id)) {
			throw new RuntimeException("用户不存在");
		}
		userRepository.deleteById(id);
	}

	/**
	 * 获取系统中所有用户信息 * @param operatorId 执行此操作的管理员 ID
	 * 
	 * @return 包含所有用户的列表
	 */
	public List<User> findAllUsers(String operatorId) {
		// 1. 获取操作者信息以确定其角色
		User operator = userRepository.findById(operatorId).orElseThrow(() -> new RuntimeException("操作员不存在"));
		String role = operator.getRole();

		// 2. 根据角色执行不同的查询逻辑
		if ("ADMIN".equalsIgnoreCase(role)) {
			// 管理员：返回系统中所有用户
			return userRepository.findAll();
		} else if ("MENTOR".equalsIgnoreCase(role)) {
			// 导师：调用 repository 查询 mentorId 等于当前操作者 ID 的学生列表
			// 注意：这需要在 UserRepository 中定义 findByMentorId 方法
			return userRepository.findByMentorId(operatorId);
		} else {
			// 学生或其他：无权查看列表
			throw new RuntimeException("权限不足：仅限管理员或导师查看用户列表");
		}
	}

	@Override
	public User getUserProfile(String userId) {
		// 直接根据当前登录的 userId 查询，无需 checkAdminPermission
		return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
	}
}