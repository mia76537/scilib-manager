package com.scimanager.core.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 *
 * <p>提供文件上传、删除、路径获取和资源加载的抽象。<br>
 * 当前实现为 {@link com.scimanager.core.service.impl.LocalStorageServiceImpl}（本地文件系统）。</p>
 */
public interface StorageService {

	/**
	 * 上传文件并返回存储路径（相对路径或文件名）
	 */
	String upload(MultipartFile file);

	/**
	 * 删除物理文件
	 *
	 * @param path 数据库中存储的文件名或路径
	 */
	void delete(String path);

	/**
	 * 根据相对路径获取绝对路径
	 */
	String getAbsolutePath(String localPath);

	/**
	 * 加载文件为 Resource 以供下载
	 */
	Resource loadAsResource(String fileName);
}