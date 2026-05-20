package com.scimanager.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.scimanager.core.entity.User;

/**
 * 用户数据访问层
 *
 * <p>提供 User 实体的基本 CRUD 操作及自定义查询。<br>
 * <b>注意：</b>findByUserIdAndPassword 当前为明文密码比对，预留 BCrypt 迁移。</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

	/**
	 * 根据 userId 和 password 查找用户（登录验证）
	 * JPA 自动生成：select * from users where userId = ? and password = ?
	 */
	User findByUserIdAndPassword(String userId, String password);

	/**
	 * 仅根据 userId 查找用户（用于区分"用户不存在"和"密码错误"）
	 */
	User findByUserId(String userId);

	/**
	 * 查询指定导师名下的所有学生
	 *
	 * @param mentorId 导师的用户 ID
	 * @return 学生列表
	 */
	List<User> findByMentorId(String mentorId);
}