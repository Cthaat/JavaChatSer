# JavaChatSer 答辩详细讲解文档

本文档用于课程答辩前完整理解 JavaChatSer 项目。它不是接口清单，也不是简单 README，而是面向答辩讲解的“源码级讲稿”：说明这个系统是什么、为什么这样设计、每个模块负责什么、一次用户操作如何穿过前端、后端、数据库、缓存和 WebSocket。

文档基于当前仓库状态：

- 本地仓库：`c:\Code\JavaChatSer`
- 远程仓库：`https://github.com/Cthaat/JavaChatSer`
- 当前提交：`a17e891 build: 优化 Dockerfile 格式`
- 重点工程：新版后端 `backend/`、新版前端 `frontend/`、部署编排 `docker-compose.yml`
- 参考工程：旧版 Java Web 代码 `src/`

## 1. 答辩主线

JavaChatSer 是一个从传统 Java Web 聊天项目升级而来的前后端分离实时聊天系统。旧版项目使用 Servlet、JSP、Spring JDBC、Druid、Jedis 和原生 `ServerSocket` 实现了用户、好友、私聊、公共聊天室等原型能力。新版项目保留旧版业务核心，但用 Spring Boot 3 + Vue 3 重新实现了完整工程结构，加入 JWT 认证、统一 REST API、WebSocket 实时通信、MySQL 持久化、Redis 缓存、Docker Compose 一键部署和可演示的现代前端页面。

答辩时可以用一句话概括：

> 本项目把一个传统 Servlet/JSP 聊天原型升级为 Spring Boot + Vue 3 的前后端分离实时聊天系统，后端负责认证、好友、消息、上传、统计和 WebSocket 推送，前端负责登录、好友管理、公共聊天室、私聊、图片消息、撤回和统计展示，数据以 MySQL 为准，Redis 用于在线状态、未读数和最近消息缓存，Docker Compose 支持一键启动完整环境。

最重要的讲解逻辑是：

1. 旧版项目有聊天业务原型，但结构偏传统，浏览器实时通信、安全认证、部署方式和可维护性不足。
2. 新版拆成 `backend/` 和 `frontend/`，形成清晰的前后端分离架构。
3. 后端采用 Controller、Service、Repository 分层，核心数据写 MySQL，缓存和在线状态写 Redis。
4. 实时消息通过 Spring WebSocket 完成，登录态通过 JWT 在 HTTP 请求头和 WebSocket 握手中复用。
5. 前端通过 Vue Router、Pinia、Axios 和原生 WebSocket，把 REST 历史数据和实时推送合并成用户可见的聊天体验。
6. 项目最终用 Docker Compose 把 MySQL、Redis、Spring Boot、Vue/Nginx 组合成可运行系统，适合课程展示和答辩。

## 2. 项目结构总览

当前仓库同时保留旧版和新版，答辩时要主动说明“旧版保留是为了展示迁移来源，新版才是最终演示系统”。

```text
JavaChatSer/
  backend/                 Spring Boot 3 后端，核心业务实现
  frontend/                Vue 3 + Vite 前端，Nginx 托管静态资源
  docs/                    接口、数据库、答辩、报告、截图和图表
  src/                     旧版 Java Web 项目，作为迁移参考
  META-INF/                旧版 Java EE 相关配置
  out/                     旧版编译输出或 IDE 产物
  tools/                   交付物、截图等辅助脚本
  docker-compose.yml       MySQL、Redis、backend、frontend 编排
  Dockerfile               旧版 Tomcat/WAR 构建参考
  README.md                项目总说明
```

新版主要目录：

```text
backend/src/main/java/com/example/javachat/
  common/       统一响应、错误码、业务异常、全局异常处理
  config/       CORS、OpenAPI、上传资源映射、WebSocket 配置
  security/     Spring Security、JWT、登录用户上下文
  user/         注册、登录、当前用户、用户搜索、头像更新
  friend/       好友申请、接受、拒绝、删除、好友列表缓存
  chat/         私聊、公共聊天室、消息持久化、已读和撤回
  websocket/    WebSocket 握手、Session 管理、在线状态和实时推送
  media/        图片上传和本地存储
  stats/        管理员全局统计和普通用户个人统计
  health/       健康检查
```

```text
frontend/src/
  api/          Axios 客户端和所有 REST API 封装
  stores/       Pinia 登录、好友、聊天状态
  views/        登录、注册、聊天、好友、概览、资料页面
  components/   AppShell 和 UI 组件
  router/       Vue Router 路由和登录守卫
  types.ts      前端和后端响应结构对应的 TypeScript 类型
```

## 3. 技术栈与选择理由

### 3.1 后端技术栈

| 技术 | 项目中的作用 | 答辩讲法 |
| --- | --- | --- |
| Java 21 | 后端运行语言 | 使用较新的 LTS Java 版本，适合 Spring Boot 3 生态 |
| Spring Boot 3.3 | 后端主框架 | 内嵌 Web 容器，简化配置和部署，替代旧版外部 Tomcat + Servlet 手工组织 |
| Spring Web | REST API | 提供注册、登录、好友、聊天、上传、统计等 HTTP 接口 |
| Spring Security | 接口鉴权 | 统一保护除登录、注册、健康检查、Swagger、上传静态文件外的接口 |
| JWT | 无状态登录凭证 | 前端保存 Token，请求时携带 `Authorization: Bearer <token>` |
| BCrypt | 密码哈希 | 数据库只保存哈希，不保存明文密码 |
| Spring Data JPA | 数据访问 | 用实体和 Repository 操作 MySQL，减少手写 SQL |
| Flyway | 数据库迁移 | 后端启动时自动创建表和初始化账号，便于部署和复现 |
| Spring Data Redis | 缓存和在线状态 | 替代旧版到处手动创建 Jedis 连接的写法 |
| Spring WebSocket | 实时通信 | 浏览器可直接连接，适合聊天消息、在线状态和通知推送 |
| springdoc-openapi | API 文档 | 提供 Swagger UI，便于调试和答辩展示 |
| JUnit 5 + Spring Boot Test | 自动化测试 | 覆盖认证、好友、聊天、统计、WebSocket 等关键流程 |

### 3.2 前端技术栈

| 技术 | 项目中的作用 | 答辩讲法 |
| --- | --- | --- |
| Vue 3 | 前端框架 | 使用组合式 API 构建登录、好友、聊天、资料、统计页面 |
| Vite | 开发和构建工具 | 启动快，配置简单，适合 Vue 3 项目 |
| TypeScript | 类型约束 | 用 `types.ts` 对齐后端响应结构，减少字段拼写和类型错误 |
| Vue Router | 页面路由 | 控制 `/login`、`/register`、`/chat`、`/friends`、`/dashboard`、`/profile` |
| Pinia | 状态管理 | 管理登录态、好友列表、WebSocket 状态、聊天消息 |
| Axios | HTTP 客户端 | 统一请求后端 REST API，自动携带 JWT |
| WebSocket API | 实时连接 | 前端直接连接 `/ws/chat?token=...` 接收实时消息 |
| Tailwind CSS 4 | 样式系统 | 提供一致的工具类样式 |
| reka-ui / shadcn-vue 风格组件 | UI 组件基础 | 表格、卡片、按钮、标签、弹窗、提示等组件复用 |
| lucide-vue | 图标 | 用于导航、按钮和状态提示 |

### 3.3 数据和部署技术栈

| 技术 | 项目中的作用 | 答辩讲法 |
| --- | --- | --- |
| MySQL 8 | 业务数据最终来源 | 用户、好友关系、私聊消息、公共消息都以 MySQL 为准 |
| Redis 7 | 可重建缓存和在线状态 | 保存好友列表缓存、最近消息、未读数、在线 TTL |
| Docker | 镜像构建 | 后端和前端分别有独立 Dockerfile |
| Docker Compose | 一键部署 | 同时启动 MySQL、Redis、backend、frontend |
| Nginx | 前端静态托管和反向代理 | 代理 `/api`、`/ws`、`/uploads` 到后端，浏览器只访问前端端口 |

## 4. 从旧版到新版的升级思路

旧版代码在 `src/` 中保留，用于说明项目的起点。

### 4.1 旧版实现包含什么

旧版主要文件：

- `src/main/java/org/http/`：Servlet 接口和原生 Socket 服务。
- `src/main/java/org/Sql/`：Spring JDBC、Druid、Jedis 相关数据访问逻辑。
- `src/main/resources/javaChat.sql`：旧版 MySQL 表和样例数据。
- `src/webapp/index.jsp`：旧版 JSP 页面。

旧版能力：

