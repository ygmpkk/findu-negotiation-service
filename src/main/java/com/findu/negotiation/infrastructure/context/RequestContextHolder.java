package com.findu.negotiation.infrastructure.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 请求上下文持有者，使用ThreadLocal管理请求上下文
 *
 * @author timothy
 * @date 2025/12/17
 */
public class RequestContextHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestContextHolder.class);

    private static final ThreadLocal<RequestContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private RequestContextHolder() {
    }

    /**
     * 获取当前请求上下文
     *
     * @return 请求上下文，如果不存在则返回null
     */
    public static RequestContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 设置当前请求上下文
     *
     * @param context 请求上下文
     */
    public static void setContext(RequestContext context) {
        CONTEXT_HOLDER.set(context);
        LOGGER.debug("设置请求上下文: {}", context);
    }

    /**
     * 清除当前请求上下文
     */
    public static void clearContext() {
        LOGGER.debug("清除请求上下文");
        CONTEXT_HOLDER.remove();
    }

    /**
     * 获取Authorization header
     *
     * @return Authorization header值，如果不存在则返回null
     */
    public static String getAuthorization() {
        RequestContext context = getContext();
        return context != null ? context.getAuthorization() : null;
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID，如果不存在则返回null
     */
    public static String getUserId() {
        RequestContext context = getContext();
        return context != null ? context.getUserId() : null;
    }

    /**
     * 获取TraceId
     *
     * @return TraceId，如果不存在则返回null
     */
    public static String getTraceId() {
        RequestContext context = getContext();
        return context != null ? context.getTraceId() : null;
    }
}

