package com.findu.negotiation.application.service;

import com.findu.negotiation.interfaces.request.CreateNegotiationRequest;
import com.findu.negotiation.interfaces.response.CreateNegotiationResponse;

public interface NegotiationService {

    CreateNegotiationResponse createNegotiation(CreateNegotiationRequest request);
}
