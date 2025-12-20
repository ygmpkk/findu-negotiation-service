# RequestContext架构图

## 整体架构流程

```
HTTP请求 (带Authorization header)
    ↓
RequestContextInterceptor (拦截器)
    ↓ 
    提取 Authorization, X-User-Id, X-Trace-Id
    ↓
RequestContext (存储上下文数据)
    ↓
RequestContextHolder (ThreadLocal管理)
    ↓
Controller (不需要手动处理Authorization)
    ↓
Service (不需要authorization参数)
    ↓
HttpClientWrapper (统一HTTP客户端)
    ↓
    根据passAuthorization参数决定是否透传
    ↓
下游服务 (可选择性接收Authorization header)
    ↓
响应返回
    ↓
RequestContextInterceptor (清理上下文)
```

## 组件关系图

```
┌─────────────────────────────────────────────────────────┐
│                      HTTP Request                        │
│              (Authorization: Bearer xxx)                 │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│          RequestContextInterceptor (拦截器)              │
│  - preHandle: 设置RequestContext                        │
│  - afterCompletion: 清理RequestContext                  │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│              RequestContextHolder                        │
│         (ThreadLocal<RequestContext>)                    │
│  ┌──────────────────────────────────────────────────┐  │
│  │          RequestContext (数据模型)                │  │
│  │  - authorization: String                         │  │
│  │  - userId: String                                │  │
│  │  - traceId: String                               │  │
│  │  - customHeaders: Map<String, String>            │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
         ▼                       ▼
┌──────────────────┐   ┌──────────────────┐
│   Controller     │   │   Service        │
│  (简化代码)      │───│  (简化代码)      │
└──────────────────┘   └────────┬─────────┘
                                │
                                ▼
                   ┌────────────────────────┐
                   │   HttpClientWrapper     │
                   │  (统一HTTP客户端)       │
                   └────────────┬───────────┘
                                │
              ┌─────────────────┼─────────────────┐
              │                 │                 │
              ▼                 ▼                 ▼
        ┌──────────┐      ┌──────────┐    ┌──────────┐
        │DmsClient │      │UserClient│    │其他Client│
        └────┬─────┘      └────┬─────┘    └────┬─────┘
             │                 │               │
             └─────────────────┼───────────────┘
                               │
                               ▼
                    ┌──────────────────┐
                    │  下游服务调用     │
                    │ (可选透传Auth)    │
                    └──────────────────┘
```

## 数据流向图

```
请求阶段:
┌──────┐     ┌──────────┐     ┌─────────┐     ┌─────────┐
│ HTTP │────▶│Interceptor│────▶│ Context │────▶│ Holder  │
│Request│     │  (拦截)   │     │ (创建)  │     │(ThreadLocal)│
└──────┘     └──────────┘     └─────────┘     └─────────┘

处理阶段:
┌─────────┐     ┌──────────┐     ┌────────┐     ┌──────────┐
│ Holder  │────▶│Controller│────▶│Service │────▶│  Client  │
│(获取Auth)│     │ (业务)   │     │(业务)  │     │(HTTP调用) │
└─────────┘     └──────────┘     └────────┘     └──────────┘

清理阶段:
┌──────────┐     ┌──────────┐     ┌─────────┐
│Response  │────▶│Interceptor│────▶│ Holder  │
│ (返回)   │     │(afterComp)│     │ (清理)  │
└──────────┘     └──────────┘     └─────────┘
```

## 使用场景对比

### 场景1: 需要透传Authorization (默认)

```
User Request
    │ Authorization: Bearer token123
    ▼
NegotiationController.create(request)
    │ (不需要手动处理authorization)
    ▼
NegotiationService.createNegotiation(request)
    │ (不需要authorization参数)
    ▼
DmsClient.getDemandDescription(userId, demandId)
    │ (默认passAuthorization=true)
    ▼
HttpClientWrapper.postForm(url, params, String.class, true)
    │ 自动从RequestContextHolder获取Authorization
    │ 添加到HTTP请求头
    ▼
DMS Service
    │ 接收到: Authorization: Bearer token123 ✅
    ▼
返回数据
```

