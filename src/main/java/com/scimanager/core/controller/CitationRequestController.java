package com.scimanager.core.controller;

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
import com.scimanager.core.model.CitationRequest;
import com.scimanager.core.model.dto.citationrequesdto.CitationRequestDetailDTO;
import com.scimanager.core.model.dto.citationrequesdto.CitationRequestSummaryDTO;
import com.scimanager.core.model.dto.citationrequesdto.CitationResultSubmitDTO;
import com.scimanager.core.model.dto.citationrequesdto.CreateCitationRequestDTO;
import com.scimanager.core.service.CitationRequestService;
import com.scimanager.core.service.mapper.CitationMapper;

import lombok.RequiredArgsConstructor;

/**
 * 查收查引请求控制层 使用 Result<T> 统一返回格式，依赖 GlobalExceptionHandler 处理异常
 */
@RestController
@RequestMapping("/api/citation-requests")
@RequiredArgsConstructor
public class CitationRequestController {

	private final CitationRequestService citationRequestService;
	private final CitationMapper citationMapper;

	/**
	 * 创建请求：仅需当前登录用户 ID
	 */
	@PostMapping
	public Result<CitationRequest> createRequest(@RequestBody CreateCitationRequestDTO dto,
			@RequestAttribute("userId") String userId) {
		return Result.success(citationRequestService.createCitationRequest(dto, userId));
	}

	/**
	 * 推进请求：需要管理员身份才能推进请求
	 */
	@PatchMapping("/serial/{serialNumber}/status")
	public Result<Void> updateStatus(@PathVariable String serialNumber,
			@RequestParam CitationRequest.RequestStatus status, @RequestAttribute("userId") String userId) {

		citationRequestService.updateRequestStatus(serialNumber, status, userId);
		return Result.success(null);
	}

	/**
	 * 删除请求：需要管理员身份，删除过时的，不合理的以及其他应当删除的请求
	 */
	@DeleteMapping("/serial/{serialNumber}")
	public Result<Void> deleteRequest(@PathVariable String serialNumber, @RequestAttribute("userId") String userId) {

		citationRequestService.deleteRequest(serialNumber, userId);
		return Result.success(null);
	}

	/**
	 * 获取详情，展示全量内容
	 */
	@GetMapping("/serial/{serialNumber}")
	public Result<CitationRequestDetailDTO> getDetail(@PathVariable String serialNumber,
			@RequestAttribute("userId") String userId) {
		CitationRequest entity = citationRequestService.findBySerialNumber(serialNumber, userId);
		return Result.success(citationMapper.toDetailDTO(entity));
	}

	/**
	 * 提交检索结果：管理员根据流水号为申请单中的论文填入具体数值
	 */
	@PostMapping("/serial/{serialNumber}/results")
	public Result<Void> submitResults(@PathVariable String serialNumber, @RequestBody CitationResultSubmitDTO submitDTO,
			@RequestAttribute("userId") String userId) {
		if (!serialNumber.equals(submitDTO.getSerialNumber())) {
			throw new RuntimeException("提交的数据流水号不匹配");
		}

		// 传递 userId 以便 Service 进行权限检查
		citationRequestService.submitResults(submitDTO, userId);
		return Result.success(null);
	}

	/**
	 * 动态条件搜索 展示简介内容，返回 SummaryDTO
	 */
	// 获取列表：服务层通过 userId 判断是返回该用户个人列表还是管理员全量列表
	@GetMapping
	public Result<List<CitationRequestSummaryDTO>> getRequests(
			@RequestParam(required = false) CitationRequest.RequestStatus status,
			@RequestParam(required = false) String serialNumber, @RequestParam(required = false) String requesterId,
			@RequestAttribute("userId") String userId) {
		List<CitationRequest> entities = citationRequestService.findRequestsByCondition(status, serialNumber,
				requesterId, userId);
		return Result.success(citationMapper.toSummaryDTOList(entities));
	}

}