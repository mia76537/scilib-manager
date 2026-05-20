package com.scimanager.core.service;

import com.scimanager.core.dto.user.PasswordUpdateDTO;
import com.scimanager.core.dto.user.UserProfileDTO;

/**
 * 个人资料服务接口
 *
 * <p>定义个人资料的查询、更新及密码修改业务。<br>
 * 实现类为 {@link com.scimanager.core.service.impl.ProfileServiceImpl}。</p>
 */
public interface ProfileService {

	/** 获取个人信息 DTO */
	UserProfileDTO getProfile(String userId);

	/** 更新个人信息 */
	void updateProfile(String userId, UserProfileDTO dto);

	/** 修改密码 */
	void changePassword(String userId, PasswordUpdateDTO dto);
}