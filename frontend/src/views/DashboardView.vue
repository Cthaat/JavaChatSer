<script setup lang="ts">
import { MessageSquare, RefreshCw, Shield, Users, Wifi } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'

import { statsApi } from '@/api'
import { ApiError } from '@/api/client'
import AppShell from '@/components/AppShell.vue'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useFriendsStore } from '@/stores/friends'
import type { StatsOverview } from '@/types'

const authStore = useAuthStore()
const chatStore = useChatStore()
const friendsStore = useFriendsStore()
const stats = ref<StatsOverview | null>(null)
const loading = ref(false)
const error = ref('')

const scopeText = computed(() => (stats.value?.scope === 'GLOBAL' ? '管理员全局概览' : '个人使用概览'))

const cards = computed(() => {
  const value = stats.value
  return [
    {
      label: value?.scope === 'GLOBAL' ? '注册用户' : '当前账号',
      value: value?.registeredUserCount ?? 0,
      hint: value?.scope === 'GLOBAL' ? '已启用账号总数' : authStore.user?.username ?? '个人账号',
      icon: Users,
    },
    {
      label: '在线用户',
      value: value?.onlineUserCount ?? 0,
      hint: value?.scope === 'GLOBAL' ? '当前 WebSocket 在线数' : chatStore.socketStatus,
      icon: Wifi,
    },
    {
      label: '今日私聊',
      value: value?.todayPrivateMessageCount ?? 0,
      hint: '当天私聊消息数',
      icon: MessageSquare,
    },
    {
      label: '今日公共消息',
      value: value?.todayPublicMessageCount ?? 0,
      hint: '当天公共聊天室消息数',
      icon: MessageSquare,
    },
    {
      label: '累计私聊',
      value: value?.totalPrivateMessageCount ?? 0,
      hint: '历史私聊消息数',
      icon: MessageSquare,
    },
    {
      label: '累计公共消息',
      value: value?.totalPublicMessageCount ?? 0,
      hint: '历史公共聊天室消息数',
      icon: MessageSquare,
    },
    {
      label: '待处理申请',
      value: value?.pendingFriendRequestCount ?? 0,
      hint: value?.scope === 'GLOBAL' ? '全站待处理申请' : '收到的待处理申请',
      icon: Shield,
    },
    {
      label: '好友关系',
      value: value?.acceptedFriendCount ?? 0,
      hint: value?.scope === 'GLOBAL' ? '全站好友对数' : '我的好友数',
      icon: Users,
    },
  ]
})

async function loadStats() {
  loading.value = true
  error.value = ''
  try {
    stats.value = await statsApi.overview()
  } catch (exception) {
    error.value = exception instanceof ApiError ? exception.message : '统计数据加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  chatStore.connect()
  await friendsStore.refreshAll()
  await loadStats()
})
</script>

<template>
  <AppShell>
    <section class="page-stack">
      <header class="panel-header">
        <div>
          <h1>系统概览</h1>
          <p>{{ scopeText }}</p>
        </div>
        <button class="icon-button" type="button" title="刷新统计" :disabled="loading" @click="loadStats">
          <RefreshCw :size="18" />
        </button>
      </header>

      <p v-if="error" class="form-error">{{ error }}</p>

      <section class="metric-grid" aria-live="polite">
        <article v-for="card in cards" :key="card.label" class="metric-card">
          <component :is="card.icon" :size="22" />
          <span>{{ card.label }}</span>
          <strong>{{ card.value }}</strong>
          <small>{{ card.hint }}</small>
        </article>
      </section>
    </section>
  </AppShell>
</template>
