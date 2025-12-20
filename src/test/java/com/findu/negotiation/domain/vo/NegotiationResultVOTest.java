package com.findu.negotiation.domain.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * NegotiationResultVO 测试类 - 演示灵活的字段定义
 *
 * @author timothy
 * @date 2025/12/20
 */
public class NegotiationResultVOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 测试构建默认的产品协商结果
     */
    @Test
    public void testBuildProductResult() throws Exception {
        Map<String, Object> content = new HashMap<>();
        content.put("service_type", "摄影服务");
        content.put("location", "北京朝阳区");
        content.put("duration", "2小时");

        List<ProductInfoVO> products = Arrays.asList(
                new ProductInfoVO("prod1", "婚礼摄影", null, 50000, false),
                new ProductInfoVO("prod2", "活动摄影", null, 20000,true),
                new ProductInfoVO("prod3", "商业摄影", null, 30000, false)
        );

        NegotiationResultVO result = NegotiationResultVO.builder()
                .title("专业摄影服务")
                .content(content)
                .price(50000)
                .products(products)
                .build();

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        System.out.println("=== 产品协商结果 ===");
        System.out.println(json);
    }

    /**
     * 测试使用Builder构建结果
     */
    @Test
    public void testBuilderPattern() throws Exception {
        Map<String, Object> content = new HashMap<>();
        content.put("deadline", "2025-12-31");
        content.put("quality", "高清");

        List<ProductInfoVO> products = Arrays.asList(
                new ProductInfoVO("video1", "短视频制作", null, 100000, true)
        );

        NegotiationResultVO result = NegotiationResultVO.builder()
                .title("视频制作服务")
                .content(content)
                .price(100000)
                .products(products)
                .build();

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        System.out.println("=== Builder模式构建结果 ===");
        System.out.println(json);
    }

    /**
     * 测试从JSON反序列化
     */
    @Test
    public void testDeserializeFromJson() throws Exception {
        String json = """
            {
                "title": "咨询服务",
                "content": {
                    "topic": "技术咨询",
                    "hours": 2
                },
                "price": 30000,
                "products": [
                    {
                        "id": "cons1",
                        "title": "技术咨询",
                        "price": 30000,
                        "is_selected": true
                    }
                ],
                "extra_field": "这是额外的字段",
                "custom_data": {
                    "key1": "value1",
                    "key2": 123
                }
            }
            """;

        NegotiationResultVO result = objectMapper.readValue(json, NegotiationResultVO.class);

        System.out.println("=== 反序列化结果 ===");
        System.out.println("Title: " + result.getTitle());
        System.out.println("Price: " + result.getPrice());
        System.out.println("Products: " + result.getProducts());
//        System.out.println("Extra Field: " + result.get("extra_field"));
//        System.out.println("Custom Data: " + result.get("custom_data"));

        String serialized = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        System.out.println("\n序列化回JSON:");
        System.out.println(serialized);
    }
}