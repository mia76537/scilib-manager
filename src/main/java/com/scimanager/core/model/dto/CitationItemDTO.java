package com.scimanager.core.model.dto;

import lombok.Data;


/**
 * 查收查引作品清单结构
 */
@Data
public class CitationItemDTO {
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
