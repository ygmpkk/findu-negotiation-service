package com.findu.negotiation.infrastructure.client;

import com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent.OrderNegotiationCompletionsRequest;
import com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent.OrderNegotiationCompletionsResponse;
import com.findu.negotiation.infrastructure.exception.BusinessException;
import com.findu.negotiation.infrastructure.exception.ErrorCode;
import com.findu.negotiation.infrastructure.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    @Value("${findu.order-negotiation-agent.base-url}")
    private String baseUrl;

    @Value("${findu.order-negotiation-agent.completions-endpoint:/api/v1/inner/order_negotiation/completions}")
    private String completionsEndpoint;

    /**
     * 调用订单协商补全接口
     *
     * @param request 订单协商补全请求
     * @return 订单协商补全响应
     */
    public OrderNegotiationCompletionsResponse completions(OrderNegotiationCompletionsRequest request) {
        String url = baseUrl + completionsEndpoint;

        LOGGER.info("调用Order Negotiation Agent Completions服务: id={}, agentConversationsCount={}, humanConversationsCount={}, serviceCardTitle={}",
                request.getId(),
                request.getAgentConversations() != null ? request.getAgentConversations().size() : 0,
                request.getHumanConversations() != null ? request.getHumanConversations().size() : 0,
                request.getServiceCard() != null ? request.getServiceCard().getTitle() : null);

        HttpUtil.HttpResponse<OrderNegotiationCompletionsResponse> completionsResponse = HttpUtil.postJson(url, request, OrderNegotiationCompletionsResponse.class);
        if (!completionsResponse.isSuccessful()) {
            throw new BusinessException(ErrorCode.AGENT_SERVICE_ERROR, "Agent服务返回异常状态码: " + completionsResponse.statusCode());
        }

        return completionsResponse.body();
    }
}
