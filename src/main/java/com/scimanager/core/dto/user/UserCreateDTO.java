package com.scimanager.core.dto.user;

import lombok.Data;

/**
 * 创建用户 DTO（管理员使用）
 *
 * <p>管理员创建新用户时提供的参数，包含用户 ID、姓名、角色和可选导师 ID。</p>
 */
@Data
public class UserCreateDTO {
	/** 用户 ID（管理员手动填写） */
	private String userId;
	/** 用户显示名称 */
	private String userName;
	/** 用户角色（ADMIN / MENTOR / STUDENT） */
	private String role;
	/** 归属导师 ID */
	private String mentorId;
}
