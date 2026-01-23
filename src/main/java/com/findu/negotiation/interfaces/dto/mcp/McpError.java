package com.findu.negotiation.interfaces.dto.mcp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class McpError {
    private int code;
    private String message;
    private Object data;
}
