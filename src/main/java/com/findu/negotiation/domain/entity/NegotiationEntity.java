package com.findu.negotiation.domain.entity;

import com.findu.negotiation.domain.vo.NegotiationResultVO;
import com.findu.negotiation.domain.vo.ProductInfoVO;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 协商实体类
 *
 * @author timothy
 * @date 2025/12/19
 */
@ToString
@Data
@Builder
public class NegotiationEntity {
    /**
     * 协商ID - 主键
     */
    private String id;

    /**
     * 服务方ID
     */
    private String providerId;

    /**
     * 需求方ID
     */
    private String customerId;

    /**
     * 需求ID
     */
    private String demandId;

    /**
     * 产品ID
     */
    private String productId;

    /**
     * 标题
     */
    private String title;

    /**
     * 协商内容
     */
    private Map<String, Object> content;

    /**
     * 价格(单位分)
     */
    private int price;

    /**
     * 产品信息
     */
    public List<ProductInfoVO> products;

    /**
     * 协商结果
     */
    public NegotiationResultVO result;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
    private Date gmtModify;
}
