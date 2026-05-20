package com.scimanager.core.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 检索结果条目实体
 *
 * <p>映射数据库 citation_result 表，存储管理员录入的查收查引检索结果。<br>
 * 每个结果条目关联到 CitationItem（某篇论文）和一个具体的检索指标（如 WOS 被引次数）。</p>
 *
 * <p><b>典型指标键（criteriaKey）：</b> wosCits, scieCits, ssciCits, cnkiCits, wosRefs 等</p>
 */
@Data
@Entity
@Table(name = "citation_result")
public class CitationResult {

	/** 主键（数据库自增） */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 关联的论文条目（多对一，懒加载） */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id")
	private CitationItem item;

	/** 检索指标键（如 "wosCits", "scieCits", "cnkiCits" 等） */
	private String criteriaKey;

	/** 检索结果数值 */
	private Integer value;

	/** 可选：该指标下的独立入藏号 */
	private String specificAccessionNumber;
}
