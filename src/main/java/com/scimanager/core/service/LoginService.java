package com.scimanager.core.service;

import com.scimanager.core.model.User;

/**
 * 用户服务接口
 * 专注于登录验证
 */
public interface LoginService {

    /**
     * 核心登录功能
     * @param userId 用户提供的ID
     * @param password 用户提供的密码
     * @return 验证成功返回完整的 User 对象（包含 role 和 mentorId），失败返回 null
     */
    User login(String userId, String password);

    
    
    
}