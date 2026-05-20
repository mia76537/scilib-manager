package com.scimanager.core.dto.citationrequest;

import java.util.List;

import lombok.Data;

/**
 * 查收查引论文条目 DTO
 *
 * <p>对应实体 {@link com.scimanager.core.entity.CitationItem}，<br>
 * 用于前端展示或提交查收查引请求时的论文清单。</p>
 */
@Data
public class CitationItemDTO {
	private Long id;                // 主键
	private String authors;         // 作者
	private String title;           // 论文标题
	private String sourcePublications; // 来源出版物
	private String publicationYear; // 出版年
	private String doi;             // DOI号
	private String accessionNumber; // 入藏号
	private String remark;          // 备注
	/** 检索结果列表（仅在详情展示时填充） */
	private List<CitationResultDTO> results;
}
