package com.findu.negotiation.interfaces.dto.calendar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * 创建日历请求
 *
 * @author timothy
 * @date 2026/01/25
 */
@ToString
@Data
public class CreateCalendarRequest {

    /**
     * 服务提供方ID
     */
    @NotBlank(message = "providerId不能为空")
    private String providerId;

    /**
     * 日历名称
     */
    @NotBlank(message = "name不能为空")
    @Size(max = 128, message = "name长度不能超过128")
    private String name;

    /**
     * 日历描述
     */
    @Size(max = 512, message = "description长度不能超过512")
    private String description;

    /**
     * 时区
     */
    private String timezone;
}
