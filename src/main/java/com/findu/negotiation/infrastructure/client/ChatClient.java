package com.findu.negotiation.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findu.negotiation.infrastructure.client.dto.chat.ChatHistoryData;
import com.findu.negotiation.infrastructure.client.dto.chat.ChatHistoryResponse;
import com.findu.negotiation.infrastructure.exception.BusinessException;
import com.findu.negotiation.infrastructure.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;


@Component
public class ChatClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClient.class);

    private final HttpClientWrapper httpClientWrapper;

    @Value("${findu.chat.base-url:http://localhost:8460}")
    private String baseUrl;

    public ChatClient(HttpClientWrapper httpClientWrapper) {
        this.httpClientWrapper = httpClientWrapper;
    }

    public ChatHistoryResponse getChatHistory(String userA, String userB) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/inner/chat_history/{userA}/{userB}")
                .buildAndExpand(userA, userB)
                .toUriString();

        LOGGER.info("调用Chat服务获取聊天记录: userA={}, userB={}, url={}", userA, userB, url);

        try {
            ResponseEntity<String> response = httpClientWrapper.get(url, String.class, true);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                        "Chat服务返回异常状态码: " + response.getStatusCode());
            }

            if (response.getBody() == null) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Chat服务返回空响应");
            }

            // Use ObjectMapper to properly deserialize the response with custom deserializers
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode resultNode = objectMapper.readTree(response.getBody());

            ChatHistoryData data = objectMapper.treeToValue(resultNode, ChatHistoryData.class);
            // Set user_a and user_b from request parameters if not in response
            if (data.getProviderId() == null) {
                data.setProviderId(userA);
            }
            if (data.getCustomerId() == null) {
                data.setCustomerId(userB);
            }

            return new ChatHistoryResponse(
                    true,
                    String.format("成功获取 %d 条消息", data.getMsgCount() != null ? data.getMsgCount() : 0),
                    data
            );
        } catch (RestClientException e) {
            LOGGER.error("调用Chat服务失败: url={}", url, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, e);
        } catch (Exception e) {
            LOGGER.error("解析Chat服务响应失败: url={}", url, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, e);
        }
    }
}
