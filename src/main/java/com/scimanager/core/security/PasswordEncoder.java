package com.scimanager.core.security;

/**
 * 密码编码器接口（预留）
 * 
 * <p>当前项目中密码以明文存储和比对。未来迁移到 BCrypt 等加密方案时，
 * 实现此接口并注册为 Spring Bean，框架将自动通过 {@code @Autowired}
 * 注入到 {@code LoginServiceImpl} 和 {@code UserServiceImpl} 中，
 * 无缝替换明文操作，无需修改业务代码。</p>
 * 
 * <p><b>使用方法：</b></p>
 * <ol>
 *   <li>创建实现类（如 {@code BCryptPasswordEncoder}）</li>
 *   <li>标注 {@code @Component} 或 {@code @Service}</li>
 *   <li>现有 Service 层已预留 {@code @Autowired(required = false)} 注入点</li>
 * </ol>
 */
public interface PasswordEncoder {

    /**
     * 将原始密码编码（加密）
     *
     * @param rawPassword 明文密码
     * @return 编码后的密码字符串
     */
    String encode(String rawPassword);

    /**
     * 验证原始密码与已编码的密码是否匹配
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 已编码的密码（数据库中存储的值）
     * @return 匹配返回 true，否则 false
     */
    boolean matches(String rawPassword, String encodedPassword);
}
