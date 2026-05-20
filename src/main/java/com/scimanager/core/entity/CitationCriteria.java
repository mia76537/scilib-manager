package com.scimanager.core.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 查收查引查询范围配置实体
 *
 * <p>映射数据库 citation_criteria 表，记录用户提交查收查引请求时选择的检索范围。<br>
 * 所有字段均为 boolean 类型，true 表示需要查询该项。</p>
 *
 * <p><b>分类说明：</b></p>
 * <ul>
 *   <li>收录情况 — 论文是否被某个数据库收录（Index）</li>
 *   <li>引用情况 — 论文被引用次数（Citations）</li>
 *   <li>引文情况 — 论文的参考文献信息（References）</li>
 *   <li>他引情况 — 排除自引后的被引次数（Non-Self Citations）</li>
 *   <li>拓展查询 — 期刊排名、热点论文等增值服务</li>
 * </ul>
 */
@Data
@Entity
@Table(name = "citation_criteria")
public class CitationCriteria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// ========== 外文库 · 收录情况 ==========
	private boolean ssciIdx;           // SSCI 收录
	private boolean scieIdx;           // SCI-E 收录
	private boolean ahciIdx;           // A&HCI 收录
	private boolean cpcisIdx;          // CPCI-S 收录
	private boolean cpciSshIdx;        // CPCI-SSH 收录
	private boolean eiCompendexIdx;    // EI Compendex 收录

	// ========== 中文库 · 收录情况 ==========
	private boolean cnkiIdx;           // CNKI 收录

	// ========== 外文库 · 引用情况 ==========
	private boolean wosCits;           // WOS 核心合集被引次数
	private boolean ssciCits;          // SSCI 被引次数
	private boolean scieCits;          // SCI-E 被引次数

	// ========== 中文库 · 引用情况 ==========
	private boolean cnkiCits;          // CNKI 被引次数

	// ========== 外文库 · 引文情况 ==========
	private boolean wosRefs;           // WOS 参考文献数
	private boolean ssciRefs;          // SSCI 参考文献数
	private boolean scieRefs;          // SCI-E 参考文献数

	// ========== 中文库 · 引文情况 ==========
	private boolean cnkiRefs;          // CNKI 参考文献数

	// ========== 外文库 · 他引情况 ==========
	private boolean woscNsc;           // WOS 他引次数
	private boolean ssciNsc;           // SSCI 他引次数
	private boolean scieNsc;           // SCI-E 他引次数

	// ========== 中文库 · 他引情况 ==========
	private boolean cnkiNsc;           // CNKI 他引次数

	// ========== 外文拓展查询 ==========
	private boolean esiHotTopics;      // ESI 热点论文
	private boolean esiHighlyCited;    // ESI 高被引论文
	private boolean casJournalRanking; // CAS 期刊排名
	private boolean jreJournalRanking; // JRE 期刊排名
	private boolean jreImpactFactor;   // JRE 影响因子

	// ========== 中文拓展查询 ==========
	private boolean cscdJournalSource;          // CSCD 期刊源
	private boolean cssciJournalSource;         // CSSCI 期刊源
	private boolean cssciExtendedSource;        // CSSCI 扩展版期刊源
	private boolean pekingUniversityCoreJournals; // 北大核心期刊
}
