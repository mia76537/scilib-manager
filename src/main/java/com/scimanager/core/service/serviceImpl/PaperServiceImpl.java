package com.scimanager.core.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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
		// 1. 获取用户信息
		User owner = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
		// 2. 物理保存文件
		String storedPath = storageService.upload(file);
		// 3. 创建数据库记录
		Paper paper = new Paper();
		paper.setPaperName(file.getOriginalFilename());
		paper.setLocalPath(storedPath);
		paper.setOwner(owner);
		Paper savedPaper = paperRepository.save(paper);
		// 调用外部 Service 的异步方法
		String absolutePath = storageService.getAbsolutePath(savedPaper.getLocalPath());
		citationInternalService.processMetadataAsync(savedPaper.getId(), absolutePath, paperRepository, userId);

		return savedPaper;
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
	@Transactional
	public Paper updatePaperMetadata(Paper updateData, String userId) {
		Paper existingPaper = paperRepository.findByIdAndOwner_UserId(updateData.getId(), userId)
				.orElseThrow(() -> new RuntimeException("文件不存在或无权操作"));

		existingPaper.setPaperName(updateData.getPaperName());
		existingPaper.setKeyWords(updateData.getKeyWords());

		// 如果前端也手动修改了引文，也可以在这里更新
		if (updateData.getPaperCitation() != null) {
			existingPaper.setPaperCitation(updateData.getPaperCitation());
		}

		return paperRepository.save(existingPaper);
	}
}
