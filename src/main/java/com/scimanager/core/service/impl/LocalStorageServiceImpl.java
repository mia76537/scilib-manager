package com.scimanager.core.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.scimanager.core.service.StorageService;

/**
 * 本地文件存储服务实现
 *
 * <p>将上传的文件保存到本地磁盘指定目录（默认 ./uploads/papers）。<br>
 * 使用 UUID 重命名文件以避名冲突，保留原始文件扩展名。</p>
 *
 * <p><b>存储路径配置：</b> {@code file.upload-path}（application.properties）</p>
 */
@Service
public class LocalStorageServiceImpl implements StorageService {

	@Value("${file.upload-path:./uploads/papers}")
	private String uploadPath;

	/**
	 * 上传文件到本地磁盘
	 *
	 * <p><b>执行流程：</b></p>
	 * <ol>
	 *   <li>检查上传目录是否存在，不存在则自动创建</li>
	 *   <li>从原始文件名提取扩展名</li>
	 *   <li>生成 UUID 作为新文件名（保留扩展名）</li>
	 *   <li>将文件输入流复制到目标路径</li>
	 *   <li>返回存储的文件名（相对路径）</li>
	 * </ol>
	 *
	 * @param file 上传的 Multipart 文件
	 * @return 存储的文件名（如 "a1b2c3d4.pdf"）
	 * @throws RuntimeException 如果文件写入失败
	 */
	@Override
	public String upload(MultipartFile file) {
		try {
			// 确保上传目录存在
			File folder = new File(uploadPath);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			// 生成唯一文件名（UUID + 原始扩展名）
			String originalFilename = file.getOriginalFilename();
			String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
			String fileName = UUID.randomUUID().toString() + suffix;

			// 写入磁盘
			Path path = Paths.get(uploadPath, fileName);
			Files.copy(file.getInputStream(), path);
			return fileName;
		} catch (IOException e) {
			throw new RuntimeException("文件存储失败: " + e.getMessage());
		}
	}

	/**
	 * 删除物理文件
	 *
	 * @param fileName 要删除的文件名（相对路径）
	 */
	@Override
	public void delete(String fileName) {
		try {
			Path path = Paths.get(uploadPath, fileName);
			Files.deleteIfExists(path);
		} catch (IOException e) {
			System.err.println("物理文件删除失败: " + fileName);
		}
	}

	/**
	 * 获取文件的绝对路径
	 *
	 * @param fileName 文件名（相对路径）
	 * @return 绝对路径字符串
	 */
	@Override
	public String getAbsolutePath(String fileName) {
		return Paths.get(uploadPath, fileName).toAbsolutePath().toString();
	}

	/**
	 * 将文件加载为 Spring Resource（用于下载）
	 *
	 * @param fileName 文件名（相对路径）
	 * @return UrlResource 对象
	 * @throws RuntimeException 如果文件不存在或路径非法
	 */
	@Override
	public Resource loadAsResource(String fileName) {
		try {
			Path file = Paths.get(uploadPath).resolve(fileName);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new RuntimeException("无法读取文件: " + fileName);
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException("文件路径非法: " + fileName);
		}
	}
}
