package com.findu.negotiation.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PricingRuleVO {
    private final TimeRangeVO timeRange;
    private final int priceCents;
}
