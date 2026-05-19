import { defineStore } from 'pinia'

import { chatApi } from '@/api'
import { tokenStorage } from '@/api/client'
import { useAuthStore } from '@/stores/auth'
import { useFriendsStore } from '@/stores/friends'
import type {
  FriendStatusMessage,
  PrivateMessage,
  PublicMessage,
  WebSocketEnvelope,
  WebSocketErrorMessage,
} from '@/types'

type SocketStatus = 'idle' | 'connecting' | 'open' | 'closed'
type ActiveConversation = { type: 'public' } | { type: 'private'; friendId: number }

const WS_BASE_URL = resolveWsBaseUrl()

interface ChatState {
  activeConversation: ActiveConversation
  publicMessages: PublicMessage[]
  privateMessages: Record<number, PrivateMessage[]>
  socketStatus: SocketStatus
  socketError: string
  loadingMessages: boolean
  sending: boolean
  socket: WebSocket | null
  reconnectTimer: ReturnType<typeof setTimeout> | null
  heartbeatTimer: ReturnType<typeof setInterval> | null
}

export const useChatStore = defineStore('chat', {
  state: (): ChatState => ({
    activeConversation: { type: 'public' },
    publicMessages: [],
    privateMessages: {},
    socketStatus: 'idle',
    socketError: '',
    loadingMessages: false,
    sending: false,
    socket: null,
    reconnectTimer: null,
    heartbeatTimer: null,
  }),
  getters: {
    isSocketOpen: (state) => state.socketStatus === 'open' && state.socket?.readyState === WebSocket.OPEN,
    activePrivateMessages: (state) => {
      if (state.activeConversation.type !== 'private') {
        return []
      }
      return state.privateMessages[state.activeConversation.friendId] ?? []
    },
  },
  actions: {
    async selectPublic() {
      this.activeConversation = { type: 'public' }
      await this.loadPublicMessages()
    },
    async selectFriend(friendId: number) {
      this.activeConversation = { type: 'private', friendId }
      await this.loadPrivateMessages(friendId)
      await chatApi.markPrivateRead(friendId)
      useFriendsStore().clearUnread(friendId)
    },
    async loadPublicMessages() {
      this.loadingMessages = true
      try {
        const page = await chatApi.publicMessages(0, 50)
        this.publicMessages = page.content
      } finally {
        this.loadingMessages = false
      }
    },
    async loadPrivateMessages(friendId: number) {
      this.loadingMessages = true
      try {
        const page = await chatApi.privateMessages(friendId, 0, 50)
        this.privateMessages = {
          ...this.privateMessages,
          [friendId]: page.content,
        }
      } finally {
        this.loadingMessages = false
      }
    },
    async sendMessage(content: string) {
      const normalizedContent = content.trim()
      if (!normalizedContent) {
        return
      }
      this.sending = true
      try {
        if (this.activeConversation.type === 'public') {
          await this.sendPublicMessage(normalizedContent)
          return
        }
        await this.sendPrivateMessage(this.activeConversation.friendId, normalizedContent)
      } finally {
        this.sending = false
      }
    },
    async sendPublicMessage(content: string) {
      if (this.isSocketOpen) {
        this.socket?.send(JSON.stringify({ type: 'PUBLIC_MESSAGE', content }))
        return
      }
      this.addPublicMessage(await chatApi.sendPublicMessage(content))
    },
    async sendPrivateMessage(friendId: number, content: string) {
      if (this.isSocketOpen) {
        this.socket?.send(JSON.stringify({ type: 'PRIVATE_MESSAGE', receiverId: friendId, content }))
        return
      }
      this.addPrivateMessage(friendId, await chatApi.sendPrivateMessage(friendId, content))
    },
    connect() {
      const token = tokenStorage.get()
      if (!token || this.socketStatus === 'connecting' || this.isSocketOpen) {
        return
      }
      this.clearTimers()
      this.socketStatus = 'connecting'
      this.socketError = ''
      this.socket = new WebSocket(`${WS_BASE_URL}/ws/chat?token=${encodeURIComponent(token)}`)
      this.socket.onopen = () => {
        this.socketStatus = 'open'
        this.socketError = ''
        this.heartbeatTimer = setInterval(() => {
          if (this.isSocketOpen) {
            this.socket?.send(JSON.stringify({ type: 'PING' }))
          }
        }, 30000)
      }
      this.socket.onmessage = (event) => this.handleSocketMessage(event.data)
      this.socket.onerror = () => {
        this.socketError = '实时连接异常'
      }
      this.socket.onclose = () => {
        this.socketStatus = 'closed'
        this.clearTimers()
        if (tokenStorage.get()) {
          this.reconnectTimer = setTimeout(() => this.connect(), 3000)
        }
      }
    },
    disconnect() {
      this.clearTimers()
      if (this.socket) {
        this.socket.onclose = null
        this.socket.close()
      }
      this.socket = null
      this.socketStatus = 'idle'
    },
    handleSocketMessage(raw: string) {
      const envelope = JSON.parse(raw) as WebSocketEnvelope
      const friendsStore = useFriendsStore()
      switch (envelope.type) {
        case 'ONLINE_USERS':
          friendsStore.applyOnlineUsers(envelope.data as number[])
          break
        case 'FRIEND_STATUS': {
          const message = envelope.data as FriendStatusMessage
          friendsStore.updateOnline(message.userId, message.online)
          break
        }
        case 'PRIVATE_MESSAGE':
          this.receivePrivateMessage(envelope.data as PrivateMessage)
          break
        case 'PUBLIC_MESSAGE':
          this.addPublicMessage(envelope.data as PublicMessage)
          break
        case 'ERROR':
          this.socketError = (envelope.data as WebSocketErrorMessage).message
          break
        default:
          break
      }
    },
    receivePrivateMessage(message: PrivateMessage) {
      const authStore = useAuthStore()
      const currentUserId = authStore.user?.id
      const friendId = message.senderId === currentUserId ? message.receiverId : message.senderId
      this.addPrivateMessage(friendId, message)
      if (
        message.receiverId === currentUserId &&
        (this.activeConversation.type !== 'private' || this.activeConversation.friendId !== friendId)
      ) {
        useFriendsStore().incrementUnread(friendId)
      }
      if (
        message.receiverId === currentUserId &&
        this.activeConversation.type === 'private' &&
        this.activeConversation.friendId === friendId
      ) {
        void chatApi.markPrivateRead(friendId)
      }
    },
    addPrivateMessage(friendId: number, message: PrivateMessage) {
      const messages = this.privateMessages[friendId] ?? []
      if (messages.some((item) => item.id === message.id)) {
        return
      }
      this.privateMessages = {
        ...this.privateMessages,
        [friendId]: [...messages, message],
      }
    },
    addPublicMessage(message: PublicMessage) {
      if (this.publicMessages.some((item) => item.id === message.id)) {
        return
      }
      this.publicMessages = [...this.publicMessages, message]
    },
    clearTimers() {
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer)
        this.reconnectTimer = null
      }
      if (this.heartbeatTimer) {
        clearInterval(this.heartbeatTimer)
        this.heartbeatTimer = null
      }
    },
    reset() {
      this.disconnect()
      this.activeConversation = { type: 'public' }
      this.publicMessages = []
      this.privateMessages = {}
      this.socketError = ''
      this.loadingMessages = false
      this.sending = false
    },
  },
})

function resolveWsBaseUrl(): string {
  if (import.meta.env.VITE_WS_BASE_URL === undefined) {
    return 'ws://localhost:8080'
  }
  if (import.meta.env.VITE_WS_BASE_URL) {
    return import.meta.env.VITE_WS_BASE_URL
  }
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}`
}
