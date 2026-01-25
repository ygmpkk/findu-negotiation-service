package com.findu.negotiation.application;

import com.findu.negotiation.domain.negotiation.entity.NegotiationEntity;
import com.findu.negotiation.domain.negotiation.vo.NegotiationResultSchemaVO;
import com.findu.negotiation.domain.negotiation.vo.ProductInfoVO;
import com.findu.negotiation.infrastructure.client.ChatClient;
import com.findu.negotiation.infrastructure.client.DmsClient;
import com.findu.negotiation.infrastructure.client.OrderNegotiationAgentClient;
import com.findu.negotiation.infrastructure.client.UserClient;
import com.findu.negotiation.infrastructure.client.dto.chat.ChatHistoryResponse;
import com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent.ConversationItem;
import com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent.OrderNegotiationCompletionsRequest;
import com.findu.negotiation.infrastructure.client.dto.orderNegotiationAgent.OrderNegotiationCompletionsResponse;
import com.findu.negotiation.infrastructure.client.dto.user.ProviderProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NegotiationBizServiceImpl implements NegotiationBizService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NegotiationBizServiceImpl.class);

    @Autowired
    private DmsClient dmsClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private OrderNegotiationAgentClient agentClient;
    @Autowired
    private ChatClient chatClient;

    @Override
    public NegotiationEntity createNegotiation(String providerId, String customerId, String demandId, String productId) {
        NegotiationEntity entity = NegotiationEntity.builder()
                .providerId(providerId)
                .customerId(customerId)
                .demandId(demandId)
                .productId(productId)
                .build();
        try {
            return createNegotiationWithAgent(entity);
        } catch (Exception e) {
            LOGGER.warn("Agent服务调用失败，降级到手动逻辑: {}", e.getMessage(), e);
            return buildNormalResult(entity, null);
        }
    }

    /**
     * 使用Agent服务创建协商草案
     */
    private NegotiationEntity createNegotiationWithAgent(NegotiationEntity entity) {
        LOGGER.info("使用Agent Completions服务创建协商草案");

        // 1. 获取provider的服务卡片
        List<ProviderProduct> providerWorks = userClient.getProviderWorks(entity.getProviderId());
        List<ProductInfoVO> productInfoVOS = new ArrayList<>();
        for (ProviderProduct product : providerWorks) {
            ProductInfoVO productInfoVO = new ProductInfoVO();
            productInfoVO.setId(product.getWorksId());
            productInfoVO.setTitle(product.getTitle());
            productInfoVO.setDescription(product.getContent());
            productInfoVO.setPrice(product.getExtendInfo().getParsedPrice());
            productInfoVOS.add(productInfoVO);
        }

        // 2. TODO 获取 customer agent 历史对话
        // 获取用户的需求

        // 3. 获取 IM 历史对话
        // 获取用户和服务方的协商条款
        ChatHistoryResponse chatHistoryResponse = chatClient.getChatHistory(entity.getProviderId(), entity.getCustomerId());
        List<ConversationItem> humanConversations = new ArrayList<>();
        if (chatHistoryResponse != null && chatHistoryResponse.getData() != null) {
            for (var msg : chatHistoryResponse.getData().getMessages()) {
                ConversationItem item = new ConversationItem();
                item.setTimestamp(msg.getMsgTime());
                item.setSender(Objects.equals(msg.getFrom(), entity.getProviderId()) ? "服务方" : "用户");

                for (var content : msg.getContent()) {
                    if (content.isTextContent()) {
                        item.setContent(content.getContentAsText());
                        humanConversations.add(item);
                        break;
                    } else if (content.isDemandCardContent()) {
                        // Handle demand card content - extract meaningful information
                        var demandCard = content.getContentAsDemandCard();
                        if (demandCard != null) {
                            StringBuilder demandInfo = new StringBuilder();
                            demandInfo.append("[需求卡片] ");
                            if (demandCard.getDemandTitle() != null) {
                                demandInfo.append(demandCard.getDemandTitle()).append(": ");
                            }
                            if (demandCard.getLocation() != null) {
                                demandInfo.append("地点: ").append(demandCard.getLocation()).append(", ");
                            }
                            if (demandCard.getBudget() != null) {
                                demandInfo.append("预算: ").append(demandCard.getBudget());
                            }
                            item.setContent(demandInfo.toString());
                            humanConversations.add(item);
                            break;
                        }
                    }
                }
            }
        }

        LOGGER.info("获取IM历史对话完成: 对话记录={}", humanConversations
                .stream()
                .map(conversationItem -> "[" + conversationItem.getSender() + "]: " + conversationItem.getContent())
                .collect(Collectors.joining("\n")));

        // 4. 调用Agent，根据对话选取最合适的服务 product

        // 构建结果Schema
        NegotiationResultSchemaVO resultSchemaVO = NegotiationResultSchemaVO.buildDefault();

        // 构造Agent请求 (暂时使用空对话列表，后续可从IM系统获取)
        OrderNegotiationCompletionsRequest agentRequest = new OrderNegotiationCompletionsRequest(
                UUID.randomUUID().toString(), // 生成唯一请求ID
                // agent_conversations - 从需求对话获取
                new ArrayList<>(),
                // human_conversations - 从IM对话获取
                humanConversations,
                productInfoVOS,
                resultSchemaVO
        );

        // 调用Agent服务
        OrderNegotiationCompletionsResponse agentResponse = agentClient.completions(agentRequest);

        if (null == agentResponse.getResult()) {
            // 如果Agent返回空，使用默认的降级逻辑
            return buildNormalResult(entity, productInfoVOS);
        }

        entity.setTitle(agentResponse.getResult().getTitle());
        entity.setContent(agentResponse.getResult().getContent());

        for (ProductInfoVO product :productInfoVOS) {
            // 找到和 productInfoVOS 匹配的 product id
            if (agentResponse.getResult().getProducts().stream().anyMatch(p -> product.getId().equals(p.getId()))) {
                product.setSelected(true);
                break;
            }
        }
        entity.setProducts(productInfoVOS);
        entity.setPrice(agentResponse.getResult().getPrice());

        LOGGER.info("Agent Completions服务创建协商成功: title={}, price={}, productsCount={}",
                entity.getTitle(), entity.getPrice(),
                entity.getProducts() != null ? entity.getProducts().size() : 0);

        return entity;
    }

    private NegotiationEntity buildNormalResult(NegotiationEntity entity, List<ProductInfoVO> productInfoVOS) {
        // 降级默认使用第一个产品
        if (null != productInfoVOS && !productInfoVOS.isEmpty()) {
            entity.setPrice(productInfoVOS.getFirst().getPrice());
            entity.getProducts().getFirst().setSelected(true);
        }

        return entity;
    }
}
