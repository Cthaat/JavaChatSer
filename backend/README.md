# JavaChatSer Backend

这里用于承载新版 Spring Boot 后端代码。当前已完成阶段 1 到阶段 7：Spring Boot 基础骨架、数据库迁移脚本、JPA 实体模型、Repository、JWT 认证模块、好友模块、私聊 REST 模块、WebSocket 实时通信和公共聊天室 REST 接口。

## 目标技术栈

- Java 21
- Spring Boot 3.x
- Spring Web
- Spring Security + JWT + BCrypt
- Spring Data JPA
- Spring Data Redis
- Spring WebSocket
- springdoc-openapi
- JUnit 5 + Spring Boot Test

## 目标模块

- `common/`：统一响应、分页响应、业务异常、全局异常处理。
- `config/`：CORS、Redis、OpenAPI、WebSocket 配置。
- `security/`：JWT、认证过滤器、安全配置、登录用户上下文。
- `user/`：用户注册、登录、资料。
- `friend/`：好友搜索、申请、同意、拒绝、删除。
- `chat/`：私聊、公共聊天室、消息持久化。
- `websocket/`：实时推送、在线状态、Session 管理。

## 已完成能力

- 后端应用可以启动。
- `GET /api/health` 返回统一成功响应。
- 基础安全配置允许健康检查和 Swagger，其他接口默认需要认证。
- 全局异常处理会返回统一 `ApiResponse`，不向前端暴露原始堆栈。
- Flyway 初始化 `chat_user`、`friend_relation`、`private_message`、`public_message`。
- JPA Repository 已覆盖用户、好友关系、私聊消息和公共消息。
- 初始化账号：`admin / 123456`、`testUser3 / 123456`，数据库内保存 BCrypt 哈希。
- `POST /api/auth/register` 支持注册并返回 JWT。
- `POST /api/auth/login` 支持用户名密码登录并返回 JWT。
- `GET /api/auth/me` 支持通过 `Authorization: Bearer <token>` 获取当前用户。
- `POST /api/auth/logout` 预留退出接口，前端清除 Token 即可完成退出。
- `GET /api/users/search` 支持按用户名或昵称搜索用户，结果排除当前用户。
- `POST /api/friends/requests` 支持发送好友申请。
- `GET /api/friends/requests` 支持查询收到的好友申请。
- `POST /api/friends/requests/{requestId}/accept` 支持接受申请并建立双向好友关系。
- `POST /api/friends/requests/{requestId}/reject` 支持拒绝申请。
- `GET /api/friends` 支持好友列表、在线状态和未读消息数。
- `DELETE /api/friends/{friendId}` 支持双向删除好友。
- 好友基础资料会缓存到 Redis `friend:list:{userId}`，在线状态和未读数每次实时计算。
- `GET /api/chats/private/{friendId}/messages` 支持私聊历史分页，按时间升序返回。
- `POST /api/chats/private/{friendId}/messages` 支持好友之间发送文本私聊，非好友返回 `40300`。
- `POST /api/chats/private/{friendId}/read` 支持将某好友发来的未读消息标记已读。
- 私聊发送后写入 MySQL，并更新 Redis `chat:private:{minUserId}:{maxUserId}` 最近消息缓存。
- 未读数使用 Redis `unread:{userId}:{friendId}` 缓存，Redis 不可用时回退数据库统计。
- `/ws/chat?token=<JWT>` 支持 WebSocket 连接，握手阶段校验 JWT。
- 用户连接后会写入 Redis `online:user:{userId}` 在线状态，并向已接受好友推送上下线通知。
- WebSocket `PRIVATE_MESSAGE` 会复用私聊服务，先写 MySQL、更新 Redis，再推送给发送者和接收者的在线连接。
- WebSocket `PUBLIC_MESSAGE` 会写入 `public_message` 并缓存到 Redis `chat:public:recent`，随后广播给所有在线连接。
- WebSocket `PING` 会返回 `PONG`，心跳消息会刷新在线状态 TTL。
- `GET /api/chats/public/messages` 支持公共聊天室历史分页，按时间升序返回。
- `POST /api/chats/public/messages` 支持发送公共消息，发送后先写 MySQL，再写 Redis `chat:public:recent`，最后向在线 WebSocket 连接广播。

