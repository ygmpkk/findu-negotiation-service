package com.findu.negotiation.infrastructure.context;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求上下文，存储请求级别的数据
 *
 * @author timothy
 */
public class RequestContext {

    private String authorization;
    private String userId;
    private String traceId;
    private Map<String, String> customHeaders;

    public RequestContext() {
        this.customHeaders = new HashMap<>();
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Map<String, String> getCustomHeaders() {
        return customHeaders;
    }

    public void addCustomHeader(String key, String value) {
        this.customHeaders.put(key, value);
    }

    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }

    @Override
    public String toString() {
        return "RequestContext{" +
                "authorization='" + (authorization != null ? "***" : null) + '\'' +
                ", userId='" + userId + '\'' +
                ", traceId='" + traceId + '\'' +
                ", customHeaders=" + customHeaders +
                '}';
    }
}


