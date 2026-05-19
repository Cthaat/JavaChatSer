import { defineStore } from 'pinia'

import { friendApi, userApi } from '@/api'
import type { Friend, FriendRequest, FriendUser } from '@/types'

interface FriendsState {
  friends: Friend[]
  requests: FriendRequest[]
  searchResults: FriendUser[]
  keyword: string
  loading: boolean
  error: string
}

export const useFriendsStore = defineStore('friends', {
  state: (): FriendsState => ({
    friends: [],
    requests: [],
    searchResults: [],
    keyword: '',
    loading: false,
    error: '',
  }),
  getters: {
    byId: (state) => new Map(state.friends.map((friend) => [friend.user.id, friend])),
  },
  actions: {
    async refreshAll() {
      await Promise.all([this.fetchFriends(), this.fetchRequests()])
    },
    async fetchFriends() {
      this.loading = true
      try {
        this.friends = await friendApi.list()
        this.error = ''
      } finally {
        this.loading = false
      }
    },
    async fetchRequests() {
      this.requests = await friendApi.requests()
    },
    async search(keyword?: string) {
      const normalizedKeyword = (keyword ?? this.keyword).trim()
      this.keyword = normalizedKeyword
      if (!normalizedKeyword) {
        this.searchResults = []
        return
      }
      const page = await userApi.search(normalizedKeyword)
      this.searchResults = page.content
    },
    async sendRequest(friendId: number) {
      await friendApi.sendRequest(friendId)
      this.searchResults = this.searchResults.filter((user) => user.id !== friendId)
      await this.fetchRequests()
    },
    async acceptRequest(requestId: number) {
      await friendApi.accept(requestId)
      await this.refreshAll()
    },
    async rejectRequest(requestId: number) {
      await friendApi.reject(requestId)
      await this.fetchRequests()
    },
    async deleteFriend(friendId: number) {
      await friendApi.remove(friendId)
      this.friends = this.friends.filter((friend) => friend.user.id !== friendId)
    },
    applyOnlineUsers(userIds: number[]) {
      const onlineIds = new Set(userIds)
      this.friends = this.friends.map((friend) => ({
        ...friend,
        online: onlineIds.has(friend.user.id),
      }))
    },
    updateOnline(userId: number, online: boolean) {
      this.friends = this.friends.map((friend) =>
        friend.user.id === userId ? { ...friend, online } : friend,
      )
    },
    incrementUnread(userId: number) {
      this.friends = this.friends.map((friend) =>
        friend.user.id === userId ? { ...friend, unreadCount: friend.unreadCount + 1 } : friend,
      )
    },
    clearUnread(userId: number) {
      this.friends = this.friends.map((friend) =>
        friend.user.id === userId ? { ...friend, unreadCount: 0 } : friend,
      )
    },
    mergeRequest(request: FriendRequest) {
      if (this.requests.some((item) => item.requestId === request.requestId)) {
        return
      }
      this.requests = [request, ...this.requests]
    },
  },
})
