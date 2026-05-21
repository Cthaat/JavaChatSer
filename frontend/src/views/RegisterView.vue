<script setup lang="ts">
import { MessageCircle, Shield, UserPlus, Users } from '@lucide/vue'
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

import { ApiError } from '@/api/client'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
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
  <main class="grid min-h-screen place-items-center bg-muted/40 px-4 py-8">
    <div class="grid w-full max-w-5xl overflow-hidden rounded-lg border bg-card shadow-sm lg:grid-cols-[1fr_26rem]">
      <section class="hidden bg-primary p-10 text-primary-foreground lg:flex lg:flex-col lg:justify-between">
        <Avatar class="size-12 rounded-lg border border-primary-foreground/20">
          <AvatarFallback class="rounded-lg bg-primary-foreground text-primary">JC</AvatarFallback>
        </Avatar>
        <div class="my-10 grid gap-3 rounded-lg border border-primary-foreground/10 bg-primary-foreground/5 p-4">
          <div class="flex items-center justify-between gap-3">
            <div class="flex items-center gap-2 text-sm">
              <Shield class="size-4" />
              JWT 登录
            </div>
            <Badge variant="secondary">Secure</Badge>
          </div>
          <div class="flex items-center justify-between gap-3">
            <div class="flex items-center gap-2 text-sm">
              <Users class="size-4" />
              好友关系
            </div>
            <Badge variant="secondary">Sync</Badge>
          </div>
          <div class="flex items-center justify-between gap-3">
            <div class="flex items-center gap-2 text-sm">
              <MessageCircle class="size-4" />
              即时消息
            </div>
            <Badge variant="secondary">Live</Badge>
          </div>
        </div>
        <div class="space-y-3">
          <p class="text-sm font-medium text-primary-foreground/70">创建账号</p>
          <h1 class="max-w-md text-4xl font-semibold tracking-tight">JavaChatSer</h1>
          <p class="max-w-md text-sm leading-6 text-primary-foreground/70">注册后会自动进入聊天工作台。</p>
        </div>
      </section>

      <Card class="border-0 shadow-none">
        <CardHeader class="space-y-2 pb-4">
          <CardTitle class="text-2xl">注册</CardTitle>
          <CardDescription>用户名只能包含字母、数字和下划线。</CardDescription>
        </CardHeader>
        <CardContent>
          <form class="grid gap-5" @submit.prevent="submit">
            <div class="grid gap-2">
              <Label for="register-username">用户名</Label>
              <Input
                id="register-username"
                v-model.trim="form.username"
                autocomplete="username"
                required
                minlength="4"
                maxlength="20"
              />
            </div>

            <div class="grid gap-2">
              <Label for="nickname">昵称</Label>
              <Input id="nickname" v-model.trim="form.nickname" maxlength="50" />
            </div>

            <div class="grid gap-2">
              <Label for="register-password">密码</Label>
              <Input
                id="register-password"
                v-model="form.password"
                autocomplete="new-password"
                required
                minlength="6"
                maxlength="32"
                type="password"
              />
            </div>

            <div class="grid gap-2">
              <Label for="avatar-url">头像地址</Label>
              <Input id="avatar-url" v-model.trim="form.avatarUrl" maxlength="255" placeholder="/default-avatar.png" />
            </div>

            <Alert v-if="error" variant="destructive">
              <AlertDescription>{{ error }}</AlertDescription>
            </Alert>

            <Button type="submit" class="w-full" :disabled="authStore.loading">
              <UserPlus class="size-4" />
              <span>{{ authStore.loading ? '创建中' : '创建账号' }}</span>
            </Button>

            <Button as-child variant="link" class="w-full">
              <RouterLink to="/login">已有账号，去登录</RouterLink>
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  </main>
</template>
