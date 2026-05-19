<script setup lang="ts">
import { BarChart3, LogOut, MessageCircle, Moon, Sun, User, Users } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useFriendsStore } from '@/stores/friends'

const authStore = useAuthStore()
const chatStore = useChatStore()
const friendsStore = useFriendsStore()
const router = useRouter()
const theme = ref<'light' | 'dark'>('light')
const THEME_KEY = 'javachat.theme'

const unreadTotal = computed(() =>
  friendsStore.friends.reduce((total, friend) => total + friend.unreadCount, 0),
)

async function logout() {
  chatStore.reset()
  await authStore.logout()
  await router.replace({ name: 'login' })
}

function applyTheme(nextTheme: 'light' | 'dark') {
  theme.value = nextTheme
  document.documentElement.dataset.theme = nextTheme
  localStorage.setItem(THEME_KEY, nextTheme)
}

function toggleTheme() {
  applyTheme(theme.value === 'dark' ? 'light' : 'dark')
}

onMounted(() => {
  const savedTheme = localStorage.getItem(THEME_KEY)
  applyTheme(savedTheme === 'dark' ? 'dark' : 'light')
})
</script>

<template>
  <div class="app-shell">
    <aside class="app-nav">
      <div class="brand">
        <div class="brand-mark">JC</div>
        <div>
          <strong>JavaChatSer</strong>
          <span>{{ chatStore.socketStatus === 'open' ? '实时在线' : '连接中' }}</span>
        </div>
      </div>

      <nav class="nav-links">
        <RouterLink to="/chat" class="nav-link">
          <MessageCircle :size="18" />
          <span>聊天</span>
          <em v-if="unreadTotal > 0">{{ unreadTotal }}</em>
        </RouterLink>
        <RouterLink to="/friends" class="nav-link">
          <Users :size="18" />
          <span>好友</span>
          <em v-if="friendsStore.requests.length > 0">{{ friendsStore.requests.length }}</em>
        </RouterLink>
        <RouterLink to="/dashboard" class="nav-link">
          <BarChart3 :size="18" />
          <span>概览</span>
        </RouterLink>
        <RouterLink to="/profile" class="nav-link">
          <User :size="18" />
          <span>资料</span>
        </RouterLink>
      </nav>

      <div class="nav-user">
        <div class="avatar">{{ authStore.user?.nickname?.slice(0, 1) || 'U' }}</div>
        <div>
          <strong>{{ authStore.user?.nickname || authStore.user?.username }}</strong>
          <span>@{{ authStore.user?.username }}</span>
        </div>
        <button class="icon-button" type="button" :title="theme === 'dark' ? '切换浅色模式' : '切换深色模式'" @click="toggleTheme">
          <Sun v-if="theme === 'dark'" :size="18" />
          <Moon v-else :size="18" />
        </button>
        <button class="icon-button" type="button" title="退出登录" @click="logout">
          <LogOut :size="18" />
        </button>
      </div>
    </aside>

    <main class="app-main">
      <slot />
    </main>
  </div>
</template>
