package com.scimanager.core.service.impl;

import com.scimanager.core.entity.User;
import com.scimanager.core.repository.UserRepository;
import com.scimanager.core.service.LoginService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 登录认证服务实现
 *
 * <p>通过 userId + password 直接查询数据库进行身份验证。<br>
 * <b>注意：</b>当前使用明文密码比对，已预留 {@link com.scimanager.core.security.PasswordEncoder}
 * 接口用于未来 BCrypt 迁移。</p>
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 用户登录验证
     *
     * <p>根据 userId 和 password 查询匹配的用户记录。<br>
     * 验证成功后返回包含角色信息的 User 实体，用于后续 JWT Token 生成。</p>
     *
     * @param userId   用户登录 ID
     * @param password 用户密码（当前为明文比对）
     * @return 匹配的 User 实体（含 role 和 mentorId），验证失败返回 null
     */
    @Override
    public User login(String userId, String password) {
        User user = userRepository.findByUserIdAndPassword(userId, password);
        if (user != null) {
            System.out.println("登录成功：用户 " + user.getUserName() + " 角色为 " + user.getRole());
            return user;
        } else {
            System.out.println("登录失败：账号或密码错误");
            return null;
        }
    }

}
