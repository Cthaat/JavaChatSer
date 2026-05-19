# JavaChatSer Spring Boot + Vue 升级开发文档

本文档用于指导后续 AI 或开发者将当前 `JavaChatSer` 项目升级为一个完整、可运行、可展示、可答辩的 Spring Boot + Vue 聊天系统大作业。执行时必须以现有项目为起点，保留“用户、好友、私聊、公共聊天室、Redis 缓存、实时通信”的核心业务，但要用现代工程结构重新实现。

## 1. 项目现状分析

### 1.1 当前技术形态

当前项目是传统 Java Web 项目：

| 模块 | 当前实现 | 主要文件 |
| --- | --- | --- |
| 构建方式 | Maven WAR 包 | `pom.xml` |
| Web 容器 | 外部 Tomcat / Tomcat Maven Plugin | `pom.xml`, `Dockerfile` |
| 接口层 | Servlet 注解路由 | `src/main/java/org/http/*.java` |
| 页面层 | JSP 占位页 | `src/webapp/index.jsp` |
| 数据访问 | Spring JDBC `JdbcTemplate` + Druid | `src/main/java/org/Sql/*.java` |
| 数据库 | MySQL | `src/main/resources/javaChat.sql` |
| 缓存 | Redis + Jedis | `src/main/resources/redis.properties` |
| 实时通信 | Java 原生 `ServerSocket` | `chatSocketSer.java`, `chatReceiveSocketThread.java` |
| 测试 | JUnit 4 手写数据库测试 | `src/test/java/*.java` |
| 部署 | 单个 Dockerfile，尝试同时启动 Tomcat/MySQL/Redis | `Dockerfile`, `start.sh` |

### 1.2 已有业务能力

当前项目已经具备以下原型能力：

1. 登录：`/logoInResp` 根据请求头 `userName/password` 查询 `users` 表。
2. 注册：`/signUp` 根据请求头 `username/password` 新增用户。
3. 好友列表：`/loadAllFriendMessage` 从 Cookie 中取当前用户名，查询 `p2p_relationship`。
4. 添加好友：`/addMyFriend` 双向写入好友关系。
5. 删除好友：`/delMyFriend` 双向删除好友关系。
6. 拉取私聊消息：`/getp2pMessages` 查询两名用户之间的 `p2p_text`。
7. 发送私聊消息：`/sendP2pMessages` 写入 `p2p_text`。
8. 公共聊天室缓存：`publicRoomfind` 可以把 `public_chat_room` 加载进 Redis。
9. 在线转发：`chatSocketSer` 在 `10086` 端口启动原生 Socket 服务，`chatReceiveSocketThread` 根据用户名转发消息。

### 1.3 当前主要问题

这些问题必须在升级时解决，不能原样迁移：

| 问题 | 影响 | 升级要求 |
| --- | --- | --- |
| 不是 Spring Boot 项目 | 不利于课程展示和现代开发 | 改为 Spring Boot 主应用，内嵌 Web 容器 |
| Servlet 直接处理业务 | 分层不清晰，难维护 | 拆成 Controller / Service / Repository |
| JSP 只是占位 | 没有真实前端 | 使用 Vue 3 开发完整聊天前端 |
| 请求参数放 Header | 不符合 REST 习惯 | 改为 JSON Body、Path、Query |
| Cookie 保存明文用户信息 | 安全风险 | 改为 JWT 或 Session Token |
| 密码明文存储 | 严重安全风险 | 使用 BCrypt 哈希 |
| 原生 Socket 独立端口 | 浏览器 Vue 前端难直接使用 | 改为 Spring WebSocket 或 STOMP |
| `HashMap<String, Socket>` 非线程安全 | 并发风险 | 使用 Spring WebSocket Session 管理 |
| SQL 表字段冗余且无约束 | 数据一致性弱 | 重新设计表结构和索引 |
| Redis 连接到处手动创建 | 资源浪费，重复代码多 | 使用 Spring Data Redis 统一封装 |
| Dockerfile 同时启动多个服务 | 不符合容器最佳实践 | 使用 Docker Compose 拆分后端、前端、MySQL、Redis |
| 测试依赖真实本地数据库 | 不稳定 | 使用 Spring Boot Test + Testcontainers 或独立测试库 |

## 2. 升级目标

### 2.1 总目标

