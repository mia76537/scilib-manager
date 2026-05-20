package com.scimanager.core.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

/**
 * 查收查引论文条目实体
 *
 * <p>映射数据库 citation_item 表，存储查收查引申请中的待检索论文信息。<br>
 * 包含论文的元数据（作者、标题、来源等）以及关联的检索结果。</p>
 */
@Data
@Entity
public class CitationItem {

	/** 主键（数据库自增） */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 作者 */
	private String authors;

	/** 论文标题 */
	private String title;

	/** 来源出版物（期刊/会议名称） */
	private String sourcePublications;

	/** 出版年份 */
	private String publicationYear;

	/** DOI */
	private String doi;

	/** 入藏号 */
	private String accessionNumber;

	/** 备注 */
	private String remark;

	/** 检索结果列表（一对多，级联保存） */
	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
	private List<CitationResult> results;

	/** 所属请求（多对一，懒加载；insertable/updatable=false 表示由 request_id 字段维护） */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "request_id", insertable = false, updatable = false)
	private CitationRequest request;
}
