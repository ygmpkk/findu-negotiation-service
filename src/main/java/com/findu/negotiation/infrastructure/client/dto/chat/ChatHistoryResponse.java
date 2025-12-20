package com.findu.negotiation.infrastructure.client.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistoryResponse {
    private Boolean success;
    private String message;
    private ChatHistoryData data;
}
