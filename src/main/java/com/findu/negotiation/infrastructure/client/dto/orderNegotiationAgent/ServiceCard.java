package com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 服务卡信息
 *
 * @author timothy
 * @date 2025/12/14
 */
public class ServiceCard {

    @JsonProperty("title")
    private String title;

    @JsonProperty("content")
    private String content;

    @JsonProperty("price")
    private Integer price;

    @JsonProperty("product_id")
    private String productId;

    public ServiceCard() {
    }

    public ServiceCard(String title, String content, Integer price, String productId) {
        this.title = title;
        this.content = content;
        this.price = price;
        this.productId = productId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}

