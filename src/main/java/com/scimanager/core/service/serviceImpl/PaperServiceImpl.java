package com.scimanager.core.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

		// 调用外部 Service 的异步方法，Spring 代理才会生效
		String absolutePath = storageService.getAbsolutePath(savedPaper.getLocalPath());
		System.out.println("添加了，准备make");
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
		System.out.println("删除了，准备make");
		userInterestsService.makeUserInterestProfile(userId);
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
	@Transactional
	public Paper updatePaperMetadata(Paper updateData, String userId) {
		Paper existingPaper = paperRepository.findByIdAndOwner_UserId(updateData.getId(), userId)
				.orElseThrow(() -> new RuntimeException("文件不存在或无权操作"));

		existingPaper.setPaperName(updateData.getPaperName());
		existingPaper.setPaperAbstract(updateData.getPaperAbstract());
		existingPaper.setKeyWords(updateData.getKeyWords());

		// 如果前端也手动修改了引文，也可以在这里更新
		if (updateData.getPaperCitation() != null) {
			existingPaper.setPaperCitation(updateData.getPaperCitation());
		}

		return paperRepository.save(existingPaper);
	}
}
