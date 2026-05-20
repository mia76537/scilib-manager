package com.scimanager.core.dto.user;

import lombok.Data;

/**
 * 管理员操作用户 DTO
 *
 * <p>管理员更新用户信息时的参数：<br>
 * - 修改归属导师（mentorId）<br>
 * - 重置密码（resetPassword）</p>
 */
@Data
public class AdminUserUpdateDTO {
	/** 修改归属导师 ID */
	private String mentorId;
	/** 是否重置为初始密码（true 时触发） */
	private Boolean resetPassword;
}
