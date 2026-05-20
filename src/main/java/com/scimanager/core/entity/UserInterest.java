package com.scimanager.core.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 用户科研兴趣画像实体
 *
 * <p>映射数据库 user_interests 表，以 userId 为主键，与 User 表一对一关联。<br>
 * 存储 DeepSeek AI 分析用户文献关键词后生成的兴趣画像结果（JSON 格式）。</p>
 *
 * <p><b>缓存策略：</b>当用户新增/删除/修改文献关键词时，通过 {@code @Async} 异步重新生成画像并更新此表。</p>
 */
@Data
@Entity
@Table(name = "user_interests")
public class UserInterest {

	/** 用户 ID（与 User 表一对一） */
	@Id
	private String userId;

	/** AI 分析结果（JSON 字符串，TEXT 类型） */
	@Column(columnDefinition = "TEXT")
	private String analysisResult;

	/** 上次分析时间 */
	private LocalDateTime lastUpdateTime;

	/** 分析时的论文总数（用于判断是否需要重新分析） */
	private Integer paperCountSnapshot;
}
