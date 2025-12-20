package com.findu.negotiation.domain.entity;

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
