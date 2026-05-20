package com.scimanager.core.dto.citationrequest;

import java.util.List;

import lombok.Data;

/**
 * 检索结果提交 DTO
 *
 * <p>管理员填写检索结果时使用。<br>
 * 包含请求流水号以及多个结果条目，每个结果条目对应一篇论文的一个检索指标。</p>
 */
@Data
public class CitationResultSubmitDTO {
	/** 请求流水号（用于定位是哪次申请） */
	private String serialNumber;
	/** 检索结果条目列表 */
	private List<ItemResultEntry> results;

	/**
	 * 单个检索结果条目
	 */
	@Data
	public static class ItemResultEntry {
		/** 对应的论文条目 ID（CitationItem.id） */
		private Long itemId;
		/** 指标键（如 "cnkiCits", "wosCits", "scieCits" 等） */
		private String criteriaKey;
		/** 查到的具体数值（如被引次数） */
		private Integer value;
		/** 可选：该指标对应的入藏号 */
		private String specificAccessionNumber;
	}
}
