package com.scimanager.core.security;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT 认证拦截器
 *
 * <p>拦截需要认证的 API 请求，从请求头中提取并验证 JWT Token，<br>
 * 验证通过后将 userId 和 role 注入到 request attribute 中，供下游 Controller 使用。</p>
 *
 * <p>拦截路径配置见 {@link com.scimanager.core.config.WebConfig#addInterceptors}。</p>
 *
 * <p><b>工作流程：</b></p>
 * <ol>
 *   <li>放行 OPTIONS 预检请求</li>
 *   <li>从 Authorization 头提取 Bearer Token</li>
 *   <li>解析 Token 获取 userId 和 role</li>
 *   <li>注入 request attributes</li>
 *   <li>验证失败返回 HTTP 401</li>
 * </ol>
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		// 1. 放行 OPTIONS 请求（CORS 预检）
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			return true;
		}

		String authHeader = request.getHeader("Authorization");

		// 2. 校验 Authorization 头格式是否为 "Bearer {token}"
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			try {
				Claims claims = JwtUtils.parseToken(token);
				if (claims != null) {
					// 3. 从 Token 中提取 userId 和 role
					String userId = claims.get("userId", String.class);
					String role = claims.get("role", String.class);
					// 4. 注入到 request attribute（Controller 中通过 @RequestAttribute 获取）
					request.setAttribute("userId", userId);
					request.setAttribute("role", role);
					return true;
				}
			} catch (Exception e) {
				System.err.println("Token 解析过程中发生异常: " + e.getMessage());
			}
		}

		// 5. 验证失败，返回 401
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		return false;
	}
}
