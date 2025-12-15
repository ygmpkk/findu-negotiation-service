package com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Order Negotiation Completions 请求体
 *
 * @author timothy
 * @date 2025/12/14
 */
public class OrderNegotiationCompletionsRequest {

    @JsonProperty("id")
    private String id;

    @JsonProperty("agent_conversations")
    private List<ConversationItem> agentConversations;

    @JsonProperty("human_conversations")
    private List<ConversationItem> humanConversations;

    @JsonProperty("service_card")
    private ServiceCard serviceCard;

    @JsonProperty("result_schema")
    private ResultSchema resultSchema;

    public OrderNegotiationCompletionsRequest() {
    }

    public OrderNegotiationCompletionsRequest(String id,
                                             List<ConversationItem> agentConversations,
                                             List<ConversationItem> humanConversations,
                                             ServiceCard serviceCard,
                                             ResultSchema resultSchema) {
        this.id = id;
        this.agentConversations = agentConversations;
        this.humanConversations = humanConversations;
        this.serviceCard = serviceCard;
        this.resultSchema = resultSchema;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ConversationItem> getAgentConversations() {
        return agentConversations;
    }

    public void setAgentConversations(List<ConversationItem> agentConversations) {
        this.agentConversations = agentConversations;
    }

    public List<ConversationItem> getHumanConversations() {
        return humanConversations;
    }

    public void setHumanConversations(List<ConversationItem> humanConversations) {
        this.humanConversations = humanConversations;
    }

    public ServiceCard getServiceCard() {
        return serviceCard;
    }

    public void setServiceCard(ServiceCard serviceCard) {
        this.serviceCard = serviceCard;
    }

    public ResultSchema getResultSchema() {
        return resultSchema;
    }

    public void setResultSchema(ResultSchema resultSchema) {
        this.resultSchema = resultSchema;
    }
}
