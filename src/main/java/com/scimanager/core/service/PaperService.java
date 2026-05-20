package com.scimanager.core.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.scimanager.core.entity.Paper;

/**
 * 文献管理服务接口
 *
 * <p>负责文献的上传、下载、删除、搜索和元数据管理。<br>
 * 实现类为 {@link com.scimanager.core.service.impl.PaperServiceImpl}。</p>
 */
public interface PaperService {

	/**
	 * 上传并保存文献，异步启动 AI 元数据解析
	 *
	 * @param file   上传的 PDF 文件
	 * @param userId 当前操作用户 ID
	 * @return 保存后的 Paper 实体（初始状态，元数据后续异步填充）
	 */
	Paper uploadPaper(MultipartFile file, String userId);

	/**
	 * 删除指定文献（校验所有权，级联删除物理文件）
	 *
	 * @param paperId 文献主键
	 * @param userId  当前用户 ID（须为文献所有者）
	 */
	void deletePaper(Long paperId, String userId);

	/**
	 * 按关键词搜索当前用户的文献（文件名 + 关键词模糊匹配）
	 *
	 * @param keyword 搜索关键词
	 * @param userId  当前用户 ID
	 * @return 符合条件的文献列表（DISTINCT 去重）
	 */
	List<Paper> searchByKeyword(String keyword, String userId);

	/** 管理员：获取系统内所有文献 */
	List<Paper> listAllPapersForAdmin();

	/** 管理员：获取指定用户的所有文献 */
	List<Paper> listPapersByUserForAdmin(String targetUserId);

	/** 导师：获取名下所有学生的文献 */
	List<Paper> listMentorStudentsPapers(String mentorId);

	/** 获取当前用户的所有文献 */
	List<Paper> listMyPapers(String userId);

	/**
	 * 更新文献元数据（含关键词变更有刷新兴趣画像）
	 *
	 * @param paper  带有新数据的实体对象
	 * @param userId 当前用户 ID（须为文献所有者）
	 * @return 更新后的实体
	 */
	Paper updatePaperMetadata(Paper paper, String userId);

	/** 导师：按关键词搜索名下学生的文献 */
	List<Paper> searchMentorStudentsPapersByKeyword(String keyword, String mentorId);

	/** 导师：按学生姓名模糊搜索名下学生的文献 */
	List<Paper> searchMentorStudentsPapersByStudentName(String studentName, String mentorId);

	/** 下载文献（含权限校验） */
	Resource downloadPaper(Long paperId, String currentUserId);
}
