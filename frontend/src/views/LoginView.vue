<script setup lang="ts">
import { LogIn, MessageCircle, Users, Wifi } from '@lucide/vue'
import { reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

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
  <main class="grid min-h-screen place-items-center bg-muted/40 px-4 py-8">
    <div class="grid w-full max-w-5xl overflow-hidden rounded-lg border bg-card shadow-sm lg:grid-cols-[1fr_26rem]">
      <section class="hidden bg-primary p-10 text-primary-foreground lg:flex lg:flex-col lg:justify-between">
        <Avatar class="size-12 rounded-lg border border-primary-foreground/20">
          <AvatarFallback class="rounded-lg bg-primary-foreground text-primary">JC</AvatarFallback>
        </Avatar>
        <div class="my-10 grid gap-3 rounded-lg border border-primary-foreground/10 bg-primary-foreground/5 p-4">
          <div class="flex items-center justify-between gap-3">
            <div class="flex items-center gap-2 text-sm">
              <MessageCircle class="size-4" />
              公共聊天室
            </div>
            <Badge variant="secondary">Live</Badge>
          </div>
          <div class="flex items-center justify-between gap-3">
            <div class="flex items-center gap-2 text-sm">
              <Users class="size-4" />
              好友状态
            </div>
            <Badge variant="secondary">Online</Badge>
          </div>
          <div class="flex items-center justify-between gap-3">
            <div class="flex items-center gap-2 text-sm">
              <Wifi class="size-4" />
              WebSocket
            </div>
            <Badge variant="secondary">Ready</Badge>
          </div>
        </div>
        <div class="space-y-3">
          <p class="text-sm font-medium text-primary-foreground/70">Spring Boot + Vue 实时聊天系统</p>
          <h1 class="max-w-md text-4xl font-semibold tracking-tight">JavaChatSer</h1>
          <p class="max-w-md text-sm leading-6 text-primary-foreground/70">登录后进入聊天工作台。</p>
        </div>
      </section>

      <Card class="border-0 shadow-none">
        <CardHeader class="space-y-2 pb-4">
          <CardTitle class="text-2xl">登录</CardTitle>
          <CardDescription>默认演示账号：admin / 123456</CardDescription>
        </CardHeader>
        <CardContent>
          <form class="grid gap-5" @submit.prevent="submit">
            <div class="grid gap-2">
              <Label for="username">用户名</Label>
              <Input
                id="username"
                v-model.trim="form.username"
                autocomplete="username"
                required
                minlength="4"
                maxlength="20"
              />
            </div>

            <div class="grid gap-2">
              <Label for="password">密码</Label>
              <Input
                id="password"
                v-model="form.password"
                autocomplete="current-password"
                required
                minlength="6"
                maxlength="32"
                type="password"
              />
            </div>

            <Alert v-if="error" variant="destructive">
              <AlertDescription>{{ error }}</AlertDescription>
            </Alert>

            <Button type="submit" class="w-full" :disabled="authStore.loading">
              <LogIn class="size-4" />
              <span>{{ authStore.loading ? '登录中' : '登录' }}</span>
            </Button>

            <Button as-child variant="link" class="w-full">
              <RouterLink to="/register">创建新账号</RouterLink>
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  </main>
</template>
