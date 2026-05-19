<script setup lang="ts">
import { LogOut, RefreshCw, Shield, Wifi } from '@lucide/vue'
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

import AppShell from '@/components/AppShell.vue'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useFriendsStore } from '@/stores/friends'

const authStore = useAuthStore()
const chatStore = useChatStore()
const friendsStore = useFriendsStore()
const router = useRouter()

const profileRows = computed(() => [
  ['用户 ID', authStore.user?.id],
  ['用户名', authStore.user?.username],
  ['昵称', authStore.user?.nickname],
  ['角色', authStore.user?.role],
  ['个人简介', authStore.user?.bio || '未填写'],
])

async function logout() {
  chatStore.reset()
  await authStore.logout()
  await router.replace({ name: 'login' })
}

onMounted(() => {
  chatStore.connect()
  void friendsStore.refreshAll()
})
</script>

<template>
  <AppShell>
    <section class="page-stack">
      <header class="panel-header">
        <div>
          <h1>个人资料</h1>
          <p>当前账号与实时连接状态。</p>
        </div>
        <button class="icon-button" type="button" title="刷新资料" @click="authStore.restore()">
          <RefreshCw :size="18" />
        </button>
      </header>

      <section class="profile-grid">
        <div class="surface profile-card">
          <div class="profile-avatar">
            {{ authStore.user?.nickname?.slice(0, 1) || authStore.user?.username?.slice(0, 1) || 'U' }}
          </div>
          <h2>{{ authStore.user?.nickname || authStore.user?.username }}</h2>
          <p>@{{ authStore.user?.username }}</p>
          <button class="secondary-button" type="button" @click="logout">
            <LogOut :size="18" />
            <span>退出登录</span>
          </button>
        </div>

        <div class="surface">
          <h2>账号信息</h2>
          <dl class="definition-list">
            <template v-for="[label, value] in profileRows" :key="label">
              <dt>{{ label }}</dt>
              <dd>{{ value }}</dd>
            </template>
          </dl>
        </div>

        <div class="surface">
          <h2>运行状态</h2>
          <div class="status-grid">
            <div>
              <Wifi :size="20" />
              <span>WebSocket</span>
              <strong>{{ chatStore.socketStatus }}</strong>
            </div>
            <div>
              <Shield :size="20" />
              <span>好友数量</span>
              <strong>{{ friendsStore.friends.length }}</strong>
            </div>
          </div>
        </div>
      </section>
    </section>
  </AppShell>
</template>
