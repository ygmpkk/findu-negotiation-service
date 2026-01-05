package com.findu.negotiation.infrastructure.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * HTTP请求响应日志过滤器
 * 记录请求路径、方法、请求头、请求体、响应体、处理时间
 *
 * @author timothy
 * @date 2026/01/05
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class HttpLoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpLoggingFilter.class);

    private static final int MAX_PAYLOAD_LENGTH = 10000; // 最大记录长度
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    // 不需要记录body的路径
    private static final Set<String> EXCLUDED_PATHS = new HashSet<>(Arrays.asList(
            "/actuator", "/health", "/metrics", "/favicon.ico"
    ));

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        String requestPath = httpRequest.getRequestURI();

        // 对于某些路径跳过详细日志
        if (shouldSkipLogging(requestPath)) {
            chain.doFilter(request, response);
            return;
        }

        // 包装请求和响应以便可以多次读取body
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();

        try {
            // 记录请求信息
            logRequest(requestWrapper);

            // 继续执行
            chain.doFilter(requestWrapper, responseWrapper);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // 记录响应信息
            logResponse(requestWrapper, responseWrapper, duration);

            // 将响应体写回客户端（重要！）
            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * 记录请求信息
     */
    private void logRequest(ContentCachingRequestWrapper request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        String traceId = request.getHeader(TRACE_ID_HEADER);
        String ipAddr = getClientIpAddress(request);

//        Map<String, String> headers = getHeaders(request);
        String requestBody = getRequestBody(request);

        StringBuilder logMessage = new StringBuilder("type=request_in||");
        logMessage.append("request_id=").append(requestId).append("||");
        logMessage.append("trace_id=").append(traceId).append("||");
        logMessage.append("ip=").append(ipAddr).append("||");
        logMessage.append("method=").append(method).append("||");
        logMessage.append("path=").append(path).append("||");
        logMessage.append("args=").append(queryString).append("||");
//        logMessage.append("headers=").append(formatHeaders(headers)).append("||");
        logMessage.append("body=").append(requestBody);

        LOGGER.info(logMessage.toString());
    }

    /**
     * 记录响应信息
     */
    private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        int status = response.getStatus();
        String responseBody = getResponseBody(response);
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        String traceId = request.getHeader(TRACE_ID_HEADER);
        String method = request.getMethod();
        String path = request.getRequestURI();
        String ipAddr = getClientIpAddress(request);

//        Map<String, String> headers = getResponseHeaders(response);

        StringBuilder logMessage = new StringBuilder("type=request_out||");
        logMessage.append("request_id=").append(requestId).append("||");
        logMessage.append("trace_id=").append(traceId).append("||");
        logMessage.append("ip=").append(ipAddr).append("||");
        logMessage.append("method=").append(method).append("||");
        logMessage.append("path=").append(path).append("||");
        logMessage.append("status_code=").append(status).append("||");
//        logMessage.append("headers=").append(formatHeaders(headers)).append("||");
        logMessage.append("resp=").append(responseBody).append("||");
        logMessage.append("proc_time=").append(duration);

        LOGGER.info(logMessage.toString());
    }

    /**
     * 获取请求头
     */
    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            // 脱敏处理敏感信息
            if (headerName.equalsIgnoreCase("Authorization")
                    || headerName.equalsIgnoreCase("Cookie")
                    || headerName.equalsIgnoreCase("token")) {
                headerValue = maskSensitiveData(headerValue);
            }

            headers.put(headerName, headerValue);
        }

        return headers;
    }

    /**
     * 获取响应头
     */
    private Map<String, String> getResponseHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();

        for (String headerName : response.getHeaderNames()) {
            String headerValue = response.getHeader(headerName);
            headers.put(headerName, headerValue);
        }

        return headers;
    }

    /**
     * 格式化请求头
     */
    private String formatHeaders(Map<String, String> headers) {
        if (headers.isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder("{");
        headers.forEach((key, value) -> {
            sb.append(key).append("=").append(value).append(", ");
        });

        // 移除最后的逗号和空格
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2);
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * 获取请求体内容
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length == 0) {
            return null;
        }

        int length = Math.min(buf.length, MAX_PAYLOAD_LENGTH);
        try {
            String encoding = request.getCharacterEncoding();
            if (encoding == null || encoding.isEmpty()) {
                encoding = StandardCharsets.UTF_8.name();
            }
            String payload = new String(buf, 0, length, encoding);
            if (buf.length > MAX_PAYLOAD_LENGTH) {
                payload += "... (truncated)";
            }
            // 将换行符、回车符、制表符替换为空格，保持单行
            return payload.replaceAll("[\\r\\n\\t]+", " ");
        } catch (UnsupportedEncodingException e) {
            String payload = new String(buf, 0, length, StandardCharsets.UTF_8);
            if (buf.length > MAX_PAYLOAD_LENGTH) {
                payload += "... (truncated)";
            }
            return payload.replaceAll("[\\r\\n\\t]+", " ");
        }
    }

    /**
     * 获取响应体内容
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] buf = response.getContentAsByteArray();
        if (buf.length == 0) {
            return null;
        }

        int length = Math.min(buf.length, MAX_PAYLOAD_LENGTH);
        try {
            String encoding = response.getCharacterEncoding();
            if (encoding == null || encoding.isEmpty()) {
                encoding = StandardCharsets.UTF_8.name();
            }
            String payload = new String(buf, 0, length, encoding);
            if (buf.length > MAX_PAYLOAD_LENGTH) {
                payload += "... (truncated)";
            }
            // 将换行符、回车符、制表符替换为空格，保持单行
            return payload.replaceAll("[\\r\\n\\t]+", " ");
        } catch (UnsupportedEncodingException e) {
            String payload = new String(buf, 0, length, StandardCharsets.UTF_8);
            if (buf.length > MAX_PAYLOAD_LENGTH) {
                payload += "... (truncated)";
            }
            return payload.replaceAll("[\\r\\n\\t]+", " ");
        }
    }

    /**
     * 脱敏处理敏感数据
     */
    private String maskSensitiveData(String data) {
        if (data == null || data.length() <= 8) {
            return "***";
        }

        return data.substring(0, 4) + "***" + data.substring(data.length() - 4);
    }

    /**
     * 获取真实的客户端IP地址，考虑反向代理的情况
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // 常见的反向代理头，按优先级检查
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For 可能包含多个IP，取第一个
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        // 如果所有头都没有，使用 getRemoteAddr()
        return request.getRemoteAddr();
    }

    /**
     * 判断是否需要跳过日志记录
     */
    private boolean shouldSkipLogging(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("HttpLoggingFilter initialized");
    }

    @Override
    public void destroy() {
        LOGGER.info("HttpLoggingFilter destroyed");
    }
}

