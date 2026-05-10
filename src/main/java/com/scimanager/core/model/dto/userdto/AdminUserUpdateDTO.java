package com.scimanager.core.model.dto.userdto;

import lombok.Data;

@Data
public class AdminUserUpdateDTO {
	private String mentorId; // 用于情景四
	private Boolean resetPassword; // 用于情景五：若为true，后端将其设为初始密码
}