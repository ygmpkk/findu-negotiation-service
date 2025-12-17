package com.findu.negotiation.interfaces.controller;

import com.findu.negotiation.application.service.NegotiationService;
import com.findu.negotiation.interfaces.request.CreateNegotiationRequest;
import com.findu.negotiation.interfaces.response.ApiResponse;
import com.findu.negotiation.interfaces.response.CreateNegotiationResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders_negotiation")
public class NegotiationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NegotiationController.class);

    private final NegotiationService negotiationService;

    public NegotiationController(NegotiationService negotiationService) {
        this.negotiationService = negotiationService;
    }

    @PostMapping("/create")
    public ApiResponse<CreateNegotiationResponse> create(
            @Valid @RequestBody CreateNegotiationRequest request) {

        LOGGER.info("创建协商请求: providerId={}, customerId={}, demandId={}, productId={}",
                request.getProviderId(), request.getCustomerId(),
                request.getDemandId(), request.getProductId());

        CreateNegotiationResponse response = negotiationService.createNegotiation(request);

        return ApiResponse.success(response);
    }
}
