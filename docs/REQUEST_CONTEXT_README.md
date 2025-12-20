# 全局RequestContext和HTTP客户端透传实现文档

## 概述

本实现提供了一个全局的`RequestContext`来管理请求级别的上下文信息（如Authorization header），并封装了一个统一的`HttpClientWrapper`，可以根据参数选择是否透传authorization header到下游服务。

## 架构设计

### 核心组件

1. **RequestContext** - 请求上下文数据类
2. **RequestContextHolder** - ThreadLocal管理器
3. **RequestContextInterceptor** - Web请求拦截器
4. **WebMvcConfig** - 拦截器配置
5. **HttpClientWrapper** - 统一HTTP客户端封装

## 详细说明

### 1. RequestContext（请求上下文）

位置: `com.findu.negotiation.infrastructure.context.RequestContext`

存储请求级别的数据，包括：
- `authorization` - Authorization header
- `userId` - 用户ID
- `traceId` - 追踪ID
- `customHeaders` - 自定义header映射

```java
RequestContext context = new RequestContext();
context.setAuthorization("Bearer token...");
context.setUserId("user123");
context.addCustomHeader("X-Custom", "value");
```

### 2. RequestContextHolder（上下文持有者）

位置: `com.findu.negotiation.infrastructure.context.RequestContextHolder`

使用ThreadLocal管理RequestContext，确保线程安全：

```java
// 设置上下文
RequestContextHolder.setContext(context);

// 获取上下文
RequestContext ctx = RequestContextHolder.getContext();

// 获取Authorization
String auth = RequestContextHolder.getAuthorization();

// 清除上下文
RequestContextHolder.clearContext();
```

### 3. RequestContextInterceptor（请求拦截器）

位置: `com.findu.negotiation.infrastructure.interceptor.RequestContextInterceptor`

自动拦截所有HTTP请求，从请求头中提取以下信息并存入RequestContext：
- `Authorization` - 认证信息
- `X-User-Id` - 用户ID
- `X-Trace-Id` - 追踪ID（如果不存在则自动生成）

请求完成后自动清理上下文。

### 4. HttpClientWrapper（HTTP客户端封装）

位置: `com.findu.negotiation.infrastructure.client.HttpClientWrapper`

统一的HTTP客户端，支持：
- GET请求
- POST JSON请求
- POST Form请求
- 可选的Authorization header透传

#### 使用示例

```java
@Autowired
private HttpClientWrapper httpClientWrapper;

// GET请求，透传Authorization
ResponseEntity<String> response = httpClientWrapper.get(
    url, 
    String.class, 
    true  // passAuthorization = true
);

// POST JSON请求，不透传Authorization
ResponseEntity<MyResponse> response = httpClientWrapper.postJson(
    url, 
    requestBody, 
    MyResponse.class, 
    false  // passAuthorization = false
);

// POST Form请求，透传Authorization
Map<String, String> formParams = new HashMap<>();
formParams.put("key", "value");
ResponseEntity<String> response = httpClientWrapper.postForm(
    url, 
    formParams, 
    String.class, 
    true  // passAuthorization = true
);

// 添加额外的请求头
Map<String, String> headers = new HashMap<>();
headers.put("X-Custom-Header", "value");
ResponseEntity<String> response = httpClientWrapper.postJson(
    url, 
    body, 
    headers,  // 额外的请求头
    String.class, 
    true
);
```

## 客户端改造示例

### DmsClient 改造

**改造前：**
```java
public String getDemandDescription(String userId, String demandId, String authorization) {
    HttpHeaders headers = new HttpHeaders();
    if (authorization != null && !authorization.isEmpty()) {
        headers.set("Authorization", authorization);
    }
    // ... 手动设置headers
}
```

**改造后：**
```java
// 默认透传Authorization
public String getDemandDescription(String userId, String demandId) {
    return getDemandDescription(userId, demandId, true);
}

// 可控制是否透传
public String getDemandDescription(String userId, String demandId, boolean passAuthorization) {
    Map<String, String> formParams = new HashMap<>();
    formParams.put("userId", userId);
    formParams.put("demandId", demandId);
    
    ResponseEntity<String> response = httpClientWrapper.postForm(
        url, formParams, String.class, passAuthorization
    );
    // ... 处理响应
}
```

### UserClient 改造

**改造前：**
```java
public List<Map<String, Object>> getProviderWorks(String providerId, String authorization) {
    HttpHeaders headers = new HttpHeaders();
    if (authorization != null && !authorization.isEmpty()) {
        headers.set("Authorization", authorization);
    }
    // ...
}
```

**改造后：**
```java
// 默认透传Authorization
public List<Map<String, Object>> getProviderWorks(String providerId) {
    return getProviderWorks(providerId, true);
}

// 可控制是否透传
public List<Map<String, Object>> getProviderWorks(String providerId, boolean passAuthorization) {
    ResponseEntity<String> response = httpClientWrapper.get(
        url, String.class, passAuthorization
    );
    // ... 处理响应
}
```

## Service层改造

