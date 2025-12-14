package com.findu.negotiation.application.service.impl;

import com.findu.negotiation.application.service.NegotiationService;
import com.findu.negotiation.infrastructure.client.DmsClient;
import com.findu.negotiation.infrastructure.client.OrderNegotiationAgentClient;
import com.findu.negotiation.infrastructure.client.UserClient;
import com.findu.negotiation.infrastructure.client.dto.AgentCreateNegotiationRequest;
import com.findu.negotiation.infrastructure.client.dto.AgentCreateNegotiationResponse;
import com.findu.negotiation.infrastructure.client.dto.AgentProductInfo;
import com.findu.negotiation.infrastructure.util.PriceParser;
import com.findu.negotiation.interfaces.request.CreateNegotiationRequest;
import com.findu.negotiation.interfaces.response.CreateNegotiationResponse;
import com.findu.negotiation.interfaces.response.ProductInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NegotiationServiceImpl implements NegotiationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NegotiationServiceImpl.class);

    private final DmsClient dmsClient;
    private final UserClient userClient;
    private final OrderNegotiationAgentClient agentClient;

    public NegotiationServiceImpl(DmsClient dmsClient, UserClient userClient, OrderNegotiationAgentClient agentClient) {
        this.dmsClient = dmsClient;
        this.userClient = userClient;
        this.agentClient = agentClient;
    }

    @Override
    public CreateNegotiationResponse createNegotiation(CreateNegotiationRequest request, String authorization) {
        return createNegotiationWithAgent(request, authorization);
    }

    /**
     * 使用Agent服务创建协商草案
     */
    private CreateNegotiationResponse createNegotiationWithAgent(CreateNegotiationRequest request, String authorization) {
        LOGGER.info("使用Agent Completions服务创建协商草案");

        // 构建服务卡信息
        ServiceCard serviceCard = buildServiceCard(request, authorization);

        // 构建结果Schema
        ResultSchema resultSchema = buildResultSchema();

        // 构造Agent请求 (暂时使用空对话列表，后续可从IM系统获取)
        OrderNegotiationCompletionsRequest agentRequest = new OrderNegotiationCompletionsRequest(
                UUID.randomUUID().toString(), // 生成唯一请求ID
                new ArrayList<>(), // agent_conversations - 从需求对话获取
                new ArrayList<>(), // human_conversations - 从IM对话获取
                serviceCard,
                resultSchema
        );

        // 调用Agent服务
        OrderNegotiationCompletionsResponse agentResponse = agentClient.completions(agentRequest);

        // 转换为业务响应
        CreateNegotiationResponse response = new CreateNegotiationResponse();

        Map<String, Object> result = agentResponse.getResult();
        if (result != null) {
            response.setTitle((String) result.getOrDefault("title", ""));
            response.setPrice(parsePrice(result.get("price")));
            response.setContent((Map<String, Object>) result.getOrDefault("content", new HashMap<>()));

            // 转换产品列表
            Object productsObj = result.get("products");
            if (productsObj instanceof List) {
                List<ProductInfo> products = new ArrayList<>();
                for (Object item : (List<?>) productsObj) {
                    if (item instanceof Map) {
                        Map<?, ?> productMap = (Map<?, ?>) item;
                        products.add(new ProductInfo(
                                (String) productMap.get("id"),
                                (String) productMap.get("title"),
                                Boolean.TRUE.equals(productMap.get("is_selected"))
                        ));
                    }
                }
                response.setProducts(products);
            } else {
                response.setProducts(new ArrayList<>());
            }
        } else {
            // 如果result为空，返回默认值
            response.setTitle("");
            response.setPrice(0);
            response.setContent(new HashMap<>());
            response.setProducts(new ArrayList<>());
        }

        LOGGER.info("Agent Completions服务创建协商成功: title={}, price={}, productsCount={}",
                response.getTitle(), response.getPrice(),
                response.getProducts() != null ? response.getProducts().size() : 0);

        return response;
    }

    /**
     * 构建服务卡信息
     */
    private ServiceCard buildServiceCard(CreateNegotiationRequest request, String authorization) {
        ServiceCard serviceCard = new ServiceCard();

        // 如果有productId，从userClient获取服务信息
        if (request.getProductId() != null && !request.getProductId().isEmpty()) {
            try {
                List<Map<String, Object>> worksList = userClient.getProviderWorks(request.getProviderId(), authorization);
                for (Map<String, Object> work : worksList) {
                    String worksId = (String) work.get("worksId");
                    if (request.getProductId().equals(worksId)) {
                        serviceCard.setWorksId(worksId);
                        serviceCard.setTitle((String) work.get("title"));
                        serviceCard.setDescription((String) work.get("description"));
                        serviceCard.setPrice(extractExpectedPrice(work));
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("获取服务卡信息失败，productId={}", request.getProductId(), e);
            }
        }

        // 设置默认值
        if (serviceCard.getTitle() == null) {
            serviceCard.setTitle("");
        }
        if (serviceCard.getDescription() == null) {
            serviceCard.setDescription("");
        }
        if (serviceCard.getPrice() == null) {
            serviceCard.setPrice("0");
        }

        return serviceCard;
    }

    /**
     * 构建结果Schema
     */
    private ResultSchema buildResultSchema() {
        ResultSchema schema = new ResultSchema();
        schema.setType("object");

        Map<String, Object> properties = new HashMap<>();

        // title字段
        Map<String, Object> titleProp = new HashMap<>();
        titleProp.put("type", "string");
        titleProp.put("description", "订单标题");
        properties.put("title", titleProp);

        // price字段
        Map<String, Object> priceProp = new HashMap<>();
        priceProp.put("type", "integer");
        priceProp.put("description", "价格，单位：分");
        properties.put("price", priceProp);

        // content字段
        Map<String, Object> contentProp = new HashMap<>();
        contentProp.put("type", "object");
        contentProp.put("description", "扩展字段，K-V对描述订单详细条件");
        properties.put("content", contentProp);

        // products字段
        Map<String, Object> productsProp = new HashMap<>();
        productsProp.put("type", "array");
        productsProp.put("description", "候选服务列表");
        Map<String, Object> productItemProp = new HashMap<>();
        productItemProp.put("type", "object");
        Map<String, Object> productItemProps = new HashMap<>();
        productItemProps.put("id", Map.of("type", "string"));
        productItemProps.put("title", Map.of("type", "string"));
        productItemProps.put("is_selected", Map.of("type", "boolean"));
        productItemProp.put("properties", productItemProps);
        productsProp.put("items", productItemProp);
        properties.put("products", productsProp);

        schema.setProperties(properties);
        schema.setRequired(Arrays.asList("title", "price", "content", "products"));

        return schema;
    }

    /**
     * 解析价格为整数（分）
     */
    private int parsePrice(Object priceObj) {
        if (priceObj == null) {
            return 0;
        }
        if (priceObj instanceof Number) {
            return ((Number) priceObj).intValue();
        }
        if (priceObj instanceof String) {
            return PriceParser.parseToCents((String) priceObj);
        }
        return 0;
    }

    /**
     * 手动创建协商草案（原有逻辑）
     */
    private CreateNegotiationResponse createNegotiationManually(CreateNegotiationRequest request, String authorization) {
        LOGGER.info("使用手动逻辑创建协商草案");
        CreateNegotiationResponse response = new CreateNegotiationResponse();

        // 1. 获取title: 如果demandId存在，调用findu-dms获取description
        String title = "";
        if (request.getDemandId() != null && !request.getDemandId().isEmpty()) {
            try {
                String description = dmsClient.getDemandDescription(request.getCustomerId(), request.getDemandId(), authorization);
                if (description != null) {
                    title = description;
                }
            } catch (Exception e) {
                LOGGER.warn("获取需求描述失败，demandId={}", request.getDemandId(), e);
            }
        }
        response.setTitle(title);

        // 2. 设置content为空对象
        response.setContent(new HashMap<>());

        // 3. 获取products: 调用findu-user获取providerId的服务列表
        List<Map<String, Object>> worksList = new ArrayList<>();
        try {
            worksList = userClient.getProviderWorks(request.getProviderId(), authorization);
        } catch (Exception e) {
            LOGGER.warn("获取服务列表失败，providerId={}", request.getProviderId(), e);
        }

        // 4. 转换为ProductInfo列表，设置is_selected
        List<ProductInfo> products = new ArrayList<>();
        int price = 0;
        String selectedProductId = request.getProductId();

        for (Map<String, Object> work : worksList) {
            String worksId = (String) work.get("worksId");
            String workTitle = (String) work.get("title");
            boolean isSelected = worksId != null && worksId.equals(selectedProductId);

            products.add(new ProductInfo(worksId, workTitle, isSelected));

            // 5. 计算price: 从选中的product或第一个product的expectedPrice解析
            if (isSelected || (price == 0 && !products.isEmpty())) {
                String expectedPrice = extractExpectedPrice(work);
                int parsedPrice = PriceParser.parseToCents(expectedPrice);
                if (isSelected || price == 0) {
                    price = parsedPrice;
                }
            }
        }

        // 如果有productId但没有找到匹配的，设置第一个为选中
        if (selectedProductId == null || selectedProductId.isEmpty()) {
            if (!products.isEmpty()) {
                products.get(0).setSelected(true);
            }
        }

        response.setProducts(products);
        response.setPrice(price);

        LOGGER.info("协商创建成功: title={}, price={}, productsCount={}",
                title, price, products.size());

        return response;
    }

    @SuppressWarnings("unchecked")
    private String extractExpectedPrice(Map<String, Object> work) {
        Object extendInfoObj = work.get("extendInfo");
        if (extendInfoObj instanceof Map) {
            Map<String, Object> extendInfo = (Map<String, Object>) extendInfoObj;
            Object expectedPrice = extendInfo.get("expectedPrice");
            if (expectedPrice != null) {
                return expectedPrice.toString();
            }
        }
        return null;
    }
}