- `signUp.java`：注册。
- `logoInResp.java`：登录并写 Cookie。
- `addMyFriend.java`、`delMyFriend.java`、`loadAllFriendMessage.java`：好友相关接口。
- `sendP2pMessages.java`、`getp2pMessages.java`：私聊发送和历史查询。
- `chatSocketSer.java`、`chatReceiveSocketThread.java`：原生 Socket 在线转发。
- `publicRoomfind.java`、`p2pRoomChat.java`：公共聊天室和私聊数据访问及 Redis 缓存尝试。

### 4.2 旧版存在的工程问题

答辩时可以客观描述，不要只说“旧版不好”，要说“旧版适合作为原型，但不适合作为最终工程”。

| 旧版点 | 风险或局限 | 新版替代 |
| --- | --- | --- |
| Servlet 分散处理请求 | 缺少统一响应、统一异常、统一鉴权 | Spring MVC Controller + Service 分层 |
| Header 或 Cookie 携带用户信息 | 登录态不够安全，Cookie 保存明文资料 | JWT + Spring Security |
| 明文密码或简单密码字段 | 数据泄露风险高 | BCrypt 哈希 |
| 手写 JDBC 和 Druid 工具类 | SQL 分散，实体模型不清晰 | JPA Entity + Repository |
| 手动创建 Jedis 连接池 | 重复代码多，资源管理复杂 | Spring Data Redis + 缓存服务类 |
| 原生 `ServerSocket` | 浏览器不能直接使用普通 TCP Socket | Spring WebSocket |
| `HashMap<String, Socket>` 保存连接 | 并发风险，连接生命周期难管理 | `ConcurrentHashMap` + `WebSocketSessionManager` |
| 单 Dockerfile 试图处理多个服务 | 不符合容器单职责 | Docker Compose 拆分服务 |
| JSP 占位页面 | 前端体验不完整 | Vue 3 页面和状态管理 |

### 4.3 新版保留的业务核心

新版不是推倒业务，而是重构工程形态：

- 用户仍然有注册、登录、资料和头像。
- 好友仍然支持搜索、申请、接受、拒绝、删除。
- 私聊仍然要求双方是好友，并持久化聊天记录。
- 公共聊天室仍然支持所有用户参与。
- Redis 仍然用于提升聊天系统状态读取效率。
- 实时通信从原生 Socket 升级为浏览器友好的 WebSocket。

## 5. 后端整体架构

后端入口是：

- `backend/src/main/java/com/example/javachat/JavaChatApplication.java`

后端遵循典型 Spring Boot 分层：

```text
Controller 层
  接收 HTTP 请求，做参数校验，返回 ApiResponse

Service 层
  执行业务规则、事务、权限判断、缓存更新和实时推送触发

Repository 层
  使用 Spring Data JPA 操作 MySQL

Redis Cache / WebSocket
  辅助保存可重建状态和推送在线事件
```

一次典型请求，例如发送私聊：

```text
前端 ChatView
  -> chatStore.sendMessage()
  -> WebSocket 或 chatApi.sendPrivateMessage()
  -> ChatWebSocketHandler 或 ChatController
  -> ChatService.sendPrivateMessage()
  -> 检查好友关系
  -> private_message 写入 MySQL
  -> Redis 更新最近消息和未读数
  -> WebSocket 推送给发送者和接收者
```

后端设计的关键点：

- Controller 不写复杂业务，保持入口清晰。
- Service 使用 `@Transactional` 保证数据库操作一致。
- MySQL 是最终事实来源，Redis 只是缓存和状态。
- WebSocket 推送失败不影响消息持久化。
- 所有 REST 接口用统一响应结构，前端处理简单。

## 6. 后端模块详解

### 6.1 common 模块：统一响应和异常

位置：

- `backend/src/main/java/com/example/javachat/common/ApiResponse.java`
- `backend/src/main/java/com/example/javachat/common/PageResponse.java`
- `backend/src/main/java/com/example/javachat/common/ErrorCode.java`
- `backend/src/main/java/com/example/javachat/common/BusinessException.java`
- `backend/src/main/java/com/example/javachat/common/GlobalExceptionHandler.java`

职责：

- `ApiResponse<T>` 统一所有 REST 响应格式。
- `PageResponse<T>` 统一分页响应格式。
- `ErrorCode` 定义业务错误码和 HTTP 状态。
- `BusinessException` 用于 Service 主动抛出业务错误。
- `GlobalExceptionHandler` 捕获参数错误、业务异常、数据库异常、Redis 异常和未知异常。

统一成功响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

常见错误码：

| code | 含义 |
| --- | --- |
| `40000` | 参数错误 |
| `40001` | 用户名或密码错误 |
| `40100` | 未登录或 Token 无效 |
| `40300` | 无权限 |
| `40400` | 数据不存在 |
| `40900` | 数据冲突 |
| `50000` | 服务端错误 |

答辩讲法：

> 我们没有让每个接口自己拼 JSON，而是抽象出统一响应和统一异常处理。这样前端只需要判断 `code` 是否为 0，异常也不会把后端堆栈暴露给用户。

### 6.2 config 模块：跨域、接口文档、上传资源、WebSocket

位置：

- `backend/src/main/java/com/example/javachat/config/CorsProperties.java`
- `backend/src/main/java/com/example/javachat/config/CorsConfig.java`
- `backend/src/main/java/com/example/javachat/config/OpenApiConfig.java`
- `backend/src/main/java/com/example/javachat/config/UploadResourceConfig.java`
- `backend/src/main/java/com/example/javachat/config/WebSocketConfig.java`

职责：

- CORS：允许前端开发地址访问后端。
- OpenAPI：启用 Swagger UI 和 `/v3/api-docs`。
- 上传资源：把后端本地 `uploads/` 映射为浏览器可访问的 `/uploads/**`。
- WebSocket：注册 `/ws/chat` 端点，并加入 JWT 握手拦截器。

配置来源：

- `backend/src/main/resources/application.yml`

重要配置：

```yaml
app:
  cors:
    allowed-origins:
      - http://localhost:5173
      - http://127.0.0.1:5173
  jwt:
    secret: ${JWT_SECRET:change-this-secret-before-production}
    expiration: ${JWT_EXPIRATION:86400000}
  upload:
    base-dir: ${UPLOAD_BASE_DIR:uploads}
```

答辩讲法：

> 这些配置类把环境差异从业务代码中拿出来，例如 CORS、JWT 密钥、上传目录都可以通过配置或环境变量控制，Docker 部署时不需要改 Java 代码。

### 6.3 security 模块：JWT 鉴权和无状态登录

位置：

- `backend/src/main/java/com/example/javachat/security/SecurityConfig.java`
- `backend/src/main/java/com/example/javachat/security/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/example/javachat/security/JwtTokenProvider.java`
- `backend/src/main/java/com/example/javachat/security/JwtProperties.java`
- `backend/src/main/java/com/example/javachat/security/LoginUser.java`

职责：

- `SecurityConfig` 配置安全链。
- `JwtAuthenticationFilter` 从请求头提取 `Authorization: Bearer <token>`。
- `JwtTokenProvider` 生成和解析 JWT。
- `LoginUser` 是 Spring Security 上下文中的当前登录用户。
- `JwtProperties` 从 `app.jwt` 读取密钥和过期时间。

安全规则：

- 放行：
  - `GET /api/health`
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `GET /uploads/**`
  - Swagger 相关路径
  - WebSocket 握手路径 `/ws/chat`
- 其他接口必须登录。

为什么 `/ws/chat` 在 Spring Security 中放行：

- HTTP 过滤链放行只是允许进入 WebSocket 握手。
- 真正的 WebSocket 登录校验在 `JwtHandshakeInterceptor` 中完成。
- 如果没有合法 token，握手会返回失败，不会建立连接。

JWT 内容：

- `subject`：用户 ID。
- `username`：用户名。
- `role`：角色。
- `issuedAt`：签发时间。
- `expiration`：过期时间。

答辩讲法：

> HTTP 接口和 WebSocket 共用同一套 JWT 身份体系。HTTP 请求通过请求头带 Token，WebSocket 因为浏览器原生 API 不方便设置自定义请求头，所以用 query 参数传 Token，并在握手拦截器中校验。

### 6.4 user 模块：注册、登录、当前用户、用户搜索、头像

位置：

- `backend/src/main/java/com/example/javachat/user/User.java`
- `backend/src/main/java/com/example/javachat/user/UserRole.java`
- `backend/src/main/java/com/example/javachat/user/UserRepository.java`
- `backend/src/main/java/com/example/javachat/user/UserService.java`
- `backend/src/main/java/com/example/javachat/user/AuthController.java`
- `backend/src/main/java/com/example/javachat/user/UserController.java`
- `backend/src/main/java/com/example/javachat/user/dto/`