**改造前：**
```java
@Override
public CreateNegotiationResponse createNegotiation(
    CreateNegotiationRequest request, 
    String authorization) {
    
    String description = dmsClient.getDemandDescription(
        request.getCustomerId(), 
        request.getDemandId(), 
        authorization
    );
    // ...
}
```

**改造后：**
```java
@Override
public CreateNegotiationResponse createNegotiation(
    CreateNegotiationRequest request) {
    
    // authorization自动从RequestContext获取
    String description = dmsClient.getDemandDescription(
        request.getCustomerId(), 
        request.getDemandId()
    );
    // ...
}
```

## Controller层改造

**改造前：**
```java
@PostMapping("/create")
public ApiResponse<CreateNegotiationResponse> create(
    @RequestHeader(value = "Authorization", required = false) String authorization,
    @Valid @RequestBody CreateNegotiationRequest request) {
    
    CreateNegotiationResponse response = 
        negotiationService.createNegotiation(request, authorization);
    return ApiResponse.success(response);
}
```

**改造后：**
```java
@PostMapping("/create")
public ApiResponse<CreateNegotiationResponse> create(
    @Valid @RequestBody CreateNegotiationRequest request) {
    
    // authorization已通过RequestContextInterceptor自动设置到RequestContext
    CreateNegotiationResponse response = 
        negotiationService.createNegotiation(request);
    return ApiResponse.success(response);
}
```

## 使用场景

### 1. 需要透传Authorization的场景
大多数内部服务调用需要透传用户的认证信息：

```java
// 自动透传
String result = dmsClient.getDemandDescription(userId, demandId);

// 或显式指定
String result = dmsClient.getDemandDescription(userId, demandId, true);
```

### 2. 不需要透传Authorization的场景
某些公共接口或系统级接口调用：

```java
// 不透传Authorization
List<Map> works = userClient.getProviderWorks(providerId, false);
```

### 3. 在异步任务中使用
由于使用ThreadLocal，异步任务需要手动传递上下文：

```java
// 在主线程中获取上下文
RequestContext context = RequestContextHolder.getContext();

// 在异步任务中设置上下文
executor.submit(() -> {
    try {
        RequestContextHolder.setContext(context);
        // 执行业务逻辑
        someService.doSomething();
    } finally {
        RequestContextHolder.clearContext();
    }
});
```

## 兼容性说明

为了保持向后兼容，所有改造的方法都保留了原有的签名作为`@Deprecated`方法：

```java
// 新方法（推荐）
public String getDemandDescription(String userId, String demandId)

// 旧方法（兼容，但已标记为废弃）
@Deprecated
public String getDemandDescription(String userId, String demandId, String authorization)
```

旧代码可以继续运行，但建议逐步迁移到新的API。

## 注意事项

1. **ThreadLocal使用** - RequestContext使用ThreadLocal存储，确保请求结束时会自动清理（由RequestContextInterceptor负责）

2. **异步场景** - 在使用异步操作（如`@Async`、CompletableFuture等）时，需要手动传递上下文

3. **测试场景** - 在单元测试中，需要手动设置RequestContext：
   ```java
   @BeforeEach
   void setUp() {
       RequestContext context = new RequestContext();
       context.setAuthorization("Bearer test-token");
       RequestContextHolder.setContext(context);
   }
   
   @AfterEach
   void tearDown() {
       RequestContextHolder.clearContext();
   }
   ```

4. **性能考虑** - ThreadLocal的使用对性能影响极小，且拦截器仅在请求开始和结束时执行一次

## 测试建议

### 集成测试示例

```java
@SpringBootTest
@AutoConfigureMockMvc
class NegotiationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCreateNegotiation() throws Exception {
        mockMvc.perform(post("/api/v1/orders_negotiation/create")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk());
        
        // Authorization会自动透传到下游服务
    }
}
```

## 总结

本实现提供了一个优雅的方式来管理请求上下文，特别是Authorization header的透传。主要优势：

1. ✅ **代码简化** - 不需要在每个方法签名中传递authorization参数
2. ✅ **灵活控制** - 可以选择是否透传authorization
3. ✅ **向后兼容** - 保留了旧的API作为过渡
4. ✅ **易于维护** - 统一的HTTP客户端封装，便于后续扩展
5. ✅ **线程安全** - 使用ThreadLocal确保多线程环境下的安全性
6. ✅ **自动清理** - 拦截器自动管理上下文的生命周期

## 相关文件清单

- `RequestContext.java` - 请求上下文数据类
- `RequestContextHolder.java` - ThreadLocal管理器
- `RequestContextInterceptor.java` - Web请求拦截器
- `WebMvcConfig.java` - 拦截器配置
- `HttpClientWrapper.java` - 统一HTTP客户端
- `DmsClient.java` - 已改造的DMS客户端
- `UserClient.java` - 已改造的User客户端
- `NegotiationService.java` - 已改造的服务接口
- `NegotiationServiceImpl.java` - 已改造的服务实现
- `NegotiationController.java` - 已改造的控制器

