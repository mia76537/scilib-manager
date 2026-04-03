package com.scimanager.core.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "user_interests")
public class UserInterest {
	@Id
	private String userId; // 与 User 一对一

	@Column(columnDefinition = "TEXT")
	private String analysisResult; // 存储 AI 返回的表格或 JSON 字符串

	private LocalDateTime lastUpdateTime; // 记录上次分析时间
	private Integer paperCountSnapshot; // 记录分析时的论文总数，用于判断是否需要更新
}