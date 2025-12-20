package com.findu.negotiation.infrastructure.client.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @author timothy
 * @date 2025/12/20
 */
@ToString
@Data
public class ProviderProduct {
    /**
     * 服务卡片ID
     */
    public String worksId;

    /**
     * 服务卡片标题
     */
    public String title;

    /**
     * 服务卡片描述
     */
    public String description;

    public ExtendInfo extendInfo;
}
