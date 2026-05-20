package com.scimanager.core.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.scimanager.core.entity.Paper;
import com.scimanager.core.entity.User;
import com.scimanager.core.repository.PaperRepository;
import com.scimanager.core.repository.UserRepository;
import com.scimanager.core.service.PaperService;
import com.scimanager.core.service.StorageService;
import com.scimanager.core.service.UserInterestsService;

import lombok.RequiredArgsConstructor;

/**
 * 文献管理服务实现
 *
 * <p>提供文献的完整生命周期管理：上传、下载、删除、搜索、元数据更新及权限校验。<br>
 * 上传后通过 {@link CitationInternalService} 异步解析 PDF 元数据，并触发兴趣画像刷新。</p>
 *
 * <p><b>权限模型：</b></p>
 * <ul>
 *   <li>STUDENT — 操作自己的文献</li>
 *   <li>MENTOR — 操作自己的文献 + 查看/搜索名下学生的文献</li>
 *   <li>ADMIN — 操作所有用户的文献</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

	final private PaperRepository paperRepository;
	final private StorageService storageService;
	final private UserRepository userRepository;
	final private CitationInternalService citationInternalService;
	final private UserInterestsService userInterestsService;

	@Value("${file.upload-path:./uploads/papers}")
	private String uploadPath;

	/**
	 * 上传文献
	 *
	 * <p><b>执行流程：</b></p>
	 * <ol>
	 *   <li>校验用户是否存在</li>
	 *   <li>保存文件到本地磁盘（UUID 重命名）</li>
	 *   <li>创建 Paper 实体并持久化（仅含文件名和路径）</li>
	 *   <li>异步启动 AI 解析（提取元数据、关键词、引文）</li>
	 *   <li>返回初始 Paper 实体（元数据后续异步填充）</li>
	 * </ol>
	 */
	@Override
	@Transactional
	public Paper uploadPaper(MultipartFile file, String userId) {
		User owner = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
		// 保存文件到磁盘
		String storedPath = storageService.upload(file);
		// 构建初始实体
		Paper paper = new Paper();
		paper.setPaperName(file.getOriginalFilename());
		paper.setLocalPath(storedPath);
		paper.setOwner(owner);
		Paper savedPaper = paperRepository.save(paper);
		// 异步解析 PDF 元数据（AI 调用可能在事务提交后才执行）
		String absolutePath = storageService.getAbsolutePath(savedPaper.getLocalPath());
		citationInternalService.processMetadataAsync(savedPaper.getId(), absolutePath, paperRepository, userId);
		return savedPaper;
	}

	/**
	 * 下载文献（权限校验）
	 *
	 * <p><b>下载权限：</b></p>
	 * <ul>
	 *   <li>ADMIN — 任意文献</li>
	 *   <li>MENTOR — 自己的文献 + 名下学生的文献</li>
	 *   <li>STUDENT — 仅自己的文献</li>
	 * </ul>
	 *
	 * @param paperId       文献 ID
	 * @param currentUserId 当前用户 ID
	 * @return 文件 Resource，用于构建 ResponseEntity 返回给前端
	 * @throws RuntimeException 如果无权限或文件不存在
	 */
	@Override
	public Resource downloadPaper(Long paperId, String currentUserId) {
		Paper paper = paperRepository.findById(paperId).orElseThrow(() -> new RuntimeException("文件不存在"));
		User owner = paper.getOwner();
		User currentUser = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("当前用户不存在"));

		// 权限校验
		boolean canDownload = false;
		if ("ADMIN".equals(currentUser.getRole())) {
			canDownload = true; // 管理员可下载任意文献
		} else if ("MENTOR".equals(currentUser.getRole())) {
			// 导师可下载自己的文献或名下学生的文献
			if (owner.getUserId().equals(currentUserId) || currentUserId.equals(owner.getMentorId())) {
				canDownload = true;
			}
		} else {
			// 学生仅可下载自己的文献
			if (owner.getUserId().equals(currentUserId)) {
				canDownload = true;
			}
		}

		if (!canDownload) {
			throw new RuntimeException("权限不足，无法下载此文件");
		}
		return storageService.loadAsResource(paper.getLocalPath());
	}

	/**
	 * 删除文献
	 *
	 * <p><b>执行流程：</b></p>
	 * <ol>
	 *   <li>校验操作者是否为文献所有者</li>
	 *   <li>删除物理文件</li>
	 *   <li>删除数据库记录（级联清除关联的关键词）</li>
	 *   <li>事务提交后异步刷新用户兴趣画像</li>
	 * </ol>
	 *
	 * @param paperId 文献 ID
	 * @param userId  当前用户 ID（须为文献所有者）
	 */
	@Override
	@Transactional
	public void deletePaper(Long paperId, String userId) {
		Paper paper = paperRepository.findByIdAndOwner_UserId(paperId, userId)
				.orElseThrow(() -> new RuntimeException("文件不存在或无权操作"));

		// 删除物理文件
		storageService.delete(paper.getLocalPath());
		// 删除数据库记录
		paperRepository.delete(paper);

		// 事务提交后刷新兴趣画像（确保数据库已同步）
		registerPostCommitInterestRefresh(userId);
	}

	/**
	 * 按关键词搜索当前用户的文献
	 *
	 * <p>对文件名和关键词列表进行 LIKE 模糊匹配，使用 DISTINCT 去重。</p>
	 *
	 * @param keyword 搜索关键词
	 * @param userId  当前用户 ID
	 * @return 匹配的文献列表
	 */
	@Override
	public List<Paper> searchByKeyword(String keyword, String userId) {
		return paperRepository.searchByKeyword(keyword, userId);
	}

	/**
	 * 获取当前用户的所有文献
	 */
	@Override
	public List<Paper> listMyPapers(String userId) {
		return paperRepository.findByOwner_UserId(userId);
	}

	/**
	 * 管理员：获取系统内所有文献
	 */
	@Override
	public List<Paper> listAllPapersForAdmin() {
		return paperRepository.findAll();
	}

	/**
	 * 管理员：获取指定用户的所有文献
	 */
	@Override
	public List<Paper> listPapersByUserForAdmin(String targetUserId) {
		return paperRepository.findByOwner_UserId(targetUserId);
	}

	/**
	 * 导师：获取名下所有学生的文献
	 */
	@Override
	public List<Paper> listMentorStudentsPapers(String mentorId) {
		return paperRepository.findByOwner_MentorId(mentorId);
	}

	/**
	 * 导师：按关键词搜索名下学生的文献
	 */
	@Override
	public List<Paper> searchMentorStudentsPapersByKeyword(String keyword, String mentorId) {
		return paperRepository.searchMentorStudentsPapersByKeyword(keyword, mentorId);
	}

	/**
	 * 导师：按学生姓名模糊搜索名下学生的文献
	 */
	@Override
	public List<Paper> searchMentorStudentsPapersByStudentName(String studentName, String mentorId) {
		return paperRepository.findByOwner_UserNameContainingAndOwner_MentorId(studentName, mentorId);
	}

	/**
	 * 更新文献元数据
	 *
	 * <p><b>执行流程：</b></p>
	 * <ol>
	 *   <li>校验操作者是否为文献所有者</li>
	 *   <li>备份旧关键词列表</li>
	 *   <li>更新所有元数据字段</li>
	 *   <li>保存到数据库</li>
	 *   <li>如果关键词发生变化，事务提交后异步刷新用户的兴趣画像</li>
	 * </ol>
	 *
	 * @param updateData 包含待更新字段的 Paper 对象（ID 需从路径参数获取）
	 * @param userId     当前用户 ID（须为文献所有者）
	 * @return 更新后的 Paper 实体
	 */
	@Override
	@Transactional
	public Paper updatePaperMetadata(Paper updateData, String userId) {
		Paper existingPaper = paperRepository.findByIdAndOwner_UserId(updateData.getId(), userId)
				.orElseThrow(() -> new RuntimeException("文件不存在或无权操作"));

		// 备份旧关键词用于比较
		List<String> oldKeywords = existingPaper.getKeyWords() != null
				? new ArrayList<>(existingPaper.getKeyWords())
				: new ArrayList<>();

		// 更新元数据字段
		existingPaper.setPaperName(updateData.getPaperName());
		existingPaper.setKeyWords(updateData.getKeyWords());
		existingPaper.setPaperTitle(updateData.getPaperTitle());
		existingPaper.setPaperAuthors(updateData.getPaperAuthors());
		existingPaper.setPaperSourcePublications(updateData.getPaperSourcePublications());
		existingPaper.setPaperPublicationYear(updateData.getPaperPublicationYear());
		existingPaper.setPaperDoi(updateData.getPaperDoi());
		existingPaper.setPaperAccessionNumber(updateData.getPaperAccessionNumber());
		if (updateData.getPaperCitation() != null) {
			existingPaper.setPaperCitation(updateData.getPaperCitation());
		}

		Paper savedPaper = paperRepository.save(existingPaper);

		// 如果关键词变化，刷新兴趣画像
		List<String> newKeywords = savedPaper.getKeyWords() != null ? savedPaper.getKeyWords() : new ArrayList<>();
		boolean keywordsChanged = !oldKeywords.equals(newKeywords);
		if (keywordsChanged) {
			registerPostCommitInterestRefresh(userId);
		}

		return savedPaper;
	}

	/**
	 * 注册事务提交后的兴趣画像刷新回调
	 *
	 * <p>如果在事务中，注册 {@link TransactionSynchronization#afterCommit()} 回调，
	 * 确保数据库已提交后再刷新画像；否则直接调用。</p>
	 *
	 * @param userId 需要刷新画像的用户 ID
	 */
	private void registerPostCommitInterestRefresh(String userId) {
		if (TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					userInterestsService.makeUserInterestProfile(userId);
				}
			});
		} else {
			userInterestsService.makeUserInterestProfile(userId);
		}
	}
}
