package com.findu.negotiation.infrastructure.config;

import com.larksuite.oapi.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 飞书(Lark) SDK Client配置
 */
@Configuration
public class LarkClientConfig {

    @Bean
    public Client larkClient(LarkProperties properties) {
        return Client.newBuilder(properties.getAppId(), properties.getAppSecret()).build();
    }
}
