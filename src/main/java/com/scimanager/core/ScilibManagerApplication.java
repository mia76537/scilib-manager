package com.scimanager.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 科研文献管理系统 —— 主启动类
 *
 * <p>系统功能概述：</p>
 * <ul>
 *   <li><b>文献管理</b>：上传 PDF、AI 提取元数据、关键词搜索、GB/T 7714 引文生成</li>
 *   <li><b>查收查引</b>：提交检索请求、管理员录入检索结果、多数据库查询范围配置</li>
 *   <li><b>科研兴趣画像</b>：基于文献关键词，通过 DeepSeek AI 分析用户研究方向</li>
 *   <li><b>用户与权限</b>：多角色（管理员/导师/学生）访问控制、JWT 认证</li>
 * </ul>
 *
 * <p>启用特性：</p>
 * <ul>
 *   <li>{@code @EnableAsync} — 开启异步任务（PDF 解析、AI 分析）</li>
 *   <li>{@code @EnableJpaAuditing} — 开启 JPA 审计（预留）</li>
 * </ul>
 */
@EnableAsync
@EnableJpaAuditing
@SpringBootApplication
public class ScilibManagerApplication {

	/**
	 * 应用入口
	 *
	 * @param args 命令行参数
	 */
	public static void main(String[] args) {
		SpringApplication.run(ScilibManagerApplication.class, args);
	}

}
