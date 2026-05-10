package com.scimanager.core.service.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import com.scimanager.core.model.User;
import com.scimanager.core.model.dto.userdto.AdminUserUpdateDTO;
import com.scimanager.core.model.dto.userdto.UserCreateDTO;
import com.scimanager.core.model.dto.userdto.UserProfileDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {

	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

	// 个人基本信息展示
	UserProfileDTO toProfileDto(User user);

	// 个人基本信息修改
	// @MappingTarget 表示将 DTO 的值更新到现有的 User 对象中，避免覆盖 userId 等字段
	@Mapping(target = "userId", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "papers", ignore = true)
	@Mapping(target = "citationRequests", ignore = true)
	void updateEntityFromDto(UserProfileDTO dto, @MappingTarget User user);

	// 管理员创建用户
	@Mapping(target = "password", ignore = true) // 初始密码逻辑在 Service 层处理
	@Mapping(target = "papers", ignore = true)
	@Mapping(target = "citationRequests", ignore = true)
	User toEntity(UserCreateDTO dto);

	// 管理员修改归属或重置密码
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "userId", ignore = true)
	@Mapping(target = "password", ignore = true) // 密码重置逻辑通过 Service 层判断 resetPassword 标志位
	void adminUpdateEntity(AdminUserUpdateDTO dto, @MappingTarget User user);
}
