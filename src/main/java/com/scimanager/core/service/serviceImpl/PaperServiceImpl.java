package com.scimanager.core.service.serviceImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.scimanager.core.model.Paper;
import com.scimanager.core.model.User;
import com.scimanager.core.repository.PaperRepository;
import com.scimanager.core.repository.UserRepository;
import com.scimanager.core.service.PaperService;
import com.scimanager.core.service.StorageService;
import com.scimanager.core.service.UserInterestsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

	final private PaperRepository paperRepository;
	final private StorageService storageService;
	final private UserRepository userRepository;
	final private CitationInternalService citationInternalService;
	final private UserInterestsService userInterestsService;

	// 拼接绝对路径
	@Value("${file.upload-path:./uploads/papers}")
	private String uploadPath;

	@Override
	@Transactional
	public Paper uploadPaper(MultipartFile file, String userId) {
		// 获取用户信息
		User owner = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
		// 物理保存文件
		String storedPath = storageService.upload(file);
		// 创建数据库记录
		Paper paper = new Paper();
		paper.setPaperName(file.getOriginalFilename());
		paper.setLocalPath(storedPath);
		paper.setOwner(owner);
		Paper savedPaper = paperRepository.save(paper);
		// 调用外部 Service 的异步方法 ，存储文献
		String absolutePath = storageService.getAbsolutePath(savedPaper.getLocalPath());
		// 调用外部 Service 的异步方法 ，填充文献数据
		citationInternalService.processMetadataAsync(savedPaper.getId(), absolutePath, paperRepository, userId);

		return savedPaper;
	}

	@Override
	public Resource downloadPaper(Long paperId, String currentUserId) {
		// 1. 获取论文信息和所有者信息
		Paper paper = paperRepository.findById(paperId).orElseThrow(() -> new RuntimeException("文件不存在"));
		User owner = paper.getOwner();
		// 2. 获取当前操作者信息以判断角色
		User currentUser = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("当前用户不存在"));

		boolean canDownload = false;
		// 3. 权限层级判定
		if ("ADMIN".equals(currentUser.getRole())) {
			canDownload = true;
		} else if ("MENTOR".equals(currentUser.getRole())) {
			// 如果是导师，判断是否是自己的文件，或者是自己名下学生的文件
			if (owner.getUserId().equals(currentUserId) || currentUserId.equals(owner.getMentorId())) {
				canDownload = true;
			}
		} else {
			// 普通学生，只能下载自己的
			if (owner.getUserId().equals(currentUserId)) {
				canDownload = true;
			}
		}

		if (!canDownload) {
			throw new RuntimeException("权限不足，无法下载此文件");
		}

		// 4. 调用存储服务获取资源
		return storageService.loadAsResource(paper.getLocalPath());
	}

	@Override
	@Transactional
	public void deletePaper(Long paperId, String userId) {
		Paper paper = paperRepository.findByIdAndOwner_UserId(paperId, userId)
				.orElseThrow(() -> new RuntimeException("文件不存在或无权操作"));

		storageService.delete(paper.getLocalPath());
		paperRepository.delete(paper);

		// 关键：注册一个同步钩子，只有当事务成功 commit 后才执行
		if (TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					// 此时数据库已经删除了，再去叫 AI 跑任务
					userInterestsService.makeUserInterestProfile(userId);
				}
			});
		} else {
			// 如果当前没事务，直接调用
			userInterestsService.makeUserInterestProfile(userId);
		}
	}

	@Override
	public List<Paper> searchByKeyword(String keyword, String userId) {
		return paperRepository.searchByKeyword(keyword, userId);
	}

	@Override
	public List<Paper> listMyPapers(String userId) {
		return paperRepository.findByOwner_UserId(userId);
	}

	@Override
	public List<Paper> listAllPapersForAdmin() {
		return paperRepository.findAll();
	}

	@Override
	public List<Paper> listPapersByUserForAdmin(String targetUserId) {
		// 这里直接查询目标用户 ID，不校验当前操作者 ID
		return paperRepository.findByOwner_UserId(targetUserId);
	}

	@Override
	public List<Paper> listMentorStudentsPapers(String mentorId) {
		// 逻辑：在 paperRepository 中查询所有 owner 的 mentorId 等于当前导师 ID 的文献
		// 这通常需要你在 PaperRepository 中定义对应的方法
		return paperRepository.findByOwner_MentorId(mentorId);
	}

	@Override
	public List<Paper> searchMentorStudentsPapersByKeyword(String keyword, String mentorId) {
		// 按关键词搜索名下学生的文献
		return paperRepository.searchMentorStudentsPapersByKeyword(keyword, mentorId);
	}

	@Override
	public List<Paper> searchMentorStudentsPapersByStudentName(String studentName, String mentorId) {
		// 按学生姓名搜索名下学生的文献
		return paperRepository.findByOwner_UserNameContainingAndOwner_MentorId(studentName, mentorId);
	}

	@Override
	@Transactional // 手动更新论文元数据
	public Paper updatePaperMetadata(Paper updateData, String userId) {
		// 获取现有数据
		Paper existingPaper = paperRepository.findByIdAndOwner_UserId(updateData.getId(), userId)
				.orElseThrow(() -> new RuntimeException("文件不存在或无权操作"));
		// 记录旧的关键词（注意：List 是引用类型，建议创建一个副本进行对比）
		List<String> oldKeywords = existingPaper.getKeyWords() != null ? new ArrayList<>(existingPaper.getKeyWords())
				: new ArrayList<>();
		// 更新所有字段
		existingPaper.setPaperName(updateData.getPaperName());
		existingPaper.setKeyWords(updateData.getKeyWords()); // 这里的更新是触发 AI 的关键点
		existingPaper.setPaperTitle(updateData.getPaperTitle());
		existingPaper.setPaperAuthors(updateData.getPaperAuthors());
		existingPaper.setPaperSourcePublications(updateData.getPaperSourcePublications());
		existingPaper.setPaperPublicationYear(updateData.getPaperPublicationYear());
		existingPaper.setPaperDoi(updateData.getPaperDoi());
		existingPaper.setPaperAccessionNumber(updateData.getPaperAccessionNumber());
		if (updateData.getPaperCitation() != null) {
			existingPaper.setPaperCitation(updateData.getPaperCitation());
		}
		// 保存到数据库
		Paper savedPaper = paperRepository.save(existingPaper);
		// 判断关键词是否真的发生了变化
		List<String> newKeywords = savedPaper.getKeyWords() != null ? savedPaper.getKeyWords() : new ArrayList<>();
		boolean keywordsChanged = !oldKeywords.equals(newKeywords);
		// 只有当关键词改变，且事务提交成功后，才联络 AI
		if (keywordsChanged) {
			if (TransactionSynchronizationManager.isActualTransactionActive()) {
				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
					@Override
					public void afterCommit() {
						// 此时数据库事务已提交，关键词已更新，通知 AI
						userInterestsService.makeUserInterestProfile(userId);
					}
				});
			} else {
				userInterestsService.makeUserInterestProfile(userId);
			}
		}
		return savedPaper;
	}
}
