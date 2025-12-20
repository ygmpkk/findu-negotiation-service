package com.findu.negotiation.infrastructure.client;

import com.findu.negotiation.infrastructure.client.dto.user.ProviderUserResponse;
import com.findu.negotiation.infrastructure.client.dto.user.ProviderProduct;
import com.findu.negotiation.infrastructure.exception.BusinessException;
import com.findu.negotiation.infrastructure.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserClient.class);

    private final HttpClientWrapper httpClientWrapper;

    @Value("${findu.user.base-url}")
    private String baseUrl;

    public UserClient(HttpClientWrapper httpClientWrapper) {
        this.httpClientWrapper = httpClientWrapper;
    }

    /**
     * 获取用户的服务列表（type=3）
     *
     * @param providerId 服务提供者ID
     * @return 服务列表，每个元素包含 worksId, title, extendInfo
     */
    public List<ProviderProduct> getProviderWorks(String providerId) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/api/v1/inner/user-admin/works/{userId}/public")
                .queryParam("type", 3)
                .queryParam("page", 1)
                .queryParam("pageSize", 50)
                .buildAndExpand(providerId)
                .toUriString();

        LOGGER.info("调用User服务获取产品列表: providerId={}, url={}", providerId, url);

        try {
            ResponseEntity<ProviderUserResponse> response = httpClientWrapper.getJson(url, ProviderUserResponse.class, true);
            LOGGER.info("User服务响应: statusCode={}, body={}", response.getStatusCode(), response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                LOGGER.warn("User服务返回code非200: code={}, body={}", response.getStatusCode(), response.getBody());
                return new ArrayList<>();
            }

            if (null == response.getBody()
                    || null == response.getBody().getData()
                    || null == response.getBody().getData().getItems()
                    || CollectionUtils.isEmpty(response.getBody().getData().getItems())) {
                LOGGER.warn("User服务返回空数据: providerId={}", providerId);
                return new ArrayList<>();
            }

            return response.getBody().getData().getItems();
        } catch (RestClientException e) {
            LOGGER.error("调用User服务失败: url={}", url, e);
            throw new BusinessException(ErrorCode.USER_SERVICE_ERROR, e);
        }
    }
}
