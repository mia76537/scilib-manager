package com.scimanager.core.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.scimanager.core.enums.RequestStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import lombok.Data;

/**
 * 查收查引请求实体
 *
 * <p>映射数据库 citation_request 表，记录用户的查收查引申请。<br>
 * 每条请求包含一组查询标准（criteria）、一份论文清单（items）和状态信息。</p>
 *
 * <p><b>状态流转：</b> PENDING（待处理）→ PROCESSING（处理中）→ COMPLETED（已完成）</p>
 * <p><b>生命周期：</b> createTime 在创建时写入且不可修改，updateTime 通过 @PreUpdate 自动更新。</p>
 */
@Data
@Entity
public class CitationRequest {

	/** 主键（数据库自增） */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 流水号（32 位 UUID，唯一标识，用户可见） */
	@Column(unique = true, nullable = false, length = 32)
	private String serialNumber;

	/** 查询标准（一对一，级联保存和删除） */
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "criteria_id")
	private CitationCriteria citationCriteria;

	/** 论文清单（一对多，级联保存和删除） */
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "request_id")
	private List<CitationItem> citationItems;

	/** 申请人（多对一，懒加载） */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
	private User requester;

	/** 请求状态（枚举字符串形式存储，默认 PENDING） */
	@Enumerated(EnumType.STRING)
	private RequestStatus status = RequestStatus.PENDING;

	/** 创建时间（写入后不可修改） */
	@Column(updatable = false)
	private LocalDateTime createTime = LocalDateTime.now();

	/** 最后更新时间 */
	private LocalDateTime updateTime;

	/**
	 * JPA 更新前回调：自动设置 updateTime 为当前时间
	 */
	@PreUpdate
	public void onPreUpdate() {
		this.updateTime = LocalDateTime.now();
	}

}
