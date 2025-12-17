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
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Component
public class DmsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmsClient.class);

    private final HttpClientWrapper httpClientWrapper;

    @Value("${findu.dms.base-url}")
    private String baseUrl;

    public DmsClient(HttpClientWrapper httpClientWrapper) {
        this.httpClientWrapper = httpClientWrapper;
    }

    /**
     * 获取需求详情（自动透传Authorization header）
     *
     * @param userId   用户ID (customerId)
     * @param demandId 需求ID
     * @return 需求描述 (作为title使用)
     */
    public String getDemandDescription(String userId, String demandId) {
        return getDemandDescription(userId, demandId, true);
    }

    /**
     * 获取需求详情
     *
     * @param userId            用户ID (customerId)
     * @param demandId          需求ID
     * @param passAuthorization 是否透传Authorization header
     * @return 需求描述 (作为title使用)
     */
    public String getDemandDescription(String userId, String demandId, boolean passAuthorization) {
        String url = baseUrl + "/api/v1/inner/demand/detail";

        Map<String, String> formParams = new HashMap<>();
        formParams.put("userId", userId);
        formParams.put("demandId", demandId);

        try {
            LOGGER.info("调用DMS服务获取需求详情: userId={}, demandId={}, passAuthorization={}",
                    userId, demandId, passAuthorization);

            ResponseEntity<String> response = httpClientWrapper.postForm(
                    url, formParams, String.class, passAuthorization);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject result = JSON.parseObject(response.getBody());
                if (result.getIntValue("code") == 200) {
                    JSONObject data = result.getJSONObject("data");
                    if (data != null) {
                        String description = data.getString("description");
                        return extractServiceTypeFromMarkdown(description);
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

    /**
     * 从 markdown 表格格式的描述中提取"服务类型"字段
     *
     * @param markdown markdown 格式的描述文本
     * @return 服务类型内容，如果没有找到则返回原始描述或空字符串
     */
    private String extractServiceTypeFromMarkdown(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        try {
            String[] lines = markdown.split("\\r?\\n");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();

                // 查找 "**服务类型**" 行
                if (line.contains("**服务类型**")) {
                    // 查找下一行的内容
                    for (int j = i + 1; j < lines.length && j < i + 3; j++) {
                        String nextLine = lines[j].trim();

                        // 跳过表格分隔行
                        if (nextLine.startsWith("|") && (nextLine.contains(":---") || nextLine.contains("---"))) {
                            continue;
                        }

                        // 查找包含服务类型的行
                        if (nextLine.startsWith("|")) {
                            String[] cells = nextLine.split("\\|");
                            if (cells.length >= 3) {
                                // cells[0] 是空的，cells[1] 是服务类型，cells[2] 是值
                                String serviceType = cells[2].trim();
                                if (!serviceType.isEmpty()) {
                                    LOGGER.info("成功提取服务类型: {}", serviceType);
                                    return serviceType;
                                }
                            }
                        }
                    }
                }
            }

            LOGGER.warn("未找到服务类型字段，返回原始描述");
            return markdown;

        } catch (Exception e) {
            LOGGER.error("解析 markdown 描述失败", e);
            return markdown;
        }
    }
}
