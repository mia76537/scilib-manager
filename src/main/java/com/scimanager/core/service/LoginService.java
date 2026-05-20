package com.scimanager.core.service;

import com.scimanager.core.entity.User;

/**
 * 登录认证服务接口
 *
 * <p>负责用户身份验证，验证成功后返回 User 信息用于生成 JWT Token。<br>
 * 实现类为 {@link com.scimanager.core.service.impl.LoginServiceImpl}。</p>
 */
public interface LoginService {

    /**
     * 核心登录功能
     *
     * @param userId   用户提供的登录 ID
     * @param password 用户提供的密码
     * @return 验证成功返回完整的 User 对象（含 role 和 mentorId），失败返回 null
     */
    User login(String userId, String password);

    
    
    
}