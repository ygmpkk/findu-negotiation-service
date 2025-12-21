package com.findu.negotiation.infrastructure.client.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * POI (Point of Interest) 信息
 * @author timothy
 * @date 2025/12/21
 */
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PoiInfo {
    /**
     * POI地址
     */
    private String address;

    /**
     * POI名称
     */
    private String poiName;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * POI ID
     */
    private String poiId;

    /**
     * 经度
     */
    private Double longitude;
}

