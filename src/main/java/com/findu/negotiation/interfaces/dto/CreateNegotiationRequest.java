package com.findu.negotiation.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * @author timothy
 * @date 2025/12/19
 */
@ToString
@Data
public class CreateNegotiationRequest {
    /**
     * 服务方ID
     */
    @NotBlank(message = "providerId不能为空")
    private String providerId;

    /**
     * 需求方ID
     */
    @NotBlank(message = "customerId不能为空")
    private String customerId;

    /**
     * 需求ID
     */
    private String demandId;

    /**
     * 产品ID
     */
    private String productId;
}
