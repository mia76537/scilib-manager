package com.scimanager.core.service.serviceImpl;

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

@Service
public class LocalStorageServiceImpl implements StorageService {

	// 从 application.properties 读取路径，默认为项目根目录下的 uploads/papers
	@Value("${file.upload-path:./uploads/papers}")
	private String uploadPath;

	@Override
	public String upload(MultipartFile file) {
		try {
			// 1. 确保目录存在
			File folder = new File(uploadPath);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			// 2. 生成唯一文件名，防止冲突 (UUID + 原始后缀)
			String originalFilename = file.getOriginalFilename();
			String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
			String fileName = UUID.randomUUID().toString() + suffix;

			// 3. 保存文件
			Path path = Paths.get(uploadPath, fileName);
			Files.copy(file.getInputStream(), path);

			// 返回文件名（相对路径），方便未来迁移
			return fileName;
		} catch (IOException e) {
			throw new RuntimeException("文件存储失败: " + e.getMessage());
		}
	}

	@Override
	public void delete(String fileName) {
		try {
			Path path = Paths.get(uploadPath, fileName);
			Files.deleteIfExists(path);
		} catch (IOException e) {
			// 物理文件删除失败通常记录日志，不一定阻塞业务
			System.err.println("物理文件删除失败: " + fileName);
		}
	}

	@Override
	public String getAbsolutePath(String fileName) {
		// 提供本地文件绝对路径
		return Paths.get(uploadPath, fileName).toAbsolutePath().toString();
	}

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
