package com.scimanager.core.controller.user;

import com.scimanager.core.model.User;
import com.scimanager.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * 提供用户信息的增删改查接口
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    /**
     * 获取当前登录用户的个人信息
     * @param currentUserId 从 Token 拦截器注入的当前用户 ID
     */
    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(@RequestAttribute("userId") String currentUserId) {
        User user = userService.getUserProfile(currentUserId);
        return ResponseEntity.ok(user);
    }

    /**
     * 创建新用户
     * @param user 待创建的用户对象
     * @param operatorId 从 Token 拦截器注入的当前操作者 ID
     */
    @PostMapping
    public ResponseEntity<User> addUser(
            @RequestBody User user, 
            @RequestAttribute("userId") String operatorId) { // 统一使用注解获取 ID
        User savedUser = userService.saveUser(user, operatorId);
        return ResponseEntity.ok(savedUser);
    }

    /**
     * 更新指定用户信息
     * @param id 目标用户 ID
     * @param user 包含更新信息的实体
     * @param operatorId 当前操作者 ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable String id, 
            @RequestBody User user, 
            @RequestAttribute("userId") String operatorId) {
        userService.updateUser(id, user, operatorId);
        return ResponseEntity.ok("用户更新成功");
    }

    /**
     * 删除指定用户
     * @param id 目标用户 ID
     * @param operatorId 当前操作者 ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable String id, 
            @RequestAttribute("userId") String operatorId) {
        userService.deleteUser(id, operatorId);
        return ResponseEntity.ok("用户删除成功");
    }

    /**
     * 获取所有用户列表
     * @param operatorId 当前操作者 ID
     */
    @GetMapping
    public ResponseEntity<List<User>> listUsers(
            @RequestAttribute("userId") String operatorId) {
        List<User> users = userService.findAllUsers(operatorId);
        return ResponseEntity.ok(users);
    }
}