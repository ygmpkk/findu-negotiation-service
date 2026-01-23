package com.findu.negotiation.interfaces.dto.mcp;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class McpTool {
    private String name;
    private String description;
    private Map<String, Object> inputSchema;
}
