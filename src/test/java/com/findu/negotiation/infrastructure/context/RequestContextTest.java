package com.findu.negotiation.infrastructure.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RequestContext 和 RequestContextHolder 单元测试
 */
class RequestContextTest {

    @BeforeEach
    void setUp() {
        RequestContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clearContext();
    }

    @Test
    void testRequestContextBasic() {
        RequestContext context = new RequestContext();
        context.setAuthorization("Bearer token123");
        context.setUserId("user456");
        context.setTraceId("trace789");

        assertEquals("Bearer token123", context.getAuthorization());
        assertEquals("user456", context.getUserId());
        assertEquals("trace789", context.getTraceId());
    }

    @Test
    void testRequestContextHolder() {
        // 初始状态应该为空
        assertNull(RequestContextHolder.getContext());
        assertNull(RequestContextHolder.getAuthorization());

        // 设置上下文
        RequestContext context = new RequestContext();
        context.setAuthorization("Bearer test-token");
        context.setUserId("testUser");
        RequestContextHolder.setContext(context);

        // 验证可以获取到上下文
        assertNotNull(RequestContextHolder.getContext());
        assertEquals("Bearer test-token", RequestContextHolder.getAuthorization());
        assertEquals("testUser", RequestContextHolder.getUserId());

        // 清除上下文
        RequestContextHolder.clearContext();
        assertNull(RequestContextHolder.getContext());
        assertNull(RequestContextHolder.getAuthorization());
    }

    @Test
    void testRequestContextCustomHeaders() {
        RequestContext context = new RequestContext();
        context.addCustomHeader("X-Custom-1", "value1");
        context.addCustomHeader("X-Custom-2", "value2");

        assertEquals("value1", context.getCustomHeaders().get("X-Custom-1"));
        assertEquals("value2", context.getCustomHeaders().get("X-Custom-2"));
        assertEquals(2, context.getCustomHeaders().size());
    }

    @Test
    void testThreadLocalIsolation() throws InterruptedException {
        // 主线程设置上下文
        RequestContext mainContext = new RequestContext();
        mainContext.setAuthorization("Bearer main-token");
        RequestContextHolder.setContext(mainContext);

        // 验证主线程上下文
        assertEquals("Bearer main-token", RequestContextHolder.getAuthorization());

        // 在新线程中验证上下文隔离
        Thread thread = new Thread(() -> {
            // 新线程应该没有上下文
            assertNull(RequestContextHolder.getContext());
            assertNull(RequestContextHolder.getAuthorization());

            // 新线程设置自己的上下文
            RequestContext threadContext = new RequestContext();
            threadContext.setAuthorization("Bearer thread-token");
            RequestContextHolder.setContext(threadContext);

            // 验证新线程的上下文
            assertEquals("Bearer thread-token", RequestContextHolder.getAuthorization());

            // 清理新线程的上下文
            RequestContextHolder.clearContext();
        });

        thread.start();
        thread.join();

        // 验证主线程的上下文没有被影响
        assertEquals("Bearer main-token", RequestContextHolder.getAuthorization());
    }

    @Test
    void testToString() {
        RequestContext context = new RequestContext();
        context.setAuthorization("Bearer secret-token");
        context.setUserId("user123");
        context.setTraceId("trace456");

        String str = context.toString();
        // Authorization应该被隐藏
        assertTrue(str.contains("authorization='***'"));
        assertTrue(str.contains("userId='user123'"));
        assertTrue(str.contains("traceId='trace456'"));
        assertFalse(str.contains("secret-token"));
    }
}

