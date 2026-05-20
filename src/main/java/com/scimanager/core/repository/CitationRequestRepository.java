package com.scimanager.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.scimanager.core.entity.CitationRequest;
import com.scimanager.core.enums.RequestStatus;

/**
 * 查收查引请求数据访问层
 *
 * <p>提供 CitationRequest 实体的 CRUD 操作及动态条件查询。</p>
 */
@Repository
public interface CitationRequestRepository extends JpaRepository<CitationRequest, Long> {

	/**
	 * 根据流水号查询请求详情
	 */
	Optional<CitationRequest> findBySerialNumber(String serialNumber);

	/**
	 * 根据状态查询请求列表
	 */
	List<CitationRequest> findByStatus(RequestStatus status);

	/**
	 * 检查流水号是否已存在（用于生成新流水号时的唯一性校验）
	 */
	boolean existsBySerialNumber(String serialNumber);

	/**
	 * 根据申请人 ID 查询
	 */
	List<CitationRequest> findByRequester_UserId(String userId);

	/**
	 * 动态条件查询（支持空值安全过滤）
	 *
	 * <p>三个条件均为可选：如果参数为 null 或空字符串，则忽略该条件。</p>
	 *
	 * @param status       状态过滤（可选）
	 * @param serialNumber 流水号精确匹配（可选）
	 * @param userId       申请人 ID 精确匹配（可选）
	 * @return 符合条件的请求列表
	 */
	@Query("SELECT r FROM CitationRequest r WHERE "
			+ "(:status IS NULL OR r.status = :status) AND "
			+ "(:serialNumber IS NULL OR :serialNumber = '' OR r.serialNumber = :serialNumber) AND "
			+ "(:userId IS NULL OR :userId = '' OR r.requester.userId = :userId)")
	List<CitationRequest> findWithConditions(RequestStatus status, String serialNumber, String userId);
}