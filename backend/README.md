# JavaChatSer Backend

这里用于承载新版 Spring Boot 后端代码。当前已完成阶段 1 到阶段 3：Spring Boot 基础骨架、数据库迁移脚本、JPA 实体模型、Repository 和 JWT 认证模块。

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
