package com.scimanager.core.enums;

/**
 * 用户角色枚举
 *
 * <p>用于替代字符串字面量，提供类型安全的角色判断。<br>
 * 当前系统中角色以字符串形式存储于 User 实体中，此枚举供后续类型安全重构使用。</p>
 *
 * <ul>
 *   <li>ADMIN — 管理员，系统最高权限</li>
 *   <li>MENTOR — 导师，可管理名下学生</li>
 *   <li>STUDENT — 学生，仅可操作本人数据</li>
 * </ul>
 */
public enum UserRole {
    ADMIN,
    MENTOR,
    STUDENT
}
