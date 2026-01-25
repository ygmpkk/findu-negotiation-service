package com.findu.negotiation.interfaces.dto.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * 位置信息请求
 *
 * @author timothy
 * @date 2026/01/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class LocationRequest {

    /**
     * 位置名称
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("name")
    private String name;

    /**
     * 详细地址
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("address")
    private String address;

    /**
     * 纬度
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("latitude")
    private Double latitude;

    /**
     * 经度
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("longitude")
    private Double longitude;
}
