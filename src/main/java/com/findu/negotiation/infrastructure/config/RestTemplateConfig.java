package com.findu.negotiation.infrastructure.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestTemplateConfig.class);

    private CloseableHttpClient httpClient;
    private PoolingHttpClientConnectionManager connectionManager;

    @Value("${spring.http.client.connection-timeout:5000}")
    private long connectionTimeout;

    @Value("${spring.http.client.read-timeout:10000}")
    private long readTimeout;

    @Value("${spring.http.client.pool.max-total:200}")
    private int maxTotal;

    @Value("${spring.http.client.pool.default-max-per-route:50}")
    private int defaultMaxPerRoute;

    @Value("${spring.http.client.pool.time-to-live:60000}")
    private long timeToLive;

    @Bean
    public PoolingHttpClientConnectionManager connectionManager() {
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxTotal);
        connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(Timeout.of(readTimeout, TimeUnit.MILLISECONDS))
                .build();
        connectionManager.setDefaultSocketConfig(socketConfig);

        LOGGER.info("HttpClient连接池配置: maxTotal={}, maxPerRoute={}, readTimeout={}ms",
                maxTotal, defaultMaxPerRoute, readTimeout);

        return connectionManager;
    }

    @Bean
    public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager connectionManager) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.of(readTimeout, TimeUnit.MILLISECONDS))
                .setConnectionRequestTimeout(Timeout.of(connectionTimeout, TimeUnit.MILLISECONDS))
                .build();

        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictIdleConnections(Timeout.of(timeToLive, TimeUnit.MILLISECONDS))
                .evictExpiredConnections()
                .build();

        LOGGER.info("HttpClient创建完成: connectTimeout={}ms, responseTimeout={}ms, timeToLive={}ms",
                connectionTimeout, readTimeout, timeToLive);

        return httpClient;
    }

    @Bean
    public RestTemplate restTemplate(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(factory);

        LOGGER.info("RestTemplate创建完成，已启用连接池管理");

        return restTemplate;
    }

    @PreDestroy
    public void destroy() {
        try {
            if (httpClient != null) {
                httpClient.close();
                LOGGER.info("HttpClient已关闭");
            }
            if (connectionManager != null) {
                connectionManager.close();
                LOGGER.info("连接池管理器已关闭");
            }
        } catch (Exception e) {
            LOGGER.error("关闭HttpClient资源时出错", e);
        }
    }
}
