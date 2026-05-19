# JavaChatSer API

本文档说明新版 Spring Boot 后端接口。默认后端地址为 `http://localhost:8080`，Docker Compose 前端会通过 `http://localhost:5173/api` 同源代理访问后端。

## 通用响应

所有 REST 接口统一返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

常用错误码：

| code | 含义 |
| --- | --- |
| `40000` | 参数错误 |
| `40001` | 用户名或密码错误 |
| `40100` | 未登录或 Token 无效 |
| `40300` | 无权限 |
| `40400` | 数据不存在 |
| `40900` | 数据冲突 |
| `50000` | 服务端错误 |

除注册、登录、健康检查和 Swagger 外，其余 REST 接口都需要请求头：

```text
Authorization: Bearer <JWT>
```

## 认证

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/auth/register` | 注册并返回 JWT |
| `POST` | `/api/auth/login` | 登录并返回 JWT |
| `GET` | `/api/auth/me` | 获取当前用户资料 |
| `POST` | `/api/auth/logout` | 退出登录，前端清理 Token |

注册请求：

```json
{
  "username": "alice",
  "password": "123456",
  "nickname": "Alice",
  "avatarUrl": "/default-avatar.png"
}
```

登录请求：

```json
{
  "username": "admin",
  "password": "123456"
}
```

## 用户和好友

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/users/search?keyword=&page=0&size=10` | 搜索用户，排除当前登录用户 |
| `GET` | `/api/friends` | 好友列表，包含在线状态和未读数 |
| `POST` | `/api/friends/requests` | 发送好友申请 |
| `GET` | `/api/friends/requests` | 查询收到的好友申请 |
| `POST` | `/api/friends/requests/{requestId}/accept` | 接受好友申请 |
| `POST` | `/api/friends/requests/{requestId}/reject` | 拒绝好友申请 |
| `DELETE` | `/api/friends/{friendId}` | 删除双向好友关系 |

发送好友申请：

```json
{
  "friendId": 2
}
```

## 私聊

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/chats/private/{friendId}/messages?page=0&size=20` | 查询私聊历史，按时间升序 |
| `POST` | `/api/chats/private/{friendId}/messages` | 发送私聊消息 |
| `POST` | `/api/chats/private/{friendId}/read` | 将该好友发来的未读消息标记已读 |

发送私聊：

```json
{
  "content": "你好"
}
```

## 公共聊天室

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/chats/public/messages?page=0&size=20` | 查询公共消息历史，按时间升序 |
| `POST` | `/api/chats/public/messages` | 发送公共消息，并通过 WebSocket 广播 |

发送公共消息：

```json
{
  "content": "大家好"
}
```

## WebSocket

连接地址：

```text
ws://localhost:8080/ws/chat?token=<JWT>
```

Docker Compose 前端同源代理地址：

```text
ws://localhost:5173/ws/chat?token=<JWT>
```

客户端发送类型：

```json
{
  "type": "PRIVATE_MESSAGE",
  "receiverId": 2,
  "content": "你好"
}
```

```json
{
  "type": "PUBLIC_MESSAGE",
  "content": "大家好"
}
```

```json
{
  "type": "PING"
}
```

服务端推送类型：

| type | data |
| --- | --- |
| `ONLINE_USERS` | 在线用户 ID 数组 |
| `FRIEND_STATUS` | `{ "userId": 1, "online": true }` |
| `PRIVATE_MESSAGE` | 私聊消息对象 |
| `PUBLIC_MESSAGE` | 公共消息对象 |
| `PONG` | `null` |
| `ERROR` | `{ "code": 40000, "message": "..." }` |

## 健康检查和文档

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/health` | 后端健康检查 |
| `GET` | `/swagger-ui.html` | Swagger UI |
| `GET` | `/v3/api-docs` | OpenAPI JSON |
