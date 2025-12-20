package com.findu.negotiation.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用JsonSchema VO - 支持灵活定义任意层级的Schema结构
 *
 * @author timothy
 * @date 2025/12/20
 */
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NegotiationResultSchemaVO {
    /**
     * Schema类型：object, array, string, number, boolean, null
     */
    private String type;

    /**
     * 属性定义（当type为object时使用）
     */
    private Map<String, NegotiationResultSchemaVO> properties;

    /**
     * 必填字段列表（当type为object时使用）
     */
    private List<String> required;

    /**
     * 数组元素定义（当type为array时使用）
     */
    private NegotiationResultSchemaVO items;

    /**
     * 字段描述
     */
    private String description;

    /**
     * 构建默认的ProductInfo Schema
     * 包含title, content, price, products字段
     */
    public static NegotiationResultSchemaVO buildDefault() {
        return builder()
                .type("object")
                .addProperty("title", stringProperty("订单标题，一般为服务名称或交易主题"))
                .addProperty("content", objectProperty(null, "协商内容，双方约定的条款，如服务描述、预约时间、场地、规格等等"))
                .addProperty("price", numberProperty("价格"))
                .addProperty("products", arrayProperty(
                        builder()
                                .type("object")
                                .addProperty("id", stringProperty("产品ID"))
                                .addProperty("title", stringProperty("产品标题"))
                                .addProperty("is_selected", booleanProperty("是否选中该产品"))
                                .required(List.of("id", "title", "is_selected"))
                                .build()
                ))
                .required(List.of("title", "content", "price", "products"))
                .build();
    }

    /**
     * 创建Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 创建string类型属性
     */
    public static NegotiationResultSchemaVO stringProperty(String description) {
        NegotiationResultSchemaVO schema = new NegotiationResultSchemaVO();
        schema.setType("string");
        schema.setDescription(description);
        return schema;
    }

    /**
     * 创建number类型属性
     */
    public static NegotiationResultSchemaVO numberProperty(String description) {
        NegotiationResultSchemaVO schema = new NegotiationResultSchemaVO();
        schema.setType("number");
        schema.setDescription(description);
        return schema;
    }

    /**
     * 创建boolean类型属性
     */
    public static NegotiationResultSchemaVO booleanProperty(String description) {
        NegotiationResultSchemaVO schema = new NegotiationResultSchemaVO();
        schema.setType("boolean");
        schema.setDescription(description);
        return schema;
    }

    /**
     * 创建object类型属性
     */
    public static NegotiationResultSchemaVO objectProperty(Map<String, NegotiationResultSchemaVO> properties, String description) {
        NegotiationResultSchemaVO schema = new NegotiationResultSchemaVO();
        schema.setType("object");
        if (null != description) {
            schema.setDescription(description);
        }
        schema.setProperties(properties);
        return schema;
    }

    /**
     * 创建array类型属性
     */
    public static NegotiationResultSchemaVO arrayProperty(NegotiationResultSchemaVO items) {
        NegotiationResultSchemaVO schema = new NegotiationResultSchemaVO();
        schema.setType("array");
        schema.setItems(items);
        return schema;
    }

    /**
     * Builder类，用于流式构建Schema
     */
    public static class Builder {
        private final NegotiationResultSchemaVO schema;

        public Builder() {
            this.schema = new NegotiationResultSchemaVO();
        }

        public Builder type(String type) {
            schema.setType(type);
            return this;
        }

        public Builder description(String description) {
            schema.setDescription(description);
            return this;
        }

        public Builder addProperty(String name, NegotiationResultSchemaVO property) {
            if (schema.getProperties() == null) {
                schema.setProperties(new HashMap<>());
            }
            schema.getProperties().put(name, property);
            return this;
        }

        public Builder properties(Map<String, NegotiationResultSchemaVO> properties) {
            schema.setProperties(properties);
            return this;
        }

        public Builder required(List<String> required) {
            schema.setRequired(required);
            return this;
        }

        public Builder items(NegotiationResultSchemaVO items) {
            schema.setItems(items);
            return this;
        }

        public NegotiationResultSchemaVO build() {
            return schema;
        }
    }
}

