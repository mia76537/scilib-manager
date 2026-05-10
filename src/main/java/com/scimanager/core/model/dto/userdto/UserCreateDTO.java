package com.scimanager.core.model.dto.userdto;

import lombok.Data;

@Data
public class UserCreateDTO {
	private String userId; // 管理员手动填写
	private String userName;
	private String role;
	private String mentorId;
}