package com.scimanager.core.dto.citationrequest;

import lombok.Data;

/**
 * 检索结果 DTO（展示用）
 *
 * <p>用于在请求详情中展示每个检索指标的结果。</p>
 */
@Data
public class CitationResultDTO {
	/** 指标键（如 "wosCits", "scieCits"） */
	private String criteriaKey;
	/** 检索结果数值 */
	private Integer value;
	/** 该指标对应的独立入藏号 */
	private String specificAccessionNumber;
}
