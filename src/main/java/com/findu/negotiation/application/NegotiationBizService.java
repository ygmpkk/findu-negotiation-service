package com.findu.negotiation.application;

import com.findu.negotiation.domain.entity.NegotiationEntity;
import com.findu.negotiation.interfaces.dto.CreateNegotiationRequest;
import com.findu.negotiation.interfaces.dto.CreateNegotiationResponse;

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
