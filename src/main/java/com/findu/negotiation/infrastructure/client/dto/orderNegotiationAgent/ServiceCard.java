package com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 服务卡信息
 *
 * @author timothy
 * @date 2025/12/14
 */
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCard {

    @JsonProperty("title")
    private String title;

    @JsonProperty("content")
    private String content;

    @JsonProperty("price")
    private Integer price;

    @JsonProperty("product_id")
    private String productId;
}