将项目升级为“在线即时聊天系统”，作为 Spring Boot 开发大作业提交。系统应包含：

1. 用户注册、登录、退出、个人信息展示。
2. JWT 登录认证和接口权限控制。
3. 好友搜索、添加、删除、好友列表。
4. 一对一私聊：历史消息分页、实时消息推送、未读消息计数。
5. 公共聊天室：实时群聊、历史消息分页。
6. 在线状态：好友在线/离线提示。
7. Vue 前端：登录页、注册页、聊天主界面、好友管理页、个人资料页。
8. MySQL 持久化、Redis 缓存/在线状态/未读计数。
9. 标准项目文档、接口文档、数据库脚本、启动教程。
10. 可本地运行、可截图展示、可答辩说明。

### 2.2 推荐技术栈

后端：

| 类别 | 技术 |
| --- | --- |
| 基础框架 | Spring Boot 3.x |
| Java 版本 | Java 21，延续当前项目设置 |
| 构建工具 | Maven |
| Web | Spring Web |
| 实时通信 | Spring WebSocket，建议 STOMP + SockJS 可选 |
| 安全认证 | Spring Security + JWT + BCrypt |
| 数据访问 | Spring Data JPA 或 MyBatis Plus，二选一 |
| 数据库 | MySQL 8 |
| 缓存 | Spring Data Redis |
| 参数校验 | Spring Validation |
| 接口文档 | springdoc-openapi / Swagger UI |
| 测试 | JUnit 5 + Spring Boot Test |

前端：

| 类别 | 技术 |
| --- | --- |
| 构建工具 | Vite |
| 框架 | Vue 3 |
| 状态管理 | Pinia |
| 路由 | Vue Router |
| HTTP | Axios |
| UI 组件 | Element Plus |
| WebSocket | 原生 WebSocket 或 STOMP 客户端 |
| 样式 | SCSS 或普通 CSS |

### 2.3 目标仓库结构

推荐将项目改造成前后端分离结构：

```text
JavaChatSer/
  backend/
    pom.xml
    src/main/java/com/example/javachat/
      JavaChatApplication.java
      common/
      config/
      security/
      user/
      friend/
      chat/
      websocket/
    src/main/resources/
      application.yml
      db/migration/
  frontend/
    package.json
    index.html
    src/
      main.ts
      router/
      stores/
      api/
      views/
      components/
      styles/
  docs/
    SPRING_BOOT_VUE_UPGRADE_GUIDE.md
    API.md
    DATABASE.md
  docker-compose.yml
  README.md
```

如果课程要求必须保留单 Maven 工程，也可以把 Vue 构建产物放入 `backend/src/main/resources/static`。但推荐前后端分离，因为更符合 Vue 项目展示和后续维护。

## 3. 业务范围设计

### 3.1 用户模块

必须实现：

1. 注册账号。
2. 登录获取 Token。
3. 获取当前登录用户信息。
4. 修改昵称、头像、个人简介。
5. 退出登录，前端清除 Token。

注册字段：

| 字段 | 必填 | 规则 |
| --- | --- | --- |
| username | 是 | 4-20 位，唯一，只允许字母、数字、下划线 |
| password | 是 | 6-32 位，后端 BCrypt 加密 |
| nickname | 否 | 默认等于 username |
| avatarUrl | 否 | 默认头像 |

登录字段：

```json
{
  "username": "admin",
  "password": "123456"
}
```

