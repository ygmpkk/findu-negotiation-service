package com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 对话项
 *
 * @author timothy
 * @date 2025/12/14
 */
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationItem {

    @JsonProperty("sender")
    private String sender;

    @JsonProperty("content")
    private String content;

    @JsonProperty("timestamp")
    private Long timestamp;
}



