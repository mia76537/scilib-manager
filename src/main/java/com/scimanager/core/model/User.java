package com.scimanager.core.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "users")

public class User {

	// --- 基础信息 ---
	@Id
	private String userId; // 用户ID
	private String userName; // 用户名
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password; // 密码（实际开发待加密存储）

	// --- 权限与组织架构 ---
	private String role; // 角色：ADMIN (管理员), MENTOR (导师), STUDENT (学生)
	private String mentorId;// 归属导师ID。如果是学生，则填写其导师的userId；如果是导师或管理员，此项可为空。

	// --- 关联关系 ---
	// 一个用户对应多篇论文
	@JsonIgnore
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Paper> papers;

	// 一个用户可提交多个查收查引请求
	@JsonIgnore
	@OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CitationRequest> citationRequests;

}