package com.findu.negotiation.infrastructure.exception;

public enum ErrorCode {
    SUCCESS(200, "success"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "内部服务错误"),

    PROVIDER_ID_REQUIRED(40001, "providerId不能为空"),
    CUSTOMER_ID_REQUIRED(40002, "customerId不能为空"),
    DMS_SERVICE_ERROR(50001, "调用DMS服务失败"),
    USER_SERVICE_ERROR(50002, "调用User服务失败"),
    AGENT_SERVICE_ERROR(50003, "调用协商Agent服务失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
