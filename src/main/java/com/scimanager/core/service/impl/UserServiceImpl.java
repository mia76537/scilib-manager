package com.scimanager.core.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scimanager.core.entity.User;
import com.scimanager.core.repository.UserRepository;
import com.scimanager.core.security.PasswordEncoder;
import com.scimanager.core.service.UserService;

/**
 * 用户管理服务实现
 *
 * <p>管理系统用户的增删改查，所有写操作（增、删、改）均需管理员权限。<br>
 * 用户列表查询根据角色返回不同范围的数据。</p>
 *
 * <p><b>权限模型：</b></p>
 * <ul>
 *   <li>ADMIN — 所有用户的增删改查</li>
 *   <li>MENTOR — 仅可查看名下学生列表</li>
 *   <li>STUDENT — 无权限查看用户列表</li>
 * </ul>
 *
 * <p><b>密码安全：</b>当前使用明文存储和比对，已预留 {@link PasswordEncoder} 接口用于未来 BCrypt 迁移。</p>
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	// 预留：未来 BCrypt 实现后自动注入，通过 @Component 注册即可激活
	@Autowired(required = false)
	private PasswordEncoder passwordEncoder;

	/**
	 * 权限校验：确保当前操作者具备管理员权限
	 *
	 * @param operatorId 操作者用户 ID
	 * @throws RuntimeException 如果操作者不存在或不是管理员
	 */
	private void checkAdminPermission(String operatorId) {
		User operator = userRepository.findById(operatorId).orElseThrow(() -> new RuntimeException("操作员不存在"));
		if (!"ADMIN".equalsIgnoreCase(operator.getRole())) {
			throw new RuntimeException("权限不足：仅限管理员操作");
		}
	}

	/**
	 * 创建新用户
	 *
	 * <ol>
	 *   <li>校验操作者是否为管理员</li>
	 *   <li>校验用户 ID 是否已被占用</li>
	 *   <li>保存到数据库</li>
	 * </ol>
	 *
	 * @param newUser    包含 userId、userName、password、role、mentorId 的实体
	 * @param operatorId 操作者 ID（需为管理员）
	 * @return 持久化后的用户实体
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
	 * 更新用户信息
	 *
	 * <p>可更新字段：userName、mentorId、role、password（仅非空时更新）。</p>
	 *
	 * @param id         目标用户 ID
	 * @param details    包含待更新字段的实体
	 * @param operatorId 操作者 ID（需为管理员）
	 */
	@Override
	@Transactional
	public void updateUser(String id, User details, String operatorId) {
		checkAdminPermission(operatorId);
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("目标用户不存在"));

		user.setUserName(details.getUserName());
		user.setMentorId(details.getMentorId());
		user.setRole(details.getRole());

		// 仅当前端传了非空密码时，才更新密码
		if (details.getPassword() != null && !details.getPassword().isEmpty()) {
			user.setPassword(details.getPassword());
		}
		userRepository.save(user);
	}

	/**
	 * 删除用户
	 *
	 * <p>级联删除该用户的所有关联数据（论文、查收查引请求）。</p>
	 *
	 * @param id         目标用户 ID
	 * @param operatorId 操作者 ID（需为管理员）
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
	 * 获取用户列表（权限分级）
	 *
	 * <ul>
	 *   <li>管理员 — 返回所有用户</li>
	 *   <li>导师 — 返回名下所有学生</li>
	 *   <li>学生 — 抛出权限异常</li>
	 * </ul>
	 *
	 * @param operatorId 当前操作者 ID
	 * @return 用户列表（范围取决于角色）
	 */
	@Override
	public List<User> findAllUsers(String operatorId) {
		User operator = userRepository.findById(operatorId).orElseThrow(() -> new RuntimeException("操作员不存在"));
		String role = operator.getRole();

		if ("ADMIN".equalsIgnoreCase(role)) {
			return userRepository.findAll();
		} else if ("MENTOR".equalsIgnoreCase(role)) {
			return userRepository.findByMentorId(operatorId);
		} else {
			throw new RuntimeException("权限不足：仅限管理员或导师查看用户列表");
		}
	}

	/**
	 * 获取用户个人详情
	 *
	 * @param userId 用户 ID
	 * @return 用户实体
	 * @throws RuntimeException 如果用户不存在
	 */
	@Override
	public User getUserProfile(String userId) {
		return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
	}
}
