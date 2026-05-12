package com.scimanager.core.model.dto.citationrequesdto;

import lombok.Data;

@Data
public class CitationResultDTO {
	private String criteriaKey; // 如 "wosCits"
	private Integer value; // 结果数值
	private String specificAccessionNumber; // 独立入藏号
}