<script setup lang="ts">
import {
  BarChart3,
  ChevronDown,
  LogOut,
  Menu,
  MessageCircle,
  Moon,
  Sun,
  User,
  Users,
} from '@lucide/vue'
import { computed, onMounted, ref, type Component } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from '@/components/ui/breadcrumb'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Separator } from '@/components/ui/separator'
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger } from '@/components/ui/sheet'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useFriendsStore } from '@/stores/friends'

interface NavItem {
  to: string
  label: string
  description: string
  icon: Component
  badge?: number
}

const authStore = useAuthStore()
const chatStore = useChatStore()
const friendsStore = useFriendsStore()
const router = useRouter()
const route = useRoute()
const theme = ref<'light' | 'dark'>('light')
const mobileOpen = ref(false)
const THEME_KEY = 'javachat.theme'

const unreadTotal = computed(() =>
  friendsStore.friends.reduce((total, friend) => total + friend.unreadCount, 0),
)

const navItems = computed<NavItem[]>(() => [
  {
    to: '/chat',
    label: '聊天',
    description: '公共频道与私聊',
    icon: MessageCircle,
    badge: unreadTotal.value,
  },
  {
    to: '/friends',
    label: '好友',
    description: '好友申请与状态',
    icon: Users,
    badge: friendsStore.requests.length,
  },
  {
    to: '/dashboard',
    label: '概览',
    description: '系统运行数据',
    icon: BarChart3,
  },
  {
    to: '/profile',
    label: '资料',
    description: '账号与头像',
    icon: User,
  },
])

const pageTitle = computed(() => navItems.value.find((item) => item.to === route.path)?.label || '工作台')

const userInitial = computed(() => authStore.user?.nickname?.slice(0, 1) || authStore.user?.username?.slice(0, 1) || 'U')

async function logout() {
  chatStore.reset()
  await authStore.logout()
  await router.replace({ name: 'login' })
}

function applyTheme(nextTheme: 'light' | 'dark') {
  theme.value = nextTheme
  document.documentElement.classList.toggle('dark', nextTheme === 'dark')
  document.documentElement.style.colorScheme = nextTheme
  localStorage.setItem(THEME_KEY, nextTheme)
}

function toggleTheme() {
  applyTheme(theme.value === 'dark' ? 'light' : 'dark')
}

function closeMobileNav() {
  mobileOpen.value = false
}

function navigateAndClose(event: MouseEvent, navigate: (event?: MouseEvent) => void) {
  navigate(event)
  closeMobileNav()
}

onMounted(() => {
  const savedTheme = localStorage.getItem(THEME_KEY)
  applyTheme(savedTheme === 'dark' ? 'dark' : 'light')
})
</script>

