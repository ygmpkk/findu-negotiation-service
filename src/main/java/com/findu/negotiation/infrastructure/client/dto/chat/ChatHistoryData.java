package com.findu.negotiation.infrastructure.client.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistoryData {

    @JsonProperty("user_a")
    private String userA;

    @JsonProperty("user_b")
    private String userB;

    @JsonProperty("msg_count")
    private Integer msgCount;

    @JsonProperty("complete")
    private Boolean complete;

    @JsonProperty("last_msg_key")
    private String lastMsgKey;

    @JsonProperty("messages")
    private List<Object> messages;
}
