package com.findu.negotiation.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 飞书(Lark) SDK配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "lark")
public class LarkProperties {
    /**
     * 应用AppId
     */
    private String appId;

    /**
     * 应用AppSecret
     */
    private String appSecret;
}
