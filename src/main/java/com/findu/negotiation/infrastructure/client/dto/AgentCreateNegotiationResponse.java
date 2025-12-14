package com.findu.negotiation.infrastructure.client.dto;

import java.util.List;
import java.util.Map;

/**
 * Agent服务返回的协商草案响应
 *
 * @author timothy
 * @date 2025/12/13
 */
public class AgentCreateNegotiationResponse {

    private String title;

    private Map<String, Object> content;

    private Integer price;

    private List<AgentProductInfo> products;

    public AgentCreateNegotiationResponse() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public void setContent(Map<String, Object> content) {
        this.content = content;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public List<AgentProductInfo> getProducts() {
        return products;
    }

    public void setProducts(List<AgentProductInfo> products) {
        this.products = products;
    }
}

