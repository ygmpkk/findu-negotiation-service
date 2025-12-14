package com.findu.negotiation.infrastructure.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.findu.negotiation.infrastructure.client.dto.OrderNegotiationCompletionsRequest;
import com.findu.negotiation.infrastructure.client.dto.OrderNegotiationCompletionsResponse;
import com.findu.negotiation.infrastructure.exception.BusinessException;
import com.findu.negotiation.infrastructure.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Order Negotiation Agent 服务客户端
 * 调用协商Agent服务生成协商草案
 *
 * @author timothy
 * @date 2025/12/14
 */
@Component
public class OrderNegotiationAgentClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderNegotiationAgentClient.class);

    private final RestTemplate restTemplate;

    @Value("${findu.order-negotiation-agent.base-url}")
    private String baseUrl;

    @Value("${findu.order-negotiation-agent.completions-endpoint:/api/v1/orders_negotiation/completions}")
    private String completionsEndpoint;

    public OrderNegotiationAgentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 调用订单协商补全接口
     *
     * @param request       订单协商补全请求
     * @param authorization Authorization header
     * @return 订单协商补全响应
     */
    public OrderNegotiationCompletionsResponse completions(OrderNegotiationCompletionsRequest request, String authorization) {
        String url = baseUrl + completionsEndpoint;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authorization != null && !authorization.isEmpty()) {
            headers.set("Authorization", authorization);
        }

        HttpEntity<OrderNegotiationCompletionsRequest> httpEntity = new HttpEntity<>(request, headers);

        try {
            LOGGER.info("调用Order Negotiation Agent Completions服务: id={}, agentConversationsCount={}, humanConversationsCount={}, serviceCardTitle={}",
                    request.getId(),
                    request.getAgentConversations() != null ? request.getAgentConversations().size() : 0,
                    request.getHumanConversations() != null ? request.getHumanConversations().size() : 0,
                    request.getServiceCard() != null ? request.getServiceCard().getTitle() : null);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject result = JSON.parseObject(response.getBody());
                String code = result.getString("code");

                if ("200".equals(code)) {
                    JSONObject data = result.getJSONObject("data");
                    if (data != null) {
                        OrderNegotiationCompletionsResponse completionsResponse =
                                JSON.parseObject(data.toJSONString(), OrderNegotiationCompletionsResponse.class);
                        LOGGER.info("Agent Completions服务返回成功: id={}, resultKeys={}",
                                completionsResponse.getId(),
                                completionsResponse.getResult() != null ? completionsResponse.getResult().keySet() : null);
                        return completionsResponse;
                    }
                }

                LOGGER.warn("Agent Completions服务返回失败: code={}, message={}", code, result.getString("message"));
                throw new BusinessException(ErrorCode.AGENT_SERVICE_ERROR, "Agent服务返回错误: " + result.getString("message"));
            }

            LOGGER.warn("Agent Completions服务返回非2xx状态码: {}", response.getStatusCode());
            throw new BusinessException(ErrorCode.AGENT_SERVICE_ERROR, "Agent服务返回异常状态码: " + response.getStatusCode());

        } catch (RestClientException e) {
            LOGGER.error("调用Agent Completions服务失败", e);
            throw new BusinessException(ErrorCode.AGENT_SERVICE_ERROR, e);
        }
    }
}