主要接口：

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| `POST` | `/api/auth/register` | 注册并返回 JWT |
| `POST` | `/api/auth/login` | 登录并返回 JWT |
| `GET` | `/api/auth/me` | 获取当前登录用户 |
| `POST` | `/api/auth/logout` | 退出，后端无状态，前端清 Token |
| `GET` | `/api/users/search` | 搜索用户 |
| `POST` | `/api/users/me/avatar` | 上传并更新头像 |

实现重点：

- 注册时检查用户名是否重复。
- 密码用 `PasswordEncoder` 的 BCrypt 保存。
- 登录时按用户名查用户，验证启用状态和密码哈希。
- 登录成功后 `JwtTokenProvider.createToken(user)` 返回 Token。
- `currentUser()` 从 Spring Security 注入的 `LoginUser` 取当前用户 ID。
- 搜索用户会排除当前登录用户。
- 头像上传复用 `MediaStorageService`，保存后更新 `avatarUrl`。

答辩讲法：

> 用户模块是整个系统的身份入口。注册和登录只负责建立身份，后续所有模块都通过 Spring Security 的 `@AuthenticationPrincipal LoginUser` 获取当前用户，不再像旧版那样从 Cookie 里手动解析用户信息。

### 6.5 friend 模块：好友关系和好友申请

位置：

- `backend/src/main/java/com/example/javachat/friend/FriendRelation.java`
- `backend/src/main/java/com/example/javachat/friend/FriendStatus.java`
- `backend/src/main/java/com/example/javachat/friend/FriendRepository.java`
- `backend/src/main/java/com/example/javachat/friend/FriendService.java`
- `backend/src/main/java/com/example/javachat/friend/FriendController.java`
- `backend/src/main/java/com/example/javachat/friend/FriendCacheService.java`
- `backend/src/main/java/com/example/javachat/friend/dto/`

主要接口：

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| `GET` | `/api/friends` | 好友列表，含在线状态和未读数 |
| `POST` | `/api/friends/requests` | 发送好友申请 |
| `GET` | `/api/friends/requests` | 查询收到的申请 |
| `POST` | `/api/friends/requests/{requestId}/accept` | 接受申请 |
| `POST` | `/api/friends/requests/{requestId}/reject` | 拒绝申请 |
| `DELETE` | `/api/friends/{friendId}` | 删除好友 |

好友状态：

| 状态 | 含义 |
| --- | --- |
| `PENDING` | 待处理申请 |
| `ACCEPTED` | 已成为好友 |
| `REJECTED` | 已拒绝 |
| `DELETED` | 已删除 |

实现重点：

- 不能添加自己为好友。
- 不能重复发送已有的申请或好友关系。
- 发送申请时创建一条 `PENDING` 关系。
- 接受申请时：
  - 原申请关系改为 `ACCEPTED`。
  - 建立反向 `ACCEPTED` 关系。
  - 清理双方好友列表缓存。
- 删除好友时把双方关系改为 `DELETED`。
- 好友列表基础资料缓存到 Redis `friend:list:{userId}`。
- 在线状态和未读数每次读取时实时合并。
- 发送好友申请后，如果接收者在线，通过 WebSocket 推送 `FRIEND_REQUEST`。

答辩讲法：

> 好友关系使用双向记录，查询某个用户的好友时只需要查 `user_id = 当前用户` 的记录。接受申请时会补齐反向关系，这样读列表更简单。好友基础资料可以缓存，但在线状态和未读数变化快，所以读取列表时动态组合。

### 6.6 chat 模块：私聊、公共聊天室、已读、撤回和缓存

位置：

- `backend/src/main/java/com/example/javachat/chat/PrivateMessage.java`
- `backend/src/main/java/com/example/javachat/chat/PublicMessage.java`
- `backend/src/main/java/com/example/javachat/chat/MessageType.java`
- `backend/src/main/java/com/example/javachat/chat/PrivateMessageRepository.java`
- `backend/src/main/java/com/example/javachat/chat/PublicMessageRepository.java`
- `backend/src/main/java/com/example/javachat/chat/ChatService.java`
- `backend/src/main/java/com/example/javachat/chat/ChatController.java`
- `backend/src/main/java/com/example/javachat/chat/PrivateChatCacheService.java`
- `backend/src/main/java/com/example/javachat/chat/PublicChatCacheService.java`
- `backend/src/main/java/com/example/javachat/chat/dto/`

主要接口：

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| `GET` | `/api/chats/private/{friendId}/messages` | 私聊历史分页 |
| `POST` | `/api/chats/private/{friendId}/messages` | REST 发送私聊 |
| `POST` | `/api/chats/private/{friendId}/read` | 标记私聊已读 |
| `DELETE` | `/api/chats/private/messages/{messageId}` | 撤回私聊 |
| `GET` | `/api/chats/public/messages` | 公共消息历史分页 |
| `POST` | `/api/chats/public/messages` | REST 发送公共消息 |
| `DELETE` | `/api/chats/public/messages/{messageId}` | 撤回公共消息 |

消息类型：

| 类型 | 说明 |
| --- | --- |
| `TEXT` | 文本消息 |
| `IMAGE` | 图片消息，`content` 是图片 URL |

私聊发送规则：

1. 不能和自己私聊。
2. 接收者必须存在且启用。
3. 双方必须是 `ACCEPTED` 好友。
4. 消息内容不能为空，最多 2000 个字符。
5. 图片消息必须使用 `/uploads/images/` 或合法 HTTP/HTTPS 地址。
6. 先写 MySQL `private_message`。
7. 再写 Redis 最近私聊缓存 `chat:private:{minUserId}:{maxUserId}`。
8. 再更新 Redis 未读数 `unread:{receiverId}:{senderId}`。

公共消息规则：

1. 发送者必须是启用用户。
2. 内容校验和消息类型校验与私聊一致。
3. 写 MySQL `public_message`。
4. 写 Redis 最近公共消息 `chat:public:recent`。
5. 通过 WebSocket 广播给所有在线用户。

撤回规则：

- 普通用户只能撤回自己发送的消息。
- 管理员可以撤回任意消息。
- 撤回不是删除数据库记录，而是写入 `recalled_at`。
- 返回给前端时内容置空并标记 `recalled=true`。
- WebSocket 推送 `MESSAGE_RECALLED` 让其他在线页面同步更新。

答辩讲法：

> 消息可靠性的核心是“先落库，再缓存，再推送”。WebSocket 只是实时通道，不是最终存储。即使对方离线或推送失败，消息仍然保存在 MySQL，重新进入聊天页可以通过历史接口加载。

### 6.7 websocket 模块：实时通信和在线状态

位置：

- `backend/src/main/java/com/example/javachat/websocket/WebSocketConfig.java`
- `backend/src/main/java/com/example/javachat/websocket/JwtHandshakeInterceptor.java`
- `backend/src/main/java/com/example/javachat/websocket/ChatWebSocketHandler.java`
- `backend/src/main/java/com/example/javachat/websocket/WebSocketSessionManager.java`
- `backend/src/main/java/com/example/javachat/websocket/OnlineStatusService.java`
- `backend/src/main/java/com/example/javachat/websocket/ChatRealtimeNotifier.java`
- `backend/src/main/java/com/example/javachat/websocket/WebSocketMessageType.java`
- `backend/src/main/java/com/example/javachat/websocket/WebSocketEnvelope.java`

连接地址：

```text
ws://localhost:8080/ws/chat?token=<JWT>
```

Docker Compose 前端同源代理后：

```text
ws://localhost:5173/ws/chat?token=<JWT>
```

客户端可发送：

| type | 作用 |
| --- | --- |
| `PRIVATE_MESSAGE` | 发送私聊 |
| `PUBLIC_MESSAGE` | 发送公共消息 |
| `PING` | 心跳 |

服务端可推送：

| type | 作用 |
| --- | --- |
| `ONLINE_USERS` | 当前在线用户 ID 集合 |
| `FRIEND_STATUS` | 好友上线或离线 |
| `FRIEND_REQUEST` | 好友申请实时通知 |
| `PRIVATE_MESSAGE` | 私聊消息 |
| `PUBLIC_MESSAGE` | 公共聊天室消息 |
| `MESSAGE_RECALLED` | 消息撤回通知 |
| `PONG` | 心跳响应 |
| `ERROR` | WebSocket 业务错误 |

实现流程：

