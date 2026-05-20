package com.scimanager.core.dto.citationrequest;

import java.util.List;

import lombok.Data;

/**
 * 创建查收查引请求的入参 DTO
 *
 * <p>前端提交查收查引申请时需要提供的参数：<br>
 * 申请人 userId、查询范围 criteria、待检索论文清单 items。</p>
 */
@Data
public class CreateCitationRequestDTO {
	/** 申请人 ID */
	private String userId;
	/** 查询范围（各数据库的收录/引用/他引等选项，由 boolean 字段组成） */
	private CitationCriteriaDTO criteria;
	/** 待检索的论文清单 */
	private List<CitationItemDTO> items;
}
