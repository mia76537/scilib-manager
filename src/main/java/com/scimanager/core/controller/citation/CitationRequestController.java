package com.scimanager.core.controller.citation;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scimanager.core.common.Result;
import com.scimanager.core.dto.citationrequest.CitationRequestDetailDTO;
import com.scimanager.core.dto.citationrequest.CitationRequestSummaryDTO;
import com.scimanager.core.dto.citationrequest.CitationResultSubmitDTO;
import com.scimanager.core.dto.citationrequest.CreateCitationRequestDTO;
import com.scimanager.core.entity.CitationRequest;
import com.scimanager.core.enums.RequestStatus;
import com.scimanager.core.mapper.CitationMapper;
import com.scimanager.core.service.CitationRequestService;

import lombok.RequiredArgsConstructor;

/**
 * 查收查引请求控制层
 *
 * <p>处理查收查引请求的 CRUD、状态流转及检索结果录入。</p>
 * <ul>
 *   <li>普通用户可创建请求、查看自己的请求列表/详情、删除自己的请求</li>
 *   <li>管理员可推进状态、提交检索结果、查看/删除任意请求</li>
 * </ul>
 * 统一使用 {@link Result} 返回格式，异常由 {@link com.scimanager.core.controller.advice.GlobalExceptionHandler} 统一处理。
 *
 * <p><b>执行流程：</b> Controller → Service → Mapper(DTO↔Entity) → Repository</p>
 */
@RestController
@RequestMapping("/api/citation-requests")
@RequiredArgsConstructor
public class CitationRequestController {

	private final CitationRequestService citationRequestService;
	private final CitationMapper citationMapper;

	/**
	 * 【POST /api/citation-requests】创建查收查引请求
	 *
	 * <p>用户提交查询范围（criteria）和论文清单（items），系统生成流水号并置状态为 PENDING。</p>
	 *
	 * @param dto    创建请求的入参（含 criteria 查询范围 + items 论文清单）
	 * @param userId 从 JWT Token 中提取的当前登录用户 ID
	 * @return 持久化后的 CitationRequest 实体（含生成的 ID 和流水号）
	 */
	@PostMapping
	public Result<CitationRequest> createRequest(@RequestBody CreateCitationRequestDTO dto,
			@RequestAttribute("userId") String userId) {
		return Result.success(citationRequestService.createCitationRequest(dto, userId));
	}

	/**
	 * 【PATCH /api/citation-requests/serial/{serialNumber}/status】推进请求状态
	 *
	 * <p>管理员操作：将指定流水号的请求状态推进到下一阶段（如 PENDING → PROCESSING → COMPLETED）。</p>
	 *
	 * @param serialNumber 请求流水号（32 位 UUID，不含连字符）
	 * @param status       目标状态（PENDING / PROCESSING / COMPLETED）
	 * @param userId       当前操作者 ID（需为管理员）
	 */
	@PatchMapping("/serial/{serialNumber}/status")
	public Result<Void> updateStatus(@PathVariable String serialNumber,
			@RequestParam RequestStatus status, @RequestAttribute("userId") String userId) {

		citationRequestService.updateRequestStatus(serialNumber, status, userId);
		return Result.success(null);
	}

	/**
	 * 【DELETE /api/citation-requests/serial/{serialNumber}】删除请求
	 *
	 * <p>级联删除请求及其关联的 criteria、items 和 results 数据。</p>
	 * <ul>
	 *   <li>管理员可删除任意请求</li>
	 *   <li>普通用户只能删除自己的请求</li>
	 * </ul>
	 *
	 * @param serialNumber 要删除的请求流水号
	 * @param userId       当前操作者 ID
	 */
	@DeleteMapping("/serial/{serialNumber}")
	public Result<Void> deleteRequest(@PathVariable String serialNumber, @RequestAttribute("userId") String userId) {

		citationRequestService.deleteRequest(serialNumber, userId);
		return Result.success(null);
	}

	/**
	 * 【GET /api/citation-requests/serial/{serialNumber}】获取请求详情
	 *
	 * <p>返回完整的请求信息，包括查询范围（criteria）和论文清单（items）及其检索结果。</p>
	 * <ul>
	 *   <li>管理员可查看任意请求</li>
	 *   <li>普通用户只能查看自己的请求</li>
	 * </ul>
	 *
	 * @param serialNumber 请求流水号
	 * @param userId       当前登录用户 ID
	 * @return 包含完整嵌套数据的 DTO（CitationCriteriaDTO + List&lt;CitationItemDTO&gt;）
	 */
	@GetMapping("/serial/{serialNumber}")
	public Result<CitationRequestDetailDTO> getDetail(@PathVariable String serialNumber,
			@RequestAttribute("userId") String userId) {
		// 1. 查实体（含权限校验）
		CitationRequest entity = citationRequestService.findBySerialNumber(serialNumber, userId);
		// 2. 转 DTO 返回（避免实体暴露给前端）
		return Result.success(citationMapper.toDetailDTO(entity));
	}

	/**
	 * 【POST /api/citation-requests/serial/{serialNumber}/results】提交检索结果
	 *
	 * <p>管理员为指定请求中的每篇论文填入各数据库的检索数值。</p>
	 * <p><b>执行流程：</b>先删除该请求下所有旧结果 → 重新批量插入新结果 → 更新请求 updateTime。</p>
	 *
	 * @param serialNumber 请求流水号（与 submitDTO 中的 serialNumber 必须一致）
	 * @param submitDTO    检索结果数据（含流水号 + 结果条目列表）
	 * @param userId       当前操作者 ID（需为管理员）
	 * @throws RuntimeException 如果路径中的流水号与请求体中的流水号不一致
	 */
	@PostMapping("/serial/{serialNumber}/results")
	public Result<Void> submitResults(@PathVariable String serialNumber, @RequestBody CitationResultSubmitDTO submitDTO,
			@RequestAttribute("userId") String userId) {
		// 校验路径流水号与请求体流水号一致
		if (!serialNumber.equals(submitDTO.getSerialNumber())) {
			throw new RuntimeException("提交的数据流水号不匹配");
		}

		citationRequestService.submitResults(submitDTO, userId);
		return Result.success(null);
	}

	/**
	 * 【GET /api/citation-requests】动态条件查询请求列表（摘要）
	 *
	 * <p>支持按状态、流水号、申请用户 ID 三个维度组合筛选。</p>
	 * <ul>
	 *   <li>管理员可传 requesterId 查看指定用户的请求</li>
	 *   <li>普通用户不传 requesterId，默认只看自己的请求</li>
	 * </ul>
	 * 返回列表摘要 DTO（不含嵌套的完整数据）。
	 *
	 * @param status       筛选状态（可选）
	 * @param serialNumber 筛选流水号（可选）
	 * @param requesterId  筛选申请人 ID（可选，仅管理员有效）
	 * @param userId       当前登录用户 ID
	 * @return 请求摘要 DTO 列表
	 */
	@GetMapping
	public Result<List<CitationRequestSummaryDTO>> getRequests(
			@RequestParam(required = false) RequestStatus status,
			@RequestParam(required = false) String serialNumber, @RequestParam(required = false) String requesterId,
			@RequestAttribute("userId") String userId) {
		List<CitationRequest> entities = citationRequestService.findRequestsByCondition(status, serialNumber,
				requesterId, userId);
		return Result.success(citationMapper.toSummaryDTOList(entities));
	}

}
