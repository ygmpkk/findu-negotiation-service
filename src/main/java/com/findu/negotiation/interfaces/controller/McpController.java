package com.findu.negotiation.interfaces.controller;

import com.findu.negotiation.application.McpToolService;
import com.findu.negotiation.interfaces.dto.mcp.McpError;
import com.findu.negotiation.interfaces.dto.mcp.McpRequest;
import com.findu.negotiation.interfaces.dto.mcp.McpResponse;
import com.findu.negotiation.interfaces.dto.mcp.McpTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class McpController {

    private static final Logger LOGGER = LoggerFactory.getLogger(McpController.class);

    private final McpToolService mcpToolService;

    public McpController(McpToolService mcpToolService) {
        this.mcpToolService = mcpToolService;
    }

    @PostMapping(value = "/mcp", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public McpResponse handle(@RequestBody McpRequest request) {
        String method = request.getMethod();
        LOGGER.info("MCP请求: method={}, id={}", method, request.getId());
        try {
            if ("initialize".equals(method)) {
                return buildResponse(request, initializeResult());
            }
            if ("tools/list".equals(method)) {
                List<McpTool> tools = mcpToolService.listTools();
                return buildResponse(request, Map.of("tools", tools));
            }
            if ("tools/call".equals(method)) {
                return handleToolCall(request);
            }
            return buildError(request, -32601, "方法未找到");
        } catch (Exception ex) {
            LOGGER.error("MCP处理失败", ex);
            return buildError(request, -32603, ex.getMessage());
        }
    }

    private McpResponse handleToolCall(McpRequest request) {
        Map<String, Object> params = request.getParams();
        if (params == null) {
            return buildError(request, -32602, "缺少params");
        }
        String toolName = (String) params.get("name");
        if (toolName == null) {
            return buildError(request, -32602, "缺少工具名称");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", new HashMap<>());
        Object result = mcpToolService.callTool(toolName, arguments);
        return buildResponse(request, Map.of("result", result));
    }

    private Map<String, Object> initializeResult() {
        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("name", "findu-negotiation-feishu-calendar");
        serverInfo.put("version", "0.0.1");

        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("tools", Map.of("listChanged", false));

        Map<String, Object> result = new HashMap<>();
        result.put("serverInfo", serverInfo);
        result.put("capabilities", capabilities);
        return result;
    }

    private McpResponse buildResponse(McpRequest request, Object result) {
        return McpResponse.builder()
            .jsonrpc("2.0")
            .id(request.getId())
            .result(result)
            .build();
    }

    private McpResponse buildError(McpRequest request, int code, String message) {
        return McpResponse.builder()
            .jsonrpc("2.0")
            .id(request.getId())
            .error(McpError.builder().code(code).message(message).build())
            .build();
    }
}
