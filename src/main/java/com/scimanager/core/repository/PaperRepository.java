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

	List<Paper> findByOwner_MentorId(String mentorId);

	List<Paper> findByOwner_UserNameContainingAndOwner_MentorId(String studentName, String mentorId);

	// 搜索：按文献关键词查询，且限定在导师名下
	// 使用 LEFT JOIN p.keyWords k 将集合展开，并使用 DISTINCT 避免因匹配到多个关键词而产生重复结果
	@Query("SELECT DISTINCT p FROM Paper p LEFT JOIN p.keyWords k "
			+ "WHERE (p.paperName LIKE %:keyword% OR k LIKE %:keyword%) " + "AND p.owner.mentorId = :mentorId")
	List<Paper> searchMentorStudentsPapersByKeyword(@Param("keyword") String keyword,
			@Param("mentorId") String mentorId);
}