登录响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "jwt-token",
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "Admin",
      "avatarUrl": "/default-avatar.png"
    }
  }
}
```

### 3.2 好友模块

必须实现：

1. 根据用户名搜索用户。
2. 发送好友申请。
3. 接受或拒绝好友申请。
4. 查询好友列表。
5. 删除好友。
6. 显示好友在线状态和未读消息数。

说明：当前项目是直接添加好友，升级后建议加入“好友申请”流程，这样更适合作为大作业展示。

### 3.3 私聊模块

必须实现：

1. 获取与某好友的历史消息，支持分页。
2. 发送文本消息。
3. 在线时通过 WebSocket 实时推送。
4. 离线时消息仍写入数据库，上线后可查看。
5. 消息已读状态。
6. 未读消息计数。

消息类型第一阶段只做 `TEXT`，后续可扩展图片、文件。

### 3.4 公共聊天室模块

必须实现：

1. 公共聊天室消息列表。
2. 发送公共消息。
3. 在线用户进入/离开提示。
4. 最近消息缓存到 Redis。

### 3.5 管理和展示模块

作为大作业建议增加一个“系统概览”页面：

1. 当前注册用户数。
2. 当前在线用户数。
3. 今日消息数。
4. 公共聊天室消息数。

此模块不需要复杂权限，普通用户可查看自己的统计，管理员可查看全局统计。

## 4. 数据库设计

### 4.1 现有表映射

| 当前表 | 用途 | 升级处理 |
| --- | --- | --- |
| `users` | 用户账号 | 改造为 `chat_user`，密码改为哈希 |
| `p2p_relationship` | 好友关系 | 改造为 `friend_relation`，增加状态和时间 |
| `p2p_text` | 私聊消息 | 改造为 `private_message` |
| `public_chat_room` | 公共消息 | 改造为 `public_message` |

### 4.2 新表结构

#### chat_user

```sql
CREATE TABLE chat_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  avatar_url VARCHAR(255) DEFAULT NULL,
  bio VARCHAR(255) DEFAULT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### friend_relation

```sql
CREATE TABLE friend_relation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  friend_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_friend (user_id, friend_id),
  INDEX idx_friend_user (friend_id),
  CONSTRAINT fk_friend_user FOREIGN KEY (user_id) REFERENCES chat_user(id),
  CONSTRAINT fk_friend_friend FOREIGN KEY (friend_id) REFERENCES chat_user(id)
);
```

`status` 可选值：

| 值 | 含义 |
| --- | --- |
| `PENDING` | 待确认 |
| `ACCEPTED` | 已成为好友 |
| `REJECTED` | 已拒绝 |
| `DELETED` | 已删除 |

#### private_message

```sql
CREATE TABLE private_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sender_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
  read_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_private_pair_time (sender_id, receiver_id, created_at),
  INDEX idx_private_receiver_read (receiver_id, read_at),
  CONSTRAINT fk_private_sender FOREIGN KEY (sender_id) REFERENCES chat_user(id),
  CONSTRAINT fk_private_receiver FOREIGN KEY (receiver_id) REFERENCES chat_user(id)
);
```

#### public_message

```sql
CREATE TABLE public_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sender_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_public_time (created_at),
  CONSTRAINT fk_public_sender FOREIGN KEY (sender_id) REFERENCES chat_user(id)
);
```

### 4.3 Redis Key 设计

| Key | 类型 | 说明 | TTL |
| --- | --- | --- | --- |
| `online:user:{userId}` | String | 用户在线状态，值为 WebSocket sessionId | 2 分钟，心跳续期 |
| `friend:list:{userId}` | List / JSON | 好友列表缓存 | 5 分钟 |
| `chat:private:{minUserId}:{maxUserId}` | List | 最近私聊消息缓存 | 1 天 |
| `chat:public:recent` | List | 公共聊天室最近消息 | 1 天 |
| `unread:{userId}:{friendId}` | String/Number | 单个好友未读数 | 无固定 TTL |

缓存原则：

1. 数据库是最终事实来源。
2. 发送消息必须先写 MySQL，再更新 Redis，再推送 WebSocket。
3. 好友变更后必须删除 `friend:list:{userId}` 和 `friend:list:{friendId}`。
4. Redis 不可用时，核心业务仍应能通过 MySQL 工作。

## 5. 后端架构设计

### 5.1 包结构

```text
com.example.javachat
  JavaChatApplication.java
  common/
    ApiResponse.java
    PageResponse.java
    BusinessException.java
    GlobalExceptionHandler.java
  config/
    CorsConfig.java
    RedisConfig.java
    OpenApiConfig.java
    WebSocketConfig.java
  security/
    JwtTokenProvider.java
    JwtAuthenticationFilter.java
    SecurityConfig.java
    LoginUser.java
  user/
    User.java
    UserRepository.java
    UserService.java
    UserController.java
    dto/
  friend/
    FriendRelation.java
    FriendRepository.java
    FriendService.java
    FriendController.java
    dto/
  chat/
    PrivateMessage.java
    PublicMessage.java
    MessageRepository.java
    ChatService.java
    ChatController.java
    dto/
  websocket/
    ChatWebSocketHandler.java
    WebSocketSessionManager.java
    WebSocketMessage.java
```

