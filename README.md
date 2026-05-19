# JavaChatSer

JavaChatSer 正在从传统 Java Web 聊天项目升级为 Spring Boot + Vue 前后端分离聊天系统。当前仓库同时保留旧版实现和新版迁移目录，后续开发按 `docs/SPRING_BOOT_VUE_UPGRADE_GUIDE.md` 的阶段顺序推进。

## 当前状态

- 旧版代码仍保留在 `src/`、`pom.xml`、`Dockerfile`、`start.sh` 中，用作业务参考。
- 新版后端已在 `backend/` 中完成 Spring Boot 3 + Java 21 骨架、认证、好友、私聊 REST、WebSocket 实时通信和公共聊天室接口。
- 新版前端将在 `frontend/` 中实现，目标是 Vite + Vue 3。
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
9. 阶段 8：Vue 前端。
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

后续阶段补齐 `backend/Dockerfile`、`frontend/Dockerfile` 和应用代码后，再使用：

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

## 下一步

下一阶段应创建 Vite + Vue 3 前端工程，并接入认证、好友、私聊、公共聊天室和 WebSocket 自动重连。
