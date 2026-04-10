package com.scimanager.core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * 查收查引作品清单结构
 */
@Data
@Entity
public class CitationItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // 主键
	private String authors;// 作者
	private String title;// 论文标题
	private String sourcePublications;// 来源出版物
	private String publicationYear;// 出版年
	private String doi;// DOI号
	private String accessionNumber;// 入藏号
	private String remark;// 备注
}
