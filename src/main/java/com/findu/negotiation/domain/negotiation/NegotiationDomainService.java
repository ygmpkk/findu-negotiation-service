package com.findu.negotiation.domain.negotiation;

import com.findu.negotiation.domain.negotiation.entity.NegotiationEntity;

/**
 * @author timothy
 * @date 2025/12/19
 */
public interface NegotiationDomainService {
    /**
     * 创建协商
     * @param negotiation
     * @return
     */
    NegotiationEntity createNegotiation(NegotiationEntity negotiation);
}
