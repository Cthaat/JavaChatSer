<script setup lang="ts">
import { LogOut, RefreshCw, Shield, Upload, Wifi } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getApiBaseUrl } from '@/api/client'
import AppShell from '@/components/AppShell.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Table, TableBody, TableCell, TableRow } from '@/components/ui/table'
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

const userInitial = computed(() => authStore.user?.nickname?.slice(0, 1) || authStore.user?.username?.slice(0, 1) || 'U')

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
    <section class="space-y-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 class="text-2xl font-semibold tracking-tight">个人资料</h1>
          <p class="mt-1 text-sm text-muted-foreground">当前账号与实时连接状态。</p>
        </div>
        <Button variant="outline" size="icon" type="button" title="刷新资料" @click="authStore.restore()">
          <RefreshCw class="size-4" />
        </Button>
      </div>

      <section class="grid gap-6 lg:grid-cols-[22rem_minmax(0,1fr)]">
        <Card>
          <CardHeader class="items-center text-center">
            <Avatar class="size-24 rounded-lg">
              <AvatarImage v-if="avatarUrl" :src="avatarUrl" alt="头像" />
              <AvatarFallback class="rounded-lg text-3xl">{{ userInitial }}</AvatarFallback>
            </Avatar>
            <div>
              <CardTitle>{{ authStore.user?.nickname || authStore.user?.username }}</CardTitle>
              <CardDescription>@{{ authStore.user?.username }}</CardDescription>
            </div>
            <Badge variant="secondary">{{ authStore.user?.role }}</Badge>
          </CardHeader>
          <CardContent class="grid gap-3">
            <input ref="avatarInput" class="sr-only" type="file" accept="image/*" @change="uploadAvatar" />
            <Button variant="outline" type="button" :disabled="uploading" @click="avatarInput?.click()">
              <Upload class="size-4" />
              <span>{{ uploading ? '上传中' : '上传头像' }}</span>
            </Button>
            <Button variant="secondary" type="button" @click="logout">
              <LogOut class="size-4" />
              <span>退出登录</span>
            </Button>
            <Alert v-if="error" variant="destructive">
              <AlertDescription>{{ error }}</AlertDescription>
            </Alert>
            <Alert v-if="notice" class="border-emerald-500/30 text-emerald-700 dark:text-emerald-300">
              <AlertDescription>{{ notice }}</AlertDescription>
            </Alert>
          </CardContent>
        </Card>

        <Tabs default-value="account" class="space-y-4">
          <TabsList>
            <TabsTrigger value="account">账号信息</TabsTrigger>
            <TabsTrigger value="runtime">运行状态</TabsTrigger>
          </TabsList>

          <TabsContent value="account">
            <Card>
              <CardHeader>
                <CardTitle>账号信息</CardTitle>
                <CardDescription>后端返回的当前登录用户资料。</CardDescription>
              </CardHeader>
              <CardContent>
                <div class="overflow-hidden rounded-md border">
                  <Table>
                    <TableBody>
                      <TableRow v-for="[label, value] in profileRows" :key="label">
                        <TableCell class="w-32 font-medium text-muted-foreground">{{ label }}</TableCell>
                        <TableCell class="break-words">{{ value }}</TableCell>
                      </TableRow>
                    </TableBody>
                  </Table>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="runtime">
            <Card>
              <CardHeader>
                <CardTitle>运行状态</CardTitle>
                <CardDescription>当前账号在聊天系统中的实时状态。</CardDescription>
              </CardHeader>
              <CardContent class="grid gap-4 sm:grid-cols-2">
                <div class="rounded-lg border bg-muted/40 p-4">
                  <Wifi class="size-5 text-muted-foreground" />
                  <p class="mt-3 text-sm text-muted-foreground">WebSocket</p>
                  <p class="mt-1 text-lg font-semibold">{{ chatStore.socketStatus }}</p>
                  <Badge class="mt-3" :variant="chatStore.socketStatus === 'open' ? 'default' : 'secondary'">
                    {{ chatStore.socketStatus === 'open' ? '实时在线' : '等待重连' }}
                  </Badge>
                </div>
                <div class="rounded-lg border bg-muted/40 p-4">
                  <Shield class="size-5 text-muted-foreground" />
                  <p class="mt-3 text-sm text-muted-foreground">好友数量</p>
                  <p class="mt-1 text-lg font-semibold">{{ friendsStore.friends.length }}</p>
                  <Badge class="mt-3" variant="outline">联系人</Badge>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </section>
    </section>
  </AppShell>
</template>
