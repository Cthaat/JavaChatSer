<script setup lang="ts">
import { UserPlus } from '@lucide/vue'
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

import { ApiError } from '@/api/client'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useFriendsStore } from '@/stores/friends'

const authStore = useAuthStore()
const friendsStore = useFriendsStore()
const chatStore = useChatStore()
const router = useRouter()
const form = reactive({
  username: '',
  nickname: '',
  password: '',
  avatarUrl: '',
})
const error = ref('')

async function submit() {
  error.value = ''
  try {
    await authStore.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname || undefined,
      avatarUrl: form.avatarUrl || undefined,
    })
    await friendsStore.refreshAll()
    chatStore.connect()
    await router.replace('/chat')
  } catch (exception) {
    error.value = exception instanceof ApiError ? exception.message : '注册失败'
  }
}
</script>

<template>
  <main class="auth-page">
    <section class="auth-panel">
      <div class="auth-copy">
        <div class="brand-mark">JC</div>
        <h1>JavaChatSer</h1>
        <p>注册后会自动进入聊天工作台。</p>
      </div>

      <form class="auth-card" @submit.prevent="submit">
        <div>
          <h2>注册</h2>
          <p>用户名只能包含字母、数字和下划线。</p>
        </div>

        <label>
          <span>用户名</span>
          <input v-model.trim="form.username" autocomplete="username" required minlength="4" maxlength="20" />
        </label>

        <label>
          <span>昵称</span>
          <input v-model.trim="form.nickname" maxlength="50" />
        </label>

        <label>
          <span>密码</span>
          <input
            v-model="form.password"
            autocomplete="new-password"
            required
            minlength="6"
            maxlength="32"
            type="password"
          />
        </label>

        <label>
          <span>头像地址</span>
          <input v-model.trim="form.avatarUrl" maxlength="255" placeholder="/default-avatar.png" />
        </label>

        <p v-if="error" class="form-error">{{ error }}</p>

        <button class="primary-button" type="submit" :disabled="authStore.loading">
          <UserPlus :size="18" />
          <span>{{ authStore.loading ? '创建中' : '创建账号' }}</span>
        </button>

        <RouterLink class="text-link" to="/login">已有账号，去登录</RouterLink>
      </form>
    </section>
  </main>
</template>
