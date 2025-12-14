package com.findu.negotiation.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Order Negotiation Completions 响应体
 *
 * @author timothy
 * @date 2025/12/14
 */
public class OrderNegotiationCompletionsResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("result")
    private Map<String, Object> result;

    public OrderNegotiationCompletionsResponse() {
    }

    public OrderNegotiationCompletionsResponse(String id, Map<String, Object> result) {
        this.id = id;
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }
}

