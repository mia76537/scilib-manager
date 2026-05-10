package com.scimanager.core.model.dto.citationrequesdto;

import java.util.List;

import lombok.Data;

/**
 * 创建查收查引请求的入参封装
 */
@Data
public class CreateCitationRequestDTO {
	private String userId;
	private CitationCriteriaDTO criteria; // 查询标准
	private List<CitationItemDTO> items; // 论文清单
}