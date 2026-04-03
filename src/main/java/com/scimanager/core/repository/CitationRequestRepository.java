package com.scimanager.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.scimanager.core.model.CitationRequest;
import com.scimanager.core.model.CitationRequest.RequestStatus;

@Repository
public interface CitationRequestRepository extends JpaRepository<CitationRequest, Long> {
	/**
	 * 通过流水号查询申请详
	 */
	Optional<CitationRequest> findBySerialNumber(String serialNumber);

	/**
	 * 根据状态查询申请列表
	 */
	List<CitationRequest> findByStatus(CitationRequest.RequestStatus status);

	/**
	 * 检查流水号是否存在，用于生成新流水号时的校验
	 */
	boolean existsBySerialNumber(String serialNumber);

	/**
	 * 根据用户ID查询
	 */
	List<CitationRequest> findByRequester_UserId(String userId);

	@Query("SELECT r FROM CitationRequest r WHERE " + "(:status IS NULL OR r.status = :status) AND "
			+ "(:serialNumber IS NULL OR :serialNumber = '' OR r.serialNumber = :serialNumber) AND "
			+ "(:userId IS NULL OR :userId = '' OR r.requester.userId = :userId)")
	List<CitationRequest> findWithConditions(RequestStatus status, String serialNumber, String userId);
}