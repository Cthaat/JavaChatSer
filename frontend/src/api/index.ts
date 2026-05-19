import { request } from './client'
import type {
  AuthResponse,
  Friend,
  FriendRequest,
  MessageRecall,
  PageResponse,
  PrivateMessage,
  PublicMessage,
  StatsOverview,
  UploadResponse,
  UserProfile,
  FriendUser,
} from '@/types'

export const authApi = {
  login(payload: { username: string; password: string }) {
    return request<AuthResponse>({
      url: '/api/auth/login',
      method: 'POST',
      data: payload,
    })
  },
  register(payload: { username: string; password: string; nickname?: string; avatarUrl?: string }) {
    return request<AuthResponse>({
      url: '/api/auth/register',
      method: 'POST',
      data: payload,
    })
  },
  me() {
    return request<UserProfile>({
      url: '/api/auth/me',
      method: 'GET',
    })
  },
  logout() {
    return request<void>({
      url: '/api/auth/logout',
      method: 'POST',
    })
  },
}

export const userApi = {
  search(keyword: string, page = 0, size = 10) {
    return request<PageResponse<FriendUser>>({
      url: '/api/users/search',
      method: 'GET',
      params: { keyword, page, size },
    })
  },
  updateAvatar(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request<UserProfile>({
      url: '/api/users/me/avatar',
      method: 'POST',
      data: formData,
    })
  },
}

export const friendApi = {
  list() {
    return request<Friend[]>({
      url: '/api/friends',
      method: 'GET',
    })
  },
  requests() {
    return request<FriendRequest[]>({
      url: '/api/friends/requests',
      method: 'GET',
    })
  },
  sendRequest(friendId: number) {
    return request<FriendRequest>({
      url: '/api/friends/requests',
      method: 'POST',
      data: { friendId },
    })
  },
  accept(requestId: number) {
    return request<FriendRequest>({
      url: `/api/friends/requests/${requestId}/accept`,
      method: 'POST',
    })
  },
  reject(requestId: number) {
    return request<FriendRequest>({
      url: `/api/friends/requests/${requestId}/reject`,
      method: 'POST',
    })
  },
  remove(friendId: number) {
    return request<void>({
      url: `/api/friends/${friendId}`,
      method: 'DELETE',
    })
  },
}

export const chatApi = {
  privateMessages(friendId: number, page = 0, size = 50) {
    return request<PageResponse<PrivateMessage>>({
      url: `/api/chats/private/${friendId}/messages`,
      method: 'GET',
      params: { page, size },
    })
  },
  sendPrivateMessage(friendId: number, content: string, messageType = 'TEXT') {
    return request<PrivateMessage>({
      url: `/api/chats/private/${friendId}/messages`,
      method: 'POST',
      data: { content, messageType },
    })
  },
  markPrivateRead(friendId: number) {
    return request<{ readCount: number; unreadCount: number }>({
      url: `/api/chats/private/${friendId}/read`,
      method: 'POST',
    })
  },
  publicMessages(page = 0, size = 50) {
    return request<PageResponse<PublicMessage>>({
      url: '/api/chats/public/messages',
      method: 'GET',
      params: { page, size },
    })
  },
  sendPublicMessage(content: string, messageType = 'TEXT') {
    return request<PublicMessage>({
      url: '/api/chats/public/messages',
      method: 'POST',
      data: { content, messageType },
    })
  },
  recallPrivateMessage(messageId: number) {
    return request<MessageRecall>({
      url: `/api/chats/private/messages/${messageId}`,
      method: 'DELETE',
    })
  },
  recallPublicMessage(messageId: number) {
    return request<MessageRecall>({
      url: `/api/chats/public/messages/${messageId}`,
      method: 'DELETE',
    })
  },
}

export const statsApi = {
  overview() {
    return request<StatsOverview>({
      url: '/api/stats/overview',
      method: 'GET',
    })
  },
}

export const uploadApi = {
  image(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request<UploadResponse>({
      url: '/api/uploads/images',
      method: 'POST',
      data: formData,
    })
  },
}
