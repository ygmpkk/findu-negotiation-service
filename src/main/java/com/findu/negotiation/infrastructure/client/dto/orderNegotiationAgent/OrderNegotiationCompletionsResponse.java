package com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.findu.negotiation.domain.negotiation.vo.NegotiationResultVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Order Negotiation Completions 响应体
 *
 * @author timothy
 * @date 2025/12/14
 */
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderNegotiationCompletionsResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("result")
    private NegotiationResultVO result;
}

