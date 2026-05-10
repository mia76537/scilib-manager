package com.scimanager.core.model;

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

@Data
@Entity
@Table(name = "papers")
public class Paper {

	// --- 主数据 ---
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // 文件ID 主键
	private String paperName; // 文件名
	@ElementCollection // JPA 自动创建一张关联表来存这些 关键词
	@CollectionTable(name = "paper_keywords", joinColumns = @JoinColumn(name = "paper_id"))
	@Column(name = "keyword")
	private List<String> keyWords; // 文件关键词

	// --- 文件属性 ---
	private String localPath; // 文件路径
	@Column(columnDefinition = "TEXT")

	// 文献元数据
	private String paperAuthors;// 作者
	private String paperTitle;// 论文标题
	private String paperSourcePublications;// 来源出版物
	private String paperPublicationYear;// 出版年
	private String paperDoi;// DOI号
	private String paperAccessionNumber;// 入藏号
	private String paperCitation; // 存储 GB/T 7714 格式引文

	// --- 归属与管理 ---
	@ManyToOne
	@JoinColumn(name = "user_id")
	@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
	private User owner; // 标记这篇文献是谁上传的

}