package com.scimanager.core.controller.paper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

import com.scimanager.core.common.Result;
import com.scimanager.core.entity.Paper;
import com.scimanager.core.service.PaperService;

/**
 * 文献管理控制器
 *
 * <p>提供文献的上传、下载、删除、搜索、元数据修改等功能。<br>
 * 根据角色（ADMIN / MENTOR / STUDENT）提供不同范围和粒度的访问控制。</p>
 *
 * <p><b>角色权限矩阵：</b></p>
 * <ul>
 *   <li>STUDENT — 操作自己的文献</li>
 *   <li>MENTOR  — 操作自己的文献 + 查看名下学生的文献</li>
 *   <li>ADMIN   — 操作所有用户的文献</li>
 * </ul>
 *
 * <p><b>上传后执行流程：</b> 保存文件 → 写入数据库 → 异步 AI 解析 PDF（提取元数据、关键词、引文）→ 更新兴趣画像</p>
 */
@RestController
@RequestMapping("/api/papers")
public class PaperController {

	@Autowired
	private PaperService paperService;

	/**
	 * 【POST /api/papers/upload】上传文献文件
	 *
	 * <p>接收 PDF 文件，保存到本地存储，并异步启动 AI 元数据解析。</p>
	 *
	 * @param file   上传的 PDF 文件（Multipart）
	 * @param userId 上传者 ID（从 Token 提取）
	 * @return 保存后的 Paper 实体（初始仅含文件名和路径，元数据后续通过异步填充）
	 */
	@PostMapping("/upload")
	public Result<Paper> uploadPaper(@RequestParam("file") MultipartFile file,
			@RequestAttribute("userId") String userId) {
		Paper paper = paperService.uploadPaper(file, userId);
		return Result.success(paper);
	}

