package com.scimanager.core.dto.citationrequest;

import lombok.Data;

/**
 * 查收查引查询范围 DTO
 *
 * <p>与实体 {@link com.scimanager.core.entity.CitationCriteria} 字段一致，<br>
 * 所有 boolean 字段均为 true 表示需要查询该项。</p>
 *
 * <p><b>分类：</b>收录情况、引用情况、引文情况、他引情况、拓展查询</p>
 */
@Data
public class CitationCriteriaDTO {
	// ========== 外文库 · 收录情况 ==========
	private boolean ssciIdx;
	private boolean scieIdx;
	private boolean ahciIdx;
	private boolean cpcisIdx;
	private boolean cpciSshIdx;
	private boolean eiCompendexIdx;

	// ========== 中文库 · 收录情况 ==========
	private boolean cnkiIdx;

	// ========== 外文库 · 引用情况 ==========
	private boolean wosCits;
	private boolean ssciCits;
	private boolean scieCits;

	// ========== 中文库 · 引用情况 ==========
	private boolean cnkiCits;

	// ========== 外文库 · 引文情况 ==========
	private boolean wosRefs;
	private boolean ssciRefs;
	private boolean scieRefs;

	// ========== 中文库 · 引文情况 ==========
	private boolean cnkiRefs;

	// ========== 外文库 · 他引情况 ==========
	private boolean woscNsc;
	private boolean ssciNsc;
	private boolean scieNsc;

	// ========== 中文库 · 他引情况 ==========
	private boolean cnkiNsc;

	// ========== 外文拓展查询 ==========
	private boolean esiHotTopics;
	private boolean esiHighlyCited;
	private boolean casJournalRanking;
	private boolean jreJournalRanking;
	private boolean jreImpactFactor;

	// ========== 中文拓展查询 ==========
	private boolean cscdJournalSource;
	private boolean cssciJournalSource;
	private boolean cssciExtendedSource;
	private boolean pekingUniversityCoreJournals;
}
