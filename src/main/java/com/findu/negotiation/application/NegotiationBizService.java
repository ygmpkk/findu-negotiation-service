package com.findu.negotiation.application;

import com.findu.negotiation.domain.negotiation.entity.NegotiationEntity;

public interface NegotiationBizService {
    /**
     * 创建协商
     * @param providerId
     * @param customerId
     * @param demandId
     * @param productId
     * @return
     */
    NegotiationEntity createNegotiation(String providerId, String customerId, String demandId, String productId);
}
