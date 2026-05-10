package com.scimanager.core.service;

import java.util.List;

import com.scimanager.core.model.CitationRequest;
import com.scimanager.core.model.dto.citationrequesdto.CreateCitationRequestDTO;

public interface CitationRequestService {
	// 创建查收查引请求
	CitationRequest createCitationRequest(CreateCitationRequestDTO dto, String userId);

	// 更新请求状态
	void updateRequestStatus(String serialNumber, CitationRequest.RequestStatus status, String userId);

	// 删除请求
	void deleteRequest(String serialNumber, String userId);

	// 获取特定请求详情
	CitationRequest findBySerialNumber(String serialNumber, String userId);

	// 综合条件查询
	List<CitationRequest> findRequestsByCondition(CitationRequest.RequestStatus status, String serialNumber,
			String requesterId, String userId);
}