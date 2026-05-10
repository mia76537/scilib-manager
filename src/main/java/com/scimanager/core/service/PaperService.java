package com.scimanager.core.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.scimanager.core.model.Paper;

/**
 * 文献管理服务接口 负责处理文件的元数据管理、物理存储逻辑以及权限校验
 */
public interface PaperService {

	/**
	 * 上传并保存文献
	 * 
	 * @param file   选中的多媒体文件
	 * @param userId 当前操作用户的ID，用于建立所有权关联
	 * @return 保存后的 Paper 实体
	 */
	Paper uploadPaper(MultipartFile file, String userId);

	/**
	 * 删除指定文献 逻辑应包含：校验该 paperId 是否属于该 userId，若是则物理删除文件并清理数据库记录
	 * 
	 * @param paperId 文献主键
	 * @param userId  当前用户ID
	 */
	void deletePaper(Long paperId, String userId);

	/**
	 * 根据关键词搜索属于当前用户的文献 对应 Paper 实体中的 keyWords 列表
	 * 
	 * @param keyword 搜索关键词
	 * @param userId  当前用户ID
	 * @return 符合条件的文献列表
	 */
	List<Paper> searchByKeyword(String keyword, String userId);

	/**
	 * 管理员：获取系统内所有文献
	 */
	List<Paper> listAllPapersForAdmin();

	/**
	 * 管理员：获取指定用户的所有文献
	 */
	List<Paper> listPapersByUserForAdmin(String targetUserId);

	/**
	 * 导师：根据导师 ID 查询该导师名下所有学生上传的文献
	 */
	List<Paper> listMentorStudentsPapers(String mentorId);

	/**
	 * 获取当前用户的所有文献列表
	 * 
	 * @param userId 当前用户ID
	 * @return 文献列表
	 */
	List<Paper> listMyPapers(String userId);

	/**
	 * 更新文献的元数据（如标题、摘要、GB/T 7714 相关字段）
	 * 
	 * @param paper  带有新数据的实体对象
	 * @param userId 当前用户ID，用于安全校验
	 * @return 更新后的实体
	 */
	Paper updatePaperMetadata(Paper paper, String userId);

	List<Paper> searchMentorStudentsPapersByKeyword(String keyword, String mentorId);

	List<Paper> searchMentorStudentsPapersByStudentName(String studentName, String mentorId);
}