<template>
  <TooltipProvider>
    <div class="min-h-screen bg-muted/40 text-foreground lg:grid lg:grid-cols-[18rem_minmax(0,1fr)]">
      <aside class="hidden min-h-screen border-r bg-sidebar text-sidebar-foreground lg:flex lg:flex-col">
        <div class="flex h-16 items-center gap-3 border-b px-5">
          <Avatar class="size-9 rounded-lg">
            <AvatarFallback class="rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
              JC
            </AvatarFallback>
          </Avatar>
          <div class="min-w-0">
            <p class="truncate text-sm font-semibold">JavaChatSer</p>
            <p class="truncate text-xs text-muted-foreground">Vue shadcn workspace</p>
          </div>
        </div>

        <nav class="grid gap-1 p-3">
          <RouterLink v-for="item in navItems" :key="item.to" v-slot="{ href, navigate, isActive }" :to="item.to" custom>
            <Button
              as-child
              :variant="isActive ? 'secondary' : 'ghost'"
              class="h-auto w-full justify-start gap-3 px-3 py-3"
            >
              <a :href="href" @click="navigate">
                <component :is="item.icon" class="size-4" />
                <span class="grid min-w-0 flex-1 text-left">
                  <span class="truncate text-sm font-medium">{{ item.label }}</span>
                  <span class="truncate text-xs text-muted-foreground">{{ item.description }}</span>
                </span>
                <Badge v-if="item.badge && item.badge > 0" variant="secondary" class="ml-auto">
                  {{ item.badge }}
                </Badge>
              </a>
            </Button>
          </RouterLink>
        </nav>

        <div class="mt-auto p-3">
          <div class="rounded-lg border bg-card p-3 text-card-foreground shadow-xs">
            <div class="flex items-center justify-between gap-3">
              <div class="space-y-1">
                <p class="text-xs text-muted-foreground">WebSocket</p>
                <p class="text-sm font-medium">{{ chatStore.socketStatus === 'open' ? '已连接' : '重连中' }}</p>
              </div>
              <Badge :variant="chatStore.socketStatus === 'open' ? 'default' : 'secondary'">
                {{ chatStore.socketStatus === 'open' ? 'Online' : 'Sync' }}
              </Badge>
            </div>
          </div>
        </div>
      </aside>

      <div class="min-w-0">
        <header class="sticky top-0 z-40 flex h-16 items-center gap-3 border-b bg-background/95 px-4 backdrop-blur supports-[backdrop-filter]:bg-background/70 sm:px-6 lg:px-8">
          <Sheet v-model:open="mobileOpen">
            <SheetTrigger as-child>
              <Button variant="outline" size="icon" class="lg:hidden" type="button">
                <Menu class="size-4" />
                <span class="sr-only">打开导航</span>
              </Button>
            </SheetTrigger>
            <SheetContent side="left" class="w-[19rem] p-0">
              <SheetHeader class="border-b px-5 py-4 text-left">
                <SheetTitle class="flex items-center gap-3">
                  <Avatar class="size-9 rounded-lg">
                    <AvatarFallback class="rounded-lg bg-primary text-primary-foreground">JC</AvatarFallback>
                  </Avatar>
                  JavaChatSer
                </SheetTitle>
                <SheetDescription>实时聊天工作台</SheetDescription>
              </SheetHeader>
              <nav class="grid gap-1 p-3">
                <RouterLink
                  v-for="item in navItems"
                  :key="item.to"
                  v-slot="{ href, navigate, isActive }"
                  :to="item.to"
                  custom
                >
                  <Button
                    as-child
                    :variant="isActive ? 'secondary' : 'ghost'"
                    class="h-auto w-full justify-start gap-3 px-3 py-3"
                  >
                    <a :href="href" @click="navigateAndClose($event, navigate)">
                      <component :is="item.icon" class="size-4" />
                      <span class="grid min-w-0 flex-1 text-left">
                        <span class="truncate text-sm font-medium">{{ item.label }}</span>
                        <span class="truncate text-xs text-muted-foreground">{{ item.description }}</span>
                      </span>
                      <Badge v-if="item.badge && item.badge > 0" variant="secondary" class="ml-auto">
                        {{ item.badge }}
                      </Badge>
                    </a>
                  </Button>
                </RouterLink>
              </nav>
            </SheetContent>
          </Sheet>

          <Breadcrumb class="hidden sm:block">
            <BreadcrumbList>
              <BreadcrumbItem>
                <BreadcrumbLink as-child>
                  <RouterLink to="/dashboard">工作台</RouterLink>
                </BreadcrumbLink>
              </BreadcrumbItem>
              <BreadcrumbSeparator />
              <BreadcrumbItem>
                <BreadcrumbPage>{{ pageTitle }}</BreadcrumbPage>
              </BreadcrumbItem>
            </BreadcrumbList>
          </Breadcrumb>

          <div class="ml-auto flex items-center gap-2">
            <Tooltip>
              <TooltipTrigger as-child>
                <Button
                  variant="outline"
                  size="icon"
                  type="button"
                  :title="theme === 'dark' ? '切换浅色模式' : '切换深色模式'"
                  @click="toggleTheme"
                >
                  <Sun v-if="theme === 'dark'" class="size-4" />
                  <Moon v-else class="size-4" />
                </Button>
              </TooltipTrigger>
              <TooltipContent>
                <p>{{ theme === 'dark' ? '浅色模式' : '深色模式' }}</p>
              </TooltipContent>
            </Tooltip>

            <DropdownMenu>
              <DropdownMenuTrigger as-child>
                <Button variant="outline" class="h-9 gap-2 px-2.5" type="button">
                  <Avatar class="size-6">
                    <AvatarFallback class="text-xs">{{ userInitial }}</AvatarFallback>
                  </Avatar>
                  <span class="hidden max-w-28 truncate text-sm font-medium sm:inline">
                    {{ authStore.user?.nickname || authStore.user?.username }}
                  </span>
                  <ChevronDown class="size-3.5 text-muted-foreground" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" class="w-56">
                <DropdownMenuLabel>
                  <span class="block truncate">{{ authStore.user?.nickname || authStore.user?.username }}</span>
                  <span class="block truncate text-xs font-normal text-muted-foreground">@{{ authStore.user?.username }}</span>
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem @click="router.push('/profile')">
                  <User class="size-4" />
                  个人资料
                </DropdownMenuItem>
                <DropdownMenuItem @click="toggleTheme">
                  <Sun v-if="theme === 'dark'" class="size-4" />
                  <Moon v-else class="size-4" />
                  {{ theme === 'dark' ? '浅色模式' : '深色模式' }}
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem variant="destructive" @click="logout">
                  <LogOut class="size-4" />
                  退出登录
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </header>

        <main class="min-w-0 p-4 sm:p-6 lg:p-8">
          <slot />
        </main>
      </div>
    </div>
  </TooltipProvider>
</template>
