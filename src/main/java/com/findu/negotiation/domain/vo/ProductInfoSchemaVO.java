package com.findu.negotiation.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Product信息Schema VO - 用于构建Agent请求的ResultSchema中的products字段定义
 *
 * @author timothy
 * @date 2025/12/20
 */
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductInfoSchemaVO {
    /**
     * Schema类型，固定为"object"
     */
    private String type;

    /**
     * 属性定义
     */
    private Properties properties;

    /**
     * 必填字段列表
     */
    private List<String> required;

    /**
     * 构建默认的ProductInfo Schema
     * 包含id, title, is_selected三个字段
     */
    public static ProductInfoSchemaVO buildDefault() {
        ProductInfoSchemaVO schema = new ProductInfoSchemaVO();
        schema.setType("object");

        Properties properties = new Properties();
        properties.setId(new StringProperty("string", "产品ID"));
        properties.setTitle(new StringProperty("string", "产品标题"));
        properties.setIsSelected(new BooleanProperty("boolean", "是否选中该产品"));

        schema.setProperties(properties);

        return schema;
    }

    /**
     * 属性集合定义
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Properties {
        /**
         * 产品ID字段
         */
        private StringProperty id;

        /**
         * 产品标题字段
         */
        private StringProperty title;

        /**
         * 是否选中字段
         */
        @JsonProperty("is_selected")
        private BooleanProperty isSelected;
    }

    /**
     * 字符串类型属性
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StringProperty {
        /**
         * 类型
         */
        private String type;

        /**
         * 描述
         */
        private String description;
    }

    /**
     * 布尔类型属性
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BooleanProperty {
        /**
         * 类型
         */
        private String type;

        /**
         * 描述
         */
        private String description;
    }
}

