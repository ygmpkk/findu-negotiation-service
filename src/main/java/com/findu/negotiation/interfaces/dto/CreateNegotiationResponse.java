package com.findu.negotiation.interfaces.dto;

import com.findu.negotiation.domain.entity.NegotiationEntity;
import com.findu.negotiation.domain.vo.ProductInfoVO;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author timothy
 * @date 2025/12/19
 */
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateNegotiationResponse {
    /**
     * 标题
     */
    private String title;

    /**
     * 附加信息内容，KV结构
     */
    private Map<String, Object> content;

    /**
     * 价格，单位分
     */
    private int price;

    /**
     * 选择的产品列表
     */
    private List<ProductInfoVO> products;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
    private Date gmtModify;

    public static CreateNegotiationResponse createByDomain(NegotiationEntity entity) {
        CreateNegotiationResponse response = new CreateNegotiationResponse();
        response.setTitle(entity.getTitle());
        response.setContent(entity.getContent());
        response.setProducts(entity.getProducts());
        response.setPrice(entity.getPrice());
        response.setGmtCreate(entity.getGmtCreate());
        response.setGmtModify(entity.getGmtModify());
        return response;
    }
}
