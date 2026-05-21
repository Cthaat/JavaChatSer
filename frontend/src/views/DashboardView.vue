<script setup lang="ts">
import { MessageSquare, RefreshCw, Shield, Users, Wifi } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'

import { statsApi } from '@/api'
import { ApiError } from '@/api/client'
import AppShell from '@/components/AppShell.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Table, TableBody, TableCell, TableRow } from '@/components/ui/table'
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
    <section class="space-y-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 class="text-2xl font-semibold tracking-tight">系统概览</h1>
          <p class="mt-1 text-sm text-muted-foreground">{{ scopeText }}</p>
        </div>
        <Button variant="outline" size="icon" type="button" title="刷新统计" :disabled="loading" @click="loadStats">
          <RefreshCw class="size-4" :class="{ 'animate-spin': loading }" />
        </Button>
      </div>

      <Alert v-if="error" variant="destructive">
        <AlertDescription>{{ error }}</AlertDescription>
      </Alert>

      <section class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4" aria-live="polite">
        <Card v-for="card in cards" :key="card.label" class="overflow-hidden">
          <CardHeader class="pb-2">
            <CardDescription>{{ card.label }}</CardDescription>
            <CardAction>
              <div class="rounded-md border bg-muted/50 p-2">
                <component :is="card.icon" class="size-4 text-muted-foreground" />
              </div>
            </CardAction>
          </CardHeader>
          <CardContent>
            <Skeleton v-if="loading" class="h-9 w-20" />
            <p v-else class="text-3xl font-semibold tracking-tight">{{ card.value }}</p>
            <p class="mt-2 text-xs text-muted-foreground">{{ card.hint }}</p>
          </CardContent>
        </Card>
      </section>

      <Tabs default-value="runtime" class="space-y-4">
        <TabsList>
          <TabsTrigger value="runtime">运行诊断</TabsTrigger>
          <TabsTrigger value="scope">统计范围</TabsTrigger>
        </TabsList>

        <TabsContent value="runtime">
          <Card>
            <CardHeader>
              <CardTitle>实时连接</CardTitle>
              <CardDescription>当前浏览器和后端 WebSocket 的连接状态。</CardDescription>
            </CardHeader>
            <CardContent>
              <div class="overflow-hidden rounded-md border">
                <Table>
                  <TableBody>
                    <TableRow>
                      <TableCell class="font-medium">WebSocket</TableCell>
                      <TableCell>{{ chatStore.socketStatus }}</TableCell>
                      <TableCell class="text-right">
                        <Badge :variant="chatStore.socketStatus === 'open' ? 'default' : 'secondary'">
                          {{ chatStore.socketStatus === 'open' ? '已连接' : '重连中' }}
                        </Badge>
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell class="font-medium">好友同步</TableCell>
                      <TableCell>{{ friendsStore.friends.length }} 位好友</TableCell>
                      <TableCell class="text-right">
                        <Badge variant="outline">已加载</Badge>
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell class="font-medium">账号</TableCell>
                      <TableCell>{{ authStore.user?.username }}</TableCell>
                      <TableCell class="text-right">
                        <Badge variant="secondary">{{ authStore.user?.role }}</Badge>
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="scope">
          <Card>
            <CardHeader>
              <CardTitle>统计范围</CardTitle>
              <CardDescription>{{ scopeText }}</CardDescription>
            </CardHeader>
            <CardContent class="grid gap-4 sm:grid-cols-3">
              <div class="rounded-lg border bg-muted/40 p-4">
                <p class="text-sm text-muted-foreground">范围</p>
                <p class="mt-1 text-lg font-semibold">{{ stats?.scope || 'PERSONAL' }}</p>
              </div>
              <div class="rounded-lg border bg-muted/40 p-4">
                <p class="text-sm text-muted-foreground">待处理申请</p>
                <p class="mt-1 text-lg font-semibold">{{ stats?.pendingFriendRequestCount ?? 0 }}</p>
              </div>
              <div class="rounded-lg border bg-muted/40 p-4">
                <p class="text-sm text-muted-foreground">好友关系</p>
                <p class="mt-1 text-lg font-semibold">{{ stats?.acceptedFriendCount ?? 0 }}</p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </section>
  </AppShell>
</template>
