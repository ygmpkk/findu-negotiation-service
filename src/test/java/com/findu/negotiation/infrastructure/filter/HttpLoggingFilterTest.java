package com.findu.negotiation.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP日志过滤器测试
 *
 * @author timothy
 * @date 2026/01/05
 */
class HttpLoggingFilterTest {

    private HttpLoggingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new HttpLoggingFilter();
    }

    @Test
    void testFilterInitAndDestroy() {
        // 测试初始化和销毁
        assertDoesNotThrow(() -> {
            filter.init(null);
            filter.destroy();
        });
    }

    @Test
    void testDoFilterWithJsonRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/negotiation/create");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer test-token-12345678");
        request.addHeader("X-Request-Id", "req-12345");
        request.addHeader("X-Trace-Id", "trace-67890");
        request.setContent("{\"productId\":\"123\",\"price\":100}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // 执行过滤器
        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });

        // 验证请求通过
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void testDoFilterWithGetRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/negotiation/status");
        request.setQueryString("id=123&status=active");
        request.addHeader("X-Request-Id", "req-11111");
        request.addHeader("X-Trace-Id", "trace-22222");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testDoFilterWithoutRequestId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/test");
        request.addHeader("Content-Type", "application/json");
        request.setContent("{\"test\":\"data\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // 即使没有 request_id 和 trace_id 也应该正常工作
        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testDoFilterWithSensitiveHeaders() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");
        request.addHeader("Authorization", "Bearer very-long-secret-token-123456789");
        request.addHeader("Cookie", "session=secret-cookie-value-987654321");
        request.addHeader("token", "another-secret-token-abcdefghijk");
        request.setContent("{\"username\":\"test\",\"password\":\"secret\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // 敏感信息应该被脱敏
        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testSkipHealthCheck() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/health");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // 应该跳过详细日志记录
        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testSkipActuatorEndpoint() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/actuator/health");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testSkipMetricsEndpoint() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/metrics");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testFilterWithLargeRequestBody() throws ServletException, IOException {
        // 创建一个大于 MAX_PAYLOAD_LENGTH 的请求体
        StringBuilder largeBody = new StringBuilder();
        for (int i = 0; i < 15000; i++) {
            largeBody.append("a");
        }

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/large-data");
        request.addHeader("Content-Type", "text/plain");
        request.addHeader("X-Request-Id", "req-large");
        request.setContent(largeBody.toString().getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // 应该能处理大请求体（会被截断）
        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testFilterWithEmptyBody() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/empty");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("X-Request-Id", "req-empty");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // 空请求体应该正常处理
        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testFilterWithMultipleHeaders() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/test");
        request.addHeader("X-Request-Id", "req-multi");
        request.addHeader("X-Trace-Id", "trace-multi");
        request.addHeader("Accept", "application/json");
        request.addHeader("User-Agent", "TestAgent/1.0");
        request.addHeader("X-Custom-Header", "custom-value");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testFilterWithErrorResponse() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/error");
        request.addHeader("X-Request-Id", "req-error");
        request.setContent("{\"invalid\":\"data\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();

        // 模拟返回错误状态码
        FilterChain errorChain = (req, resp) -> {
            ((jakarta.servlet.http.HttpServletResponse) resp).setStatus(400);
        };

        // 错误响应应该被记录到ERROR级别
        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, errorChain);
        });

        assertEquals(400, response.getStatus());
    }

    @Test
    void testFilterWithServerError() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/server-error");
        request.addHeader("X-Trace-Id", "trace-error");

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain serverErrorChain = (req, resp) -> {
            ((jakarta.servlet.http.HttpServletResponse) resp).setStatus(500);
        };

        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, serverErrorChain);
        });

        assertEquals(500, response.getStatus());
    }

    @Test
    void testFilterWithPutMethod() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("PUT");
        request.setRequestURI("/api/negotiation/update");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("X-Request-Id", "req-put");
        request.setContent("{\"id\":\"123\",\"status\":\"updated\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testFilterWithDeleteMethod() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("DELETE");
        request.setRequestURI("/api/negotiation/delete");
        request.setQueryString("id=123");
        request.addHeader("X-Request-Id", "req-delete");
        request.addHeader("X-Trace-Id", "trace-delete");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testFilterWithXForwardedForHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/test");
        request.addHeader("X-Forwarded-For", "203.0.113.45, 198.51.100.1");
        request.addHeader("X-Request-Id", "req-xff");
        request.setContent("{\"test\":\"data\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // 应该记录第一个IP地址
        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testFilterWithXRealIPHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/test");
        request.addHeader("X-Real-IP", "192.168.1.100");
        request.addHeader("X-Trace-Id", "trace-realip");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testFilterWithProxyClientIPHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/test");
        request.addHeader("Proxy-Client-IP", "10.0.0.50");
        request.addHeader("X-Request-Id", "req-proxy");
        request.setContent("{\"data\":\"test\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testFilterWithRemoteAddrOnly() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/test");
        request.setRemoteAddr("172.16.0.10");
        request.addHeader("X-Trace-Id", "trace-remote");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // 没有反向代理头时，应该使用 RemoteAddr
        assertDoesNotThrow(() -> {
            filter.doFilter(request, response, filterChain);
        });
    }
}

