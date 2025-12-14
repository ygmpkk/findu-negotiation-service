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

    @JsonProperty("description")
    private String description;

    @JsonProperty("price")
    private String price;

    @JsonProperty("works_id")
    private String worksId;

    public ServiceCard() {
    }

    public ServiceCard(String title, String description, String price, String worksId) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.worksId = worksId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getWorksId() {
        return worksId;
    }

    public void setWorksId(String worksId) {
        this.worksId = worksId;
    }
}

