<script setup lang="ts">
import { Check, RefreshCw, Search, Trash2, UserPlus, X } from '@lucide/vue'
import { onMounted, ref } from 'vue'

import { ApiError } from '@/api/client'
import AppShell from '@/components/AppShell.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  Table,
  TableBody,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
import { useFriendsStore } from '@/stores/friends'

const friendsStore = useFriendsStore()
const keyword = ref('')
const error = ref('')
const notice = ref('')

function initials(name?: string) {
  return name?.slice(0, 1).toUpperCase() || 'U'
}

async function run(action: () => Promise<void>, successMessage?: string) {
  error.value = ''
  notice.value = ''
  try {
    await action()
    if (successMessage) {
      notice.value = successMessage
    }
  } catch (exception) {
    error.value = exception instanceof ApiError ? exception.message : '操作失败'
  }
}

async function searchUsers() {
  await run(() => friendsStore.search(keyword.value))
}

onMounted(() => {
  void friendsStore.refreshAll()
})
</script>

<template>
  <AppShell>
    <section class="space-y-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 class="text-2xl font-semibold tracking-tight">好友管理</h1>
          <p class="mt-1 text-sm text-muted-foreground">搜索用户、处理申请、查看好友在线状态。</p>
        </div>
        <Button variant="outline" size="icon" type="button" title="刷新" @click="friendsStore.refreshAll()">
          <RefreshCw class="size-4" />
        </Button>
      </div>

      <Card>
        <CardContent class="pt-6">
          <form class="flex flex-col gap-3 sm:flex-row" @submit.prevent="searchUsers">
            <div class="relative flex-1">
              <Search class="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input v-model.trim="keyword" maxlength="50" placeholder="按用户名或昵称搜索" class="pl-9" />
            </div>
            <Button type="submit">
              <Search class="size-4" />
              <span>搜索</span>
            </Button>
          </form>
        </CardContent>
      </Card>

      <Alert v-if="error" variant="destructive">
        <AlertDescription>{{ error }}</AlertDescription>
      </Alert>
      <Alert v-if="notice" class="border-emerald-500/30 text-emerald-700 dark:text-emerald-300">
        <AlertDescription>{{ notice }}</AlertDescription>
      </Alert>

      <Tabs default-value="friends" class="space-y-4">
        <TabsList class="grid w-full grid-cols-3 sm:w-auto">
          <TabsTrigger value="friends">好友列表</TabsTrigger>
          <TabsTrigger value="requests">收到的申请</TabsTrigger>
          <TabsTrigger value="search">搜索结果</TabsTrigger>
        </TabsList>

        <TabsContent value="friends">
          <Card>
            <CardHeader>
              <CardTitle>好友列表</CardTitle>
              <CardDescription>当前好友关系、在线状态与未读消息。</CardDescription>
            </CardHeader>
            <CardContent>
              <div class="overflow-hidden rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>用户</TableHead>
                      <TableHead>状态</TableHead>
                      <TableHead>未读</TableHead>
                      <TableHead class="w-16 text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    <TableEmpty v-if="friendsStore.friends.length === 0" :colspan="4">
                      暂无好友。
                    </TableEmpty>
                    <TableRow v-for="friend in friendsStore.friends" :key="friend.user.id">
                      <TableCell>
                        <div class="flex items-center gap-3">
                          <span class="relative">
                            <Avatar class="size-9 rounded-lg">
                              <AvatarFallback class="rounded-lg">
                                {{ initials(friend.user.nickname || friend.user.username) }}
                              </AvatarFallback>
                            </Avatar>
                            <span
                              class="absolute -right-0.5 -bottom-0.5 size-3 rounded-full border-2 border-card"
                              :class="friend.online ? 'bg-emerald-500' : 'bg-muted-foreground'"
                            />
                          </span>
                          <div class="min-w-0">
                            <p class="truncate text-sm font-medium">{{ friend.user.nickname || friend.user.username }}</p>
                            <p class="truncate text-xs text-muted-foreground">@{{ friend.user.username }}</p>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge :variant="friend.online ? 'default' : 'secondary'">
                          {{ friend.online ? '在线' : '离线' }}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <Badge variant="outline">未读 {{ friend.unreadCount }}</Badge>
                      </TableCell>
                      <TableCell class="text-right">
                        <Tooltip>
                          <TooltipTrigger as-child>
                            <Button
                              variant="ghost"
                              size="icon"
                              type="button"
                              title="删除好友"
                              @click="run(() => friendsStore.deleteFriend(friend.user.id), '好友已删除')"
                            >
                              <Trash2 class="size-4 text-destructive" />
                            </Button>
                          </TooltipTrigger>
                          <TooltipContent>删除好友</TooltipContent>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="requests">
          <Card>
            <CardHeader>
              <CardTitle>收到的申请</CardTitle>
              <CardDescription>接受或拒绝待处理好友申请。</CardDescription>
            </CardHeader>
            <CardContent>
              <div class="overflow-hidden rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>申请人</TableHead>
                      <TableHead class="w-32 text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    <TableEmpty v-if="friendsStore.requests.length === 0" :colspan="2">
                      暂无待处理申请。
                    </TableEmpty>
                    <TableRow v-for="request in friendsStore.requests" :key="request.requestId">
                      <TableCell>
                        <div class="flex items-center gap-3">
                          <Avatar class="size-9 rounded-lg">
                            <AvatarFallback class="rounded-lg">
                              {{ initials(request.requester.nickname || request.requester.username) }}
                            </AvatarFallback>
                          </Avatar>
                          <div class="min-w-0">
                            <p class="truncate text-sm font-medium">
                              {{ request.requester.nickname || request.requester.username }}
                            </p>
                            <p class="truncate text-xs text-muted-foreground">@{{ request.requester.username }}</p>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell class="text-right">
                        <div class="flex justify-end gap-2">
                          <Tooltip>
                            <TooltipTrigger as-child>
                              <Button
                                variant="outline"
                                size="icon"
                                type="button"
                                title="接受"
                                @click="run(() => friendsStore.acceptRequest(request.requestId), '已接受好友申请')"
                              >
                                <Check class="size-4" />
                              </Button>
                            </TooltipTrigger>
                            <TooltipContent>接受</TooltipContent>
                          </Tooltip>
                          <Tooltip>
                            <TooltipTrigger as-child>
                              <Button
                                variant="destructive"
                                size="icon"
                                type="button"
                                title="拒绝"
                                @click="run(() => friendsStore.rejectRequest(request.requestId), '已拒绝好友申请')"
                              >
                                <X class="size-4" />
                              </Button>
                            </TooltipTrigger>
                            <TooltipContent>拒绝</TooltipContent>
                          </Tooltip>
                        </div>
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="search">
          <Card>
            <CardHeader>
              <CardTitle>搜索结果</CardTitle>
              <CardDescription>向检索到的用户发送好友申请。</CardDescription>
            </CardHeader>
            <CardContent>
              <div class="overflow-hidden rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>用户</TableHead>
                      <TableHead class="w-16 text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    <TableEmpty v-if="friendsStore.searchResults.length === 0" :colspan="2">
                      暂无搜索结果。
                    </TableEmpty>
                    <TableRow v-for="user in friendsStore.searchResults" :key="user.id">
                      <TableCell>
                        <div class="flex items-center gap-3">
                          <Avatar class="size-9 rounded-lg">
                            <AvatarFallback class="rounded-lg">{{ initials(user.nickname || user.username) }}</AvatarFallback>
                          </Avatar>
                          <div class="min-w-0">
                            <p class="truncate text-sm font-medium">{{ user.nickname || user.username }}</p>
                            <p class="truncate text-xs text-muted-foreground">@{{ user.username }}</p>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell class="text-right">
                        <Tooltip>
                          <TooltipTrigger as-child>
                            <Button
                              variant="ghost"
                              size="icon"
                              type="button"
                              title="发送好友申请"
                              @click="run(() => friendsStore.sendRequest(user.id), '好友申请已发送')"
                            >
                              <UserPlus class="size-4" />
                            </Button>
                          </TooltipTrigger>
                          <TooltipContent>发送好友申请</TooltipContent>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </section>
  </AppShell>
</template>
