package com.findu.negotiation.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "findu.feishu")
public class FeishuProperties {
    private String appId;
    private String appSecret;
    private String baseUrl = "https://open.feishu.cn/open-apis";
    private String userIdType = "user_id";
}
