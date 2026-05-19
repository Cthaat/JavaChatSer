# JavaChatSer Frontend

这里用于承载新版 Vue 3 前端代码。当前已完成 Vite + Vue 3 工程、路由、Pinia、Axios、登录注册、聊天主页、好友管理、个人资料、系统概览、WebSocket 客户端封装和深色模式。

## 目标技术栈

- Vite
- Vue 3
- TypeScript
- Vue Router
- Pinia
- Axios
- SCSS 或普通 CSS
- WebSocket 客户端封装

## 已实现页面

- `/login`：登录。
- `/register`：注册。
- `/chat`：聊天主页。
- `/friends`：好友管理。
- `/dashboard`：系统概览或个人统计。
- `/profile`：个人资料。

## 已实现能力

- 所有 HTTP 请求通过 `src/api/` 统一封装。
- Token 保存在 Pinia 和 `localStorage`。
- Axios 自动附带 `Authorization: Bearer <token>`。
- Axios 统一处理 `40100`，Token 失效时自动回到登录页。
- 页面刷新后会调用 `/api/auth/me` 恢复登录状态。
- 聊天主页支持公共聊天室、好友私聊、未读消息、在线状态和 WebSocket 自动重连。
- 聊天主页支持文本消息、图片消息和消息撤回。
- WebSocket 客户端发送 `PRIVATE_MESSAGE`、`PUBLIC_MESSAGE`、`PING`，接收 `FRIEND_REQUEST`、`FRIEND_STATUS`、`ONLINE_USERS`、`PRIVATE_MESSAGE`、`PUBLIC_MESSAGE`、`MESSAGE_RECALLED`、`ERROR`。
- 好友页支持搜索用户、发送好友申请、接受/拒绝申请、删除好友。
- 好友申请通知会实时更新侧边栏和好友页待处理数量。
- 个人资料页展示当前账号和实时连接状态，并支持头像上传。
- 系统概览页展示用户数、在线数、消息数、好友申请数等统计。
- 侧边栏支持浅色/深色模式切换，并保存在 `localStorage`。

## 本地运行

```powershell
npm install
npm run dev
```

默认访问地址：

```text
http://localhost:5173
```

默认后端地址：

```text
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_BASE_URL=ws://localhost:8080
```

Docker Compose 构建时默认把这两个变量留空，让前端通过 Nginx 同源代理访问：

```text
http://localhost:5173/api -> backend:8080/api
ws://localhost:5173/ws -> backend:8080/ws
http://localhost:5173/uploads -> backend:8080/uploads
```

构建验证：

```powershell
npm run type-check
npm run build
```

## Docker 构建

前端镜像由 `frontend/Dockerfile` 独立构建。构建阶段使用 Node 生成静态资源，运行阶段使用 Nginx 托管，并代理 `/api`、`/ws` 和 `/uploads`：

```powershell
docker build -t javachatser-frontend ./frontend
```

完整项目推荐在根目录使用：

```powershell
docker compose up -d --build
```
