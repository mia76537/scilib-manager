package com.scimanager.core.controller.paper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.scimanager.core.common.Result;
import com.scimanager.core.model.Paper;
import com.scimanager.core.service.PaperService;

@RestController
@RequestMapping("/api/papers")
public class PaperController {

	@Autowired
	private PaperService paperService;

	// 上传文件
	@PostMapping("/upload")
	public Result<Paper> uploadPaper(@RequestParam("file") MultipartFile file,
			@RequestAttribute("userId") String userId) {
		Paper paper = paperService.uploadPaper(file, userId);
		// 使用 Result.success 包装结果
		return Result.success(paper);
	}

	// 删除文件
	@DeleteMapping("/{id}")
	public Result<String> deletePaper(@PathVariable Long id, @RequestAttribute("userId") String userId) {
		paperService.deletePaper(id, userId);
		// 返回操作成功的文字提示
		return Result.success("删除成功");
	}

	// 按关键词搜索
	@GetMapping("/search")
	public Result<List<Paper>> search(@RequestParam String keyword, @RequestAttribute("userId") String userId) {
		List<Paper> papers = paperService.searchByKeyword(keyword, userId);
		// 统一包装 List 结果
		return Result.success(papers);
	}

	// 修改文献元数据
	@PutMapping("/{id}/metadata")
	public Result<Paper> updateMetadata(@PathVariable Long id, @RequestBody Paper updateData,
			@RequestAttribute("userId") String userId) {
		updateData.setId(id);
		Paper updatedPaper = paperService.updatePaperMetadata(updateData, userId);
		return Result.success(updatedPaper);
	}

	// --- 管理员功能 ---

	/**
	 * 查看所有用户的文献
	 */
	@GetMapping("/admin/all")
	public Result<List<Paper>> getAllPapers(@RequestAttribute("role") String role) {
		if (!"ADMIN".equals(role)) {
			return Result.error(403, "权限不足，仅管理员可见");
		}
		return Result.success(paperService.listAllPapersForAdmin());
	}

	/**
	 * 查看指定用户的文献
	 */
	@GetMapping("/admin/user/{targetUserId}")
	public Result<List<Paper>> getPapersByUser(@PathVariable String targetUserId,
			@RequestAttribute("role") String role) {

		if (!"ADMIN".equals(role)) {
			return Result.error(403, "权限不足");
		}
		return Result.success(paperService.listPapersByUserForAdmin(targetUserId));
	}

}
