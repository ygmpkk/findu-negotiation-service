package com.findu.negotiation.interfaces.dto.mcp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class McpResponse {
    private String jsonrpc;
    private Object id;
    private Object result;
    private McpError error;
}
