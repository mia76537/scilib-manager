package com.scimanager.core.model.dto.userdto;

import lombok.Data;

@Data
public class PasswordUpdateDTO {
	private String oldPassword; // 旧密码校验
	private String newPassword; // 新密码
}