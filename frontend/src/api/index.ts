import { request } from './client'
import type {
  AuthResponse,
  Friend,
  FriendRequest,
  PageResponse,
  PrivateMessage,
  PublicMessage,
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
  sendPrivateMessage(friendId: number, content: string) {
    return request<PrivateMessage>({
      url: `/api/chats/private/${friendId}/messages`,
      method: 'POST',
      data: { content },
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
  sendPublicMessage(content: string) {
    return request<PublicMessage>({
      url: '/api/chats/public/messages',
      method: 'POST',
      data: { content },
    })
  },
}