### 场景2: 不需要透传Authorization

```
User Request
    │ Authorization: Bearer token123
    ▼
SomeController.publicEndpoint(request)
    ▼
SomeService.getPublicData(id)
    ▼
PublicClient.getPublicInfo(id, false)
    │ (显式设置passAuthorization=false)
    ▼
HttpClientWrapper.get(url, String.class, false)
    │ 不添加Authorization header
    ▼
Public Service
    │ 未接收到Authorization ✅
    ▼
返回公开数据
```

## 线程隔离示意图

```
主线程 (Thread-1):
┌─────────────────────────────────────┐
│ RequestContext:                     │
│   authorization: "Bearer main-token"│
│   userId: "user123"                 │
│   traceId: "trace-main-123"         │
└─────────────────────────────────────┘
          │
          │ (ThreadLocal隔离)
          │
异步线程 (Thread-2):
┌─────────────────────────────────────┐
│ RequestContext: null                │
│ (需要手动传递上下文)                 │
└─────────────────────────────────────┘

解决方案:
// 主线程
RequestContext ctx = RequestContextHolder.getContext();

// 传递给异步线程
executor.submit(() -> {
    RequestContextHolder.setContext(ctx);
    // 执行业务逻辑
    RequestContextHolder.clearContext();
});
```

## 关键优势

```
┌──────────────────────────────────────────────────────┐
│                  改造前 (旧方式)                      │
├──────────────────────────────────────────────────────┤
│ Controller:                                          │
│   ❌ 需要接收 @RequestHeader("Authorization")        │
│   ❌ 需要传递给Service                               │
│                                                      │
│ Service:                                             │
│   ❌ 方法签名包含authorization参数                   │
│   ❌ 需要传递给每个Client                            │
│                                                      │
│ Client:                                              │
│   ❌ 手动设置HTTP headers                            │
│   ❌ 重复的代码逻辑                                  │
└──────────────────────────────────────────────────────┘
                        ↓ 改造
┌──────────────────────────────────────────────────────┐
│                  改造后 (新方式)                      │
├──────────────────────────────────────────────────────┤
│ Controller:                                          │
│   ✅ 自动处理，无需手动接收header                    │
│   ✅ 方法签名简洁                                    │
│                                                      │
│ Service:                                             │
│   ✅ 无需authorization参数                          │
│   ✅ 代码更简洁易读                                  │
│                                                      │
│ Client:                                              │
│   ✅ 使用HttpClientWrapper统一封装                   │
│   ✅ 灵活控制是否透传                                │
│   ✅ 代码复用度高                                    │
└──────────────────────────────────────────────────────┘
```

## 生命周期管理

```
HTTP请求到达
     │
     ▼
┌────────────────┐
│ preHandle      │ ← 创建RequestContext
│                │   设置authorization, userId, traceId
└────────┬───────┘
         │
         ▼
┌────────────────┐
│ Controller     │
│    ↓           │
│ Service        │ ← RequestContext生命周期
│    ↓           │   (整个请求期间有效)
│ Client         │
│    ↓           │
│ 下游服务调用   │
└────────┬───────┘
         │
         ▼
┌────────────────┐
│ afterCompletion│ ← 清理RequestContext
│                │   防止内存泄漏
└────────────────┘
     │
     ▼
HTTP响应返回
```

## 兼容性策略

```
新API (推荐):
┌──────────────────────────────────────┐
│ createNegotiation(request)           │
│   ↓                                  │
│ getDemandDescription(userId, id)     │
│   ↓                                  │
│ 自动从RequestContext获取Authorization│
└──────────────────────────────────────┘

旧API (兼容，已废弃):
┌──────────────────────────────────────┐
│ createNegotiation(request, auth)     │
│   ↓                                  │
│ getDemandDescription(userId, id, auth)│
│   ↓                                  │
│ 内部调用新API (忽略auth参数)         │
└──────────────────────────────────────┘

迁移路径:
旧代码 → 继续运行 (@Deprecated) → 逐步迁移 → 使用新API
```

