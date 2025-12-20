package com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * 订单数据结构的 JSON Schema
 *
 * @author timothy
 * @date 2025/12/14
 */
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultSchema {

    @JsonProperty("type")
    private String type;

    @JsonProperty("properties")
    private Map<String, Object> properties;

    @JsonProperty("required")
    private java.util.List<String> required;
}

