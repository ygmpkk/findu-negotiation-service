package com.findu.negotiation.infrastructure.config;

import com.findu.negotiation.infrastructure.interceptor.RequestContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * @author timothy
 * @date 2025/12/17
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestContextInterceptor requestContextInterceptor;

    public WebMvcConfig(RequestContextInterceptor requestContextInterceptor) {
        this.requestContextInterceptor = requestContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestContextInterceptor)
                .addPathPatterns("/**")
                .order(0); // 确保最先执行
    }
}

