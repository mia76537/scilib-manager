package com.scimanager.core.service;


import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    /**
     * 上传文件并返回存储路径（建议返回相对路径或文件名）
     */
    String upload(MultipartFile file);

    /**
     * 删除物理文件
     * @param path 数据库中存储的路径
     */
    void delete(String path);
    
    /**
     * 获取绝对路径
     */
    String getAbsolutePath(String localPath);
}