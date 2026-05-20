package com.scimanager.core.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.scimanager.core.dto.citationrequest.CitationCriteriaDTO;
import com.scimanager.core.dto.citationrequest.CitationItemDTO;
import com.scimanager.core.dto.citationrequest.CitationRequestDetailDTO;
import com.scimanager.core.dto.citationrequest.CitationRequestSummaryDTO;
import com.scimanager.core.dto.citationrequest.CitationResultSubmitDTO;
import com.scimanager.core.dto.citationrequest.CreateCitationRequestDTO;
import com.scimanager.core.entity.CitationCriteria;
import com.scimanager.core.entity.CitationItem;
import com.scimanager.core.entity.CitationRequest;
import com.scimanager.core.entity.CitationResult;
import com.scimanager.core.entity.User;

/**
 * 查收查引模块 MapStruct 映射器
 *
 * <p>负责 CitationRequest 相关实体与 DTO 之间的双向转换。<br>
 * 使用 {@code componentModel = "spring"}，自动生成实现并注册为 Spring Bean。</p>
 *
 * <p><b>关键映射说明：</b></p>
 * <ul>
 *   <li>Entity → DTO：通过 source 指定嵌套路径（如 requester.userId → requesterId）</li>
 *   <li>DTO → Entity：忽略自增 ID，硬编码 PENDING 初始状态</li>
 *   <li>CitationResult 的 item 关联在 Service 层处理（Mapper 中忽略）</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface CitationMapper {

	/**
	 * Entity → 摘要 DTO（列表页展示）
	 * 将 requester.userId 扁平化为 requesterId
	 */
	@Mapping(target = "requesterId", source = "requester.userId")
	CitationRequestSummaryDTO toSummaryDTO(CitationRequest entity);

	/**
	 * Entity → 详情 DTO（详情页展示）
	 * 自动映射嵌套的 citationCriteria 和 citationItems 对象树
	 */
	@Mapping(target = "requesterId", source = "requester.userId")
	CitationRequestDetailDTO toDetailDTO(CitationRequest entity);

	/**
	 * 批量 Entity → 摘要 DTO 列表
	 */
	@IterableMapping(elementTargetType = CitationRequestSummaryDTO.class)
	List<CitationRequestSummaryDTO> toSummaryDTOList(List<CitationRequest> entities);

	/**
	 * CreateDTO + 额外参数 → Entity
	 *
	 * <p>将前端提交的创建请求 DTO 转换为 JPA 实体：</p>
	 * <ul>
	 *   <li>id — 忽略（由 JPA 自增）</li>
	 *   <li>serialNumber — 从 sn 参数映射</li>
	 *   <li>requester — 从 user 参数映射</li>
	 *   <li>status — 硬编码为 PENDING</li>
	 *   <li>createTime — 从 now 参数映射</li>
	 *   <li>updateTime — 忽略（初始为 null）</li>
	 *   <li>citationCriteria — 从 dto.criteria 映射（嵌套转换）</li>
	 *   <li>citationItems — 从 dto.items 映射（嵌套转换）</li>
	 * </ul>
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "serialNumber", source = "sn")
	@Mapping(target = "requester", source = "user")
	@Mapping(target = "status", constant = "PENDING")
	@Mapping(target = "createTime", source = "now")
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "citationCriteria", source = "dto.criteria")
	@Mapping(target = "citationItems", source = "dto.items")
	CitationRequest toEntity(CreateCitationRequestDTO dto, User user, String sn, LocalDateTime now);

	/**
	 * DTO → 查询标准实体（忽略自增 ID）
	 */
	@Mapping(target = "id", ignore = true)
	CitationCriteria toCriteriaEntity(CitationCriteriaDTO dto);

	/**
	 * 批量 DTO → 论文条目实体列表
	 */
	List<CitationItem> toItemEntityList(List<CitationItemDTO> dtos);

	/**
	 * 结果提交条目 → 检索结果实体
	 *
	 * <p>item 字段在 Service 层通过 EntityManager 手动关联，Mapper 中忽略。</p>
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "item", ignore = true)
	@Mapping(target = "specificAccessionNumber", source = "entry.specificAccessionNumber")
	CitationResult toResultEntity(CitationResultSubmitDTO.ItemResultEntry entry);

	/**
	 * 批量转换结果提交条目
	 */
	List<CitationResult> toResultEntityList(List<CitationResultSubmitDTO.ItemResultEntry> entries);
}
