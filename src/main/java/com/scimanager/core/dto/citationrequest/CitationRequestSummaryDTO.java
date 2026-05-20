package com.scimanager.core.dto.citationrequest;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 查收查引请求摘要 DTO（列表展示用）
 *
 * <p>对应实体 {@link com.scimanager.core.entity.CitationRequest} 的摘要信息，<br>
 * 不含嵌套的 criteria 和 items 详情。用于列表页展示。</p>
 */
@Data
public class CitationRequestSummaryDTO {
	/** 流水号 */
	private String serialNumber;
	/** 请求状态（PENDING / PROCESSING / COMPLETED） */
	private String status;
	/** 创建时间 */
	private LocalDateTime createTime;
	/** 最后更新时间 */
	private LocalDateTime updateTime;
	/** 申请人 ID（从 requester.userId 映射） */
	private String requesterId;
}
