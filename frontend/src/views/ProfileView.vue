<script setup lang="ts">
import { LogOut, RefreshCw, Shield, Upload, Wifi } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getApiBaseUrl } from '@/api/client'
import AppShell from '@/components/AppShell.vue'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useFriendsStore } from '@/stores/friends'

const authStore = useAuthStore()
const chatStore = useChatStore()
const friendsStore = useFriendsStore()
const router = useRouter()
const avatarInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const error = ref('')
const notice = ref('')

const profileRows = computed(() => [
  ['用户 ID', authStore.user?.id],
  ['用户名', authStore.user?.username],
  ['昵称', authStore.user?.nickname],
  ['角色', authStore.user?.role],
  ['个人简介', authStore.user?.bio || '未填写'],
])

const avatarUrl = computed(() => {
  const url = authStore.user?.avatarUrl
  if (!url || url === '/default-avatar.png') {
    return ''
  }
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
    return url
  }
  const baseUrl = getApiBaseUrl()
  return baseUrl ? `${baseUrl}${url}` : url
})

async function logout() {
  chatStore.reset()
  await authStore.logout()
  await router.replace({ name: 'login' })
}

async function uploadAvatar(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  target.value = ''
  if (!file) {
    return
  }
  uploading.value = true
  error.value = ''
  notice.value = ''
  try {
    await authStore.updateAvatar(file)
    notice.value = '头像已更新'
  } catch {
    error.value = '头像上传失败，请检查图片格式和大小'
  } finally {
    uploading.value = false
  }
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
            <img v-if="avatarUrl" :src="avatarUrl" alt="头像" />
            <span v-else>
              {{ authStore.user?.nickname?.slice(0, 1) || authStore.user?.username?.slice(0, 1) || 'U' }}
            </span>
          </div>
          <h2>{{ authStore.user?.nickname || authStore.user?.username }}</h2>
          <p>@{{ authStore.user?.username }}</p>
          <input ref="avatarInput" class="visually-hidden" type="file" accept="image/*" @change="uploadAvatar" />
          <button class="secondary-button" type="button" :disabled="uploading" @click="avatarInput?.click()">
            <Upload :size="18" />
            <span>{{ uploading ? '上传中' : '上传头像' }}</span>
          </button>
          <button class="secondary-button" type="button" @click="logout">
            <LogOut :size="18" />
            <span>退出登录</span>
          </button>
          <p v-if="error" class="form-error">{{ error }}</p>
          <p v-if="notice" class="form-notice">{{ notice }}</p>
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
