package com.scimanager.core.service.serviceImpl;

import com.scimanager.core.model.User;
import com.scimanager.core.repository.UserRepository;
import com.scimanager.core.service.LoginService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 实现登录逻辑
     */
    @Override
    public User login(String userId, String password) {
        // 直接通过 Repository 查询匹配的用户
        User user = userRepository.findByUserIdAndPassword(userId, password);
        // 逻辑判断
        if (user != null) {
            System.out.println("登录成功：用户 " + user.getUserName() + " 角色为 " + user.getRole());
            return user; // 
        } else {
            System.out.println("登录失败：账号或密码错误");
            return null;
        }
    }
    
   
}