1. 前端登录后保存 JWT。
2. 前端调用 `chatStore.connect()`。
3. 浏览器连接 `/ws/chat?token=<JWT>`。
4. `JwtHandshakeInterceptor` 从 query 中取 token 并解析成 `LoginUser`。
5. `ChatWebSocketHandler.afterConnectionEstablished()` 注册 session。
6. `OnlineStatusService.markOnline()` 写 Redis `online:user:{userId}`，TTL 为 2 分钟。
7. 后端向当前连接推送 `ONLINE_USERS`。
8. 后端向好友推送 `FRIEND_STATUS`。
9. 前端每 30 秒发送 `PING`，后端返回 `PONG` 并刷新在线 TTL。
10. 连接关闭时，`WebSocketSessionManager.unregister()` 判断是否最后一个连接，如果是则标记离线并通知好友。

`WebSocketSessionManager` 的关键设计：

- 使用 `ConcurrentHashMap<Long, Set<WebSocketSession>>` 保存用户到多个 session 的映射。
- 一个用户可以开多个浏览器窗口。
- 只有最后一个 session 断开时，用户才真正离线。
- 推送时序列化成统一的 `WebSocketEnvelope`。

答辩讲法：

> WebSocket 模块不是直接替代数据库，而是实时通知通道。它负责把已经通过 Service 校验和持久化的事件推送给在线用户。在线状态用 Redis TTL 防止异常断线后状态永久保留，内存 session 管理当前节点的连接。

### 6.8 media 模块：头像和聊天图片上传

位置：

- `backend/src/main/java/com/example/javachat/media/MediaController.java`
- `backend/src/main/java/com/example/javachat/media/MediaStorageService.java`
- `backend/src/main/java/com/example/javachat/media/dto/UploadResponse.java`
- `backend/src/main/java/com/example/javachat/config/UploadResourceConfig.java`

接口：

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| `POST` | `/api/uploads/images` | 上传聊天图片 |
| `POST` | `/api/users/me/avatar` | 上传头像并更新用户资料 |

实现重点：

- 使用 `multipart/form-data`，字段名是 `file`。
- 最大图片大小 5MB。
- 支持 `image/jpeg`、`image/png`、`image/gif`、`image/webp`。
- 文件名使用 UUID，避免用户原始文件名带来的冲突和路径风险。
- 图片 URL 格式为 `/uploads/images/{uuid}.png` 或 `/uploads/avatars/{uuid}.png`。
- Docker Compose 中通过 `backend-uploads:/app/uploads` 持久化上传文件。

答辩讲法：

> 上传模块没有把文件存进数据库，而是把文件保存到后端文件系统，数据库或消息内容只保存 URL。这样数据库压力更小，前端也可以直接通过 `/uploads/**` 访问静态资源。

### 6.9 stats 模块：统计概览

位置：

- `backend/src/main/java/com/example/javachat/stats/StatsController.java`
- `backend/src/main/java/com/example/javachat/stats/StatsService.java`
- `backend/src/main/java/com/example/javachat/stats/dto/StatsOverviewResponse.java`

接口：

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| `GET` | `/api/stats/overview` | 返回统计概览 |

实现重点：

- 管理员返回 `GLOBAL` 视角：
  - 启用用户总数。
  - 当前在线用户数。
  - 今日私聊和公共消息数。
  - 累计私聊和公共消息数。
  - 全站待处理好友申请数。
  - 已接受好友关系数，双向记录除以 2。
- 普通用户返回 `PERSONAL` 视角：
  - 当前账号数量为 1。
  - 当前用户是否在线。
  - 当前用户参与或发送的消息统计。
  - 当前用户收到的待处理申请。
  - 当前用户好友数。

答辩讲法：

> 统计接口复用了已有 Repository，不需要新表。它根据登录用户角色返回不同范围的数据，体现了权限控制和业务展示的结合。

### 6.10 health 模块：健康检查

位置：

- `backend/src/main/java/com/example/javachat/health/HealthController.java`

接口：

```text
GET /api/health
```

作用：

- 返回应用名、状态和时间。
- Docker Compose 后端健康检查使用该接口判断服务是否启动完成。
- 前端容器依赖后端健康后再启动。

## 7. 数据库设计

数据库迁移脚本：

- `backend/src/main/resources/db/migration/V1__init_chat_schema.sql`
- `backend/src/main/resources/db/migration/V2__message_recall_columns.sql`

Flyway 在后端启动时自动执行迁移，保证空数据库也能初始化。

### 7.1 chat_user

用户表，保存账号、密码哈希、昵称、头像、简介、角色和启用状态。

关键字段：

| 字段 | 说明 |
| --- | --- |
| `id` | 主键 |
| `username` | 登录用户名，唯一 |
| `password_hash` | BCrypt 密码哈希 |
| `nickname` | 昵称 |
| `avatar_url` | 头像 URL |
| `bio` | 个人简介 |
| `role` | `USER` 或 `ADMIN` |
| `enabled` | 是否启用 |
| `created_at` / `updated_at` | 创建和更新时间 |

默认账号：

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `123456` | `ADMIN` |
| `testUser3` | `123456` | `USER` |

### 7.2 friend_relation

好友关系和好友申请表。

关键字段：

| 字段 | 说明 |
| --- | --- |
| `id` | 主键 |
| `user_id` | 关系拥有者或申请发起人 |
| `friend_id` | 目标用户 |
| `status` | `PENDING`、`ACCEPTED`、`REJECTED`、`DELETED` |
| `created_at` / `updated_at` | 时间 |

约束：

- `(user_id, friend_id)` 唯一，避免重复关系。
- `user_id` 和 `friend_id` 都外键关联 `chat_user(id)`。

为什么用双向好友记录：

- A 和 B 成为好友后保存 A -> B 和 B -> A。
- 查询 A 的好友列表时只查 `user_id = A`。
- 查询 B 的好友列表时只查 `user_id = B`。
- 读操作简单，适合聊天系统高频读取好友列表。

### 7.3 private_message

私聊消息表。

关键字段：

| 字段 | 说明 |
| --- | --- |
| `id` | 主键 |
| `sender_id` | 发送者 |
| `receiver_id` | 接收者 |
| `content` | 文本内容或图片 URL |
| `message_type` | `TEXT` 或 `IMAGE` |
| `read_at` | 已读时间 |
| `recalled_at` | 撤回时间 |
| `created_at` | 发送时间 |

索引：

- `idx_private_pair_time` 用于按会话双方和时间查询历史。
- `idx_private_receiver_read` 用于统计或更新未读消息。

### 7.4 public_message

公共聊天室消息表。

关键字段：

| 字段 | 说明 |
| --- | --- |
| `id` | 主键 |
| `sender_id` | 发送者 |
| `content` | 文本内容或图片 URL |
| `message_type` | `TEXT` 或 `IMAGE` |
| `recalled_at` | 撤回时间 |
| `created_at` | 发送时间 |

公共消息只记录发送者，不记录接收者，因为所有在线用户都能看到。

### 7.5 V2 迁移的意义

`V2__message_recall_columns.sql` 为私聊和公共消息增加 `recalled_at`。

这样撤回消息不是物理删除，而是软状态变更：

- 保留记录，方便审计。
- 前端显示“消息已撤回”。
- 避免删除后分页或引用关系混乱。

## 8. Redis 设计

Redis 设计原则：

> MySQL 保存不可丢失的业务数据，Redis 保存可以重建的状态和缓存。

Key 设计：

| Key | 类型 | 说明 | 过期 |
| --- | --- | --- | --- |
| `online:user:{userId}` | String | 用户在线状态，值为 sessionId | 2 分钟 |
| `friend:list:{userId}` | JSON String | 好友基础资料缓存 | 5 分钟 |
| `chat:private:{minUserId}:{maxUserId}` | List | 最近私聊消息 JSON | 1 天 |
| `chat:public:recent` | List | 最近公共消息 JSON | 1 天 |
| `unread:{userId}:{friendId}` | String Number | 某好友未读消息数 | 无固定 TTL |

各缓存用途：

- 在线状态：用于好友列表显示；统计接口当前使用 `WebSocketSessionManager` 统计当前节点在线连接。
- 好友列表：减少频繁查询关系和用户表。
- 最近消息：为后续优化近期消息读取准备。
- 未读数：好友列表展示消息提醒。

缓存一致性：

- 好友关系变化后清理双方 `friend:list:*`。
- 私聊发送后更新最近消息和接收者未读数。
- 标记已读后清理对应未读数。
- Redis 异常时，核心数据仍可从 MySQL 恢复。

答辩讲法：

> 我们没有把 Redis 当作主数据库。比如消息一定先写 MySQL，Redis 最近消息丢失也可以从历史接口重新加载。Redis 的作用是提升实时状态和高频读取体验。

