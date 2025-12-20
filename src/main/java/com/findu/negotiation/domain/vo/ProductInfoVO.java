package com.findu.negotiation.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * @author timothy
 * @date 2025/12/19
 */
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductInfoVO {
    /**
     * 产品ID
     */
    private String id;

    /**
     * 产品标题
     */
    private String title;

    /**
     * 是否选择
     */
    @JsonProperty("is_selected")
    private boolean isSelected;
}
