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

	// 按关键词搜索（根据response，只要关键词缺省就能进行全量搜索）
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

	/**
	 * 导师查看自己名下所有学生的文献列表
	 */
	@GetMapping("/mentor/students-papers")
	public Result<List<Paper>> getMentorStudentsPapers(@RequestAttribute("userId") String mentorId,
			@RequestAttribute("role") String role) {
		if (!"MENTOR".equals(role) && !"ADMIN".equals(role)) {
			return Result.error(403, "权限不足，仅导师可见");
		}
		System.out.println("开始查看学生文献了");
		return Result.success(paperService.listMentorStudentsPapers(mentorId));
	}

	/**
	 * 导师按关键词搜索名下学生的文献
	 */
	@GetMapping("/mentor/search/keyword")
	public Result<List<Paper>> searchStudentsPapersByKeyword(@RequestParam String keyword,
			@RequestAttribute("userId") String mentorId, @RequestAttribute("role") String role) {
		try {
			if (!"MENTOR".equals(role)) {
				return Result.error(403, "权限不足");
			}
			System.out.println("开始执行数据库查询: " + keyword);
			List<Paper> result = paperService.searchMentorStudentsPapersByKeyword(keyword, mentorId);
			return Result.success(result);
		} catch (Exception e) {
			e.printStackTrace(); // 这行非常重要，它会在 IDEA 控制台打印出真正的错误原因
			return Result.error(400, "后端执行出错: " + e.getMessage());
		}
	}

	/**
	 * 导师按学生姓名搜索名下学生的文献
	 */
	@GetMapping("/mentor/search/student")
	public Result<List<Paper>> searchStudentsPapersByStudentName(@RequestParam String studentName,
			@RequestAttribute("userId") String mentorId, @RequestAttribute("role") String role) {
		System.out.println("收到请求了！");
		if (!"MENTOR".equals(role)) {
			return Result.error(403, "权限不足");
		}
		return Result.success(paperService.searchMentorStudentsPapersByStudentName(studentName, mentorId));
	}

}
