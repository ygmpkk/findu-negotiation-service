package com.findu.negotiation.infrastructure.client.dto.user;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 服务方用户信息
 *
 * @author timothy
 * @date 2025/12/20
 */
@ToString
@Data
public class ProviderData {
    private List<ProviderProduct> items;
}