## 数据库迁移

迁移脚本位于：

```text
src/main/resources/db/migration/V1__init_chat_schema.sql
```

默认启动后端时会自动执行 Flyway 迁移。若本机 `3306` 已被占用，可以临时指定 Compose 暴露端口：

```powershell
$env:MYSQL_PORT='13306'
docker compose up -d mysql
```

## 验证命令

```powershell
cd backend
mvn test
mvn spring-boot:run
```

当前本机如果没有安装 Maven，也可以用 Maven Docker 镜像验证：

```powershell
docker run --rm -v "${PWD}:/workspace" -v "$HOME/.m2:/root/.m2" -w /workspace/backend maven:3.9-eclipse-temurin-21 mvn test
```

## Docker 构建

后端镜像由 `backend/Dockerfile` 独立构建。构建阶段使用 Maven 生成 Spring Boot JAR，运行阶段使用 JRE 21 启动：

```powershell
docker build -t javachatser-backend ./backend
```

完整项目推荐在根目录使用：

```powershell
docker compose up -d --build
```

## 认证接口示例

登录：

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/auth/login `
  -Method Post `
  -ContentType 'application/json' `
  -Body '{"username":"admin","password":"123456"}'
```

当前用户：

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/auth/me `
  -Headers @{ Authorization = "Bearer <token>" }
```

## 好友接口示例

搜索用户：

```powershell
Invoke-RestMethod -Uri 'http://localhost:8080/api/users/search?keyword=test' `
  -Headers @{ Authorization = "Bearer <token>" }
```

发送好友申请：

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/friends/requests `
  -Method Post `
  -ContentType 'application/json' `
  -Headers @{ Authorization = "Bearer <token>" } `
  -Body '{"friendId":2}'
```

查看好友列表：

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/friends `
  -Headers @{ Authorization = "Bearer <token>" }
```

## 私聊接口示例

发送私聊：

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/chats/private/2/messages `
  -Method Post `
  -ContentType 'application/json' `
  -Headers @{ Authorization = "Bearer <token>" } `
  -Body '{"content":"你好"}'
```

查看历史：

```powershell
Invoke-RestMethod -Uri 'http://localhost:8080/api/chats/private/2/messages?page=0&size=20' `
  -Headers @{ Authorization = "Bearer <token>" }
```

标记已读：

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/chats/private/2/read `
  -Method Post `
  -Headers @{ Authorization = "Bearer <token>" }
```

## 公共聊天室接口示例

发送公共消息：

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/chats/public/messages `
  -Method Post `
  -ContentType 'application/json' `
  -Headers @{ Authorization = "Bearer <token>" } `
  -Body '{"content":"大家好"}'
```

查看公共消息历史：

```powershell
Invoke-RestMethod -Uri 'http://localhost:8080/api/chats/public/messages?page=0&size=20' `
  -Headers @{ Authorization = "Bearer <token>" }
```

## WebSocket 消息示例

连接地址：

```text
ws://localhost:8080/ws/chat?token=<JWT>
```

发送私聊：

```json
{
  "type": "PRIVATE_MESSAGE",
  "receiverId": 2,
  "content": "你好"
}
```

发送公共消息：

```json
{
  "type": "PUBLIC_MESSAGE",
  "content": "大家好"
}
```

心跳：

```json
{
  "type": "PING"
}
```

服务端推送统一使用 `type + data` 结构，当前类型包括 `PRIVATE_MESSAGE`、`PUBLIC_MESSAGE`、`FRIEND_STATUS`、`ONLINE_USERS`、`PONG` 和 `ERROR`。
