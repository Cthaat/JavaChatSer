CREATE TABLE IF NOT EXISTS chat_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  avatar_url VARCHAR(255) DEFAULT NULL,
  bio VARCHAR(255) DEFAULT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS friend_relation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  friend_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_friend (user_id, friend_id),
  INDEX idx_friend_user (friend_id),
  CONSTRAINT fk_friend_user FOREIGN KEY (user_id) REFERENCES chat_user(id),
  CONSTRAINT fk_friend_friend FOREIGN KEY (friend_id) REFERENCES chat_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS private_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sender_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
  read_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_private_pair_time (sender_id, receiver_id, created_at),
  INDEX idx_private_receiver_read (receiver_id, read_at),
  CONSTRAINT fk_private_sender FOREIGN KEY (sender_id) REFERENCES chat_user(id),
  CONSTRAINT fk_private_receiver FOREIGN KEY (receiver_id) REFERENCES chat_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS public_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sender_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_public_time (created_at),
  CONSTRAINT fk_public_sender FOREIGN KEY (sender_id) REFERENCES chat_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO chat_user (id, username, password_hash, nickname, avatar_url, bio, role, enabled)
VALUES
  (1, 'admin', '$2a$10$.Ii0O1CysULU4HEXIffY7.43wHwzvG517G3sU1P3usAGUvU/e7Vb2', 'Admin', '/default-avatar.png', '系统管理员', 'ADMIN', 1),
  (2, 'testUser3', '$2a$10$.Ii0O1CysULU4HEXIffY7.43wHwzvG517G3sU1P3usAGUvU/e7Vb2', 'Test User', '/default-avatar.png', '测试账号', 'USER', 1);
