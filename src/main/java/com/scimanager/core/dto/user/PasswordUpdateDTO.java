package com.scimanager.core.dto.user;

import lombok.Data;

/**
 * 密码修改 DTO
 *
 * <p>用户修改密码时需提供旧密码（用于验证）和新密码。</p>
 */
@Data
public class PasswordUpdateDTO {
	/** 旧密码（用于验证身份） */
	private String oldPassword;
	/** 新密码 */
	private String newPassword;
}
