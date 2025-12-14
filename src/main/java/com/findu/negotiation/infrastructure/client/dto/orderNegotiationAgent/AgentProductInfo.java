package com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Agent服务返回的产品信息
 *
 * @author timothy
 * @date 2025/12/13
 */
public class AgentProductInfo {

    private String id;

    private String title;

    @JsonProperty("is_selected")
    private boolean isSelected;

    public AgentProductInfo() {
    }

    public AgentProductInfo(String id, String title, boolean isSelected) {
        this.id = id;
        this.title = title;
        this.isSelected = isSelected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}