### 5.2 统一响应结构

所有接口必须返回统一结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

错误示例：

```json
{
  "code": 40001,
  "message": "用户名或密码错误",
  "data": null
}
```

建议错误码：

| 错误码 | 含义 |
| --- | --- |
| `0` | 成功 |
| `40000` | 参数错误 |
| `40001` | 登录失败 |
| `40100` | 未登录或 Token 无效 |
| `40300` | 无权限 |
| `40400` | 数据不存在 |
| `40900` | 数据冲突，例如重复好友 |
| `50000` | 服务端错误 |

### 5.3 REST API 设计

#### 认证接口

| 方法 | 路径 | 说明 | 是否登录 |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | 注册 | 否 |
| POST | `/api/auth/login` | 登录 | 否 |
| GET | `/api/auth/me` | 当前用户 | 是 |
| POST | `/api/auth/logout` | 退出 | 是 |

#### 用户接口

| 方法 | 路径 | 说明 | 是否登录 |
| --- | --- | --- | --- |
| GET | `/api/users/search?keyword=` | 搜索用户 | 是 |
| PUT | `/api/users/me` | 修改个人信息 | 是 |

#### 好友接口

| 方法 | 路径 | 说明 | 是否登录 |
| --- | --- | --- | --- |
| GET | `/api/friends` | 好友列表 | 是 |
| POST | `/api/friends/requests` | 发送好友申请 | 是 |
| GET | `/api/friends/requests` | 收到的好友申请 | 是 |
| POST | `/api/friends/requests/{requestId}/accept` | 接受申请 | 是 |
| POST | `/api/friends/requests/{requestId}/reject` | 拒绝申请 | 是 |
| DELETE | `/api/friends/{friendId}` | 删除好友 | 是 |

#### 私聊接口

| 方法 | 路径 | 说明 | 是否登录 |
| --- | --- | --- | --- |
| GET | `/api/chats/private/{friendId}/messages?page=&size=` | 私聊历史 | 是 |
| POST | `/api/chats/private/{friendId}/messages` | 发送私聊消息 | 是 |
| POST | `/api/chats/private/{friendId}/read` | 标记已读 | 是 |

#### 公共聊天室接口

| 方法 | 路径 | 说明 | 是否登录 |
| --- | --- | --- | --- |
| GET | `/api/chats/public/messages?page=&size=` | 公共消息历史 | 是 |
| POST | `/api/chats/public/messages` | 发送公共消息 | 是 |

#### 统计接口

| 方法 | 路径 | 说明 | 是否登录 |
| --- | --- | --- | --- |
| GET | `/api/stats/overview` | 系统概览 | 是 |

### 5.4 WebSocket 设计

推荐路径：

```text
/ws/chat?token=JWT
```

连接建立流程：

1. 前端登录后拿到 JWT。
2. 前端连接 `/ws/chat?token=...`。
3. 后端握手阶段解析 JWT，确认用户身份。
4. 后端记录 `userId -> session`，并写 Redis 在线状态。
5. 前端收到 `ONLINE_USERS` 或 `FRIEND_ONLINE` 事件后更新好友列表状态。

前端发送私聊消息：

```json
{
  "type": "PRIVATE_MESSAGE",
  "receiverId": 2,
  "content": "你好"
}
```

后端推送私聊消息：

```json
{
  "type": "PRIVATE_MESSAGE",
  "data": {
    "id": 1001,
    "senderId": 1,
    "receiverId": 2,
    "content": "你好",
    "createdAt": "2026-05-19 20:30:00"
  }
}
```

后端推送好友状态：

```json
{
  "type": "FRIEND_STATUS",
  "data": {
    "userId": 2,
    "online": true
  }
}
```

### 5.5 安全要求

1. 密码必须使用 BCrypt，禁止明文存储。
2. 登录接口返回 JWT，前端放在 Pinia 和 `localStorage`。
3. Axios 请求统一带 `Authorization: Bearer <token>`。
4. 后端除注册、登录、Swagger 外，其他接口默认需要认证。
5. CORS 只允许前端开发端口和生产域名，不要放开所有来源到生产。
6. 请求体必须使用 DTO + Validation，不允许直接接收 Entity。
7. 不要把数据库、Redis 密码写死在代码里，必须放入 `application.yml` 或环境变量。

## 6. 前端架构设计

