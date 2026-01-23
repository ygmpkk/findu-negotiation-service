package com.findu.negotiation.infrastructure.config;

import com.lark.oapi.Client;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FeishuProperties.class)
public class FeishuConfig {

    @Bean
    @ConditionalOnProperty(prefix = "findu.feishu", name = {"app-id", "app-secret"})
    public Client feishuClient(FeishuProperties properties) {
        return Client.newBuilder(properties.getAppId(), properties.getAppSecret()).build();
    }
}
