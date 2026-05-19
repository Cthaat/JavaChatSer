# JavaChatSer Frontend

这里用于承载新版 Vue 3 前端代码。当前处于阶段 0，仅建立目录并保留迁移说明；阶段 8 开始创建 Vite + Vue 项目。

## 目标技术栈

- Vite
- Vue 3
- TypeScript
- Vue Router
- Pinia
- Axios
- Element Plus
- SCSS 或普通 CSS
- WebSocket 客户端封装

## 目标页面

- `/login`：登录。
- `/register`：注册。
- `/chat`：聊天主页。
- `/friends`：好友管理。
- `/profile`：个人资料。
- `/dashboard`：系统概览。

## 前端约束

- 所有 HTTP 请求通过 `src/api/` 统一封装。
- Token 保存在 Pinia 和 `localStorage`。
- Axios 自动附带 `Authorization: Bearer <token>`。
- WebSocket 消息必须包含明确的 `type` 字段。
- 聊天主页直接展示可用聊天界面，不做营销页。
