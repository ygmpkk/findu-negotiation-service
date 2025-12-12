package com.findu.negotiation.application.service.impl;

import com.findu.negotiation.application.service.NegotiationService;
import com.findu.negotiation.infrastructure.client.DmsClient;
import com.findu.negotiation.infrastructure.client.UserClient;
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

    public NegotiationServiceImpl(DmsClient dmsClient, UserClient userClient) {
        this.dmsClient = dmsClient;
        this.userClient = userClient;
    }

    @Override
    public CreateNegotiationResponse createNegotiation(CreateNegotiationRequest request, String authorization) {
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
