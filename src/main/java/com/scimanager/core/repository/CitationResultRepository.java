package com.scimanager.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.scimanager.core.entity.CitationResult;

/**
 * 检索结果数据访问层
 *
 * <p>提供 CitationResult 实体的 CRUD 操作。<br>
 * 批量删除操作用于"先删后插"的幂等结果提交策略。</p>
 */
@Repository
public interface CitationResultRepository extends JpaRepository<CitationResult, Long> {

	/**
	 * 批量删除指定论文条目下的所有检索结果
	 *
	 * <p>在管理员重新提交检索结果时，先删除旧结果再插入新结果，保证幂等性。</p>
	 *
	 * @param itemIds 论文条目 ID 列表
	 */
	void deleteByItemIdIn(List<Long> itemIds);

}