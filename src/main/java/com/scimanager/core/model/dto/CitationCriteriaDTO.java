package com.scimanager.core.model.dto;

import lombok.Data;

/**
 * 查收查引查询范围结构，boolean确定是否需要这项查询
 */
@Data
public class CitationCriteriaDTO {
	// --数据库，外文库，收录情况
	private boolean ssciIdx;
	private boolean scieIdx;
	private boolean ahciIdx;
	private boolean cpcisIdx;
	private boolean cpciSshIdx;
	private boolean eiCompendexIdx;
	// --数据库，中文库，收录情况
	private boolean cnkiIdx;
	// --数据库，外文库，引用情况
	private boolean wosCits;
	private boolean ssciCits;
	private boolean scieCits;
	// --数据库，中文库，引用情况
	private boolean cnkiCits;
	// --数据库，外文库，引文情况
	private boolean wosRefs;
	private boolean ssciRefs;
	private boolean scieRefs;
	// --数据库，中文库，引文情况
	private boolean cnkiRefs;
	// --数据库，外文库，他引情况
	private boolean woscNsc;
	private boolean ssciNsc;
	private boolean scieNsc;
	// --数据库，中文库，他引情况
	private boolean cnkiNsc;
	// --其他外文数据库查询拓展
	private boolean esiHotTopics;
	private boolean esiHighlyCited;
	private boolean casJournalRanking;
	private boolean jreJournalRanking;
	private boolean jreImpactFactor;

	// --其他中文数据库查询拓展
	private boolean cscdJournalSource;
	private boolean cssciJournalSource;
	private boolean cssciExtendedSource;
	private boolean pekingUniversityCoreJournals;
}
