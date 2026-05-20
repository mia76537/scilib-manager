package com.scimanager.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.scimanager.core.security.JwtInterceptor;

import lombok.RequiredArgsConstructor;

/**
 * Web MVC 配置类
 *
 * <p>注册 JWT 认证拦截器，配置需要/不需要认证的 API 路径。</p>
 *
 * <p><b>拦截范围：</b></p>
 * <ul>
 *   <li>需认证：/api/papers/**, /api/users/**, /api/citation-requests/**, /api/profile/**</li>
 *   <li>无需认证：/api/users/login（登录接口）</li>
 * </ul>
 * <p><b>注意：</b>/api/auth/login 虽然没有显式添加拦截路径，但也不在被拦截路径中，因此无需认证。</p>
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	final private JwtInterceptor jwtInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(jwtInterceptor)
				.addPathPatterns("/api/papers/**", "/api/users/**", "/api/citation-requests/**", "/api/profile/**")
				.excludePathPatterns("/api/users/login");
	}

}