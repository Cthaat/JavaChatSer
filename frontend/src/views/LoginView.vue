<script setup lang="ts">
import { LogIn } from '@lucide/vue'
import { reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { ApiError } from '@/api/client'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useFriendsStore } from '@/stores/friends'

const authStore = useAuthStore()
const friendsStore = useFriendsStore()
const chatStore = useChatStore()
const router = useRouter()
const route = useRoute()
const form = reactive({
  username: 'admin',
  password: '123456',
})
const error = ref('')

async function submit() {
  error.value = ''
  try {
    await authStore.login(form.username, form.password)
    await friendsStore.refreshAll()
    chatStore.connect()
    await router.replace((route.query.redirect as string) || '/chat')
  } catch (exception) {
    error.value = exception instanceof ApiError ? exception.message : '登录失败'
  }
}
</script>

<template>
  <main class="auth-page">
    <section class="auth-panel">
      <div class="auth-copy">
        <div class="brand-mark">JC</div>
        <h1>JavaChatSer</h1>
        <p>登录后进入聊天工作台。</p>
      </div>

      <form class="auth-card" @submit.prevent="submit">
        <div>
          <h2>登录</h2>
          <p>默认演示账号：admin / 123456</p>
        </div>

        <label>
          <span>用户名</span>
          <input v-model.trim="form.username" autocomplete="username" required minlength="4" maxlength="20" />
        </label>

        <label>
          <span>密码</span>
          <input
            v-model="form.password"
            autocomplete="current-password"
            required
            minlength="6"
            maxlength="32"
            type="password"
          />
        </label>

        <p v-if="error" class="form-error">{{ error }}</p>

        <button class="primary-button" type="submit" :disabled="authStore.loading">
          <LogIn :size="18" />
          <span>{{ authStore.loading ? '登录中' : '登录' }}</span>
        </button>

        <RouterLink class="text-link" to="/register">创建新账号</RouterLink>
      </form>
    </section>
  </main>
</template>
