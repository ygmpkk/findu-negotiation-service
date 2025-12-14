package com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * 订单数据结构的 JSON Schema
 *
 * @author timothy
 * @date 2025/12/14
 */
public class ResultSchema {

    @JsonProperty("type")
    private String type;

    @JsonProperty("properties")
    private Map<String, Object> properties;

    @JsonProperty("required")
    private java.util.List<String> required;

    public ResultSchema() {
    }

    public ResultSchema(String type, Map<String, Object> properties, java.util.List<String> required) {
        this.type = type;
        this.properties = properties;
        this.required = required;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public java.util.List<String> getRequired() {
        return required;
    }

    public void setRequired(java.util.List<String> required) {
        this.required = required;
    }
}

