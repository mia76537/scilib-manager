package com.scimanager.core.tool;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

	// 密钥（ HS256 算法要求密钥长度至少 256 位
	private static final String SECRET_STR = "your_super_secret_key_for_scimanager_system_2024";
	private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STR.getBytes());

	// 过期时间： 120 分钟 (毫秒计算)
	private static final long EXPIRATION_TIME = 120 * 60 * 1000;

	/**
	 * 生成 Token
	 * 
	 * @param userId 用户ID
	 * @param role   用户角色
	 */
	public static String createToken(String userId, String role) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("userId", userId);
		claims.put("role", role);

		return Jwts.builder().setClaims(claims) // 设置自定义负载
				.setSubject(userId) // 设置主题（通常是用户ID）
				.setIssuedAt(new Date()) // 签发时间
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 过期时间
				.signWith(SECRET_KEY, SignatureAlgorithm.HS256) // 签名算法
				.compact();
	}

	/**
	 * 解析并验证 Token
	 */
	public static Claims parseToken(String token) {
		try {
			return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
		} catch (Exception e) {
			// Token 过期、被篡改等都会抛出异常
			return null;
		}
	}

	/**
	 * 校验 Token 是否有效
	 */
	public static boolean validateToken(String token) {
		return parseToken(token) != null;
	}
}