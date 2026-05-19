<script setup lang="ts">
import { Check, RefreshCw, Search, Trash2, UserPlus, X } from '@lucide/vue'
import { onMounted, ref } from 'vue'

import { ApiError } from '@/api/client'
import AppShell from '@/components/AppShell.vue'
import { useFriendsStore } from '@/stores/friends'

const friendsStore = useFriendsStore()
const keyword = ref('')
const error = ref('')
const notice = ref('')

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
    <section class="page-stack">
      <header class="panel-header">
        <div>
          <h1>好友管理</h1>
          <p>搜索用户、处理申请、查看好友在线状态。</p>
        </div>
        <button class="icon-button" type="button" title="刷新" @click="friendsStore.refreshAll()">
          <RefreshCw :size="18" />
        </button>
      </header>

      <form class="toolbar" @submit.prevent="searchUsers">
        <Search :size="18" />
        <input v-model.trim="keyword" maxlength="50" placeholder="按用户名或昵称搜索" />
        <button class="primary-button compact" type="submit">
          <Search :size="18" />
          <span>搜索</span>
        </button>
      </form>

      <p v-if="error" class="form-error">{{ error }}</p>
      <p v-if="notice" class="form-notice">{{ notice }}</p>

      <section class="content-grid">
        <div class="surface">
          <h2>搜索结果</h2>
          <div v-if="friendsStore.searchResults.length === 0" class="empty-text">暂无搜索结果。</div>
          <div v-for="user in friendsStore.searchResults" :key="user.id" class="list-row">
            <div class="avatar">{{ (user.nickname || user.username).slice(0, 1) }}</div>
            <div>
              <strong>{{ user.nickname || user.username }}</strong>
              <span>@{{ user.username }}</span>
            </div>
            <button
              class="icon-button"
              type="button"
              title="发送好友申请"
              @click="run(() => friendsStore.sendRequest(user.id), '好友申请已发送')"
            >
              <UserPlus :size="18" />
            </button>
          </div>
        </div>

        <div class="surface">
          <h2>收到的申请</h2>
          <div v-if="friendsStore.requests.length === 0" class="empty-text">暂无待处理申请。</div>
          <div v-for="request in friendsStore.requests" :key="request.requestId" class="list-row">
            <div class="avatar">{{ (request.requester.nickname || request.requester.username).slice(0, 1) }}</div>
            <div>
              <strong>{{ request.requester.nickname || request.requester.username }}</strong>
              <span>@{{ request.requester.username }}</span>
            </div>
            <div class="row-actions">
              <button
                class="icon-button success"
                type="button"
                title="接受"
                @click="run(() => friendsStore.acceptRequest(request.requestId), '已接受好友申请')"
              >
                <Check :size="18" />
              </button>
              <button
                class="icon-button danger"
                type="button"
                title="拒绝"
                @click="run(() => friendsStore.rejectRequest(request.requestId), '已拒绝好友申请')"
              >
                <X :size="18" />
              </button>
            </div>
          </div>
        </div>

        <div class="surface wide">
          <h2>好友列表</h2>
          <div v-if="friendsStore.friends.length === 0" class="empty-text">暂无好友。</div>
          <div v-for="friend in friendsStore.friends" :key="friend.user.id" class="list-row">
            <span class="presence-dot" :class="{ online: friend.online }"></span>
            <div class="avatar">{{ (friend.user.nickname || friend.user.username).slice(0, 1) }}</div>
            <div>
              <strong>{{ friend.user.nickname || friend.user.username }}</strong>
              <span>
                @{{ friend.user.username }} · {{ friend.online ? '在线' : '离线' }} · 未读
                {{ friend.unreadCount }}
              </span>
            </div>
            <button
              class="icon-button danger"
              type="button"
              title="删除好友"
              @click="run(() => friendsStore.deleteFriend(friend.user.id), '好友已删除')"
            >
              <Trash2 :size="18" />
            </button>
          </div>
        </div>
      </section>
    </section>
  </AppShell>
</template>
