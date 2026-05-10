package com.scimanager.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.scimanager.core.model.Paper;

@Repository
public interface PaperRepository extends JpaRepository<Paper, Long> {

	// 安全查询：根据 ID 和 用户 ID 获取文献，确保只能操作自己的数据
	Optional<Paper> findByIdAndOwner_UserId(Long id, String userId);

	// 获取某个用户的所有文献
	List<Paper> findByOwner_UserId(String userId);

	// 获取所有文献
	List<Paper> findAll();

	// 在文件名按关键词搜索。 使用 JOIN 处理集合字段，使用 LIKE %:keyword% 处理文件名模糊匹配
	@Query("SELECT DISTINCT p FROM Paper p LEFT JOIN p.keyWords k " + "WHERE p.owner.userId = :userId "
			+ "AND (p.paperName LIKE %:keyword% OR k LIKE %:keyword%)")
	List<Paper> searchByKeyword(@Param("keyword") String keyword, @Param("userId") String userId);

	// 检查文件路径是否冲突
	boolean existsByLocalPath(String localPath);
}