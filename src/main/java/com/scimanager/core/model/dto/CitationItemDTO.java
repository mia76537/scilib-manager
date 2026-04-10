package com.scimanager.core.model.dto;

import lombok.Data;

/**
 * 查收查引作品清单结构
 */
@Data
public class CitationItemDTO {
	private Long id; // 主键
	private String authors;// 作者
	private String title;// 论文标题
	private String sourcePublications;// 来源出版物
	private String publicationYear;// 出版年
	private String doi;// DOI号
	private String accessionNumber;// 入藏号
	private String remark;// 备注
}
