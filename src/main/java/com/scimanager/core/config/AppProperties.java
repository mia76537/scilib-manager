package com.scimanager.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 应用全局配置属性类（预留）
 *
 * <p>集中管理外部服务密钥等敏感配置项。<br>
 * 当前 DeepSeek API Key 通过 {@code @Value("\${deepseek.api-key}")} 在各 Service 中分散读取。<br>
 * 未来迁移时可将所有外部配置集中于此，统一从环境变量注入。</p>
 *
 * <p><b>使用示例（application.properties）：</b></p>
 * <pre>
 * app.deepseek.api-key=${DEEPSEEK_API_KEY}
 * </pre>
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Deepseek deepseek = new Deepseek();

    public Deepseek getDeepseek() {
        return deepseek;
    }

    /**
     * DeepSeek AI 服务配置
     */
    public static class Deepseek {
        /** DeepSeek API 密钥 */
        private String apiKey;
        /** DeepSeek API 请求地址 */
        private String apiUrl = "https://api.deepseek.com/chat/completions";

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getApiUrl() { return apiUrl; }
        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    }
}