	/**
	 * 【GET /api/papers/{paperId}/download】下载文献文件
	 *
	 * <p>下载前校验权限：仅文献所有者、其导师或管理员可下载。</p>
	 *
	 * @param paperId 文献 ID
	 * @param userId  当前用户 ID
	 * @return 文件资源（二进制流）
	 */
	@GetMapping("/{paperId}/download")
	public ResponseEntity<Resource> downloadFile(@PathVariable Long paperId, @RequestParam String userId) {
		Resource resource = paperService.downloadPaper(paperId, userId);
		// 获取原始文件名，防止下载后文件名是 UUID
		String fileName = resource.getFilename();
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"").body(resource);
	}

	/**
	 * 【DELETE /api/papers/{id}】删除文献
	 *
	 * <p>仅文献所有者可删除。级联操作：删除数据库记录 + 删除物理文件 + 刷新兴趣画像。</p>
	 *
	 * @param id     文献 ID
	 * @param userId 当前用户 ID
	 * @return 操作成功提示
	 */
	@DeleteMapping("/{id}")
	public Result<String> deletePaper(@PathVariable Long id, @RequestAttribute("userId") String userId) {
		paperService.deletePaper(id, userId);
		return Result.success("删除成功");
	}

	/**
	 * 【GET /api/papers/search】按关键词搜索当前用户的文献
	 *
	 * <p>对文件名和关键词列表进行 LIKE 模糊匹配。</p>
	 *
	 * @param keyword 搜索关键词
	 * @param userId  当前用户 ID
	 * @return 匹配的文献列表（使用 DISTINCT 去重）
	 */
	@GetMapping("/search")
	public Result<List<Paper>> search(@RequestParam String keyword, @RequestAttribute("userId") String userId) {
		List<Paper> papers = paperService.searchByKeyword(keyword, userId);
		return Result.success(papers);
	}

	/**
	 * 【PUT /api/papers/{id}/metadata】修改文献元数据
	 *
	 * <p>更新文献的标题、作者、来源出版物、DOI、关键词等信息。<br>
	 * 若关键词发生变化，异步刷新用户的科研兴趣画像。</p>
	 *
	 * @param id         文献 ID
	 * @param updateData 包含待更新字段的 Paper 对象
	 * @param userId     当前用户 ID（须为文献所有者）
	 * @return 更新后的 Paper 实体
	 */
	@PutMapping("/{id}/metadata")
	public Result<Paper> updateMetadata(@PathVariable Long id, @RequestBody Paper updateData,
			@RequestAttribute("userId") String userId) {

		// 强制设置 ID，防止前端传错
		updateData.setId(id);
		Paper updatedPaper = paperService.updatePaperMetadata(updateData, userId);
		return Result.success(updatedPaper);
	}

	// ==================== 管理员功能 ====================

	/**
	 * 【GET /api/papers/admin/all】查看所有用户的文献（管理员）
	 *
	 * @param role 当前用户角色（需为 ADMIN）
	 * @return 系统内全部文献列表
	 */
	@GetMapping("/admin/all")
	public Result<List<Paper>> getAllPapers(@RequestAttribute("role") String role) {
		if (!"ADMIN".equals(role)) {
			return Result.error(403, "权限不足，仅管理员可见");
		}
		return Result.success(paperService.listAllPapersForAdmin());
	}

	/**
	 * 【GET /api/papers/admin/user/{targetUserId}】查看指定用户的文献（管理员）
	 *
	 * @param targetUserId 目标用户 ID
	 * @param role         当前用户角色（需为 ADMIN）
	 * @return 指定用户的文献列表
	 */
	@GetMapping("/admin/user/{targetUserId}")
	public Result<List<Paper>> getPapersByUser(@PathVariable String targetUserId,
			@RequestAttribute("role") String role) {

		if (!"ADMIN".equals(role)) {
			return Result.error(403, "权限不足");
		}
		return Result.success(paperService.listPapersByUserForAdmin(targetUserId));
	}

	// ==================== 导师功能 ====================

	/**
	 * 【GET /api/papers/mentor/students-papers】查看名下所有学生的文献列表（导师）
	 *
	 * @param mentorId 导师用户 ID（从 Token 提取）
	 * @param role     当前用户角色（需为 MENTOR 或 ADMIN）
	 * @return 名下所有学生的文献列表
	 */
	@GetMapping("/mentor/students-papers")
	public Result<List<Paper>> getMentorStudentsPapers(@RequestAttribute("userId") String mentorId,
			@RequestAttribute("role") String role) {
		if (!"MENTOR".equals(role) && !"ADMIN".equals(role)) {
			return Result.error(403, "权限不足，仅导师可见");
		}
		return Result.success(paperService.listMentorStudentsPapers(mentorId));
	}

	/**
	 * 【GET /api/papers/mentor/search/keyword】按关键词搜索名下学生的文献（导师）
	 *
	 * @param keyword  搜索关键词
	 * @param mentorId 导师用户 ID
	 * @param role     当前用户角色（需为 MENTOR）
	 * @return 匹配的文献列表
	 */
	@GetMapping("/mentor/search/keyword")
	public Result<List<Paper>> searchStudentsPapersByKeyword(@RequestParam String keyword,
			@RequestAttribute("userId") String mentorId, @RequestAttribute("role") String role) {
		try {
			if (!"MENTOR".equals(role)) {
				return Result.error(403, "权限不足");
			}
			List<Paper> result = paperService.searchMentorStudentsPapersByKeyword(keyword, mentorId);
			return Result.success(result);
		} catch (Exception e) {
			e.printStackTrace(); // 在控制台打印详细错误原因
			return Result.error(400, "后端执行出错: " + e.getMessage());
		}
	}

	/**
	 * 【GET /api/papers/mentor/search/student】按学生姓名搜索名下学生的文献（导师）
	 *
	 * @param studentName 学生姓名（模糊匹配）
	 * @param mentorId    导师用户 ID
	 * @param role        当前用户角色（需为 MENTOR）
	 * @return 匹配的文献列表
	 */
	@GetMapping("/mentor/search/student")
	public Result<List<Paper>> searchStudentsPapersByStudentName(@RequestParam String studentName,
			@RequestAttribute("userId") String mentorId, @RequestAttribute("role") String role) {
		if (!"MENTOR".equals(role)) {
			return Result.error(403, "权限不足");
		}
		return Result.success(paperService.searchMentorStudentsPapersByStudentName(studentName, mentorId));
	}

}
