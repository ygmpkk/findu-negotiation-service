package com.findu.negotiation.domain.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.findu.negotiation.domain.vo.NegotiationResultSchemaVO.*;

/**
 * ProductInfoSchemaVO 测试类 - 演示灵活构建任意层级的JsonSchema
 *
 * @author timothy
 * @date 2025/12/20
 */
public class ProductInfoSchemaVOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 测试构建默认的Schema
     */
    @Test
    public void testBuildDefault() throws Exception {
        NegotiationResultSchemaVO schema = NegotiationResultSchemaVO.buildDefault();
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println("=== 默认Schema ===");
        System.out.println(json);
    }

    /**
     * 测试构建简单的Schema（只有基本字段）
     */
    @Test
    public void testBuildSimpleSchema() throws Exception {
        NegotiationResultSchemaVO schema = builder()
                .type("object")
                .addProperty("name", stringProperty("用户名称"))
                .addProperty("age", numberProperty("年龄"))
                .addProperty("active", booleanProperty("是否激活"))
                .required(List.of("name"))
                .build();

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println("=== 简单Schema ===");
        System.out.println(json);
    }

    /**
     * 测试构建嵌套对象的Schema
     */
    @Test
    public void testBuildNestedObjectSchema() throws Exception {
        NegotiationResultSchemaVO schema = builder()
                .type("object")
                .addProperty("user", builder()
                        .type("object")
                        .addProperty("id", stringProperty("用户ID"))
                        .addProperty("profile", builder()
                                .type("object")
                                .addProperty("nickname", stringProperty("昵称"))
                                .addProperty("avatar", stringProperty("头像URL"))
                                .required(List.of("nickname"))
                                .build())
                        .required(List.of("id", "profile"))
                        .build())
                .addProperty("timestamp", numberProperty("时间戳"))
                .required(List.of("user"))
                .build();

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println("=== 嵌套对象Schema ===");
        System.out.println(json);
    }

    /**
     * 测试构建数组嵌套数组的Schema
     */
    @Test
    public void testBuildNestedArraySchema() throws Exception {
        NegotiationResultSchemaVO schema = builder()
                .type("object")
                .addProperty("matrix", arrayProperty(
                        arrayProperty(
                                numberProperty("矩阵元素")
                        )
                ))
                .addProperty("tags", arrayProperty(
                        stringProperty("标签")
                ))
                .build();

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println("=== 嵌套数组Schema ===");
        System.out.println(json);
    }

    /**
     * 测试构建复杂的电商订单Schema
     */
    @Test
    public void testBuildComplexEcommerceSchema() throws Exception {
        NegotiationResultSchemaVO schema = builder()
                .type("object")
                .addProperty("order_id", stringProperty("订单ID"))
                .addProperty("customer", builder()
                        .type("object")
                        .addProperty("id", stringProperty("客户ID"))
                        .addProperty("name", stringProperty("客户姓名"))
                        .addProperty("contact", builder()
                                .type("object")
                                .addProperty("phone", stringProperty("电话"))
                                .addProperty("email", stringProperty("邮箱"))
                                .build())
                        .required(List.of("id", "name"))
                        .build())
                .addProperty("items", arrayProperty(
                        builder()
                                .type("object")
                                .addProperty("product_id", stringProperty("商品ID"))
                                .addProperty("quantity", numberProperty("数量"))
                                .addProperty("price", numberProperty("单价"))
                                .addProperty("options", arrayProperty(
                                        builder()
                                                .type("object")
                                                .addProperty("name", stringProperty("选项名称"))
                                                .addProperty("value", stringProperty("选项值"))
                                                .required(List.of("name", "value"))
                                                .build()
                                ))
                                .required(List.of("product_id", "quantity", "price"))
                                .build()
                ))
                .addProperty("total_amount", numberProperty("总金额"))
                .addProperty("paid", booleanProperty("是否已支付"))
                .required(List.of("order_id", "customer", "items", "total_amount"))
                .build();

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println("=== 复杂电商订单Schema ===");
        System.out.println(json);
    }

    /**
     * 测试构建动态表单Schema
     */
    @Test
    public void testBuildDynamicFormSchema() throws Exception {
        NegotiationResultSchemaVO schema = builder()
                .type("object")
                .addProperty("form_id", stringProperty("表单ID"))
                .addProperty("fields", arrayProperty(
                        builder()
                                .type("object")
                                .addProperty("field_name", stringProperty("字段名"))
                                .addProperty("field_type", stringProperty("字段类型"))
                                .addProperty("validation", builder()
                                        .type("object")
                                        .addProperty("required", booleanProperty("是否必填"))
                                        .addProperty("min_length", numberProperty("最小长度"))
                                        .addProperty("max_length", numberProperty("最大长度"))
                                        .addProperty("pattern", stringProperty("正则表达式"))
                                        .build())
                                .addProperty("options", arrayProperty(
                                        stringProperty("选项值")
                                ))
                                .required(List.of("field_name", "field_type"))
                                .build()
                ))
                .required(List.of("form_id", "fields"))
                .build();

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        System.out.println("=== 动态表单Schema ===");
        System.out.println(json);
    }
}

