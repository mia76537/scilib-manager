package com.scimanager.core.service.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.scimanager.core.model.CitationCriteria;
import com.scimanager.core.model.CitationItem;
import com.scimanager.core.model.CitationRequest;
import com.scimanager.core.model.User;
import com.scimanager.core.model.dto.CitationCriteriaDTO;
import com.scimanager.core.model.dto.CitationItemDTO;
import com.scimanager.core.model.dto.CitationRequestDetailDTO;
import com.scimanager.core.model.dto.CitationRequestSummaryDTO;
import com.scimanager.core.model.dto.CreateCitationRequestDTO;

@Mapper(componentModel = "spring")
public interface CitationMapper {

	// 1. 映射精简摘要（列表页）
	// 通过 source 指定实体嵌套路径到 DTO 扁平字段的映射
	@Mapping(target = "requesterId", source = "requester.userId")
	CitationRequestSummaryDTO toSummaryDTO(CitationRequest entity);

	// 2. 映射完整详情（详情页）
	// 字段名已对应，MapStruct 会自动映射嵌套的 citationCriteria 和 citationItems
	@Mapping(target = "requesterId", source = "requester.userId")
	CitationRequestDetailDTO toDetailDTO(CitationRequest entity);

	// 3. 批量映射
	@IterableMapping(elementTargetType = CitationRequestSummaryDTO.class)
	List<CitationRequestSummaryDTO> toSummaryDTOList(List<CitationRequest> entities);

	@Mapping(target = "id", ignore = true) // 数据库自增ID通常由JPA处理
	@Mapping(target = "serialNumber", source = "sn") // 映射生成的流水号
	@Mapping(target = "requester", source = "user") // 映射关联的用户实体对象
	@Mapping(target = "status", constant = "PENDING") // 硬编码初始状态
	@Mapping(target = "createTime", source = "now") // 映射传入的系统时间
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "citationCriteria", source = "dto.criteria") // 映射嵌套的查询标准
	@Mapping(target = "citationItems", source = "dto.items") // 映射嵌套的论文清单
	CitationRequest toEntity(CreateCitationRequestDTO dto, User user, String sn, LocalDateTime now);

	// MapStruct 发现 CitationRequest 里的字段需要 CitationCriteria 实体时，
	// 会自动调用下面这个方法来转换嵌套对象
	CitationCriteria toCriteriaEntity(CitationCriteriaDTO dto);

	// 同理，处理 List<CitationItem> 的转换
	List<CitationItem> toItemEntityList(List<CitationItemDTO> dtos);
}