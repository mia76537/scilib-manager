package com.scimanager.core.model;

import java.time.LocalDateTime;
import java.util.List;

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
 * 查收查引请求结构，包含标准，论文，来源用户与状态
 */
@Data
@Entity
public class CitationRequest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 32)
	private String serialNumber; // 流水号

	// 一个申请对应一组标准
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "criteria_id")
	private CitationCriteria citationCriteria;

	// 一个申请包含多篇论文
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "request_id")
	private List<CitationItem> citationItems;

	// 多个申请能对应一个用户
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
	private User requester;

	@Enumerated(EnumType.STRING)
	private RequestStatus status = RequestStatus.PENDING;

	@Column(updatable = false)
	private LocalDateTime createTime = LocalDateTime.now();// 创建时间

	private LocalDateTime updateTime;// 更新时间

	public enum RequestStatus {
		// 待处理、处理中、已完成
		PENDING, PROCESSING, COMPLETED
	}

	@PreUpdate
	public void onPreUpdate() {
		this.updateTime = LocalDateTime.now(); // 仅在更新操作时触发,修改更新时间
	}

}
