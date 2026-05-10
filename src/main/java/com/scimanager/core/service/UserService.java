package com.scimanager.core.service;

import java.util.List;

import com.scimanager.core.model.User;

/**
 * 用户管理服务接口 负责系统用户的管理、权限控制
 */
public interface UserService {

	/**
	 * 获取用户个人详情
	 * 
	 * @param userId 当前登录用户的ID
	 * @return 用户实体
	 */
	User getUserProfile(String userId);

	/**
	 * 新增系统用户
	 * 
	 * @param user       包含账号、初始密码及角色信息的实体
	 * @param operatorId 执行创建操作的人员ID，用于操作审计
	 * @return 持久化后的用户实体，包含生成的唯一标识
	 */
	User saveUser(User user, String operatorId);

	/**
	 * 更新用户信息 验证操作者是否有权修改目标用户
	 * 
	 * @param id          被修改用户的唯一标识
	 * @param userDetails 包含待更新字段的对象
	 * @param operatorId  当前执行修改操作的用户ID
	 */
	void updateUser(String id, User userDetails, String operatorId);

	/**
	 * 移除系统用户
	 * 
	 * @param id         目标用户ID
	 * @param operatorId 执行删除操作的用户ID，需校验管理权限
	 */
	void deleteUser(String id, String operatorId);

	/**
	 * 获取系统全量用户列表
	 * 
	 * @param operatorId 发起查询请求的用户ID
	 * @return 当前系统中所有激活状态的用户列表
	 */
	List<User> findAllUsers(String operatorId);
}