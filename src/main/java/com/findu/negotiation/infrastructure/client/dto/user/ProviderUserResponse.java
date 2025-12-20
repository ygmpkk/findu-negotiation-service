package com.findu.negotiation.infrastructure.client.dto.user;

import com.findu.negotiation.interfaces.dto.ApiResponse;
import lombok.*;

/**
 * 服务方用户信息
 *
 * @author timothy
 * @date 2025/12/20
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@Data
public class ProviderUserResponse extends ApiResponse<ProviderData> {
    private ProviderData data;
}
