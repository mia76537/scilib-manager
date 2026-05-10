package com.scimanager.core.model.dto.userdto;

import lombok.Data;

@Data
public class UserProfileDTO {
	private String userId; // 只读展示
	private String userName; // 可编辑
	private String role; // 只读展示
	private String mentorId; // 只读展示（个人不能修改归属）
}