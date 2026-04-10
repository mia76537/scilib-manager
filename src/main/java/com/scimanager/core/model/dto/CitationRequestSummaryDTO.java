package com.scimanager.core.model.dto;

import java.time.LocalDateTime;

import lombok.Data;

//列表展示摘要DTO
@Data
public class CitationRequestSummaryDTO {
	private String serialNumber;
	private String status;
	private LocalDateTime createTime;
	private String requesterId; // 这里映射 entity.getRequester().getUserId()
}