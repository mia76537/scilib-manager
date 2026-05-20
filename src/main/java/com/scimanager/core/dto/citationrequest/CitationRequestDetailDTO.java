package com.scimanager.core.dto.citationrequest;

import java.util.List;

import lombok.Data;

/**
 * 查收查引请求详情 DTO（详情页展示用）
 *
 * <p>继承 {@link CitationRequestSummaryDTO} 的摘要字段，<br>
 * 额外包含完整的查询范围（citationCriteria）和论文清单（citationItems）。</p>
 */
@Data
public class CitationRequestDetailDTO extends CitationRequestSummaryDTO {
	/** 查询范围（各数据库检索选项） */
	private CitationCriteriaDTO citationCriteria;
	/** 论文清单（含各论文的检索结果） */
	private List<CitationItemDTO> citationItems;
}
