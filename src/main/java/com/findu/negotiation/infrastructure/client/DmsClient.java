package com.findu.negotiation.infrastructure.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.findu.negotiation.infrastructure.exception.BusinessException;
import com.findu.negotiation.infrastructure.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class DmsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmsClient.class);

    private final RestTemplate restTemplate;

    @Value("${findu.dms.base-url}")
    private String baseUrl;

    public DmsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 获取需求详情
     *
     * @param userId   用户ID (customerId)
     * @param demandId 需求ID
     * @return 需求描述 (作为title使用)
     */
    public String getDemandDescription(String userId, String demandId) {
        String url = baseUrl + "/api/v1/inner/demand/detail";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + userId);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("userId", userId);
        params.add("demandId", demandId);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            LOGGER.info("调用DMS服务获取需求详情: userId={}, demandId={}", userId, demandId);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject result = JSON.parseObject(response.getBody());
                if (result.getIntValue("code") == 200) {
                    JSONObject data = result.getJSONObject("data");
                    if (data != null) {
                        return data.getString("description");
                    }
                }
                LOGGER.warn("DMS服务返回失败: {}", response.getBody());
            }
        } catch (RestClientException e) {
            LOGGER.error("调用DMS服务失败", e);
            throw new BusinessException(ErrorCode.DMS_SERVICE_ERROR, e);
        }

        return null;
    }
}