## 9. 前端整体架构

前端入口：

- `frontend/src/main.ts`
- `frontend/src/App.vue`

前端核心结构：

```text
Vue Router
  管页面和登录守卫

Pinia stores
  auth: 登录态和当前用户
  friends: 好友、申请、在线状态和未读数
  chat: 当前会话、消息、WebSocket、重连和心跳

api
  Axios 封装所有 REST 请求

views
  页面组件调用 store，不直接散落复杂请求逻辑
```

前端设计重点：

- 登录态保存在 Pinia 和 `localStorage`。
- 页面刷新后调用 `/api/auth/me` 恢复用户信息。
- Axios 请求拦截器自动加 JWT。
- Axios 响应拦截器统一处理 `40100`。
- WebSocket 优先用于实时发送和接收消息。
- WebSocket 不可用时，发送消息有 REST fallback。

## 10. 前端模块详解

### 10.1 router：路由和登录守卫

位置：

- `frontend/src/router/index.ts`

路由：

| 路径 | 页面 |
| --- | --- |
| `/login` | 登录 |
| `/register` | 注册 |
| `/chat` | 聊天 |
| `/friends` | 好友管理 |
| `/dashboard` | 系统概览 |
| `/profile` | 个人资料 |

守卫逻辑：

1. 如果应用还没有初始化登录态，先调用 `authStore.restore()`。
2. 访问需要登录的页面但未登录，跳转到 `/login`。
3. 已登录访问登录或注册页，跳回 `/chat`。

答辩讲法：

> 路由守卫保证了用户必须登录才能进入业务页面，同时刷新页面后不会立即丢失状态，而是用本地 Token 调 `/api/auth/me` 恢复会话。

### 10.2 api：HTTP 请求统一封装

位置：

- `frontend/src/api/client.ts`
- `frontend/src/api/index.ts`

`client.ts` 职责：

- 创建 Axios 实例。
- 设置基础地址。
- 从 `localStorage` 读取 Token。
- 请求时自动加 `Authorization`。
- 响应时检查统一 `ApiResponse.code`。
- 遇到 `40100` 或 HTTP 401 时派发 `auth:expired` 事件。

`index.ts` 按业务分组：

- `authApi`
- `userApi`
- `friendApi`
- `chatApi`
- `statsApi`
- `uploadApi`

答辩讲法：

> 前端没有在每个页面里手动写 URL 和 Token，而是把 API 调用集中在 `src/api/`。页面和 store 面对的是业务方法，例如 `chatApi.sendPrivateMessage()`，降低了重复代码。

### 10.3 types.ts：前后端数据结构对齐

位置：

- `frontend/src/types.ts`

主要类型：

- `ApiResponse<T>`
- `PageResponse<T>`
- `UserProfile`
- `AuthResponse`
- `Friend`
- `FriendRequest`
- `PrivateMessage`
- `PublicMessage`
- `MessageRecall`
- `StatsOverview`
- `UploadResponse`
- `WebSocketEnvelope`
- `WebSocketMessageType`

答辩讲法：

> TypeScript 类型让前端字段和后端 DTO 对齐。比如 WebSocket 推送统一是 `type + data`，前端可以根据 `type` 把 `data` 转成对应消息类型。

### 10.4 auth store：登录态管理

位置：

- `frontend/src/stores/auth.ts`

职责：

- 保存 `token`、`user`、`bootstrapped`、`loading`、`error`。
- 登录成功后写 Pinia 和 `localStorage`。
- 注册成功后同样建立会话。
- 刷新页面时用 Token 调 `/api/auth/me`。
- 头像更新后刷新当前用户资料。
- 退出时清理 Token 和用户信息。

关键行为：

```text
登录页面提交
  -> authStore.login()
  -> authApi.login()
  -> 后端返回 token 和 user
  -> applySession()
  -> tokenStorage.set()
```

### 10.5 friends store：好友、申请、在线状态、未读数

位置：

- `frontend/src/stores/friends.ts`

职责：

- 保存好友列表。
- 保存收到的好友申请。
- 保存用户搜索结果。
- 查询、发送申请、接受、拒绝、删除好友。
- 根据 WebSocket 推送更新在线状态。
- 根据收到的私聊消息增加未读数。
- 进入私聊后清理未读数。
- 收到 `FRIEND_REQUEST` 时合并到申请列表。

答辩讲法：

> 好友页面和聊天侧边栏共用同一个 friends store，所以好友申请和未读数变化会同步影响导航、好友页和聊天页。

### 10.6 chat store：聊天状态和 WebSocket 客户端

位置：

- `frontend/src/stores/chat.ts`

职责：

- 记录当前会话：公共聊天室或某个好友私聊。
- 保存公共消息数组。
- 按好友 ID 保存私聊消息。
- 管理 WebSocket 状态：`idle`、`connecting`、`open`、`closed`。
- 管理自动重连和心跳。
- 发送文本和图片消息。
- 处理服务端推送。
- 应用消息撤回。

WebSocket 连接逻辑：

1. 从 `tokenStorage` 读取 JWT。
2. 拼接 `${WS_BASE_URL}/ws/chat?token=...`。
3. `onopen` 后状态改为 `open`。
4. 每 30 秒发送 `{ type: 'PING' }`。
5. `onmessage` 根据 `envelope.type` 分发。
6. `onclose` 后如果仍有 token，3 秒后自动重连。

收到推送后的处理：

| type | 前端动作 |
| --- | --- |
| `ONLINE_USERS` | 批量更新好友在线状态 |
| `FRIEND_STATUS` | 更新单个好友在线状态 |
| `FRIEND_REQUEST` | 合并到收到的申请 |
| `PRIVATE_MESSAGE` | 加入对应私聊会话，必要时加未读数 |
| `PUBLIC_MESSAGE` | 加入公共消息列表 |
| `MESSAGE_RECALLED` | 将对应消息改为已撤回 |
| `ERROR` | 展示实时连接错误 |

答辩讲法：

> chat store 是前端实时体验的核心。它把 REST 加载的历史消息和 WebSocket 推送的新消息合并，并且处理断线重连、心跳、未读数和撤回同步。

### 10.7 页面组件

#### LoginView

位置：

- `frontend/src/views/LoginView.vue`

流程：

1. 用户输入用户名和密码。
2. 调用 `authStore.login()`。
3. 登录成功后刷新好友数据。
4. 建立 WebSocket 连接。
5. 跳转到重定向地址或 `/chat`。

#### RegisterView

位置：

- `frontend/src/views/RegisterView.vue`

流程：

1. 用户输入用户名、密码、昵称。
2. 调用 `authStore.register()`。
3. 注册成功后直接登录。
4. 刷新好友和连接 WebSocket。
5. 进入聊天页。

#### ChatView

位置：

- `frontend/src/views/ChatView.vue`

功能：

- 左侧公共聊天室和好友列表。
- 好友在线状态小圆点。
- 未读数徽标。
- 聊天消息列表。
- 文本输入。
- 图片上传按钮。
- 消息撤回按钮。
- WebSocket 连接状态。
- 刷新当前会话消息。
- 自动滚动到底部。

核心调用：

- `chatStore.selectPublic()`
- `chatStore.selectFriend(friendId)`
- `chatStore.sendMessage(content)`
- `chatStore.sendImage(file)`
- `chatStore.recallMessage(message)`
- `chatStore.connect()`

#### FriendsView

位置：

- `frontend/src/views/FriendsView.vue`

功能：

- 搜索用户。
- 发送好友申请。
- 查看好友列表。
- 查看在线状态和未读数。
- 接受或拒绝收到的申请。
- 删除好友。

#### DashboardView

位置：

- `frontend/src/views/DashboardView.vue`

功能：

- 调用 `/api/stats/overview`。
- 管理员展示全局统计。
- 普通用户展示个人统计。
- 展示 WebSocket 状态、好友同步状态和账号角色。

#### ProfileView

位置：

- `frontend/src/views/ProfileView.vue`

功能：

- 展示当前用户 ID、用户名、昵称、角色、简介。
- 上传头像。
- 展示实时连接状态和好友数量。
- 退出登录。

### 10.8 AppShell 和 UI 组件

位置：

- `frontend/src/components/AppShell.vue`
- `frontend/src/components/ui/`

`AppShell` 负责：

- 侧边栏导航。
- 顶部面包屑。
- 当前用户下拉菜单。
- 退出登录。
- 深色模式切换。
- 移动端抽屉导航。
- 导航徽标显示好友申请数量和未读消息总数。
- 展示 WebSocket 状态。

