package com.scimanager.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.scimanager.core.entity.Paper;

/**
 * 文献数据访问层
 *
 * <p>提供 Paper 实体的 CRUD 操作及按角色范围查询。</p>
 *
 * <p><b>查询设计说明：</b></p>
 * <ul>
 *   <li>通过 JPA 方法命名规则自动生成简单查询</li>
 *   <li>关键词搜索使用 LEFT JOIN + DISTINCT + LIKE 模糊匹配</li>
 *   <li>导师查询通过 owner.mentorId 关联实现</li>
 * </ul>
 */
@Repository
public interface PaperRepository extends JpaRepository<Paper, Long> {

	/**
	 * 安全查询：根据文献 ID 和所有者 ID 查询（确保只能操作自己的数据）
	 */
	Optional<Paper> findByIdAndOwner_UserId(Long id, String userId);

	/**
	 * 获取某个用户的所有文献
	 */
	List<Paper> findByOwner_UserId(String userId);

	/**
	 * 获取系统内所有文献（管理员功能）
	 */
	List<Paper> findAll();

	/**
	 * 按关键词搜索当前用户的文献
	 *
	 * <p>LEFT JOIN paper_keywords 表进行关键词匹配，<br>
	 * 同时对文件名进行 LIKE 模糊匹配，使用 DISTINCT 去重。</p>
	 */
	@Query("SELECT DISTINCT p FROM Paper p LEFT JOIN p.keyWords k "
			+ "WHERE p.owner.userId = :userId "
			+ "AND (p.paperName LIKE %:keyword% OR k LIKE %:keyword%)")
	List<Paper> searchByKeyword(@Param("keyword") String keyword, @Param("userId") String userId);

	/**
	 * 检查文件路径是否已存在（上传时避免重复）
	 */
	boolean existsByLocalPath(String localPath);

	/**
	 * 获取某导师名下所有学生的文献
	 */
	List<Paper> findByOwner_MentorId(String mentorId);

	/**
	 * 按学生姓名模糊匹配，查找导师名下学生的文献
	 */
	List<Paper> findByOwner_UserNameContainingAndOwner_MentorId(String studentName, String mentorId);

	/**
	 * 导师按关键词搜索名下学生的文献
	 *
	 * <p>LEFT JOIN + DISTINCT 避免因一篇文献匹配多个关键词而产生重复行。</p>
	 */
	@Query("SELECT DISTINCT p FROM Paper p LEFT JOIN p.keyWords k "
			+ "WHERE (p.paperName LIKE %:keyword% OR k LIKE %:keyword%) "
			+ "AND p.owner.mentorId = :mentorId")
	List<Paper> searchMentorStudentsPapersByKeyword(@Param("keyword") String keyword,
			@Param("mentorId") String mentorId);
}