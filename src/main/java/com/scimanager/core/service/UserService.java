package com.scimanager.core.service;

import java.util.List;

import com.scimanager.core.entity.User;

/**
 * 用户管理服务接口
 *
 * <p>负责系统用户的管理和权限控制。<br>
 * 实现类为 {@link com.scimanager.core.service.impl.UserServiceImpl}。</p>
 */
public interface UserService {

	/**
	 * 获取用户个人详情
	 *
	 * @param userId 目标用户 ID
	 * @return 用户实体
	 */
	User getUserProfile(String userId);

	/**
	 * 新增系统用户（管理员）
	 *
	 * @param user       包含账号、初始密码及角色信息的实体
	 * @param operatorId 执行创建操作的人员 ID（需为管理员）
	 * @return 持久化后的用户实体
	 */
	User saveUser(User user, String operatorId);

	/**
	 * 更新用户信息（管理员）
	 *
	 * @param id          被修改用户的唯一标识
	 * @param userDetails 包含待更新字段的对象
	 * @param operatorId  当前执行修改操作的用户 ID（需为管理员）
	 */
	void updateUser(String id, User userDetails, String operatorId);

	/**
	 * 移除系统用户（管理员）
	 *
	 * @param id         目标用户 ID
	 * @param operatorId 执行删除操作的用户 ID（需为管理员）
	 */
	void deleteUser(String id, String operatorId);

	/**
	 * 获取系统用户列表（权限分级）
	 *
	 * <p>管理员返回所有用户，导师仅返回名下学生，学生抛出权限异常。</p>
	 *
	 * @param operatorId 发起查询请求的用户 ID
	 * @return 根据角色过滤后的用户列表
	 */
	List<User> findAllUsers(String operatorId);
}