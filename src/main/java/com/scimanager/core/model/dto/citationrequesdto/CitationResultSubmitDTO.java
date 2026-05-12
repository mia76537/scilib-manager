package com.scimanager.core.model.dto.citationrequesdto;

import java.util.List;

import lombok.Data;

//这是作为结果填写入参
@Data
public class CitationResultSubmitDTO {
	private String serialNumber; // 用于锁定是哪次申请
	private List<ItemResultEntry> results;

	@Data
	public static class ItemResultEntry {
		private Long itemId; // 对应 CitationItem 的 id
		private String criteriaKey; // 对应的指标键，如 "cnkiCits"
		private Integer value; // 查到的具体数值
		private String specificAccessionNumber; // 可选：入藏号
	}
}