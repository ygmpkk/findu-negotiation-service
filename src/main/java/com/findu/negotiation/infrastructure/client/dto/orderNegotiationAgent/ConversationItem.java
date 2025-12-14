package com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 对话项
 *
 * @author timothy
 * @date 2025/12/14
 */
public class ConversationItem {

    @JsonProperty("role")
    private String role;

    @JsonProperty("content")
    private String content;

    @JsonProperty("timestamp")
    private Long timestamp;

    public ConversationItem() {
    }

    public ConversationItem(String role, String content, Long timestamp) {
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}



