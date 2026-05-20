package com.scimanager.core.dto.user;

import lombok.Data;

/**
 * 用户个人信息 DTO
 *
 * <p>用于个人资料的查询和更新。<br>
 * <b>只读字段：</b>userId、role、mentorId（个人不可修改）<br>
 * <b>可编辑字段：</b>userName</p>
 */
@Data
public class UserProfileDTO {
	/** 用户 ID（只读展示） */
	private String userId;
	/** 用户显示名称（可编辑） */
	private String userName;
	/** 用户角色（只读展示） */
	private String role;
	/** 导师 ID（只读展示，个人不可修改归属） */
	private String mentorId;
}