### 6.1 页面清单

| 页面 | 路由 | 功能 |
| --- | --- | --- |
| 登录页 | `/login` | 登录、跳转注册 |
| 注册页 | `/register` | 注册账号 |
| 聊天主页 | `/chat` | 左侧好友/公共聊天室，右侧消息窗口 |
| 好友管理 | `/friends` | 搜索用户、处理好友申请 |
| 个人资料 | `/profile` | 修改昵称、头像、简介 |
| 系统概览 | `/dashboard` | 展示用户数、在线数、消息数 |

### 6.2 前端目录结构

```text
frontend/src/
  main.ts
  App.vue
  router/index.ts
  stores/auth.ts
  stores/chat.ts
  stores/friend.ts
  api/http.ts
  api/auth.ts
  api/user.ts
  api/friend.ts
  api/chat.ts
  websocket/chatSocket.ts
  views/LoginView.vue
  views/RegisterView.vue
  views/ChatView.vue
  views/FriendView.vue
  views/ProfileView.vue
  views/DashboardView.vue
  components/chat/ConversationList.vue
  components/chat/MessagePanel.vue
  components/chat/MessageInput.vue
  components/layout/AppLayout.vue
  styles/global.scss
```

### 6.3 聊天主页布局

聊天主页必须直接可用，不做营销页：

```text
+------------------------------------------------+
| 顶部栏：项目名 / 当前用户 / 在线状态 / 退出       |
+------------------+-----------------------------+
| 左侧会话列表       | 右侧聊天窗口                  |
| - 公共聊天室       | - 消息历史                    |
| - 好友 A          | - 实时新消息                  |
| - 好友 B          | - 输入框 + 发送按钮           |
+------------------+-----------------------------+
```

交互要求：

1. 点击好友切换私聊会话。
2. 点击公共聊天室切换公共聊天。
3. 新消息自动追加到当前会话。
4. 非当前会话收到消息时，左侧显示未读数。
5. 发送消息后输入框清空。
6. 网络或 WebSocket 断开时顶部显示离线状态，并自动重连。

### 6.4 前端状态管理

`authStore`：

| 状态 | 说明 |
| --- | --- |
| `token` | JWT |
| `user` | 当前用户 |
| `isLoggedIn` | 是否登录 |

`friendStore`：

| 状态 | 说明 |
| --- | --- |
| `friends` | 好友列表 |
| `requests` | 好友申请 |

`chatStore`：

| 状态 | 说明 |
| --- | --- |
| `activeConversation` | 当前会话 |
| `messagesByConversation` | 会话消息 Map |
| `unreadByUser` | 未读计数 |
| `socketConnected` | WebSocket 状态 |

## 7. 分阶段开发计划

后续 AI 必须按阶段执行。每个阶段完成后都要运行对应验证，再进入下一阶段。

### 阶段 0：保护现有项目和确定迁移方式

目标：不破坏当前项目，建立新结构。

任务：

1. 新建 `backend/` 和 `frontend/`。
2. 保留旧代码在当前 `src/` 中，作为业务参考。
3. 新建根目录 `README.md`，说明旧项目和新项目关系。
4. 新建 `docker-compose.yml`，规划 MySQL、Redis、backend、frontend 服务。

验收：

1. 旧代码没有被误删。
2. 新目录结构清晰。
3. `git status` 中只出现预期新增/修改文件。

### 阶段 1：创建 Spring Boot 后端骨架

目标：后端应用可以启动，具备基础配置。

任务：

1. 在 `backend/` 创建 Spring Boot Maven 项目。
2. 添加依赖：Web、Validation、Security、Data JPA 或 MyBatis Plus、MySQL Driver、Redis、WebSocket、OpenAPI、Test。
3. 创建 `JavaChatApplication`。
4. 创建 `application.yml`，配置 server port、datasource、redis、jwt。
5. 创建统一响应 `ApiResponse`、全局异常处理 `GlobalExceptionHandler`。
6. 创建健康检查接口 `GET /api/health`。

验收：

1. `backend` 可以启动。
2. `GET /api/health` 返回 `success`。
3. 后端没有连接数据库时的错误可读，不出现难以理解的堆栈暴露给前端。

### 阶段 2：数据库迁移和实体建模

目标：建立新的数据库表和实体模型。

任务：

