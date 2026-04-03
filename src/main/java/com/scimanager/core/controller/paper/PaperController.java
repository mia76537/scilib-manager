package com.scimanager.core.controller.paper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.scimanager.core.model.Paper;
import com.scimanager.core.service.PaperService;

@RestController
@RequestMapping("/api/papers")
public class PaperController {

	@Autowired
	private PaperService paperService;

	// 1. 上传文件
	@PostMapping("/upload")
	public ResponseEntity<?> uploadPaper(@RequestParam("file") MultipartFile file,
			@RequestAttribute("userId") String userId) { // 从 Token 拦截器获取当前用户ID
		Paper paper = paperService.uploadPaper(file, userId);
		return ResponseEntity.ok(paper);
	}

	// 2. 删除文件
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deletePaper(@PathVariable Long id, @RequestAttribute("userId") String userId) {
		paperService.deletePaper(id, userId); // 传入 userId 确保只能删自己的
		return ResponseEntity.ok("删除成功");
	}

	// 3. 按关键词搜索
	@GetMapping("/search")
	public List<Paper> search(@RequestParam String keyword, @RequestAttribute("userId") String userId) {
		return paperService.searchByKeyword(keyword, userId);
	}

	// 4. 修改文献元数据（标题、摘要、关键词等）

	@PutMapping("/{id}/metadata")
	public ResponseEntity<?> updateMetadata(@PathVariable Long id, @RequestBody Paper updateData, // 接收前端传来的新数据
			@RequestAttribute("userId") String userId) {

		// 确保 ID 一致性
		updateData.setId(id);
		Paper updatedPaper = paperService.updatePaperMetadata(updateData, userId);
		return ResponseEntity.ok(updatedPaper);
	}

}
