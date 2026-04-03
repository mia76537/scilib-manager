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
    private Long id; // 添加主键
	private String authors;
	private String title;
    private String sourcePublications;
    private String publicationYear;
    private String volume;
    private String issue;
    private String page;
    private String accessionNumber;
    private String remark;
}