1. 在 `backend/src/main/resources/db/migration/` 添加初始化 SQL。
2. 创建 `User`、`FriendRelation`、`PrivateMessage`、`PublicMessage` 实体。
3. 创建 Repository 或 Mapper。
4. 添加基础索引和唯一约束。
5. 写入测试用户种子数据，密码使用 BCrypt 结果。

验收：

1. MySQL 启动后能初始化表。
2. 表字段和本文档一致。
3. 用户名唯一约束有效。

### 阶段 3：认证模块

目标：完成注册、登录、当前用户接口。

任务：

1. 实现 `AuthController`。
2. 实现 `UserService.register`。
3. 实现 `UserService.login`。
4. 实现 JWT 生成和解析。
5. 实现 Spring Security 过滤器。
6. 实现 `GET /api/auth/me`。
7. 写认证接口测试。

验收：

1. 注册成功后数据库密码不是明文。
2. 重复用户名返回 `40900`。
3. 登录成功返回 JWT。
4. 未带 Token 访问 `/api/auth/me` 返回 `40100`。
5. 带正确 Token 返回当前用户。

### 阶段 4：好友模块

目标：完成好友搜索、申请、同意、拒绝、删除。

任务：

1. 实现 `UserController.searchUsers`。
2. 实现 `FriendController`。
3. 好友申请写入 `friend_relation`。
4. 接受申请时双向建立 `ACCEPTED` 关系。
5. 删除好友时双向标记 `DELETED` 或删除记录。
6. Redis 缓存好友列表。

验收：

1. 不能添加自己为好友。
2. 不能重复添加同一好友。
3. 接受申请后双方好友列表都能看到对方。
4. 删除好友后双方好友列表都消失。

### 阶段 5：私聊历史和发送接口

目标：不依赖 WebSocket，也能完成消息收发和历史查询。

任务：

1. 实现 `ChatController` 私聊接口。
2. 发送消息时校验双方是好友。
3. 消息写入 `private_message`。
4. 历史消息按时间升序展示，分页查询。
5. 标记已读接口更新 `read_at`。
6. Redis 缓存最近消息和未读数。

验收：

1. 非好友不能发送私聊消息。
2. 发送后能在历史消息中查询到。
3. 分页参数有效。
4. 当前会话标记已读后未读数清零。

### 阶段 6：WebSocket 实时通信

目标：完成浏览器实时收发消息和在线状态。

任务：

1. 配置 `/ws/chat`。
2. 握手时校验 JWT。
3. 实现 `WebSocketSessionManager`。
4. 用户上线写 Redis，通知好友。
5. 用户离线删除在线状态，通知好友。
6. 私聊消息发送后，如果接收者在线则立即推送。
7. 公共消息发送后广播给所有在线用户。
8. 实现心跳或断线清理。

验收：

1. 两个浏览器登录不同用户，能实时收到私聊消息。
2. 刷新页面后 WebSocket 能重连。
3. 关闭浏览器后好友看到离线状态。
4. 离线用户再次登录后能看到历史消息。

### 阶段 7：公共聊天室

目标：完成公共聊天页面和后端接口。

任务：

1. 实现公共消息历史分页。
2. 实现公共消息发送接口。
3. 发送后写 MySQL、写 Redis、WebSocket 广播。
4. Vue 页面显示公共聊天室。

验收：

1. 多用户同时在线时公共消息实时出现。
2. 刷新后公共消息仍可从历史接口加载。
3. Redis 缓存最近消息，不影响 MySQL 持久化。

### 阶段 8：Vue 前端

目标：前端完整可用，并与后端联调。

任务：

1. 创建 Vite + Vue 3 项目。
2. 配置 Vue Router。
3. 配置 Pinia。
4. 封装 Axios，自动附带 Token，统一处理 `40100`。
5. 完成登录页和注册页。
6. 完成主聊天布局。
7. 完成好友管理页。
8. 完成个人资料页。
9. 完成 WebSocket 客户端封装。
10. 完成未读消息、在线状态、自动重连。

验收：

1. 用户可以从注册到登录再进入聊天主页。
2. 可以搜索用户并添加好友。
3. 可以打开两个浏览器窗口完成实时聊天。
4. 页面刷新后登录状态恢复。
5. Token 失效时自动回到登录页。

### 阶段 9：部署和文档

目标：项目可一键启动、可演示、可答辩。

任务：