UI 组件来自 `components/ui/`，包含按钮、卡片、表格、标签、输入框、下拉菜单、提示、滚动区、Sheet 等。它们让页面风格一致，减少重复样式。

## 11. 关键业务调用链

### 11.1 登录流程

```text
LoginView 表单提交
  -> authStore.login(username, password)
  -> authApi.login()
  -> POST /api/auth/login
  -> AuthController.login()
  -> UserService.login()
  -> UserRepository.findByUsername()
  -> BCrypt 校验密码
  -> JwtTokenProvider.createToken()
  -> 返回 AuthResponse(token, user)
  -> 前端 applySession()
  -> localStorage 保存 token
  -> friendsStore.refreshAll()
  -> chatStore.connect()
  -> 跳转聊天页
```

答辩重点：

- 密码不是明文比较，而是 BCrypt。
- Token 是无状态的，后端不需要保存 session。
- 前端刷新后可以通过 Token 恢复会话。

### 11.2 页面刷新恢复登录态

```text
浏览器刷新
  -> router.beforeEach()
  -> authStore.restore()
  -> authApi.me()
  -> GET /api/auth/me
  -> JwtAuthenticationFilter 解析 Authorization
  -> SecurityContext 写入 LoginUser
  -> AuthController.me()
  -> UserService.currentUser()
  -> 返回 UserProfile
```

如果 Token 失效：

```text
后端返回 40100 或 HTTP 401
  -> Axios 响应拦截器触发 auth:expired
  -> App.vue 监听事件
  -> chatStore.reset()
  -> authStore.clearSession()
  -> 跳转 /login
```

### 11.3 好友申请流程

```text
FriendsView 搜索用户
  -> friendsStore.search()
  -> GET /api/users/search
  -> UserService.searchUsers()
  -> 返回用户列表

点击发送申请
  -> friendsStore.sendRequest(friendId)
  -> POST /api/friends/requests
  -> FriendController.sendRequest()
  -> FriendService.sendRequest()
  -> 检查不能加自己、不能重复申请
  -> friend_relation 写入 PENDING
  -> ChatRealtimeNotifier.notifyFriendRequest()
  -> 如果对方在线，WebSocket 推送 FRIEND_REQUEST
```

答辩重点：

- 好友申请是数据库状态，不依赖对方在线。
- 对方在线时会实时收到通知。
- 对方离线时，下次进入好友页仍可通过接口看到申请。

### 11.4 接受好友申请流程

```text
FriendsView 点击接受
  -> friendsStore.acceptRequest(requestId)
  -> POST /api/friends/requests/{requestId}/accept
  -> FriendService.acceptRequest()
  -> 校验申请是否属于当前用户且状态为 PENDING
  -> 原关系改为 ACCEPTED
  -> 创建或更新反向 ACCEPTED 关系
  -> 删除双方 friend:list 缓存
  -> 返回申请信息
  -> 前端 refreshAll()
```

答辩重点：

- 接受时建立双向关系，方便双方查询好友列表。
- 缓存失效后下次读取会重新查 MySQL。

### 11.5 私聊流程

WebSocket 在线发送：

```text
ChatView 输入消息
  -> chatStore.sendMessage()
  -> activeConversation 是 private
  -> socket.send({ type: PRIVATE_MESSAGE, receiverId, content, messageType })
  -> ChatWebSocketHandler.handleTextMessage()
  -> ChatService.sendPrivateMessage()
  -> ensurePrivateChatAllowed()
  -> private_message 写入 MySQL
  -> Redis 写最近消息和未读数
  -> sessionManager.sendToUser(sender)
  -> sessionManager.sendToUser(receiver)
  -> 两边前端 receivePrivateMessage()
```

WebSocket 不可用时 REST fallback：

```text
chatStore.sendPrivateMessage()
  -> chatApi.sendPrivateMessage()
  -> POST /api/chats/private/{friendId}/messages
  -> ChatController.sendPrivateMessage()
  -> ChatService.sendPrivateMessage()
  -> 前端 addPrivateMessage()
```

答辩重点：

- WebSocket 和 REST 发送最终都复用 `ChatService`。
- 业务校验不会因为走 WebSocket 就绕过。
- 消息先写数据库，再更新缓存和推送。

### 11.6 私聊已读和未读数

```text
进入某好友私聊
  -> chatStore.selectFriend(friendId)
  -> loadPrivateMessages(friendId)
  -> chatApi.markPrivateRead(friendId)
  -> POST /api/chats/private/{friendId}/read
  -> ChatService.markPrivateMessagesAsRead()
  -> 数据库更新 read_at
  -> Redis 删除 unread:{userId}:{friendId}
  -> 前端 friendsStore.clearUnread(friendId)
```

收到非当前会话的私聊：

```text
WebSocket PRIVATE_MESSAGE
  -> chatStore.receivePrivateMessage()
  -> 如果接收者是当前用户，且当前没打开这个好友会话
  -> friendsStore.incrementUnread(friendId)
```

### 11.7 公共聊天室流程

```text
ChatView 选择公共聊天室
  -> chatStore.selectPublic()
  -> GET /api/chats/public/messages
  -> 加载历史

发送公共消息
  -> WebSocket PUBLIC_MESSAGE 或 REST POST /api/chats/public/messages
  -> ChatService.sendPublicMessage()
  -> public_message 写 MySQL
  -> Redis 写 chat:public:recent
  -> ChatRealtimeNotifier.broadcastPublicMessage()
  -> 所有在线 session 收到 PUBLIC_MESSAGE
```

答辩重点：

- 公共消息不要求好友关系。
- 公共消息同样先持久化，再广播。
- 页面刷新后仍可从历史接口恢复消息。

### 11.8 图片消息流程

```text
用户点击图片按钮
  -> ChatView.openImagePicker()
  -> 选择图片
  -> chatStore.sendImage(file)
  -> uploadApi.image(file)
  -> POST /api/uploads/images
  -> MediaController.uploadImage()
  -> MediaStorageService.storeImage()
  -> 校验大小和类型
  -> 保存到 uploads/images
  -> 返回 /uploads/images/{uuid}.{ext}
  -> chatStore.sendMessage(url, 'IMAGE')
  -> 按普通消息流程发送
```

答辩重点：

- 图片上传和消息发送是两个步骤。
- 消息表只保存 URL，不直接保存文件内容。
- 前端通过 `messageType === 'IMAGE'` 判断是否渲染图片。

### 11.9 消息撤回流程

```text
用户点击撤回
  -> chatStore.recallMessage(message)
  -> DELETE /api/chats/private/messages/{messageId}
     或 DELETE /api/chats/public/messages/{messageId}
  -> ChatService.recallPrivateMessage()
     或 ChatService.recallPublicMessage()
  -> 校验权限：本人或管理员
  -> 写 recalled_at
  -> ChatRealtimeNotifier 推送 MESSAGE_RECALLED
  -> 前端 applyRecall()
  -> 消息内容置空，显示“消息已撤回”
```

答辩重点：

- 撤回不是删除，保留记录更利于审计。
- 私聊撤回只推送给发送者和接收者。
- 公共消息撤回广播给所有在线用户。

### 11.10 WebSocket 重连和心跳

```text
chatStore.connect()
  -> socketStatus = connecting
  -> onopen 后 socketStatus = open
  -> 每 30 秒发送 PING
  -> 后端刷新 Redis 在线 TTL
  -> onclose 后 socketStatus = closed
  -> 如果 token 还存在，3 秒后自动 connect()
```

答辩重点：

- 心跳解决异常断线后的在线状态刷新问题。
- 自动重连提升用户体验。
- Redis TTL 是 2 分钟，即使异常断开，在线状态也不会永久存在。

### 11.11 统计概览流程

```text
DashboardView mounted
  -> chatStore.connect()
  -> friendsStore.refreshAll()
  -> statsApi.overview()
  -> GET /api/stats/overview
  -> StatsController.overview()
  -> StatsService.overview(loginUser)
  -> 根据角色返回 GLOBAL 或 PERSONAL
  -> 前端卡片展示
```

## 12. 部署架构

部署文件：

- `docker-compose.yml`
- `backend/Dockerfile`
- `frontend/Dockerfile`
- `frontend/nginx.conf`
- `.env.example`

Compose 服务：

| 服务 | 镜像或构建 | 作用 |
| --- | --- | --- |
| `mysql` | `mysql:8.0` | 业务数据库 |
| `redis` | `redis:7-alpine` | 缓存和在线状态 |
| `backend` | `./backend/Dockerfile` | Spring Boot API 和 WebSocket |
| `frontend` | `./frontend/Dockerfile` | Nginx 托管 Vue 静态资源并反向代理 |

