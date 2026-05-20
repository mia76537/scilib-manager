package com.scimanager.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.scimanager.core.entity.UserInterest;

/**
 * 用户兴趣画像数据访问层
 *
 * <p>提供 UserInterest 实体的基本 CRUD 操作。<br>
 * 主键 userId 与 User 表一对一关联。</p>
 */
public interface UserInterestRepository extends JpaRepository<UserInterest, String> {

}
