# JavaChatSer Frontend

这里用于承载新版 Vue 3 前端代码。当前已完成阶段 8：Vite + Vue 3 工程、路由、Pinia、Axios、登录注册、聊天主页、好友管理、个人资料和 WebSocket 客户端封装。

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
- `/profile`：个人资料。

## 已实现能力

- 所有 HTTP 请求通过 `src/api/` 统一封装。
- Token 保存在 Pinia 和 `localStorage`。
- Axios 自动附带 `Authorization: Bearer <token>`。
- Axios 统一处理 `40100`，Token 失效时自动回到登录页。
- 页面刷新后会调用 `/api/auth/me` 恢复登录状态。
- 聊天主页支持公共聊天室、好友私聊、未读消息、在线状态和 WebSocket 自动重连。
- WebSocket 客户端发送 `PRIVATE_MESSAGE`、`PUBLIC_MESSAGE`、`PING`，接收 `FRIEND_STATUS`、`ONLINE_USERS`、`PRIVATE_MESSAGE`、`PUBLIC_MESSAGE`、`ERROR`。
- 好友页支持搜索用户、发送好友申请、接受/拒绝申请、删除好友。
- 个人资料页展示当前账号和实时连接状态。

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

构建验证：

```powershell
npm run type-check
npm run build
```