端口：

| 服务 | 容器端口 | 宿主机端口 |
| --- | --- | --- |
| backend | 8080 | 8080 |
| frontend | 80 | 5173 |
| mysql | 3306 | 仅内部网络 |
| redis | 6379 | 仅内部网络 |

后端 Dockerfile：

1. 使用 Maven + JDK 21 构建。
2. `mvn dependency:go-offline` 预下载依赖。
3. `mvn package` 生成 JAR。
4. 使用 JRE 21 Alpine 运行。
5. 安装 `curl` 用于健康检查。

前端 Dockerfile：

1. 使用 Node 22 Alpine 构建。
2. `npm ci` 安装锁定依赖。
3. `npm run build` 生成 `dist/`。
4. 使用 Nginx 1.27 Alpine 托管静态资源。

Nginx 代理：

| 路径 | 代理目标 |
| --- | --- |
| `/api/` | `http://backend:8080/api/` |
| `/ws/` | `http://backend:8080/ws/`，支持 Upgrade |
| `/uploads/` | `http://backend:8080/uploads/` |
| 其他路径 | Vue 单页应用 `index.html` |

一键启动：

```powershell
docker compose up -d --build
```

访问地址：

| 地址 | 用途 |
| --- | --- |
| `http://localhost:5173` | 前端页面 |
| `http://localhost:8080/api/health` | 后端健康检查 |
| `http://localhost:8080/swagger-ui.html` | Swagger UI |

答辩讲法：

> Compose 把每个组件拆成独立容器，MySQL 和 Redis 不暴露到宿主机，后端通过服务名访问它们。浏览器只访问前端端口，Nginx 再把 API、WebSocket 和上传资源代理到后端，这样部署结构清楚，也接近真实前后端分离项目。

## 13. 测试与质量保证

后端测试目录：

- `backend/src/test/java/com/example/javachat/health/HealthControllerTest.java`
- `backend/src/test/java/com/example/javachat/user/AuthControllerTest.java`
- `backend/src/test/java/com/example/javachat/user/UserControllerTest.java`
- `backend/src/test/java/com/example/javachat/friend/FriendControllerTest.java`
- `backend/src/test/java/com/example/javachat/chat/ChatControllerTest.java`
- `backend/src/test/java/com/example/javachat/chat/PublicChatCacheServiceTest.java`
- `backend/src/test/java/com/example/javachat/stats/StatsControllerTest.java`
- `backend/src/test/java/com/example/javachat/websocket/WebSocketSessionManagerTest.java`
- `backend/src/test/java/com/example/javachat/websocket/OnlineStatusServiceTest.java`
- `backend/src/test/java/com/example/javachat/websocket/ChatWebSocketIntegrationTest.java`

测试覆盖方向：

- 健康检查。
- 注册、登录、Token 认证。
- 密码是否 BCrypt 保存。
- 用户头像上传。
- 好友申请、接受、拒绝、删除。
- 私聊发送、历史查询、已读、非好友禁止发送。
- 公共聊天室发送和历史。
- 图片消息和撤回。
- 统计接口按角色返回不同范围。
- WebSocket 连接、私聊、公共消息、心跳、好友申请通知。
- Redis 在线状态和公共消息缓存行为。

后端验证命令：

```powershell
cd backend
mvn test
```

前端验证命令：

```powershell
cd frontend
npm run type-check
npm run build
```

答辩讲法：

> 项目不是只靠手动点页面验证，后端为认证、好友、聊天、统计和 WebSocket 写了自动化测试。前端通过 TypeScript 类型检查和构建来保证接口字段、组件和路由没有明显编译错误。

## 14. 推荐答辩演示顺序

建议准备两个浏览器窗口，一个登录 `admin / 123456`，一个登录或注册普通用户。

1. 先展示项目目录。
   - 说明旧版 `src/` 和新版 `backend/`、`frontend/` 的关系。
   - 展示 `README.md`、`docs/API.md`、`docs/DATABASE.md`。
2. 启动系统。
   - 执行 `docker compose up -d --build`。
   - 打开 `http://localhost:5173`。
   - 打开 `http://localhost:8080/api/health`。
   - 打开 `http://localhost:8080/swagger-ui.html`。
3. 登录。
   - 使用 `admin / 123456` 登录。
   - 说明 JWT 保存和 `/api/auth/me` 恢复登录态。
4. 注册或登录第二个用户。
   - 用另一个窗口注册普通用户，或登录 `testUser3 / 123456`。
5. 好友申请。
   - 在好友页搜索用户。
   - 发送好友申请。
   - 另一个窗口实时看到申请通知或刷新申请列表。
6. 接受好友。
   - 接受申请。
   - 展示双方好友列表和在线状态。
7. 私聊。
   - 两个窗口互相发送文本。
   - 展示实时收发和未读数。
8. 图片消息。
   - 上传一张图片。
   - 说明上传接口返回 URL，消息表保存 URL。
9. 消息撤回。
   - 撤回一条私聊或公共消息。
   - 展示两个窗口同步变成“消息已撤回”。
10. 公共聊天室。
    - 切换公共聊天室。
    - 发送公共消息。
    - 展示所有在线用户收到广播。
11. 个人资料。
    - 上传头像。
    - 说明 `/uploads/avatars/` 和静态资源映射。
12. 系统概览。
    - 管理员查看全局统计。
    - 普通用户查看个人统计。
13. 深色模式。
    - 切换主题并刷新页面，说明保存在 `localStorage`。
14. 总结架构。
    - MySQL 是事实来源。
    - Redis 是可重建缓存。
    - WebSocket 是实时推送通道。
    - Docker Compose 是部署入口。

## 15. 常见答辩问题与回答

### Q1：为什么从 Servlet/JSP 升级到 Spring Boot + Vue？

旧版 Servlet/JSP 更像原型项目，请求处理、登录状态、数据库操作、Socket 连接都比较分散。新版使用 Spring Boot 做后端分层，Vue 做前端页面，前后端通过 REST 和 WebSocket 通信。这样结构更清晰，也更符合现代 Web 项目的开发和部署方式。

### Q2：为什么用 JWT，不用 Session？

JWT 是无状态的，后端不需要保存登录 session，前端每次请求携带 Token 即可。这个项目的前后端是分离部署，JWT 更适合 API 调用和 WebSocket 握手复用。项目中 HTTP 请求通过 `Authorization` 头传 Token，WebSocket 通过 query 参数传 Token。

### Q3：JWT 放在 WebSocket query 参数安全吗？

浏览器原生 WebSocket API 不方便设置自定义 `Authorization` 请求头，所以常见做法是在握手 URL 中传 token。项目在握手阶段立即校验 token，不合法就拒绝连接。真实生产环境还应配合 HTTPS/WSS、较短有效期和更安全的日志脱敏。

### Q4：为什么密码用 BCrypt？

BCrypt 是专门用于密码哈希的算法，会加入盐并且计算成本较高。即使数据库泄露，也不会直接暴露明文密码。项目的 `chat_user.password_hash` 保存的是 BCrypt 哈希，登录时用 `passwordEncoder.matches()` 校验。

### Q5：MySQL 和 Redis 分别保存什么？

MySQL 保存核心业务数据，包括用户、好友关系、私聊消息和公共消息。Redis 保存可重建数据，包括在线状态、好友列表缓存、最近消息缓存和未读数。Redis 丢失不会导致消息丢失，因为消息最终以 MySQL 为准。

### Q6：如何保证消息不丢？

发送消息时先经过 `ChatService` 校验，再写入 MySQL。写库成功后才更新 Redis 和 WebSocket 推送。即使接收者离线，或者 WebSocket 推送失败，消息仍然在数据库中。用户下次进入聊天页面会通过历史接口加载。

### Q7：私聊为什么要求双方是好友？

这是业务规则，防止任意用户给陌生人发私聊。后端在 `ensurePrivateChatAllowed()` 中检查双方是否存在 `ACCEPTED` 好友关系。无论消息通过 REST 还是 WebSocket 发送，都会复用这个 Service 校验。

### Q8：好友关系为什么保存双向记录？

因为聊天系统读取好友列表非常频繁。保存双向记录后，查询某个用户的好友只需要查 `user_id = 当前用户` 的 `ACCEPTED` 关系。接受申请时补齐反向记录，删除时双方都标记为 `DELETED`。

### Q9：在线状态是怎么实现的？

用户建立 WebSocket 连接后，后端在 Redis 写入 `online:user:{userId}`，过期时间 2 分钟。前端每 30 秒发心跳，后端刷新 TTL。正常断开时删除在线状态并通知好友。异常断开时，TTL 到期后状态自然失效。

