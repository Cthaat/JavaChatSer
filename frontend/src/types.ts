export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface UserProfile {
  id: number
  username: string
  nickname: string
  avatarUrl: string | null
  bio: string | null
  role: string
}

export interface AuthResponse {
  token: string
  user: UserProfile
}

export interface FriendUser {
  id: number
  username: string
  nickname: string
  avatarUrl: string | null
  bio: string | null
}

export interface Friend {
  user: FriendUser
  online: boolean
  unreadCount: number
}

export interface FriendRequest {
  requestId: number
  requester: FriendUser
  status: string
  createdAt: string
}

export interface PrivateMessage {
  id: number
  senderId: number
  receiverId: number
  content: string
  messageType: string
  recalled: boolean
  readAt: string | null
  recalledAt: string | null
  createdAt: string
}

export interface PublicMessage {
  id: number
  senderId: number
  content: string
  messageType: string
  recalled: boolean
  recalledAt: string | null
  createdAt: string
}

export interface MessageRecall {
  scope: 'PRIVATE' | 'PUBLIC'
  messageId: number
  senderId: number
  receiverId: number | null
  recalledAt: string
}

export interface StatsOverview {
  scope: 'GLOBAL' | 'PERSONAL'
  registeredUserCount: number
  onlineUserCount: number
  todayPrivateMessageCount: number
  todayPublicMessageCount: number
  totalPrivateMessageCount: number
  totalPublicMessageCount: number
  pendingFriendRequestCount: number
  acceptedFriendCount: number
}

export interface UploadResponse {
  url: string
  contentType: string
  size: number
}

export interface WebSocketEnvelope<T = unknown> {
  type: WebSocketMessageType
  data: T
}

export type WebSocketMessageType =
  | 'PRIVATE_MESSAGE'
  | 'PUBLIC_MESSAGE'
  | 'FRIEND_REQUEST'
  | 'FRIEND_STATUS'
  | 'MESSAGE_RECALLED'
  | 'ONLINE_USERS'
  | 'PING'
  | 'PONG'
  | 'ERROR'

export interface FriendStatusMessage {
  userId: number
  online: boolean
}

export interface WebSocketErrorMessage {
  code: number
  message: string
}
