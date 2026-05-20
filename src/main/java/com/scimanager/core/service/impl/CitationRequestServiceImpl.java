package com.scimanager.core.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scimanager.core.dto.citationrequest.CitationResultSubmitDTO;
import com.scimanager.core.dto.citationrequest.CreateCitationRequestDTO;
import com.scimanager.core.entity.CitationItem;
import com.scimanager.core.entity.CitationRequest;
import com.scimanager.core.entity.CitationResult;
import com.scimanager.core.entity.User;
import com.scimanager.core.enums.RequestStatus;
import com.scimanager.core.mapper.CitationMapper;
import com.scimanager.core.repository.CitationRequestRepository;
import com.scimanager.core.repository.CitationResultRepository;
import com.scimanager.core.repository.UserRepository;
import com.scimanager.core.service.CitationRequestService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

/**
 * 查收查引请求服务实现
 *
 * <p>处理请求的创建、状态流转、检索结果录入和查询。<br>
 * 核心权限模型：管理员可执行所有操作，普通用户只能操作自己的请求。</p>
 *
 * <p><b>关键业务规则：</b></p>
 * <ul>
 *   <li>创建请求时自动生成 UUID 流水号，初始状态为 PENDING</li>
 *   <li>状态变更仅限管理员操作</li>
 *   <li>提交结果采用"先删后插"策略，确保幂等性</li>
 *   <li>查询时根据角色自动限定数据范围</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CitationRequestServiceImpl implements CitationRequestService {

	private final CitationRequestRepository requestRepository;
	private final UserRepository userRepository;
	private final CitationMapper citationMapper;
	private final CitationResultRepository resultRepository;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * 校验用户是否为管理员
	 *
	 * @param userId 用户 ID
	 * @return true 如果用户角色为 ADMIN
	 */
	private boolean isAdmin(String userId) {
		User user = userRepository.findByUserId(userId);
		return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
	}

	/**
	 * 创建查收查引请求
	 *
	 * <p><b>执行流程：</b></p>
	 * <ol>
	 *   <li>校验用户是否存在</li>
	 *   <li>使用 MapStruct Mapper 将 DTO 转换为实体（含流水号生成、PENDING 状态设置）</li>
	 *   <li>持久化到数据库（级联保存 criteria 和 items）</li>
	 * </ol>
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public CitationRequest createCitationRequest(CreateCitationRequestDTO dto, String userId) {
		User user = userRepository.findByUserId(userId);
		if (user == null)
			throw new RuntimeException("用户不存在");

		// mapper 内部处理：sn 生成、PENDING 初始状态、关联 User
		CitationRequest request = citationMapper.toEntity(dto, user, generateSerialNumber(), LocalDateTime.now());
		return requestRepository.save(request);
	}

	/**
	 * 根据流水号查询请求详情（含权限校验）
	 *
	 * <p>管理员可查看任意请求；普通用户只能查看自己的请求。</p>
	 *
	 * @throws RuntimeException 如果请求不存在或无权访问
	 */
	@Override
	public CitationRequest findBySerialNumber(String serialNumber, String userId) {
		CitationRequest request = requestRepository.findBySerialNumber(serialNumber)
				.orElseThrow(() -> new RuntimeException("请求不存在"));

		// 权限校验：非管理员只能查看自己的请求
		if (!isAdmin(userId) && !request.getRequester().getUserId().equals(userId)) {
			throw new RuntimeException("无权访问该请求");
		}
		return request;
	}

	/**
	 * 更新请求状态（管理员）
	 *
	 * <p>典型状态流转：PENDING → PROCESSING → COMPLETED</p>
	 *
	 * @throws RuntimeException 如果操作者不是管理员，或请求不存在
	 */
	@Override
	@Transactional
	public void updateRequestStatus(String serialNumber, RequestStatus status, String userId) {
		if (!isAdmin(userId)) {
			throw new RuntimeException("权限不足，仅管理员可修改状态");
		}
		CitationRequest request = requestRepository.findBySerialNumber(serialNumber)
				.orElseThrow(() -> new RuntimeException("请求不存在"));

		request.setStatus(status);
		request.setUpdateTime(LocalDateTime.now());
		requestRepository.save(request);
	}

	/**
	 * 删除请求
	 *
	 * <p>级联删除关联的 criteria、items 及 results（通过 JPA cascade 配置自动处理）。</p>
	 * <ul>
	 *   <li>管理员可删除任意请求</li>
	 *   <li>普通用户只能删除自己的请求</li>
	 * </ul>
	 */
	@Override
	@Transactional
	public void deleteRequest(String serialNumber, String userId) {
		CitationRequest request = requestRepository.findBySerialNumber(serialNumber)
				.orElseThrow(() -> new RuntimeException("请求不存在"));

		if (!isAdmin(userId) && !request.getRequester().getUserId().equals(userId)) {
			throw new RuntimeException("无权删除该请求");
		}
		requestRepository.delete(request);
	}

	/**
	 * 综合条件查询请求列表
	 *
	 * <p>支持按状态、流水号、申请人三个维度组合筛选。<br>
	 * <b>权限逻辑：</b>管理员可指定 requesterId 查任意用户；普通用户只能查自己的请求。</p>
	 *
	 * @param status       状态过滤（可选）
	 * @param serialNumber 流水号过滤（可选）
	 * @param requesterId  申请人过滤（可选，仅管理员有效）
	 * @param userId       当前用户 ID
	 */
	@Override
	public List<CitationRequest> findRequestsByCondition(RequestStatus status, String serialNumber, String requesterId,
			String userId) {
		boolean isAdmin = isAdmin(userId);
		// 管理员可按 requesterId 筛选，非管理员只能查自己的
		String targetUserId = isAdmin ? requesterId : userId;
		return requestRepository.findWithConditions(status, serialNumber, targetUserId);
	}

	/**
	 * 提交检索结果（管理员）
	 *
	 * <p><b>执行流程：</b></p>
	 * <ol>
	 *   <li>校验管理员权限</li>
	 *   <li>将提交 DTO 批量转换为 CitationResult 实体（仅填充 criteriaKey、value、specificAccessionNumber）</li>
	 *   <li>删除该请求下所有旧的结果（保证幂等）</li>
	 *   <li>遍历结果列表，通过 EntityManager 加载对应的 CitationItem 并建立关联</li>
	 *   <li>批量保存新结果</li>
	 *   <li>更新请求的 updateTime 时间戳</li>
	 * </ol>
	 *
	 * @param submitDTO 包含流水号和结果条目的 DTO
	 * @param userId    操作者 ID（需为管理员）
	 */
	@Override
	@Transactional
	public void submitResults(CitationResultSubmitDTO submitDTO, String userId) {
		if (!isAdmin(userId)) {
			throw new RuntimeException("权限不足，仅管理员可提交检索结果");
		}

		// 将提交条目转换为 CitationResult 实体（此时 item 字段为空）
		List<CitationResult> newResults = citationMapper.toResultEntityList(submitDTO.getResults());

		// 收集涉及的所有 itemId，用于批量删除旧结果
		List<Long> itemIds = submitDTO.getResults().stream()
				.map(CitationResultSubmitDTO.ItemResultEntry::getItemId)
				.distinct()
				.toList();

		// 先删后插：删除这些 item 下已有的所有结果，保证幂等
		if (!itemIds.isEmpty()) {
			resultRepository.deleteByItemIdIn(itemIds);
		}

		// 为每个结果关联对应的 CitationItem 实体
		CitationRequest parentRequest = null;
		for (int i = 0; i < newResults.size(); i++) {
			Long itemId = submitDTO.getResults().get(i).getItemId();
			CitationItem item = entityManager.find(CitationItem.class, itemId);

			if (item != null) {
				newResults.get(i).setItem(item);
				// 首次找到有效 item 时，一并加载父请求用于更新时间戳
				if (parentRequest == null) {
					parentRequest = requestRepository.findBySerialNumber(submitDTO.getSerialNumber()).orElse(null);
				}
			}
		}

		// 批量保存新结果
		resultRepository.saveAll(newResults);

		// 更新父请求的更新时间
		if (parentRequest != null) {
			parentRequest.setUpdateTime(LocalDateTime.now());
			requestRepository.save(parentRequest);
		}
	}

	/**
	 * 生成唯一流水号（32 位 UUID，不含连字符）
	 */
	private String generateSerialNumber() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
