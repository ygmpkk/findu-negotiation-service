package com.findu.negotiation.infrastructure.client.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.findu.negotiation.infrastructure.util.PriceParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author timothy
 * @date 2025/12/20
 */
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtendInfo {
    public String pois;

    /**
     * 预期价格，示例：300元\/小时
     */
    @JsonProperty("expected_price")
    public String expectedPrice;

    @JsonProperty("service_location")
    public String serviceLocation;

    @JsonProperty("service_method")
    public String serviceMethod;

    /**
     * 获取解析后的价格，单位分
     * @return 价格
     */
    public int getParsedPrice() {
        if (expectedPrice == null) {
            return 0;
        }

        return PriceParser.parseToCents(expectedPrice);
    }
}
