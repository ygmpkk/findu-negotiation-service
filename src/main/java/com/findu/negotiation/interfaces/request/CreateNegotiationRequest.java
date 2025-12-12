package com.findu.negotiation.interfaces.request;

import jakarta.validation.constraints.NotBlank;

public class CreateNegotiationRequest {

    @NotBlank(message = "providerId不能为空")
    private String providerId;

    @NotBlank(message = "customerId不能为空")
    private String customerId;

    private String demandId;

    private String productId;

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
