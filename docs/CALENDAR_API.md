# 日历服务 API 文档

## 概述

日历服务提供完整的日程管理功能，包括日历管理、日程管理和参与人管理。

**Base URL**: `http://localhost:8810/api/v1`

**通用响应格式**:

所有接口均返回统一的响应格式：

```json
{
  "code": 0,
  "message": "success",
  "data": {...}
}
```

**错误码**:

| Code | 说明 |
|------|------|
| 0 | 成功 |
| 400 | 参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 1. 日历管理 API

### 1.1 创建日历

**请求**: `POST /api/v1/calendars`

**请求体**:

```json
{
  "providerId": "string",     // 必填，服务提供方ID
  "name": "string",            // 必填，日历名称，最大长度128
  "description": "string",     // 可选，日历描述
  "timezone": "string"         // 可选，时区，默认 Asia/Shanghai
}
```

**响应**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "calendarId": "string",
    "providerId": "string",
    "name": "string",
    "description": "string",
    "timezone": "string",
    "status": 1,
    "gmtCreate": "2026-01-25T10:00:00",
    "gmtModify": "2026-01-25T10:00:00"
  }
}
```

---

### 1.2 更新日历

**请求**: `PUT /api/v1/calendars/{calendarId}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| calendarId | String | 日历ID |

**请求体**:

```json
{
  "name": "string",            // 可选
  "description": "string",     // 可选
  "timezone": "string",        // 可选
  "status": 1                  // 可选，0: 禁用, 1: 启用
}
```

**响应**: 同创建日历

---

### 1.3 删除日历

**请求**: `DELETE /api/v1/calendars/{calendarId}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| calendarId | String | 日历ID |

**响应**:

```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

---

### 1.4 获取日历详情

**请求**: `GET /api/v1/calendars/{calendarId}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| calendarId | String | 日历ID |

**响应**: 同创建日历

---

### 1.5 获取服务提供方的日历列表

**请求**: `GET /api/v1/calendars/provider/{providerId}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| providerId | String | 服务提供方ID |

**响应**:

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "calendarId": "string",
      "providerId": "string",
      "name": "string",
      "description": "string",
      "timezone": "string",
      "status": 1,
      "gmtCreate": "2026-01-25T10:00:00",
      "gmtModify": "2026-01-25T10:00:00"
    }
  ]
}
```

---

### 1.6 获取服务提供方的激活日历列表

**请求**: `GET /api/v1/calendars/provider/{providerId}/active`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| providerId | String | 服务提供方ID |

**响应**: 同获取日历列表

---

## 2. 日程管理 API

### 2.1 创建日程

**请求**: `POST /api/v1/calendars/events`

**请求体**:

```json
{
  "calendarId": "string",      // 必填，所属日历ID
  "title": "string",            // 必填，日程标题，最大长度256
  "description": "string",      // 可选，日程描述
  "visibility": 0,              // 可选，可见性，0: 公开, 1: 私有，默认1
  "startTime": {                // 必填，开始时间
    "date": "2026-01-25",       // 全天事件使用 date
    "timestamp": "2026-01-25T10:00:00",  // 非全天事件使用 timestamp
    "timezone": "Asia/Shanghai" // 必填，时区
  },
  "endTime": {                  // 必填，结束时间
    "date": "2026-01-25",
    "timestamp": "2026-01-25T11:00:00",
    "timezone": "Asia/Shanghai"
  },
  "eventType": 0,               // 必填，日程类型，0: 普通日程, 1: 重复日程
  "recurrence": "string",       // 可选，重复规则(RFC5545 RRULE)，eventType=1 时需要
  "reminders": [                // 可选，提醒设置
    {
      "minutes": 30,            // 提前分钟数
      "hour": 0,                // 提前小时数
      "day": 0                  // 提前天数
    }
  ],
  "location": {                 // 可选，位置信息
    "name": "string",           // 位置名称
    "address": "string",        // 详细地址
    "latitude": 39.9042,        // 纬度
    "longitude": 116.4074       // 经度
  }
}
```

**响应**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "eventId": "string",
    "calendarId": "string",
    "title": "string",
    "description": "string",
    "visibility": 0,
    "startTime": {...},
    "endTime": {...},
    "eventType": 0,
    "recurrence": "string",
    "status": 0,                // 0: 已安排, 1: 已完成, 2: 已取消
    "freeBusyStatus": 0,        // 0: 忙碌, 1: 空闲
    "isException": false,
    "recurringEventId": "string",
    "reminders": [...],
    "location": {...},
    "gmtCreate": "2026-01-25T10:00:00",
    "gmtModify": "2026-01-25T10:00:00"
  }
}
```

---

### 2.2 更新日程

**请求**: `PUT /api/v1/calendars/events/{eventId}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| eventId | String | 日程ID |

**请求体**:

```json
{
  "eventId": "string",          // 必填
  "title": "string",            // 可选
  "description": "string",      // 可选
  "visibility": 0,              // 可选
  "startTime": {...},           // 可选
  "endTime": {...},             // 可选
  "status": 0,                  // 可选
  "freeBusyStatus": 0,          // 可选
  "reminders": [...],           // 可选
  "location": {...}             // 可选
}
```

**响应**: 同创建日程

---

### 2.3 删除日程

**请求**: `DELETE /api/v1/calendars/events/{eventId}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| eventId | String | 日程ID |

