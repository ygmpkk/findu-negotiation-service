package com.findu.negotiation.infrastructure.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.findu.negotiation.infrastructure.exception.BusinessException;
import com.findu.negotiation.infrastructure.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class UserClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserClient.class);

    private final RestTemplate restTemplate;

    @Value("${findu.user.base-url}")
    private String baseUrl;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 获取用户的服务列表（type=3）
     *
     * @param providerId    服务提供者ID
     * @param authorization Authorization header
     * @return 服务列表，每个元素包含 worksId, title, extendInfo
     */
    public List<Map<String, Object>> getProviderWorks(String providerId, String authorization) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/v1/inner/user-admin/works/{userId}/public")
                .queryParam("type", 3)
                .queryParam("page", 1)
                .queryParam("pageSize", 50)
                .buildAndExpand(providerId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authorization != null && !authorization.isEmpty()) {
            headers.set("Authorization", authorization);
        }

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            LOGGER.info("调用User服务获取产品列表: providerId={}, url={}", providerId, url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            LOGGER.info("User服务响应: statusCode={}, body={}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject result = JSON.parseObject(response.getBody());
                String code = result.getString("code");
                LOGGER.info("User服务响应解析: code={}", code);
                if ("200".equals(code)) {
                    JSONObject data = result.getJSONObject("data");
                    if (data != null) {
                        JSONArray items = data.getJSONArray("items");
                        LOGGER.info("User服务获取到items: count={}", items != null ? items.size() : 0);
                        if (items != null) {
                            List<Map<String, Object>> worksList = new ArrayList<>();
                            for (int i = 0; i < items.size(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                worksList.add(item.getInnerMap());
                                LOGGER.debug("Works item[{}]: worksId={}, title={}", i, item.getString("worksId"), item.getString("title"));
                            }
                            LOGGER.info("User服务返回worksList: size={}", worksList.size());
                            return worksList;
                        }
                    } else {
                        LOGGER.warn("User服务返回data为null");
                    }
                } else {
                    LOGGER.warn("User服务返回code非200: code={}, body={}", code, response.getBody());
                }
            }
        } catch (RestClientException e) {
            LOGGER.error("调用User服务失败: url={}", url, e);
            throw new BusinessException(ErrorCode.USER_SERVICE_ERROR, e);
        }

        LOGGER.warn("User服务返回空列表: providerId={}", providerId);
        return new ArrayList<>();
    }
}
