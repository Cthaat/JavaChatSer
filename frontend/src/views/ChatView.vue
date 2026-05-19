<script setup lang="ts">
import { Hash, Image as ImageIcon, MessageCircle, RefreshCw, Send, Trash2, Wifi, WifiOff } from '@lucide/vue'
import { computed, nextTick, onMounted, ref, watch } from 'vue'

import { getApiBaseUrl } from '@/api/client'
import AppShell from '@/components/AppShell.vue'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useFriendsStore } from '@/stores/friends'
import type { PrivateMessage, PublicMessage } from '@/types'

const authStore = useAuthStore()
const friendsStore = useFriendsStore()
const chatStore = useChatStore()
const draft = ref('')
const messageList = ref<HTMLElement | null>(null)
const imageInput = ref<HTMLInputElement | null>(null)

const activeFriend = computed(() => {
  if (chatStore.activeConversation.type !== 'private') {
    return null
  }
  return friendsStore.byId.get(chatStore.activeConversation.friendId) ?? null
})

const visibleMessages = computed(() => {
  if (chatStore.activeConversation.type === 'public') {
    return chatStore.publicMessages
  }
  return chatStore.activePrivateMessages
})

const title = computed(() =>
  chatStore.activeConversation.type === 'public'
    ? '公共聊天室'
    : activeFriend.value?.user.nickname || activeFriend.value?.user.username || '私聊',
)

const subtitle = computed(() => {
  if (chatStore.activeConversation.type === 'public') {
    return '所有在线用户都能实时收到公共消息'
  }
  return activeFriend.value?.online ? '好友在线' : '好友离线'
})

function senderName(message: PrivateMessage | PublicMessage) {
  if ('receiverId' in message) {
    if (message.senderId === authStore.user?.id) {
      return authStore.user.nickname || authStore.user.username
    }
    return activeFriend.value?.user.nickname || activeFriend.value?.user.username || `用户 #${message.senderId}`
  }
  if (message.senderId === authStore.user?.id) {
    return authStore.user.nickname || authStore.user.username
  }
  const friend = friendsStore.byId.get(message.senderId)
  return friend?.user.nickname || friend?.user.username || `用户 #${message.senderId}`
}

function isOwnMessage(message: PrivateMessage | PublicMessage) {
  return message.senderId === authStore.user?.id
}

function isImageMessage(message: PrivateMessage | PublicMessage) {
  return message.messageType === 'IMAGE' && !message.recalled
}

function resolveMediaUrl(url: string) {
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
    return url
  }
  const baseUrl = getApiBaseUrl()
  return baseUrl ? `${baseUrl}${url}` : url
}

function openImagePicker() {
  imageInput.value?.click()
}

async function send() {
  const content = draft.value
  draft.value = ''
  await chatStore.sendMessage(content)
  await scrollToBottom()
}

async function uploadImage(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  target.value = ''
  if (!file) {
    return
  }
  await chatStore.sendImage(file)
  await scrollToBottom()
}

async function recall(message: PrivateMessage | PublicMessage) {
  await chatStore.recallMessage(message)
}

async function refreshActive() {
  if (chatStore.activeConversation.type === 'public') {
    await chatStore.loadPublicMessages()
    return
  }
  await chatStore.loadPrivateMessages(chatStore.activeConversation.friendId)
}

async function scrollToBottom() {
  await nextTick()
  if (messageList.value) {
    messageList.value.scrollTop = messageList.value.scrollHeight
  }
}

watch(
  () => visibleMessages.value.length,
  () => {
    void scrollToBottom()
  },
)

onMounted(async () => {
  await friendsStore.refreshAll()
  chatStore.connect()
  if (chatStore.activeConversation.type === 'public') {
    await chatStore.loadPublicMessages()
  }
  await scrollToBottom()
})
</script>

<template>
  <AppShell>
    <section class="chat-layout">
      <aside class="conversation-list">
        <button
          class="conversation-item"
          :class="{ active: chatStore.activeConversation.type === 'public' }"
          type="button"
          @click="chatStore.selectPublic()"
        >
          <span class="conversation-icon"><Hash :size="18" /></span>
          <span>
            <strong>公共聊天室</strong>
            <small>实时群聊</small>
          </span>
        </button>

        <div class="side-section-title">好友</div>

        <button
          v-for="friend in friendsStore.friends"
          :key="friend.user.id"
          class="conversation-item"
          :class="{
            active:
              chatStore.activeConversation.type === 'private' &&
              chatStore.activeConversation.friendId === friend.user.id,
          }"
          type="button"
          @click="chatStore.selectFriend(friend.user.id)"
        >
          <span class="presence-dot" :class="{ online: friend.online }"></span>
          <span>
            <strong>{{ friend.user.nickname || friend.user.username }}</strong>
            <small>{{ friend.online ? '在线' : '离线' }}</small>
          </span>
          <em v-if="friend.unreadCount > 0">{{ friend.unreadCount }}</em>
        </button>

        <p v-if="friendsStore.friends.length === 0" class="empty-text">还没有好友。</p>
      </aside>

      <section class="chat-panel">
        <header class="panel-header">
          <div>
            <h1>{{ title }}</h1>
            <p>{{ subtitle }}</p>
          </div>
          <div class="header-actions">
            <span class="socket-state" :class="chatStore.socketStatus">
              <Wifi v-if="chatStore.socketStatus === 'open'" :size="16" />
              <WifiOff v-else :size="16" />
              {{ chatStore.socketStatus === 'open' ? '已连接' : '重连中' }}
            </span>
            <button class="icon-button" type="button" title="刷新消息" @click="refreshActive">
              <RefreshCw :size="18" />
            </button>
          </div>
        </header>

        <div ref="messageList" class="message-list">
          <div v-if="chatStore.loadingMessages" class="empty-text">消息加载中...</div>
          <div v-else-if="visibleMessages.length === 0" class="empty-text">暂无消息。</div>

          <article
            v-for="message in visibleMessages"
            :key="`${chatStore.activeConversation.type}-${message.id}`"
            class="message-bubble"
            :class="{ own: isOwnMessage(message) }"
          >
            <div class="message-meta">
              <span>{{ senderName(message) }}</span>
              <time>{{ message.createdAt }}</time>
              <button
                v-if="isOwnMessage(message) && !message.recalled"
                class="message-action"
                type="button"
                title="撤回消息"
                @click="recall(message)"
              >
                <Trash2 :size="14" />
              </button>
            </div>
            <p v-if="message.recalled" class="message-recalled">消息已撤回</p>
            <a v-else-if="isImageMessage(message)" :href="resolveMediaUrl(message.content)" target="_blank">
              <img class="message-image" :src="resolveMediaUrl(message.content)" alt="聊天图片" />
            </a>
            <p v-else>{{ message.content }}</p>
          </article>
        </div>

        <form class="message-form" @submit.prevent="send">
          <MessageCircle :size="18" />
          <button class="icon-button" type="button" title="发送图片" :disabled="chatStore.sending" @click="openImagePicker">
            <ImageIcon :size="18" />
          </button>
          <input ref="imageInput" class="visually-hidden" type="file" accept="image/*" @change="uploadImage" />
          <input
            v-model="draft"
            maxlength="2000"
            placeholder="输入消息"
            :disabled="chatStore.sending"
          />
          <button class="primary-button compact" type="submit" :disabled="chatStore.sending || !draft.trim()">
            <Send :size="18" />
            <span>发送</span>
          </button>
        </form>

        <p v-if="chatStore.socketError" class="form-error">{{ chatStore.socketError }}</p>
      </section>
    </section>
  </AppShell>
</template>
