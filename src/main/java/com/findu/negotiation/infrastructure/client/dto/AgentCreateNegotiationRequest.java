package com.findu.negotiation.infrastructure.client.dto;

/**
 * 请求协商Agent服务创建协商草案的请求体
 *
 * @author timothy
 * @date 2025/12/13
 */
public class AgentCreateNegotiationRequest {

    private String providerId;

    private String customerId;

    private String demandId;

    private String productId;

    public AgentCreateNegotiationRequest() {
    }

    public AgentCreateNegotiationRequest(String providerId, String customerId, String demandId, String productId) {
        this.providerId = providerId;
        this.customerId = customerId;
        this.demandId = demandId;
        this.productId = productId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getDemandId() {
        return demandId;
    }

    public void setDemandId(String demandId) {
        this.demandId = demandId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}



