package com.scimanager.core.service;

import java.util.List;

import com.scimanager.core.dto.citationrequest.CitationResultSubmitDTO;
import com.scimanager.core.dto.citationrequest.CreateCitationRequestDTO;
import com.scimanager.core.entity.CitationRequest;
import com.scimanager.core.enums.RequestStatus;

/**
 * 查收查引请求服务接口
 *
 * <p>定义查收查引请求的完整生命周期管理：创建、状态流转、结果录入、删除和查询。<br>
 * 实现类为 {@link com.scimanager.core.service.impl.CitationRequestServiceImpl}。</p>
 */
public interface CitationRequestService {

	/** 创建查收查引请求 */
	CitationRequest createCitationRequest(CreateCitationRequestDTO dto, String userId);

	/** 更新请求状态（管理员） */
	void updateRequestStatus(String serialNumber, RequestStatus status, String userId);

	/** 提交检索结果（管理员，先删后插） */
	void submitResults(CitationResultSubmitDTO submitDTO, String userId);

	/** 删除请求 */
	void deleteRequest(String serialNumber, String userId);

	/** 根据流水号获取请求详情（含权限校验） */
	CitationRequest findBySerialNumber(String serialNumber, String userId);

	/** 综合条件查询请求列表 */
	List<CitationRequest> findRequestsByCondition(RequestStatus status, String serialNumber,
			String requesterId, String userId);

}