package com.scimanager.core.entity;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 文献（论文）实体
 *
 * <p>映射数据库 papers 表，存储上传的文献信息。<br>
 * 元数据（标题、作者等）通过 {@link com.scimanager.core.service.impl.CitationInternalService}
 * 异步 AI 解析后填充。</p>
 *
 * <p><b>关键字段说明：</b></p>
 * <ul>
 *   <li>paperCitation — 存储 GB/T 7714-2015 格式引文，由 AI 生成</li>
 *   <li>keyWords — 使用 @ElementCollection 存储在单独的 paper_keywords 表中</li>
 *   <li>localPath — 文件在磁盘上的相对路径（相对于配置的上传目录）</li>
 *   <li>owner — 文献所有者（N:1 关联 User）</li>
 * </ul>
 */
@Data
@Entity
@Table(name = "papers")
public class Paper {

	// ==================== 主数据 ====================

	/** 文献 ID（数据库自增主键） */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 原始文件名（上传时的文件名） */
	private String paperName;

	/**
	 * 关键词列表
	 *
	 * <p>通过 @ElementCollection 自动创建关联表 paper_keywords，<br>
	 * 每条关键词占一行，外键关联 paper_id。</p>
	 */
	@ElementCollection
	@CollectionTable(name = "paper_keywords", joinColumns = @JoinColumn(name = "paper_id"))
	@Column(name = "keyword")
	private List<String> keyWords;

	// ==================== 文件属性 ====================

	/** 文件在磁盘上的存储路径（相对路径，相对于 file.upload-path） */
	private String localPath;

	// ==================== 文献元数据（AI 提取后填充） ====================

	/** 作者（多个作者用逗号分隔） */
	private String paperAuthors;

	/** 论文标题 */
	private String paperTitle;

	/** 来源出版物（期刊名或会议名） */
	private String paperSourcePublications;

	/** 出版年份 */
	private String paperPublicationYear;

	/** DOI（数字对象标识符） */
	private String paperDoi;

	/** 入藏号（WOS/CNKI 等数据库的索引号） */
	private String paperAccessionNumber;

	/** GB/T 7714-2015 格式引文（TEXT 类型，支持长文本） */
	@Column(columnDefinition = "TEXT")
	private String paperCitation;

	// ==================== 归属与管理 ====================

	/**
	 * 文献所有者（多对一关联 User）
	 *
	 * <p>用户删除时会级联删除其所有文献。</p>
	 */
	@ManyToOne
	@JoinColumn(name = "user_id")
	@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
	private User owner;

}
