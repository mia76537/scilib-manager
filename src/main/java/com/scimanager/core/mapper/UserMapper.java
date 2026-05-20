package com.scimanager.core.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import com.scimanager.core.dto.user.AdminUserUpdateDTO;
import com.scimanager.core.dto.user.UserCreateDTO;
import com.scimanager.core.dto.user.UserProfileDTO;
import com.scimanager.core.entity.User;

/**
 * 用户模块 MapStruct 映射器
 *
 * <p>负责 User 实体与各 DTO 之间的转换。<br>
 * 使用 {@code componentModel = "spring"}，自动注册为 Spring Bean。</p>
 *
 * <p><b>安全策略：</b>在 DTO → Entity 转换时，userId、password、role 等敏感字段被忽略，<br>
 * 确保仅更新允许的字段，防止字段越权修改。</p>
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

	/** 手动实例化（用于非 Spring 环境测试） */
	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

	/**
	 * Entity → 个人资料 DTO
	 * 不映射 password 字段（User 实体中 password 已标注 @JsonProperty(WRITE_ONLY)）
	 */
	UserProfileDTO toProfileDto(User user);

	/**
	 * DTO → Entity（个人资料更新）
	 *
	 * <p>使用 @MappingTarget 将 DTO 的值合并到现有 User 实体中。<br>
	 * 忽略 userId、password、role、papers、citationRequests 以确保安全。</p>
	 */
	@Mapping(target = "userId", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "papers", ignore = true)
	@Mapping(target = "citationRequests", ignore = true)
	void updateEntityFromDto(UserProfileDTO dto, @MappingTarget User user);

	/**
	 * CreateDTO → Entity（管理员创建用户）
	 *
	 * <p>password 在 Service 层处理初始密码逻辑，此处忽略。</p>
	 */
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "papers", ignore = true)
	@Mapping(target = "citationRequests", ignore = true)
	User toEntity(UserCreateDTO dto);

	/**
	 * AdminDTO → Entity（管理员修改归属/重置密码）
	 *
	 * <p>使用 {@link NullValuePropertyMappingStrategy#IGNORE}，<br>
	 * 仅更新 DTO 中非 null 的字段。</p>
	 */
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "userId", ignore = true)
	void updateFromAdminDto(AdminUserUpdateDTO dto, @MappingTarget User user);
}
