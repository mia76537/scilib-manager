package com.scimanager.core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "citation_result")
public class CitationResult {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 关键：关联具体的某篇论文
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id")
	private CitationItem item;
	// 标识这是哪个指标（存字符串，如 "wosCits", "scieCits"）
	private String criteriaKey;
	// 结果数值
	private Integer value;
	// 可选：该指标下的入藏号（如果每种检索都有独立编号）
	private String specificAccessionNumber;
}