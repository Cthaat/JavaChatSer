# JavaChatSer

JavaChatSer 是一个从传统 Java Web 聊天项目升级而来的 Spring Boot + Vue 前后端分离聊天系统。当前仓库同时保留旧版实现和新版工程，便于对比原始业务和现代化重构结果。

## 当前状态

- 旧版代码仍保留在 `src/`、`pom.xml`、`Dockerfile`、`start.sh` 中，用作业务参考。
- 新版后端已在 `backend/` 中完成 Spring Boot 3 + Java 21 骨架、认证、好友、私聊 REST、WebSocket 实时通信和公共聊天室接口。
- 新版前端已在 `frontend/` 中建立 Vite + Vue 3 工程，并接入后端认证、好友、聊天和 WebSocket。
- 根目录 `docker-compose.yml` 已完成 MySQL、Redis、backend、frontend 一键部署。

## 旧版业务参考

旧项目已经包含用户、好友、私聊、公共聊天室、Redis 缓存和原生 Socket 在线转发等原型能力：

- `src/main/java/org/http/`：Servlet 接口和原生 Socket 服务。
- `src/main/java/org/Sql/`：Spring JDBC、Druid、Jedis 相关数据访问逻辑。
- `src/main/resources/javaChat.sql`：旧版 MySQL 表和样例数据。
- `src/webapp/`：旧版 JSP 占位页面。

旧版实现只作为迁移参考，不应把 Header 登录、明文 Cookie、明文密码、手写 Jedis 连接和原生 `ServerSocket` 直接迁移到新版系统。

## 新版目标结构

```text
JavaChatSer/
  backend/                 # Spring Boot 后端
  frontend/                # Vue 3 前端，Nginx 托管
  docs/
    API.md
    DATABASE.md
    DEFENSE_GUIDE.md
    SPRING_BOOT_VUE_UPGRADE_GUIDE.md
    screenshots/           # 功能截图建议放置位置
  docker-compose.yml       # MySQL、Redis、backend、frontend 一键部署
  src/                     # 旧版 Java Web 项目，保留作参考
```

## 阶段推进

开发必须按文档中的阶段逐步完成：

1. 阶段 0：建立新版目录和部署规划，保留旧代码。已完成。
2. 阶段 1：创建 Spring Boot 后端骨架。已完成。
3. 阶段 2：数据库迁移和实体建模。已完成。
4. 阶段 3：JWT 认证模块。已完成。
5. 阶段 4：好友模块。已完成。
6. 阶段 5：私聊历史和发送接口。已完成。
7. 阶段 6：WebSocket 实时通信。已完成。
8. 阶段 7：公共聊天室。已完成。
9. 阶段 8：Vue 前端。已完成。
10. 阶段 9：Docker 部署和答辩文档。已完成。

## 快速开始

环境要求：

- Docker Desktop 或 Docker Engine + Docker Compose v2。
- 本机端口 `5173`、`8080` 未被占用，或通过 `.env` 改端口。

一键启动：

```powershell
docker compose up -d --build
```

访问地址：

| 地址 | 说明 |
| --- | --- |
| `http://localhost:5173` | 前端页面 |
| `http://localhost:8080/api/health` | 后端健康检查 |
| `http://localhost:8080/swagger-ui.html` | Swagger UI |

默认账号：

| 用户名 | 密码 |
| --- | --- |
| `admin` | `123456` |
| `testUser3` | `123456` |

停止服务：

```powershell
docker compose down
```

如需重置数据库和 Redis 数据：

```powershell
docker compose down -v
```

## Compose 服务

`docker-compose.yml` 包含四类服务：

- `mysql`：MySQL 8，持久化数据库。
- `redis`：Redis 7，缓存好友列表、消息和在线状态。
- `backend`：Spring Boot 服务，默认端口 `8080`。
- `frontend`：Vue 静态资源 + Nginx 反向代理，默认端口 `5173`。

## 默认端口

| 服务 | 容器端口 | 本机端口 |
| --- | --- | --- |
| backend | 8080 | 8080 |
| frontend | 80 | 5173 |
| mysql | 3306 | 仅 Compose 内部网络 |
| redis | 6379 | 仅 Compose 内部网络 |

可复制 `.env.example` 为 `.env` 修改端口、数据库账号或 JWT 密钥。

## 前端本地运行

```powershell
cd frontend
npm install
npm run dev
```

默认访问地址为 `http://localhost:5173`。前端默认连接 `http://localhost:8080` 和 `ws://localhost:8080`，可以通过 `frontend/.env.example` 中的变量覆盖。

## 文档

- [接口文档](docs/API.md)
- [数据库与 Redis 设计](docs/DATABASE.md)
- [答辩讲解指南](docs/DEFENSE_GUIDE.md)
- [升级执行文档](docs/SPRING_BOOT_VUE_UPGRADE_GUIDE.md)

功能截图建议放在 `docs/screenshots/`，便于答辩文档或 PPT 引用。
