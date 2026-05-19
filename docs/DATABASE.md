# JavaChatSer Database

数据库以 MySQL 为最终事实来源，Redis 只保存可重建的缓存、在线状态和未读计数。初始化脚本位于 `backend/src/main/resources/db/migration/V1__init_chat_schema.sql`，后端启动时由 Flyway 自动执行。

## MySQL

默认连接信息：

| 配置 | 默认值 |
| --- | --- |
| 数据库 | `javachat` |
| 用户 | `javachat` |
| 密码 | `javachat_password` |
| Compose 内部地址 | `mysql:3306` |

### chat_user

用户账号表。密码保存 BCrypt 哈希，不保存明文。

| 字段 | 说明 |
| --- | --- |
| `id` | 主键 |
| `username` | 登录用户名，唯一 |
| `password_hash` | BCrypt 密码哈希 |
| `nickname` | 昵称 |
| `avatar_url` | 头像地址 |
| `bio` | 个人简介 |
| `role` | `USER` 或 `ADMIN` |
| `enabled` | 是否启用 |
| `created_at` / `updated_at` | 创建和更新时间 |

初始化账号：

| 用户名 | 密码 |
| --- | --- |
| `admin` | `123456` |
| `testUser3` | `123456` |

### friend_relation

好友关系和好友申请表。好友建立后保存双向记录。

| 字段 | 说明 |
| --- | --- |
| `id` | 主键 |
| `user_id` | 发起方或关系拥有者 |
| `friend_id` | 目标用户 |
| `status` | `PENDING`、`ACCEPTED`、`REJECTED`、`DELETED` |
| `created_at` / `updated_at` | 创建和更新时间 |

约束：`(user_id, friend_id)` 唯一，避免重复关系。

### private_message

私聊消息表。

| 字段 | 说明 |
| --- | --- |
| `id` | 主键 |
| `sender_id` | 发送者 |
| `receiver_id` | 接收者 |
| `content` | 文本内容 |
| `message_type` | 当前为 `TEXT` |
| `read_at` | 已读时间，未读为 `NULL` |
| `created_at` | 发送时间 |

查询私聊历史时使用发送者、接收者和时间索引；标记已读时更新接收者为当前用户且发送者为好友的未读消息。

### public_message

公共聊天室消息表。

| 字段 | 说明 |
| --- | --- |
| `id` | 主键 |
| `sender_id` | 发送者 |
| `content` | 文本内容 |
| `message_type` | 当前为 `TEXT` |
| `created_at` | 发送时间 |

公共消息历史按 `created_at` 升序分页返回。

## Redis

默认 Compose 内部地址为 `redis:6379`。缓存原则是：先写 MySQL，再更新 Redis，再推送 WebSocket；Redis 不可用时核心业务仍可通过 MySQL 工作。

| Key | 类型 | 说明 | 过期时间 |
| --- | --- | --- | --- |
| `online:user:{userId}` | String | 在线状态，值为 WebSocket sessionId | 2 分钟，心跳续期 |
| `friend:list:{userId}` | JSON String | 好友基础资料列表 | 5 分钟 |
| `chat:private:{minUserId}:{maxUserId}` | List | 最近私聊消息 JSON | 1 天 |
| `chat:public:recent` | List | 最近公共聊天室消息 JSON | 1 天 |
| `unread:{userId}:{friendId}` | String Number | 某好友未读消息数 | 无固定 TTL |

好友新增、删除后会删除双方 `friend:list:*` 缓存。私聊发送后更新最近消息缓存和接收者未读数。公共消息发送后更新 `chat:public:recent`。

## 数据一致性

1. 用户、好友关系、消息内容都以 MySQL 为准。
2. Redis 中的最近消息可以丢失，重新访问历史接口仍可从 MySQL 加载。
3. 在线状态依赖 WebSocket 连接和心跳 TTL，断开后会删除或自然过期。
4. 未读数写 Redis，同时可以通过数据库未读消息统计回补。
