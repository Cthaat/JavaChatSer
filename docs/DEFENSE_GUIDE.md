# JavaChatSer Defense Guide

本文档用于课程答辩时讲解项目架构、功能、数据流和关键技术选型。

## 项目定位

JavaChatSer 是一个从传统 Servlet/JSP 聊天项目升级而来的前后端分离聊天系统。旧项目保留在 `src/` 中作为业务参考，新系统放在 `backend/` 和 `frontend/` 中，使用 Spring Boot + Vue 3 重新实现用户、好友、私聊、公共聊天室、Redis 缓存和实时通信。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 前端 | Vite、Vue 3、TypeScript、Vue Router、Pinia、Axios、WebSocket |
| 后端 | Java 21、Spring Boot 3、Spring Web、Spring Security、JWT、JPA、Flyway、Spring WebSocket |
| 数据 | MySQL 8、Redis 7 |
| 部署 | Docker、Docker Compose、Nginx |

## 架构说明

```text
Browser
  |
  | HTTP / WebSocket
  v
Nginx frontend container
  |-- static Vue files
  |-- /api -> backend:8080
  |-- /ws  -> backend:8080
  |-- /uploads -> backend:8080
       |
       v
Spring Boot backend
  |-- REST controllers
  |-- services
  |-- repositories
       |
       |-- MySQL: users, friends, messages
       |-- Redis: cache, unread count, online status
```

前端容器只暴露 `5173` 到宿主机。浏览器访问 `http://localhost:5173` 后，静态资源由 Nginx 返回，接口 `/api` 和 WebSocket `/ws` 由 Nginx 反向代理到后端容器。

## 核心功能讲解

### 认证

用户注册或登录后，后端返回 JWT。前端把 Token 保存到 Pinia 和 `localStorage`，Axios 每次请求自动添加 `Authorization: Bearer <token>`。如果后端返回 `40100`，前端会清理登录状态并跳转到登录页。

### 好友

好友模块包含搜索用户、发送好友申请、接受或拒绝申请、删除好友。好友关系用 `friend_relation` 表保存，接受好友申请时会建立双向 `ACCEPTED` 关系。

### 私聊

私聊消息发送时会先检查双方是否为好友。通过 REST 或 WebSocket 发送私聊都会写入 MySQL，并更新 Redis 最近消息和接收者未读数。接收者在线时，消息通过 WebSocket 实时到达；离线时，消息仍保存在数据库中，下次进入聊天页面可以从历史接口加载。

### 公共聊天室

公共消息不要求好友关系。发送公共消息后，后端先写入 `public_message`，再写入 Redis `chat:public:recent`，最后广播给所有在线 WebSocket 连接。刷新页面后，前端会通过历史接口重新加载公共消息。

### 在线状态和未读数

用户 WebSocket 连接成功后，后端写入 `online:user:{userId}`，并通知好友该用户上线。心跳会刷新 2 分钟 TTL。断开最后一个会话后，后端删除在线状态并通知好友离线。私聊未读数保存在 Redis `unread:{receiverId}:{senderId}`，读取好友列表时返回。

### 增强功能

第 12 节增强版本已经落到可演示页面和接口：好友申请会向在线接收者推送 `FRIEND_REQUEST`；头像和聊天图片通过统一上传服务保存到 `/uploads`；文本和图片消息都支持撤回，撤回后推送 `MESSAGE_RECALLED`；`/dashboard` 调用 `/api/stats/overview` 展示管理员全局统计或普通用户个人统计；前端提供深色模式切换。

## 数据流示例

### 登录

```text
登录页 -> POST /api/auth/login -> 校验 BCrypt 密码 -> 生成 JWT -> 前端保存 Token -> 进入聊天页
```

### WebSocket 连接

```text
聊天页 -> ws://.../ws/chat?token=JWT -> 握手校验 JWT -> 注册 session -> 写 Redis 在线状态 -> 通知好友上线
```

### 私聊消息

```text
用户 A 发送 PRIVATE_MESSAGE
  -> 后端检查好友关系
  -> 写 private_message
  -> 写 chat:private:* 最近消息
  -> 写 unread:B:A
  -> 推送给 A 和 B 的在线 session
```

### 公共消息

```text
用户发送 PUBLIC_MESSAGE 或 POST /api/chats/public/messages
  -> 写 public_message
  -> 写 chat:public:recent
  -> WebSocket 广播给所有在线用户
```

### 好友申请通知

```text
用户 A 发送好友申请
  -> 写 friend_relation(PENDING)
  -> 如果用户 B 在线，WebSocket 推送 FRIEND_REQUEST
  -> B 的好友页申请数量实时变化
```

## 部署演示

启动：

```powershell
docker compose up -d --build
```

访问：

```text
前端：http://localhost:5173
后端：http://localhost:8080/api/health
Swagger：http://localhost:8080/swagger-ui.html
```

默认账号：

```text
admin / 123456
testUser3 / 123456
```

推荐演示顺序：

1. 打开 `http://localhost:5173`，使用 `admin / 123456` 登录。
2. 新开无痕窗口注册一个新用户。
3. 在好友页搜索用户并发送好友申请。
4. 另一个窗口接受申请。
5. 两个窗口进入聊天页，演示在线状态、私聊实时收发、未读数。
6. 切换公共聊天室，演示公共消息实时广播。
7. 上传一张聊天图片，撤回该消息，演示图片消息和撤回同步。
8. 进入个人资料页上传头像，再进入系统概览页查看统计。
9. 切换深色模式，刷新页面演示主题保持。

功能截图建议放在 `docs/screenshots/`，答辩 PPT 或报告可以直接引用该目录。

## 可能被问到的问题

### 为什么不用旧项目的原生 Socket？

旧项目使用 Java `ServerSocket`，浏览器前端无法直接使用。新版改为 Spring WebSocket，浏览器可以原生连接，也更容易和登录态、Session 管理、在线状态结合。

### 为什么要用 Redis？

Redis 用于保存在线状态、好友列表缓存、最近消息缓存和未读数，能减少频繁数据库查询。关键业务数据仍写入 MySQL，所以 Redis 失效不会导致消息丢失。

### 为什么 JWT 放在 WebSocket query 参数？

浏览器原生 WebSocket API 不方便自定义 `Authorization` 请求头。项目在握手 URL 中传入 `token`，后端握手拦截器校验 JWT 后才允许连接。

### 如何保证消息不丢？

消息发送流程先写 MySQL，再更新 Redis，再推送 WebSocket。即使 WebSocket 推送失败，消息仍在 MySQL 中，用户刷新或重新登录后可以从历史接口读取。

### 前端如何处理 Token 失效？

Axios 响应拦截器会捕获 `40100` 或 HTTP 401，触发全局登录失效事件。应用清理 Token、断开 WebSocket，并跳转到登录页。
