package com.scimanager.core.repository;

import com.scimanager.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * 通过 Repository 查询匹配的用户
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // JPA 会自动根据方法名生成 SQL：select * from users where userId = ? and userPwd = ?
    User findByUserIdAndPassword(String userId, String password);
    //新增：仅根据 ID 查找，用于区分错误类型
    User findByUserId(String userId);
}