### Q10：一个用户打开多个窗口怎么办？

`WebSocketSessionManager` 允许一个用户对应多个 WebSocket session。关闭一个窗口时只移除对应 session。只有该用户所有 session 都关闭后，才会真正标记离线并通知好友。

### Q11：WebSocket 和 REST 的关系是什么？

REST 用于登录、注册、好友管理、历史消息、上传、统计等请求。WebSocket 用于实时消息、在线状态、好友申请通知、撤回通知。对于发送消息，前端优先走 WebSocket，如果连接不可用，可以 fallback 到 REST 接口。后端最终都复用 `ChatService`。

### Q12：消息撤回为什么不直接删除？

直接删除会影响历史记录和审计，也可能影响分页。项目使用 `recalled_at` 标记撤回，返回给前端时把内容置空并显示“消息已撤回”。这样既能满足用户体验，又保留了记录。

### Q13：图片消息如何实现？

先调用上传接口保存图片，后端返回 `/uploads/images/...` URL。然后把这个 URL 作为消息 `content`，并设置 `messageType=IMAGE` 发送。前端看到图片类型后渲染 `<img>`。

### Q14：上传文件如何防止路径问题？

后端不使用用户原始文件名，而是使用 UUID 生成文件名。保存路径经过 normalize，并检查目标路径是否仍在目标目录下，防止路径穿越。同时限制图片大小和 MIME 类型。

### Q15：为什么前端要用 Pinia？

聊天系统有很多跨页面共享状态，例如当前用户、好友列表、未读数、WebSocket 连接状态和消息列表。Pinia 可以集中管理这些状态，让页面组件只负责展示和触发动作。

### Q16：Token 失效时前端怎么处理？

Axios 响应拦截器发现 `40100` 或 HTTP 401 后派发 `auth:expired` 事件。`App.vue` 监听该事件，重置聊天状态、清理登录状态并跳转登录页。

### Q17：Docker Compose 有什么意义？

项目依赖 MySQL、Redis、后端和前端。Compose 可以一条命令启动所有服务，并通过内部服务名连接。这样答辩演示时不需要手动安装和配置多个组件。

### Q18：Nginx 在前端容器里做什么？

Nginx 托管 Vue 构建后的静态文件，同时把 `/api`、`/ws`、`/uploads` 代理到后端。浏览器只访问 `localhost:5173`，避免前端部署后跨域和 WebSocket 地址混乱。

### Q19：这个项目有哪些工程化亮点？

- 前后端分离。
- 后端 Controller、Service、Repository 分层。
- JWT + Spring Security 统一鉴权。
- MySQL + Redis 职责清晰。
- WebSocket 实时通信。
- Docker Compose 一键部署。
- Flyway 自动初始化数据库。
- OpenAPI/Swagger 接口文档。
- 自动化测试覆盖主要后端能力。
- 前端 TypeScript 类型对齐后端响应。

### Q20：还有哪些可以继续改进？

- 使用 WSS/HTTPS 部署，保护 Token 和消息传输。
- 引入 Refresh Token 或 Token 黑名单，实现更严格的退出登录。
- 多后端实例部署时，用 Redis Pub/Sub 或消息队列广播 WebSocket 事件。
- 使用对象存储保存上传图片。
- 增加消息搜索、群聊、文件消息、语音消息。
- 增加管理员用户管理和内容审核。
- 前端增加更完整的端到端测试。

## 16. 项目亮点总结

答辩最后可以这样总结：

1. 项目有明确迁移价值：从传统 Java Web 原型升级为现代前后端分离工程。
2. 后端分层清楚：Controller 处理入口，Service 处理业务，Repository 处理数据。
3. 安全性有提升：JWT 鉴权、BCrypt 密码、统一异常响应、接口权限控制。
4. 实时性完整：WebSocket 支持私聊、公共消息、好友状态、申请通知、撤回通知。
5. 数据可靠性清楚：MySQL 是事实来源，Redis 只做缓存和状态。
6. 前端体验完整：登录、注册、好友、聊天、图片、撤回、统计、头像、深色模式都有页面。
7. 部署方式工程化：Docker Compose 一键启动 MySQL、Redis、后端、前端。
8. 文档和测试较完整：已有 API、数据库、答辩、截图、报告和后端测试。

## 17. 三分钟答辩浓缩稿

如果老师只给很短时间，可以按下面讲：

> JavaChatSer 是一个在线即时聊天系统。项目最初是一个传统 Servlet/JSP 聊天原型，里面有用户、好友、私聊、公共聊天室、Redis 缓存和原生 Socket。新版在保留业务核心的基础上，用 Spring Boot 3 和 Vue 3 做了前后端分离重构。
>
> 后端使用 Java 21、Spring Web、Spring Security、JWT、JPA、Flyway、Redis 和 Spring WebSocket。用户登录后后端返回 JWT，前端请求时携带 Token，后端通过过滤器解析身份。用户、好友、消息都持久化到 MySQL，Redis 保存在线状态、好友缓存、最近消息和未读数。实时通信使用 WebSocket，连接时通过 JWT 握手校验，消息发送后先写 MySQL，再写 Redis，最后推送给在线用户，所以即使 WebSocket 失败也不会丢消息。
>
> 前端使用 Vue 3、Vite、TypeScript、Vue Router、Pinia、Axios 和原生 WebSocket。登录态、好友列表和聊天状态都放在 Pinia 中统一管理。聊天页支持公共聊天室、好友私聊、在线状态、未读数、图片消息和消息撤回。好友页支持搜索、申请、接受、拒绝和删除。概览页可以按管理员或普通用户展示不同统计。
>
> 部署上，项目使用 Docker Compose 一键启动 MySQL、Redis、Spring Boot 后端和 Vue/Nginx 前端。Nginx 托管前端静态资源，并代理 `/api`、`/ws` 和 `/uploads` 到后端。这个项目的重点不是只实现聊天功能，而是把传统 Java Web 原型升级成一个结构清晰、安全性更好、可部署、可测试、可答辩展示的完整工程。

## 18. 答辩时可指向的文件

| 讲解点 | 文件 |
| --- | --- |
| 项目总览 | `README.md` |
| 升级过程 | `docs/SPRING_BOOT_VUE_UPGRADE_GUIDE.md` |
| 短版答辩指南 | `docs/DEFENSE_GUIDE.md` |
| 接口文档 | `docs/API.md` |
| 数据库和 Redis | `docs/DATABASE.md` |
| 后端依赖 | `backend/pom.xml` |
| 后端配置 | `backend/src/main/resources/application.yml` |
| 数据库迁移 | `backend/src/main/resources/db/migration/` |
| 认证安全 | `backend/src/main/java/com/example/javachat/security/` |
| 用户模块 | `backend/src/main/java/com/example/javachat/user/` |
| 好友模块 | `backend/src/main/java/com/example/javachat/friend/` |
| 聊天模块 | `backend/src/main/java/com/example/javachat/chat/` |
| WebSocket | `backend/src/main/java/com/example/javachat/websocket/` |
| 上传模块 | `backend/src/main/java/com/example/javachat/media/` |
| 统计模块 | `backend/src/main/java/com/example/javachat/stats/` |
| 前端依赖 | `frontend/package.json` |
| 路由守卫 | `frontend/src/router/index.ts` |
| API 封装 | `frontend/src/api/` |
| 登录状态 | `frontend/src/stores/auth.ts` |
| 好友状态 | `frontend/src/stores/friends.ts` |
| 聊天状态 | `frontend/src/stores/chat.ts` |
| 聊天页面 | `frontend/src/views/ChatView.vue` |
| 好友页面 | `frontend/src/views/FriendsView.vue` |
| 概览页面 | `frontend/src/views/DashboardView.vue` |
| Docker 编排 | `docker-compose.yml` |
| 前端 Nginx | `frontend/nginx.conf` |

## 19. 最后检查清单

答辩前建议确认：

- `docker compose up -d --build` 可以启动。
- `http://localhost:5173` 可以访问。
- `http://localhost:8080/api/health` 返回成功。
- `http://localhost:8080/swagger-ui.html` 可以打开。
- `admin / 123456` 可以登录。
- 第二个账号可以注册或用 `testUser3 / 123456` 登录。
- 好友申请、接受、私聊、公共消息、图片消息、撤回都能演示。
- `docs/screenshots/` 中截图可以用于报告或 PPT。
- 能解释“为什么 MySQL 是最终事实来源，Redis 是缓存，WebSocket 是实时通道”。
- 能解释“HTTP JWT 鉴权”和“WebSocket JWT 握手校验”的区别。
