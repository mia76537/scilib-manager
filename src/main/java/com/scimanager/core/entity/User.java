package com.scimanager.core.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 用户实体
 *
 * <p>映射数据库 users 表，存储系统用户信息。</p>
 *
 * <p><b>角色体系：</b></p>
 * <ul>
 *   <li>ADMIN — 管理员，拥有系统最高权限</li>
 *   <li>MENTOR — 导师，可查看名下学生的文献和兴趣画像</li>
 *   <li>STUDENT — 学生，仅可操作自己的数据</li>
 * </ul>
 *
 * <p><b>组织架构：</b>通过 mentorId 字段建立导师-学生关联关系。<br>
 * <b>注意：</b>当前密码以明文存储，@JsonProperty(WRITE_ONLY) 确保 JSON 序列化时不返回密码。</p>
 */
@Data
@Entity
@Table(name = "users")
public class User {

	// ==================== 基础信息 ====================

	/** 用户 ID（主键，由管理员创建时指定） */
	@Id
	private String userId;

	/** 用户显示名称 */
	private String userName;

	/**
	 * 登录密码
	 *
	 * <p>@JsonProperty(WRITE_ONLY) 确保 JSON 序列化时密码不会返回给前端，<br>
	 * 但前端传入的密码可以被正确反序列化。</p>
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	// ==================== 权限与组织架构 ====================

	/**
	 * 用户角色
	 *
	 * <p>取值：ADMIN（管理员）、MENTOR（导师）、STUDENT（学生）</p>
	 */
	private String role;

	/**
	 * 归属导师 ID
	 *
	 * <p>如果是学生，则填写其导师的 userId；如果是导师或管理员，此项可为空。</p>
	 */
	private String mentorId;

	// ==================== 关联关系 ====================

	/** 该用户上传的所有文献（一对多） */
	@JsonIgnore
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Paper> papers;

	/** 该用户提交的所有查收查引请求（一对多） */
	@JsonIgnore
	@OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CitationRequest> citationRequests;

}
