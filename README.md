# JavaChatSer

JavaChatSer 正在从传统 Java Web 聊天项目升级为 Spring Boot + Vue 前后端分离聊天系统。当前仓库同时保留旧版实现和新版迁移目录，后续开发按 `docs/SPRING_BOOT_VUE_UPGRADE_GUIDE.md` 的阶段顺序推进。

## 当前状态

- 旧版代码仍保留在 `src/`、`pom.xml`、`Dockerfile`、`start.sh` 中，用作业务参考。
- 新版后端已在 `backend/` 中完成 Spring Boot 3 + Java 21 骨架、认证、好友、私聊 REST、WebSocket 实时通信和公共聊天室接口。
- 新版前端已在 `frontend/` 中建立 Vite + Vue 3 工程，并接入后端认证、好友、聊天和 WebSocket。
- 根目录 `docker-compose.yml` 已预留 MySQL、Redis、backend、frontend 服务规划。

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
  backend/                 # Spring Boot 后端，后续阶段实现
  frontend/                # Vue 3 前端，后续阶段实现
  docs/
    SPRING_BOOT_VUE_UPGRADE_GUIDE.md
  docker-compose.yml       # MySQL、Redis、backend、frontend 服务规划
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
10. 阶段 9：Docker 部署和答辩文档。

## Compose 规划

当前 `docker-compose.yml` 已包含四类服务：

- `mysql`：MySQL 8，持久化数据库。
- `redis`：Redis 7，缓存好友列表、消息和在线状态。
- `backend`：后续 Spring Boot 服务，默认端口 `8080`。
- `frontend`：后续 Vue/Nginx 服务，默认端口 `5173`。

阶段 0 只验证 Compose 配置结构：

```powershell
docker compose config
```

后续阶段补齐 `backend/Dockerfile`、`frontend/Dockerfile` 后，再使用：

```powershell
docker compose up -d --build
```

## 默认端口

| 服务 | 容器端口 | 本机端口 |
| --- | --- | --- |
| backend | 8080 | 8080 |
| frontend | 80 | 5173 |
| mysql | 3306 | 3306 |
| redis | 6379 | 6379 |

## 前端本地运行

```powershell
cd frontend
npm install
npm run dev
```

默认访问地址为 `http://localhost:5173`。前端默认连接 `http://localhost:8080` 和 `ws://localhost:8080`，可以通过 `frontend/.env.example` 中的变量覆盖。

## 下一步

下一阶段应补齐 Docker 部署能力和答辩文档，包括前后端镜像、Compose 一键启动、接口说明和架构讲解材料。
