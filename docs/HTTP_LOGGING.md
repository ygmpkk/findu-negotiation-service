# HTTP 请求响应日志记录

## 概述

`HttpLoggingFilter` 是一个 Servlet 过滤器，用于记录所有 HTTP 请求和响应的详细信息，包括：

- 请求路径（path）
- 请求方法（method）
- 请求头（headers）
- 请求体（request body）
- 响应状态码（status code）
- 响应体（response body）
- 处理时间（processing time）
- 请求ID（X-Request-Id）
- 追踪ID（X-Trace-Id）

## 功能特性

### 1. 自动记录请求信息
- 记录 HTTP 方法（GET、POST、PUT、DELETE等）
- 记录完整的请求路径和查询参数
- 记录所有请求头（敏感信息会自动脱敏）
- 记录请求体内容（支持 JSON 等格式）
- 记录 Request ID 和 Trace ID 用于链路追踪

### 2. 自动记录响应信息
- 记录 HTTP 状态码
- 记录响应头
- 记录响应体内容
- 记录请求处理时间（毫秒）
- 记录 Request ID 和 Trace ID 便于关联请求响应

### 3. 安全特性
- **敏感信息脱敏**：自动对 `Authorization`、`Cookie`、`token` 等敏感请求头进行脱敏处理
- **大小限制**：超过 10KB 的请求/响应体会被自动截断，避免日志过大

### 4. 智能过滤
- 自动跳过健康检查端点（`/health`、`/actuator`、`/metrics` 等）
- 可配置需要跳过的路径

## 日志格式

### 请求日志格式
```
_com_request_in||method=POST||path=/api/negotiation/create?id=123||request_id=req-12345||trace_id=trace-67890||headers={Content-Type=application/json, Authorization=Bear***5678}||body={"productId":"123","price":100}||
```

### 响应日志格式
```
_com_request_out||request_id=req-12345||trace_id=trace-67890||status_code=200||proc_time=156||headers={Content-Type=application/json}||resp={"success":true,"data":{"id":"123"}}||
```

## 配置说明

### 日志级别
- **INFO 级别**：记录正常的请求和响应（状态码 < 400）
- **ERROR 级别**：记录错误的请求和响应（状态码 >= 400）

### Log4j2 配置
在 `log4j2.xml` 中已配置专用的异步日志记录器：

```xml
<AsyncLogger name="com.findu.negotiation.infrastructure.filter.HttpLoggingFilter" 
             level="INFO" 
             additivity="false" 
             includeLocation="false">
    <AppenderRef ref="Console"/>
    <AppenderRef ref="RollingFile"/>
    <AppenderRef ref="ErrorFile"/>
</AsyncLogger>
```

### 自定义配置

如需修改过滤器行为，可以在 `HttpLoggingFilter.java` 中调整以下常量：

```java
private static final int MAX_PAYLOAD_LENGTH = 10000; // 最大记录长度（字节）

// 不需要记录详细日志的路径
private static final Set<String> EXCLUDED_PATHS = new HashSet<>(Arrays.asList(
    "/actuator", "/health", "/metrics", "/favicon.ico"
));
```

## 使用示例

### 1. 标准 REST API 请求

**请求**：
```bash
curl -X POST http://localhost:8080/api/negotiation/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer token123456" \
  -H "X-Request-Id: req-001" \
  -H "X-Trace-Id: trace-001" \
  -d '{"productId":"P123","price":100}'
```

**日志输出**：
```
2026-01-05 23:45:12.123 INFO  - _com_request_in||method=POST||path=/api/negotiation/create||request_id=req-001||trace_id=trace-001||headers={Content-Type=application/json, Authorization=Bear***3456, ...}||body={"productId":"P123","price":100}||

2026-01-05 23:45:12.280 INFO  - _com_request_out||request_id=req-001||trace_id=trace-001||status_code=200||proc_time=157||headers={Content-Type=application/json}||resp={"success":true,"negotiationId":"N001"}||
```

### 2. GET 请求

**请求**：
```bash
curl -X GET http://localhost:8080/api/negotiation/status?id=N001 \
  -H "X-Trace-Id: trace-002"
```

**日志输出**：
```
2026-01-05 23:46:00.123 INFO  - _com_request_in||method=GET||path=/api/negotiation/status?id=N001||trace_id=trace-002||headers={...}||

2026-01-05 23:46:00.156 INFO  - _com_request_out||trace_id=trace-002||status_code=200||proc_time=33||headers={...}||resp={"status":"active"}||
```

### 3. 错误请求

当请求返回错误状态码时，日志级别会自动提升到 ERROR：

```
2026-01-05 23:47:00.123 INFO  - _com_request_in||method=POST||path=/api/negotiation/invalid||...

2026-01-05 23:47:00.145 ERROR - _com_request_out||status_code=400||proc_time=22||resp={"error":"Invalid request"}||
```

## 性能优化

1. **异步日志**：使用 Log4j2 的异步日志记录器，不影响主线程性能
2. **智能过滤**：跳过不需要记录的端点（如健康检查）
3. **内容截断**：大请求/响应体自动截断，避免内存溢出
4. **高优先级**：过滤器优先级设置为 `HIGHEST_PRECEDENCE + 1`，确保尽早捕获请求

## 测试

项目包含完整的单元测试：

```bash
mvn test -Dtest=HttpLoggingFilterTest
```

测试覆盖场景：
- ✅ POST 请求带 JSON body
- ✅ GET 请求带查询参数
- ✅ 没有 Request ID 的请求
- ✅ 敏感信息脱敏
- ✅ 跳过健康检查端点
- ✅ 大请求体处理
- ✅ 空请求体处理
- ✅ 多个请求头
- ✅ 错误响应（4xx）
- ✅ 服务器错误（5xx）
- ✅ PUT/DELETE 请求

## 注意事项

1. **性能影响**：由于需要读取请求和响应体，会对性能产生一定影响。建议在生产环境中根据需要调整日志级别或排除某些高频路径。

2. **敏感信息**：虽然已对常见的敏感请求头进行脱敏，但请确保请求/响应体中不包含敏感信息，或根据需要扩展脱敏逻辑。

3. **日志大小**：虽然已限制单条日志的大小，但高并发场景下仍可能产生大量日志，建议配置好日志轮转策略。

4. **链路追踪**：配合 `X-Request-Id` 和 `X-Trace-Id` 使用，可以实现完整的请求链路追踪。

## 相关文件

- 过滤器实现：`src/main/java/com/findu/negotiation/infrastructure/filter/HttpLoggingFilter.java`
- 单元测试：`src/test/java/com/findu/negotiation/infrastructure/filter/HttpLoggingFilterTest.java`
- 日志配置：`src/main/resources/log4j2.xml`