1. 完成根目录 `docker-compose.yml`。
2. 后端 Dockerfile 独立构建 Spring Boot JAR。
3. 前端 Dockerfile 独立构建并使用 Nginx 托管。
4. README 写清楚环境要求、启动命令、账号密码、功能截图位置。
5. 编写 `docs/API.md`。
6. 编写 `docs/DATABASE.md`。
7. 编写 `docs/DEFENSE_GUIDE.md`，用于答辩讲解。

验收：

1. `docker compose up -d --build` 能启动 MySQL、Redis、后端、前端。
2. 前端可以访问后端接口。
3. README 中的命令逐条可执行。
4. 答辩文档能解释架构、数据流、接口、缓存、WebSocket。

## 8. AI 执行规范

后续 AI 开发时必须遵守：

1. 先读本文档，再读当前代码。
2. 每次只完成一个阶段，不跨阶段大改。
3. 保留旧 `src/` 代码作为参考，除非明确进入清理阶段。
4. 不允许复制旧代码中的明文密码、Header 参数认证、Cookie 明文身份逻辑。
5. 所有新接口必须有 DTO，不直接暴露 Entity。
6. 所有新增后端接口必须在 Swagger 中可见。
7. 所有前端 API 必须通过 `frontend/src/api/` 统一封装。
8. 所有 WebSocket 消息必须有明确 `type`。
9. 每个阶段完成后必须运行验证命令。
10. 如果验证失败，先修复当前阶段，不继续做下一阶段。

## 9. 推荐验证命令

后端：

```powershell
cd backend
mvn test
mvn spring-boot:run
```

前端：

```powershell
cd frontend
npm install
npm run type-check
npm run build
npm run dev
```

Docker：

```powershell
docker compose config
docker compose up -d --build
docker compose ps
```

接口冒烟测试：

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

## 10. 大作业展示重点

答辩时应重点讲清楚：

1. 为什么从 Servlet/JSP 升级到 Spring Boot + Vue。
2. Controller / Service / Repository 三层职责。
3. JWT 登录认证流程。
4. WebSocket 实时消息流程。
5. MySQL 和 Redis 分别负责什么。
6. 好友申请和私聊消息的数据表设计。
7. Vue 前端如何管理登录状态、会话状态和 WebSocket 状态。
8. Docker Compose 如何一键启动完整系统。
9. 项目相比原版解决了哪些工程问题。

## 11. 最小可交付版本

如果时间有限，必须优先完成：

1. Spring Boot 后端能启动。
2. Vue 登录、注册、聊天主页能打开。
3. JWT 登录认证。
4. 好友列表。
5. 私聊历史和发送。
6. WebSocket 实时私聊。
7. MySQL 持久化。
8. README 启动文档。

这些完成后，项目已经具备大作业核心展示价值。

## 12. 增强版本

时间充足时再做：

1. 公共聊天室。
2. 好友申请通知。
3. 头像上传。
4. 消息撤回。
5. 图片消息。
6. 管理员统计面板。
7. 单元测试和集成测试。
8. 前端深色模式。

## 13. 当前项目可复用内容

可以复用的业务思路：

1. `users`、`p2p_relationship`、`p2p_text`、`public_chat_room` 的业务概念。
2. 好友关系双向保存的思路。
3. 私聊消息先入库再读取历史的思路。
4. Redis 缓存好友列表和消息列表的思路。
5. 在线用户映射和消息转发的思路。

不能直接复用的实现方式：

1. Servlet 类直接写业务逻辑。
2. Header 传用户名密码。
3. Cookie 保存完整用户字段。
4. 明文密码。
5. 每次方法内手动创建 JedisPool。
6. 原生 `ServerSocket` 给浏览器前端使用。
7. 单容器同时启动 MySQL、Redis、Tomcat。

## 14. 建议开发顺序总览

```text
旧项目分析
  -> 新建 backend Spring Boot
  -> 设计数据库
  -> 注册登录 JWT
  -> 好友模块
  -> 私聊 REST
  -> WebSocket 实时私聊
  -> 公共聊天室
  -> 新建 frontend Vue
  -> 前后端联调
  -> Docker Compose
  -> README/API/答辩文档
```

严格按该顺序执行，可以避免“前端写完但接口没有”“WebSocket 写完但认证没做”“数据库字段频繁返工”等问题。
