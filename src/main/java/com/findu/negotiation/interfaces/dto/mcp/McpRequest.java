package com.findu.negotiation.interfaces.dto.mcp;

import lombok.Data;

import java.util.Map;

@Data
public class McpRequest {
    private String jsonrpc;
    private Object id;
    private String method;
    private Map<String, Object> params;
}
