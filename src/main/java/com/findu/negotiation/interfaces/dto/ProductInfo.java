package com.findu.negotiation.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author timothy
 * @date 2025/12/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductInfo {
    private String id;

    private String title;

    @JsonProperty("is_selected")
    private boolean selected;
}
