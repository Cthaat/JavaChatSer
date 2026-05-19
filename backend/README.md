# JavaChatSer Backend

这里用于承载新版 Spring Boot 后端代码。当前处于阶段 0，仅建立目录并保留迁移说明；阶段 1 开始创建完整 Maven 工程。

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

## 阶段 1 验收目标

- 后端应用可以启动。
- `GET /api/health` 返回统一成功响应。
- 配置错误对前端返回可读信息，不暴露原始堆栈。
