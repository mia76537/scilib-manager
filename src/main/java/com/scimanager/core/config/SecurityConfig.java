package com.scimanager.core.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.scimanager.core.security.JwtProperties;

/**
 * 安全与应用配置聚合类
 *
 * <p>注册 {@link JwtProperties} 和 {@link AppProperties} 的配置绑定，<br>
 * 使 application.properties 中的 {@code jwt.*} 和 {@code app.*} 前缀配置生效。</p>
 *
 * <p>未来可在此集中管理 CORS、CSRF 等安全配置。</p>
 */
@Configuration
@EnableConfigurationProperties({ JwtProperties.class, AppProperties.class })
public class SecurityConfig {
}
