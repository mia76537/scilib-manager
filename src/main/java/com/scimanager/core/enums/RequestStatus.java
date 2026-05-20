package com.scimanager.core.enums;

/**
 * 查收查引请求状态枚举
 *
 * <p>定义请求生命周期的三个状态，在 CitationRequest 实体中以 {@code @Enumerated(EnumType.STRING)} 存储。</p>
 *
 * <ul>
 *   <li>PENDING — 待处理（初始状态）</li>
 *   <li>PROCESSING — 处理中（管理员已开始检索）</li>
 *   <li>COMPLETED — 已完成（检索结果已录入）</li>
 * </ul>
 */
public enum RequestStatus {
    PENDING,
    PROCESSING,
    COMPLETED
}
