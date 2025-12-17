package com.findu.negotiation.application.service;

import com.findu.negotiation.interfaces.request.CreateNegotiationRequest;
import com.findu.negotiation.interfaces.response.CreateNegotiationResponse;

public interface NegotiationService {

    /**
     * 创建协商草案（从RequestContext自动获取Authorization）
     */
    CreateNegotiationResponse createNegotiation(CreateNegotiationRequest request);
}
