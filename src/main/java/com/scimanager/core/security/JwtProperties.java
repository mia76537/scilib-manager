package com.scimanager.core.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置属性类（预留）
 * 
 * <p>当前 JWT 密钥和过期时间在 {@link JwtUtils} 中硬编码。
 * 未来迁移时：</p>
 * <ol>
 *   <li>在 {@code application.properties} 或环境变量中配置
 *       {@code jwt.secret} 和 {@code jwt.expiration-ms}</li>
 *   <li>修改 {@link JwtUtils} 从此类读取配置，替代硬编码常量</li>
 * </ol>
 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** JWT 签名密钥（HS256 要求至少 256 位） */
    private String secret;

    /** Token 过期时间（毫秒），默认 120 分钟 */
    private long expirationMs = 120 * 60 * 1000;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getExpirationMs() { return expirationMs; }
    public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
}
