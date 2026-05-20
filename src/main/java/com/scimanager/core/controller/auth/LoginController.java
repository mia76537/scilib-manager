package com.scimanager.core.controller.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scimanager.core.entity.User;
import com.scimanager.core.security.JwtUtils;
import com.scimanager.core.service.LoginService;

/**
 * 登录认证控制器
 *
 * <p>处理用户登录请求，验证账号密码后签发 JWT Token。<br>
 * 此接口未被 JWT 拦截器拦截，属于公开访问端点。</p>
 *
 * <p><b>登录流程：</b> 接收 userId + password → 校验身份 → 生成 Token → 返回给前端</p>
 */
@RestController
@RequestMapping("/api/auth")
public class LoginController {

	@Autowired
	private LoginService loginService;

	/**
	 * 【POST /api/auth/login】用户登录
	 *
	 * <p>验证用户凭据，成功后签发 JWT Token 并返回用户信息。</p>
	 *
	 * <p><b>执行流程：</b></p>
	 * <ol>
	 *   <li>调用 {@link LoginService#login} 验证 userId + password</li>
	 *   <li>验证成功 → 使用 {@link JwtUtils#createToken} 生成 Token（含 userId 和 role）</li>
	 *   <li>验证失败 → 返回 success=false 及错误提示</li>
	 * </ol>
	 *
	 * @param userId   用户登录 ID
	 * @param password 用户密码（当前为明文，预留 BCrypt 加密方案）
	 * @return Map 包含：token（JWT字符串）、user（用户实体）、success（是否成功）、message（失败原因）
	 */
	@PostMapping("/login")
	public Map<String, Object> login(@RequestParam String userId, @RequestParam String password) {
		User user = loginService.login(userId, password);
		Map<String, Object> response = new HashMap<>();

		if (user != null) {
			// 登录成功：生成 JWT Token，内含 userId 和 role 两个 claims
			String token = JwtUtils.createToken(user.getUserId(), user.getRole());
			response.put("token", token);
			response.put("user", user);
			response.put("success", true);
		} else {
			// 登录失败：不提示具体是账号还是密码错误（安全考虑）
			response.put("success", false);
			response.put("message", "账号或密码错误");
		}
		return response;
	}

}
