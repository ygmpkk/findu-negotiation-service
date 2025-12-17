package com.findu.negotiation.infrastructure.interceptor;

import com.findu.negotiation.infrastructure.context.RequestContext;
import com.findu.negotiation.infrastructure.context.RequestContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 请求上下文拦截器，用于在请求开始时设置上下文，请求结束时清理上下文
 *
 * @author timothy
 * @date 2025/12/17
 */
@Component
public class RequestContextInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestContextInterceptor.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        RequestContext context = new RequestContext();

        // 设置Authorization
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization != null && !authorization.isEmpty()) {
            context.setAuthorization(authorization);
        }

        // 设置UserId
        String userId = request.getHeader(USER_ID_HEADER);
        if (userId != null && !userId.isEmpty()) {
            context.setUserId(userId);
        }

        // 设置TraceId，如果请求中没有则生成一个
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        context.setTraceId(traceId);

        RequestContextHolder.setContext(context);

        LOGGER.debug("请求上下文已设置: uri={}, traceId={}, hasAuth={}",
                request.getRequestURI(), traceId, authorization != null);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestContextHolder.clearContext();
        LOGGER.debug("请求上下文已清除: uri={}", request.getRequestURI());
    }
}

