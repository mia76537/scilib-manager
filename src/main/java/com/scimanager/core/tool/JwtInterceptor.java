package com.scimanager.core.tool;


import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	// 1. 放行 OPTIONS 请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        System.out.println("收到请求: " + request.getRequestURI() + "，Auth Header: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = JwtUtils.parseToken(token);
                if (claims != null) {
                    String userId = claims.get("userId", String.class);
                    request.setAttribute("userId", userId);
                    System.out.println("Token 验证成功，用户ID: " + userId);
                    return true;
                } else {
                    System.out.println("Token 解析结果为空");
                }
            } catch (Exception e) {
                System.err.println("Token 解析过程中发生异常: " + e.getMessage());
            }
        } else {
            System.out.println("未检测到 Bearer Token 或 Header 为空");
        }

        // 3. 校验失败，返回 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
        
    }
}