package com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.findu.negotiation.domain.negotiation.vo.NegotiationResultSchemaVO;
import com.findu.negotiation.domain.negotiation.vo.ProductInfoVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Order Negotiation Completions 请求体
 *
 * @author timothy
 * @date 2025/12/14
 */
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderNegotiationCompletionsRequest {

    @JsonProperty("id")
    private String id;

    @JsonProperty("agent_conversations")
    private List<ConversationItem> agentConversations;

    @JsonProperty("human_conversations")
    private List<ConversationItem> humanConversations;

    private List<ProductInfoVO> products;

    @JsonProperty("result_schema")
    private NegotiationResultSchemaVO resultSchema;
}
