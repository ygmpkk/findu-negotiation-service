package com.findu.negotiation.interfaces.controller;

import com.findu.negotiation.application.NegotiationBizService;
import com.findu.negotiation.domain.entity.NegotiationEntity;
import com.findu.negotiation.infrastructure.exception.BusinessException;
import com.findu.negotiation.infrastructure.exception.ErrorCode;
import com.findu.negotiation.interfaces.dto.CreateNegotiationRequest;
import com.findu.negotiation.interfaces.dto.ApiResponse;
import com.findu.negotiation.interfaces.dto.CreateNegotiationResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders_negotiation")
public class NegotiationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NegotiationController.class);

    @Autowired
    private NegotiationBizService negotiationBizService;

    @PostMapping("/create")
    public ApiResponse<CreateNegotiationResponse> create(@Valid @RequestBody CreateNegotiationRequest request) {
        LOGGER.info("创建协商请求: request={}", request);

        try {
            NegotiationEntity negotiationEntity = negotiationBizService.createNegotiation(
                    request.getProviderId(),
                    request.getCustomerId(),
                    request.getDemandId(),
                    request.getProductId());

            LOGGER.info("协商创建成功: entity={}", negotiationEntity);

            CreateNegotiationResponse response = CreateNegotiationResponse.createByDomain(negotiationEntity);
            return ApiResponse.success(response);
        } catch (BusinessException e) {
            LOGGER.error("创建协商失败，系统错误", e);
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("创建协商失败，未知错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "创建协商异常，请稍后重试");
        }
    }
}