**响应**:

```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

---

### 2.4 获取日程详情

**请求**: `GET /api/v1/calendars/events/{eventId}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| eventId | String | 日程ID |

**响应**: 同创建日程

---

### 2.5 获取日历中的所有日程

**请求**: `GET /api/v1/calendars/events/calendar/{calendarId}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| calendarId | String | 日历ID |

**响应**:

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {...},  // 日程对象
    {...}
  ]
}
```

---

## 3. 参与人管理 API

### 3.1 添加参与人

**请求**: `POST /api/v1/calendars/attendees`

**请求体**:

```json
{
  "eventId": "string",          // 必填，日程ID
  "customerId": "string",       // 必填，客户ID
  "role": 0,                    // 可选，参与角色，0: 必需, 1: 可选，默认0
  "rsvpStatus": 0               // 可选，RSVP状态，0: 待响应, 1: 已接受, 2: 已拒绝，默认0
}
```

**响应**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "attendeeId": "string",
    "eventId": "string",
    "customerId": "string",
    "role": 0,
    "rsvpStatus": 0,
    "gmtCreate": "2026-01-25T10:00:00"
  }
}
```

---

### 3.2 更新参与人

**请求**: `PUT /api/v1/calendars/attendees/{attendeeId}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| attendeeId | String | 参与人ID |

**请求体**:

```json
{
  "attendeeId": "string",       // 必填
  "role": 0,                    // 可选
  "rsvpStatus": 0               // 可选
}
```

**响应**: 同添加参与人

---

### 3.3 移除参与人

**请求**: `DELETE /api/v1/calendars/attendees/{attendeeId}?eventId={eventId}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| attendeeId | String | 参与人ID |

**查询参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| eventId | String | 日程ID |

**响应**:

```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

---

### 3.4 获取参与人详情

**请求**: `GET /api/v1/calendars/attendees/{attendeeId}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| attendeeId | String | 参与人ID |

**响应**: 同添加参与人

---

### 3.5 获取日程的所有参与人

**请求**: `GET /api/v1/calendars/attendees/event/{eventId}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| eventId | String | 日程ID |

**响应**:

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "attendeeId": "string",
      "eventId": "string",
      "customerId": "string",
      "role": 0,
      "rsvpStatus": 0,
      "gmtCreate": "2026-01-25T10:00:00"
    }
  ]
}
```

---

### 3.6 更新参与人 RSVP 状态

**请求**: `PATCH /api/v1/calendars/attendees/{attendeeId}/rsvp?status={status}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| attendeeId | String | 参与人ID |

**查询参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| status | Integer | RSVP 状态，0: 待响应, 1: 已接受, 2: 已拒绝 |

**响应**:

```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

---

## 附录 A: 枚举值说明

### 日历状态 (CalendarStatus)

| Code | 说明 |
|------|------|
| 0    | 禁用 |
| 1    | 激活 |


### 日程类型 (EventType)

| Code | 说明 |
|------|------|
| 0 | 普通日程 |
| 1 | 重复日程 |

### 日程状态 (EventStatus)

| Code | 说明 |
|------|------|
| 0 | 已安排 |
| 1 | 已完成 |
| 2 | 已取消 |

### 闲忙状态 (FreeBusyStatus)

| Code | 说明 |
|------|------|
| 0 | 忙碌 |
| 1 | 空闲 |

### 可见性 (Visibility)

| Code | 说明 |
|------|------|
| 0 | 公开 |
| 1 | 私有 |

### 参与角色 (AttendeeRole)

| Code | 说明 |
|------|------|
| 0 | 必需 |
| 1 | 可选 |

### RSVP 状态 (RsvpStatus)

| Code | 说明 |
|------|------|
| 0 | 待响应 |
| 1 | 已接受 |
| 2 | 已拒绝 |

---

## 附录 B: 时间格式说明

### 日期格式 (全天事件)

```
YYYY-MM-DD
```

示例: `2026-01-25`

### 时间戳格式 (非全天事件)

```
YYYY-MM-DDTHH:mm:ss
```

示例: `2026-01-25T10:30:00`

### 时区

使用 IANA 时区标识符，例如:

- `Asia/Shanghai` - 中国标准时间
- `America/New_York` - 美国东部时间
- `Europe/London` - 格林威治时间

---

## 附录 C: RRULE 重复规则说明

重复规则遵循 RFC5545 标准。

**格式**: `FREQ=<频率>;INTERVAL=<间隔>`

**频率 (FREQ)**:

| 值 | 说明 |
|------|------|
| MINUTELY | 每分钟 |
| HOURLY | 每小时 |
| DAILY | 每天 |
| WEEKLY | 每周 |
| MONTHLY | 每月 |
| YEARLY | 每年 |

**示例**:

| RRULE | 说明 |
|-------|------|
| `FREQ=DAILY;INTERVAL=1` | 每天重复 |
| `FREQ=WEEKLY;INTERVAL=1` | 每周重复 |
| `FREQ=MONTHLY;INTERVAL=1` | 每月重复 |
| `FREQ=YEARLY;INTERVAL=1` | 每年重复 |
| `FREQ=HOURLY;INTERVAL=2` | 每2小时重复 |
