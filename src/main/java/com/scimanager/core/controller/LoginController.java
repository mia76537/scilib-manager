package com.scimanager.core.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scimanager.core.model.User;
import com.scimanager.core.service.LoginService;
import com.scimanager.core.tool.JwtUtils;

/**
 * 登录请求处理 处理 Vue 前端传来的登录信息
 */
@RestController
@RequestMapping("/api/auth")
public class LoginController {

	@Autowired
	private LoginService loginService;

	/**
	 * 登录请求处理 处理 Vue 前端传来的登录信息
	 */
	@PostMapping("/login")
	public Map<String, Object> login(@RequestParam String userId, @RequestParam String password) {
		User user = loginService.login(userId, password);
		Map<String, Object> response = new HashMap<>();

		if (user != null) {
			// 生成 Token
			String token = JwtUtils.createToken(user.getUserId(), user.getRole());
			response.put("token", token);
			response.put("user", user);
			response.put("success", true);
		} else {
			response.put("success", false);
			response.put("message", "账号或密码错误");
		}
		return response;
	}

}