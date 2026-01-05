package com.findu.negotiation.infrastructure.client;

import com.findu.negotiation.infrastructure.context.RequestContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP客户端包装器，支持自动透传Authorization header
 *
 * @author timothy
 * @date 2025/12/17
 */
@Component
public class HttpClientWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientWrapper.class);

    private final RestTemplate restTemplate;

    public HttpClientWrapper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 发送GET请求
     *
     * @param url               请求URL
     * @param responseType      响应类型
     * @param passAuthorization 是否透传Authorization header
     * @return 响应结果
     */
    public <T> ResponseEntity<T> get(String url, Class<T> responseType, boolean passAuthorization) {
        return get(url, null, responseType, passAuthorization);
    }

    public <T> ResponseEntity<T> getJson(String url, Class<T> responseType, boolean passAuthorization) {
        Map<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return get(url, additionalHeaders, responseType, passAuthorization);
    }

    /**
     * 发送GET请求
     *
     * @param url               请求URL
     * @param additionalHeaders 额外的请求头
     * @param responseType      响应类型
     * @param passAuthorization 是否透传Authorization header
     * @return 响应结果
     */
    public <T> ResponseEntity<T> get(String url, Map<String, String> additionalHeaders,
                                      Class<T> responseType, boolean passAuthorization) {
        HttpHeaders headers = buildHeaders(additionalHeaders, passAuthorization);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        long startTime = System.currentTimeMillis();
        try {
            LOGGER.debug("发送GET请求: url={}, passAuthorization={}", url, passAuthorization);
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, request, responseType);
            long procTime = System.currentTimeMillis() - startTime;
            LOGGER.info("type=http_client, method=GET, uri={}, status={}, proc_time={}ms",
                url, response.getStatusCode().value(), procTime);
            return response;
        } catch (RestClientException e) {
            long procTime = System.currentTimeMillis() - startTime;
            LOGGER.error("type=http_client, method=GET, uri={}, status=error, proc_time={}ms, error={}",
                url, procTime, e.getMessage());
            throw e;
        }
    }

    /**
     * 发送POST请求（JSON格式）
     *
     * @param url               请求URL
     * @param body              请求体
     * @param responseType      响应类型
     * @param passAuthorization 是否透传Authorization header
     * @return 响应结果
     */
    public <T> ResponseEntity<T> postJson(String url, Object body, Class<T> responseType, boolean passAuthorization) {
        return postJson(url, body, null, responseType, passAuthorization);
    }

    /**
     * 发送POST请求（JSON格式）
     *
     * @param url               请求URL
     * @param body              请求体
     * @param additionalHeaders 额外的请求头
     * @param responseType      响应类型
     * @param passAuthorization 是否透传Authorization header
     * @return 响应结果
     */
    public <T> ResponseEntity<T> postJson(String url, Object body, Map<String, String> additionalHeaders,
                                           Class<T> responseType, boolean passAuthorization) {
        HttpHeaders headers = buildHeaders(additionalHeaders, passAuthorization);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> request = new HttpEntity<>(body, headers);

        long startTime = System.currentTimeMillis();
        try {
            LOGGER.debug("发送POST JSON请求: url={}, passAuthorization={}", url, passAuthorization);
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, request, responseType);
            long procTime = System.currentTimeMillis() - startTime;
            LOGGER.info("type=http_client, method=POST, uri={}, status={}, proc_time={}ms",
                url, response.getStatusCode().value(), procTime);
            return response;
        } catch (RestClientException e) {
            long procTime = System.currentTimeMillis() - startTime;
            LOGGER.error("type=http_client, method=POST, uri={}, status=error, proc_time={}ms, error={}",
                url, procTime, e.getMessage());
            throw e;
        }
    }

    /**
     * 发送POST请求（表单格式）
     *
     * @param url               请求URL
     * @param formParams        表单参数
     * @param responseType      响应类型
     * @param passAuthorization 是否透传Authorization header
     * @return 响应结果
     */
    public <T> ResponseEntity<T> postForm(String url, Map<String, String> formParams,
                                           Class<T> responseType, boolean passAuthorization) {
        return postForm(url, formParams, null, responseType, passAuthorization);
    }

    /**
     * 发送POST请求（表单格式）
     *
     * @param url               请求URL
     * @param formParams        表单参数
     * @param additionalHeaders 额外的请求头
     * @param responseType      响应类型
     * @param passAuthorization 是否透传Authorization header
     * @return 响应结果
     */
    public <T> ResponseEntity<T> postForm(String url, Map<String, String> formParams,
                                           Map<String, String> additionalHeaders,
                                           Class<T> responseType, boolean passAuthorization) {
        HttpHeaders headers = buildHeaders(additionalHeaders, passAuthorization);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (formParams != null) {
            formParams.forEach(params::add);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        long startTime = System.currentTimeMillis();
        try {
            LOGGER.debug("发送POST FORM请求: url={}, passAuthorization={}", url, passAuthorization);
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, request, responseType);
            long procTime = System.currentTimeMillis() - startTime;
            LOGGER.info("type=http_client, method=POST, uri={}, status={}, proc_time={}ms",
                url, response.getStatusCode().value(), procTime);
            return response;
        } catch (RestClientException e) {
            long procTime = System.currentTimeMillis() - startTime;
            LOGGER.error("type=http_client, method=POST, uri={}, status=error, proc_time={}ms, error={}",
                url, procTime, e.getMessage());
            throw e;
        }
    }

    /**
     * 构建请求头
     *
     * @param additionalHeaders 额外的请求头
     * @param passAuthorization 是否透传Authorization header
     * @return HttpHeaders
     */
    private HttpHeaders buildHeaders(Map<String, String> additionalHeaders, boolean passAuthorization) {
        HttpHeaders headers = new HttpHeaders();

        // 透传Authorization header
        if (passAuthorization) {
            String authorization = RequestContextHolder.getAuthorization();
            if (authorization != null && !authorization.isEmpty()) {
                headers.set("Authorization", authorization);
                LOGGER.debug("透传Authorization header");
            } else {
                LOGGER.debug("请求上下文中没有Authorization header");
            }
        }

        // 添加额外的请求头
        if (additionalHeaders != null && !additionalHeaders.isEmpty()) {
            additionalHeaders.forEach((key, value) -> {
                if (value != null) {
                    headers.set(key, value);
                }
            });
        }

        return headers;
    }

    /**
     * 获取当前请求的Authorization header
     *
     * @return Authorization header值，如果不存在则返回null
     */
    public static String getCurrentAuthorization() {
        return RequestContextHolder.getAuthorization();
    }
}

