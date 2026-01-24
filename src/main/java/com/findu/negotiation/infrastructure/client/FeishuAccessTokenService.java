package com.findu.negotiation.infrastructure.client;

import com.alibaba.fastjson.JSONObject;
import com.findu.negotiation.infrastructure.config.FeishuProperties;
import com.findu.negotiation.infrastructure.exception.BusinessException;
import com.findu.negotiation.infrastructure.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class FeishuAccessTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeishuAccessTokenService.class);
    private static final long EXPIRY_SAFETY_WINDOW_SECONDS = 60L;

    private final RestTemplate restTemplate;
    private final FeishuProperties properties;
    private final AtomicReference<AccessTokenCache> cache = new AtomicReference<>();

    public FeishuAccessTokenService(RestTemplate restTemplate, FeishuProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public String getTenantAccessToken() {
        AccessTokenCache cached = cache.get();
        if (cached != null && cached.isValid()) {
            return cached.token();
        }

        if (!StringUtils.hasText(properties.getAppId()) || !StringUtils.hasText(properties.getAppSecret())) {
            throw new BusinessException(ErrorCode.FEISHU_SERVICE_ERROR, "飞书AppId或AppSecret未配置");
        }

        String url = properties.getBaseUrl() + "/auth/v3/tenant_access_token/internal";
        Map<String, Object> payload = new HashMap<>();
        payload.put("app_id", properties.getAppId());
        payload.put("app_secret", properties.getAppSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        LOGGER.info("请求飞书租户Token: url={}", url);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BusinessException(ErrorCode.FEISHU_SERVICE_ERROR, "获取飞书Token失败: HTTP " + response.getStatusCode());
        }

        JSONObject body = JSONObject.parseObject(response.getBody());
        if (body == null) {
            throw new BusinessException(ErrorCode.FEISHU_SERVICE_ERROR, "获取飞书Token失败: 响应为空");
        }

        Integer code = body.getInteger("code");
        if (code != null && code != 0) {
            throw new BusinessException(ErrorCode.FEISHU_SERVICE_ERROR,
                "获取飞书Token失败: " + body.getString("msg"));
        }

        String token = body.getString("tenant_access_token");
        long expireSeconds = body.getLongValue("expire");
        if (expireSeconds <= 0) {
            expireSeconds = body.getLongValue("expire_in");
        }

        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ErrorCode.FEISHU_SERVICE_ERROR, "获取飞书Token失败: token为空");
        }

        Instant expiresAt = Instant.now().plusSeconds(Math.max(0, expireSeconds - EXPIRY_SAFETY_WINDOW_SECONDS));
        cache.set(new AccessTokenCache(token, expiresAt));
        return token;
    }

    private record AccessTokenCache(String token, Instant expiresAt) {
        boolean isValid() {
            return Instant.now().isBefore(expiresAt);
        }
    }
}
