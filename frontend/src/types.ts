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
  readAt: string | null
  createdAt: string
}

export interface PublicMessage {
  id: number
  senderId: number
  content: string
  messageType: string
  createdAt: string
}

export interface WebSocketEnvelope<T = unknown> {
  type: WebSocketMessageType
  data: T
}

export type WebSocketMessageType =
  | 'PRIVATE_MESSAGE'
  | 'PUBLIC_MESSAGE'
  | 'FRIEND_STATUS'
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
