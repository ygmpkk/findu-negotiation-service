package com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 对话项
 *
 * @author timothy
 * @date 2025/12/14
 */
public class ConversationItem {

    @JsonProperty("sender")
    private String sender;

    @JsonProperty("content")
    private String content;

    @JsonProperty("timestamp")
    private Long timestamp;

    public ConversationItem() {
    }

    public ConversationItem(String sender, String content, Long timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
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



