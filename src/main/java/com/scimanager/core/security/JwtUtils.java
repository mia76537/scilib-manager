package com.scimanager.core.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * JWT 工具类
 *
 * <p>提供 JWT Token 的创建、解析和校验功能。<br>
 * 使用 HS256 签名算法，密钥硬编码于此类中（预留 {@link JwtProperties} 用于未来配置化迁移）。</p>
 *
 * <p><b>Token 负载结构：</b></p>
 * <ul>
 *   <li>userId — 用户唯一标识</li>
 *   <li>role — 用户角色（ADMIN / MENTOR / STUDENT）</li>
 *   <li>sub — 主题（同为 userId）</li>
 *   <li>iat — 签发时间</li>
 *   <li>exp — 过期时间（默认 120 分钟）</li>
 * </ul>
 */
@Component
public class JwtUtils {

	/**
	 * HMAC 密钥（HS256 算法要求密钥长度至少 256 位）
	 */
	private static final String SECRET_STR = "your_super_secret_key_for_scimanager_system_2024";
	private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STR.getBytes());

	/**
	 * Token 过期时间：120 分钟（毫秒）
	 */
	private static final long EXPIRATION_TIME = 120 * 60 * 1000;

	/**
	 * 生成 JWT Token
	 *
	 * @param userId 用户 ID（放入自定义 claims）
	 * @param role   用户角色（放入自定义 claims）
	 * @return JWT 字符串（格式：header.payload.signature）
	 */
	public static String createToken(String userId, String role) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("userId", userId);
		claims.put("role", role);

		return Jwts.builder()
				.setClaims(claims)                             // 自定义负载（userId, role）
				.setSubject(userId)                            // 主题
				.setIssuedAt(new Date())                       // 签发时间
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 过期时间
				.signWith(SECRET_KEY, SignatureAlgorithm.HS256) // HS256 签名
				.compact();
	}

	/**
	 * 解析并验证 JWT Token
	 *
	 * <p>验证签名和过期时间，成功返回 Claims，失败返回 null。</p>
	 *
	 * @param token JWT 字符串
	 * @return 解析后的 Claims（含 userId、role 等），解析失败返回 null
	 */
	public static Claims parseToken(String token) {
		try {
			return Jwts.parserBuilder()
					.setSigningKey(SECRET_KEY)
					.build()
					.parseClaimsJws(token)
					.getBody();
		} catch (Exception e) {
			// Token 过期、签名篡改、格式错误等均返回 null
			return null;
		}
	}

	/**
	 * 校验 Token 是否有效
	 *
	 * @param token JWT 字符串
	 * @return true 如果 Token 合法且未过期
	 */
	public static boolean validateToken(String token) {
		return parseToken(token) != null;
	}
}
