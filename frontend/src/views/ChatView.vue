<script setup lang="ts">
import { Hash, Image as ImageIcon, MessageCircle, RefreshCw, Send, Trash2, Wifi, WifiOff } from '@lucide/vue'
import { computed, nextTick, onMounted, ref, watch } from 'vue'

import { getApiBaseUrl } from '@/api/client'
import AppShell from '@/components/AppShell.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Separator } from '@/components/ui/separator'
import { Textarea } from '@/components/ui/textarea'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
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

function initials(name?: string) {
  return name?.slice(0, 1).toUpperCase() || 'U'
}

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
  const viewport = messageList.value?.querySelector<HTMLElement>('[data-slot="scroll-area-viewport"]')
  if (viewport) {
    viewport.scrollTop = viewport.scrollHeight
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
    <Card
      class="grid h-[calc(100dvh-6rem)] min-h-0 grid-rows-[15rem_minmax(0,1fr)] gap-0 overflow-hidden p-0 sm:h-[calc(100dvh-7rem)] lg:h-[calc(100dvh-8rem)] lg:grid-cols-[20rem_minmax(0,1fr)] lg:grid-rows-1"
    >
      <aside class="min-h-0 border-b bg-card lg:border-b-0 lg:border-r">
        <ScrollArea class="h-full">
          <div class="space-y-2 p-3">
            <Button
              :variant="chatStore.activeConversation.type === 'public' ? 'secondary' : 'ghost'"
              class="h-auto w-full justify-start gap-3 px-3 py-3"
              type="button"
              @click="chatStore.selectPublic()"
            >
              <Avatar class="size-9">
                <AvatarFallback class="bg-primary text-primary-foreground">
                  <Hash class="size-4" />
                </AvatarFallback>
              </Avatar>
              <span class="grid flex-1 text-left">
                <span class="font-medium">公共聊天室</span>
                <span class="text-xs text-muted-foreground">实时群聊</span>
              </span>
            </Button>

            <div class="px-3 pt-4 text-xs font-semibold uppercase text-muted-foreground">好友</div>

            <Button
              v-for="friend in friendsStore.friends"
              :key="friend.user.id"
              :variant="
                chatStore.activeConversation.type === 'private' &&
                chatStore.activeConversation.friendId === friend.user.id
                  ? 'secondary'
                  : 'ghost'
              "
              class="h-auto w-full justify-start gap-3 px-3 py-3"
              type="button"
              @click="chatStore.selectFriend(friend.user.id)"
            >
              <span class="relative">
                <Avatar class="size-9">
                  <AvatarFallback>
                    {{ initials(friend.user.nickname || friend.user.username) }}
                  </AvatarFallback>
                </Avatar>
                <span
                  class="absolute -right-0.5 -bottom-0.5 size-3 rounded-full border-2 border-card"
                  :class="friend.online ? 'bg-emerald-500' : 'bg-muted-foreground'"
                />
              </span>
              <span class="grid min-w-0 flex-1 text-left">
                <span class="truncate font-medium">{{ friend.user.nickname || friend.user.username }}</span>
                <span class="text-xs text-muted-foreground">{{ friend.online ? '在线' : '离线' }}</span>
              </span>
              <Badge v-if="friend.unreadCount > 0" variant="secondary">{{ friend.unreadCount }}</Badge>
            </Button>

            <p v-if="friendsStore.friends.length === 0" class="rounded-md border border-dashed p-4 text-sm text-muted-foreground">
              还没有好友。
            </p>
          </div>
        </ScrollArea>
      </aside>

      <section class="grid min-h-0 grid-rows-[auto_minmax(0,1fr)_auto_auto]">
        <header class="flex items-center justify-between gap-4 border-b px-4 py-4 sm:px-6">
          <div class="min-w-0">
            <h1 class="truncate text-xl font-semibold">{{ title }}</h1>
            <p class="mt-1 text-sm text-muted-foreground">{{ subtitle }}</p>
          </div>
          <div class="flex shrink-0 items-center gap-2">
            <Badge :variant="chatStore.socketStatus === 'open' ? 'default' : 'secondary'" class="gap-1.5">
              <Wifi v-if="chatStore.socketStatus === 'open'" class="size-3.5" />
              <WifiOff v-else class="size-3.5" />
              {{ chatStore.socketStatus === 'open' ? '已连接' : '重连中' }}
            </Badge>
            <Tooltip>
              <TooltipTrigger as-child>
                <Button variant="outline" size="icon" type="button" title="刷新消息" @click="refreshActive">
                  <RefreshCw class="size-4" />
                </Button>
              </TooltipTrigger>
              <TooltipContent>刷新消息</TooltipContent>
            </Tooltip>
          </div>
        </header>

        <div ref="messageList" class="min-h-0 bg-muted/30">
          <ScrollArea class="h-full">
            <div class="space-y-3 p-4 sm:p-6">
              <div v-if="chatStore.loadingMessages" class="text-sm text-muted-foreground">消息加载中...</div>
              <div
                v-else-if="visibleMessages.length === 0"
                class="rounded-lg border border-dashed bg-card p-8 text-center text-sm text-muted-foreground"
              >
                暂无消息。
              </div>

              <article
                v-for="message in visibleMessages"
                :key="`${chatStore.activeConversation.type}-${message.id}`"
                class="flex"
                :class="isOwnMessage(message) ? 'justify-end' : 'justify-start'"
              >
                <div
                  class="max-w-[min(42rem,82%)] rounded-lg border px-3 py-2 shadow-xs"
                  :class="
                    isOwnMessage(message)
                      ? 'border-primary bg-primary text-primary-foreground'
                      : 'bg-card text-card-foreground'
                  "
                >
                  <div class="flex flex-wrap items-center gap-2 text-xs opacity-75">
                    <span class="font-medium">{{ senderName(message) }}</span>
                    <time>{{ message.createdAt }}</time>
                    <Tooltip v-if="isOwnMessage(message) && !message.recalled">
                      <TooltipTrigger as-child>
                        <Button
                          variant="ghost"
                          size="icon-sm"
                          class="size-6 hover:text-destructive"
                          type="button"
                          title="撤回消息"
                          @click="recall(message)"
                        >
                          <Trash2 class="size-3.5" />
                        </Button>
                      </TooltipTrigger>
                      <TooltipContent>撤回消息</TooltipContent>
                    </Tooltip>
                  </div>
                  <p v-if="message.recalled" class="mt-1 text-sm italic opacity-75">消息已撤回</p>
                  <a v-else-if="isImageMessage(message)" :href="resolveMediaUrl(message.content)" target="_blank">
                    <img
                      class="mt-2 block max-h-64 w-full max-w-80 rounded-md object-cover"
                      :src="resolveMediaUrl(message.content)"
                      alt="聊天图片"
                    />
                  </a>
                  <p v-else class="mt-1 whitespace-pre-wrap break-words text-sm leading-6">{{ message.content }}</p>
                </div>
              </article>
            </div>
          </ScrollArea>
        </div>

        <Separator />
        <form class="grid gap-3 p-4 sm:grid-cols-[auto_auto_minmax(0,1fr)_auto] sm:items-end" @submit.prevent="send">
          <MessageCircle class="hidden size-4 text-muted-foreground sm:block" />
          <Tooltip>
            <TooltipTrigger as-child>
              <Button
                variant="outline"
                size="icon"
                type="button"
                title="发送图片"
                :disabled="chatStore.sending"
                @click="openImagePicker"
              >
                <ImageIcon class="size-4" />
              </Button>
            </TooltipTrigger>
            <TooltipContent>发送图片</TooltipContent>
          </Tooltip>
          <input ref="imageInput" class="sr-only" type="file" accept="image/*" @change="uploadImage" />
          <Textarea
            v-model="draft"
            maxlength="2000"
            placeholder="输入消息"
            :disabled="chatStore.sending"
            class="min-h-10 resize-none"
          />
          <Button type="submit" :disabled="chatStore.sending || !draft.trim()">
            <Send class="size-4" />
            <span>发送</span>
          </Button>
        </form>

        <Alert v-if="chatStore.socketError" variant="destructive" class="m-4 mt-0">
          <AlertDescription>{{ chatStore.socketError }}</AlertDescription>
        </Alert>
      </section>
    </Card>
  </AppShell>
</template>
