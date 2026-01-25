package com.findu.negotiation.domain.negotiation.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * 通用协商结果VO - 支持灵活定义任意字段结构
 * 可以动态添加任何字段，适配不同的协商场景
 *
 * @author timothy
 * @date 2025/12/19
 */
@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NegotiationResultVO {
    private String id;
    private String title;
    private int price;
    private Map<String, Object> content;
    private List<ProductInfoVO> products;
}
