package com.findu.negotiation.interfaces.dto.calendar;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PricingRuleRequest {
    @NotBlank(message = "startTime不能为空")
    private String startTime;

    @NotBlank(message = "endTime不能为空")
    private String endTime;

    @Min(value = 0, message = "priceCents不能小于0")
    private int priceCents;
}
