package com.scimanager.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.scimanager.core.tool.JwtInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	final private JwtInterceptor jwtInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 设置拦截路径
		registry.addInterceptor(jwtInterceptor)
				.addPathPatterns("/api/papers/**", "/api/users/**", "/api/citation-requests/**", "/api/profile/**")
				.excludePathPatterns("/api/users/login");
	}

}