import { spawn } from 'node:child_process'
import { createRequire } from 'node:module'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.resolve(__dirname, '..')
const frontend = path.join(root, 'frontend')
const screenshots = path.join(root, 'docs', 'screenshots')
const apiOrigin = 'http://localhost:8080'
const appOrigin = 'http://127.0.0.1:5173'

async function loadPlaywright() {
  try {
    return await import('playwright')
  } catch {
    const nodeModules = process.env.PLAYWRIGHT_NODE_MODULES
    if (!nodeModules) {
      throw new Error('Set PLAYWRIGHT_NODE_MODULES to a node_modules folder that contains playwright.')
    }
    const req = createRequire(pathToFileURL(path.join(nodeModules, 'package.json')))
    return req('playwright')
  }
}

function startDevServer() {
  const command = process.platform === 'win32' ? 'npm.cmd' : 'npm'
  const child = spawn(command, ['run', 'dev', '--', '--host', '127.0.0.1', '--port', '5173', '--strictPort'], {
    cwd: frontend,
    env: {
      ...process.env,
      VITE_API_BASE_URL: apiOrigin,
      VITE_WS_BASE_URL: 'ws://localhost:8080',
    },
    stdio: ['ignore', 'pipe', 'pipe'],
    shell: process.platform === 'win32',
    windowsHide: true,
  })
  child.stdout.on('data', (chunk) => process.stdout.write(chunk))
  child.stderr.on('data', (chunk) => process.stderr.write(chunk))
  return child
}

async function appAvailable() {
  try {
    const response = await fetch(`${appOrigin}/login`)
    return response.ok
  } catch {
    return false
  }
}

async function waitForApp(child) {
  const startedAt = Date.now()
  while (Date.now() - startedAt < 30000) {
    if (child.exitCode !== null) {
      throw new Error(`Vite dev server exited with code ${child.exitCode}`)
    }
    try {
      const response = await fetch(`${appOrigin}/login`)
      if (response.ok) {
        return
      }
    } catch {
      // Retry until Vite is listening.
    }
    await new Promise((resolve) => setTimeout(resolve, 500))
  }
  throw new Error('Timed out waiting for Vite dev server.')
}

function stopDevServer(child) {
  if (!child || !child.pid) {
    return
  }
  if (process.platform === 'win32') {
    spawn('taskkill.exe', ['/pid', String(child.pid), '/t', '/f'], {
      stdio: 'ignore',
      windowsHide: true,
    })
    return
  }
  child.kill('SIGTERM')
}

const user = {
  id: 1,
  username: 'admin',
  nickname: 'Admin',
  avatarUrl: '/default-avatar.png',
  bio: '系统管理员',
  role: 'ADMIN',
}

const alice = {
  id: 2,
  username: 'alice',
  nickname: 'Alice',
  avatarUrl: '/default-avatar.png',
  bio: '前端测试账号',
}

const bob = {
  id: 3,
  username: 'bob',
  nickname: 'Bob',
  avatarUrl: '/default-avatar.png',
  bio: '后端测试账号',
}

const chris = {
  id: 4,
  username: 'testUser3',
  nickname: 'Test User',
  avatarUrl: '/default-avatar.png',
  bio: '课程演示账号',
}

function pageResponse(content) {
  return {
    content,
    page: 0,
    size: 50,
    totalElements: content.length,
    totalPages: 1,
  }
}

function api(data) {
  return JSON.stringify({ code: 0, message: 'success', data })
}

async function mockApi(route) {
  const request = route.request()
  const url = new URL(request.url())
  const method = request.method()
  const pathname = url.pathname

  if (pathname === '/api/auth/login' || pathname === '/api/auth/register') {
    return route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: api({ token: 'demo-token', user }),
    })
  }
  if (pathname.startsWith('/uploads/images/')) {
    return route.fulfill({
      status: 200,
      contentType: 'image/svg+xml',
      body: `
        <svg xmlns="http://www.w3.org/2000/svg" width="520" height="320" viewBox="0 0 520 320">
          <rect width="520" height="320" rx="20" fill="#dbeafe"/>
          <rect x="28" y="28" width="464" height="264" rx="16" fill="#ffffff" opacity="0.72"/>
          <text x="260" y="145" text-anchor="middle" font-size="34" font-family="Microsoft YaHei, Arial" fill="#1d4ed8">聊天图片消息</text>
          <text x="260" y="195" text-anchor="middle" font-size="22" font-family="Microsoft YaHei, Arial" fill="#475569">/uploads/images/demo.png</text>
        </svg>`,
    })
  }
  if (pathname === '/api/auth/me') {
    return route.fulfill({ status: 200, contentType: 'application/json', body: api(user) })
  }
  if (pathname === '/api/auth/logout') {
    return route.fulfill({ status: 200, contentType: 'application/json', body: api(null) })
  }
  if (pathname === '/api/friends' && method === 'GET') {
    return route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: api([
        { user: alice, online: true, unreadCount: 2 },
        { user: bob, online: false, unreadCount: 0 },
      ]),
    })
  }
  if (pathname === '/api/friends/requests' && method === 'GET') {
    return route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: api([{ requestId: 101, requester: chris, status: 'PENDING', createdAt: '2026-05-21 09:30:00' }]),
    })
  }
  if (pathname === '/api/friends/requests' && method === 'POST') {
    return route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: api({ requestId: 102, requester: user, status: 'PENDING', createdAt: '2026-05-21 10:00:00' }),
    })
  }
  if (pathname.startsWith('/api/friends/requests/') || pathname.startsWith('/api/friends/')) {
    return route.fulfill({ status: 200, contentType: 'application/json', body: api(null) })
  }
  if (pathname === '/api/users/search') {
    return route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: api(pageResponse([chris, bob])),
    })
  }
  if (pathname === '/api/chats/public/messages') {
    return route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: api(pageResponse([
        {
          id: 201,
          senderId: 1,
          content: '大家好，这是公共聊天室演示消息。',
          messageType: 'TEXT',
          recalled: false,
          recalledAt: null,
          createdAt: '2026-05-21 09:40:00',
        },
        {
          id: 202,
          senderId: 2,
          content: '/uploads/images/demo-chat-image.png',
          messageType: 'IMAGE',
          recalled: false,
          recalledAt: null,
          createdAt: '2026-05-21 09:41:00',
        },
        {
          id: 203,
          senderId: 3,
          content: '',
          messageType: 'TEXT',
          recalled: true,
          recalledAt: '2026-05-21 09:42:00',
          createdAt: '2026-05-21 09:42:00',
        },
      ])),
    })
  }
  if (pathname === '/api/chats/private/2/messages') {
    return route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: api(pageResponse([
        {
          id: 301,
          senderId: 2,
          receiverId: 1,
          content: '老师演示时可以先发送好友申请，再进入私聊。',
          messageType: 'TEXT',
          recalled: false,
          readAt: '2026-05-21 09:50:00',
          recalledAt: null,
          createdAt: '2026-05-21 09:48:00',
        },
        {
          id: 302,
          senderId: 1,
          receiverId: 2,
          content: '这条消息会通过 WebSocket 实时推送，同时保存到 MySQL。',
          messageType: 'TEXT',
          recalled: false,
          readAt: null,
          recalledAt: null,
          createdAt: '2026-05-21 09:49:00',
        },
        {
          id: 303,
          senderId: 1,
          receiverId: 2,
          content: '/uploads/images/demo-private-image.png',
          messageType: 'IMAGE',
          recalled: false,
          readAt: null,
          recalledAt: null,
          createdAt: '2026-05-21 09:50:00',
        },
      ])),
    })
  }
  if (pathname === '/api/chats/private/2/read') {
    return route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: api({ readCount: 2, unreadCount: 0 }),
    })
  }
  if (pathname.startsWith('/api/chats/')) {
    return route.fulfill({ status: 200, contentType: 'application/json', body: api(null) })
  }
  if (pathname === '/api/stats/overview') {
    return route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: api({
        scope: 'GLOBAL',
        registeredUserCount: 4,
        onlineUserCount: 2,
        todayPrivateMessageCount: 18,
        todayPublicMessageCount: 9,
        totalPrivateMessageCount: 126,
        totalPublicMessageCount: 58,
        pendingFriendRequestCount: 1,
        acceptedFriendCount: 2,
      }),
    })
  }
  return route.fulfill({ status: 200, contentType: 'application/json', body: api(null) })
}

async function installMocks(context) {
  await context.route(`${apiOrigin}/**`, mockApi)
  await context.route('http://127.0.0.1:8080/**', mockApi)
  await context.addInitScript(() => {
    class FakeWebSocket extends EventTarget {
      static CONNECTING = 0
      static OPEN = 1
      static CLOSING = 2
      static CLOSED = 3
      constructor(url) {
        super()
        this.url = url
        this.readyState = FakeWebSocket.CONNECTING
        setTimeout(() => {
          this.readyState = FakeWebSocket.OPEN
          this.onopen?.(new Event('open'))
          this.dispatchEvent(new Event('open'))
          this.emit({ type: 'ONLINE_USERS', data: [1, 2] })
        }, 30)
      }
      send(raw) {
        let message
        try {
          message = JSON.parse(raw)
        } catch {
          return
        }
        if (message.type === 'PING') {
          this.emit({ type: 'PONG', data: null })
        }
      }
      close() {
        this.readyState = FakeWebSocket.CLOSED
        this.onclose?.(new CloseEvent('close'))
        this.dispatchEvent(new CloseEvent('close'))
      }
      emit(payload) {
        const event = new MessageEvent('message', { data: JSON.stringify(payload) })
        this.onmessage?.(event)
        this.dispatchEvent(event)
      }
    }
    window.WebSocket = FakeWebSocket
  })
}

async function screenshot(page, route, fileName) {
  await page.goto(`${appOrigin}${route}`, { waitUntil: 'networkidle' })
  await page.screenshot({ path: path.join(screenshots, fileName), fullPage: true })
}

async function main() {
  await fs.mkdir(screenshots, { recursive: true })
  const vite = (await appAvailable()) ? null : startDevServer()
  try {
    if (vite) {
      await waitForApp(vite)
    }
    const { chromium } = await loadPlaywright()
    let browser
    try {
      browser = await chromium.launch({ headless: true })
    } catch {
      browser = await chromium.launch({
        headless: true,
        executablePath: 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
      })
    }

    const guest = await browser.newContext({ viewport: { width: 1440, height: 1000 }, deviceScaleFactor: 1 })
    await installMocks(guest)
    const guestPage = await guest.newPage()
    await guestPage.goto(`${appOrigin}/login`, { waitUntil: 'networkidle' })
    await guestPage.getByLabel('用户名').fill('admin')
    await guestPage.getByLabel('密码').fill('123456')
    await guestPage.screenshot({ path: path.join(screenshots, '01-login.png'), fullPage: true })
    await guestPage.goto(`${appOrigin}/register`, { waitUntil: 'networkidle' })
    await guestPage.getByLabel('用户名').fill('student_user')
    await guestPage.getByLabel('昵称').fill('Student')
    await guestPage.getByLabel('密码').fill('123456')
    await guestPage.getByLabel('头像地址').fill('/default-avatar.png')
    await guestPage.screenshot({ path: path.join(screenshots, '02-register.png'), fullPage: true })
    await guest.close()

    const auth = await browser.newContext({ viewport: { width: 1440, height: 1000 }, deviceScaleFactor: 1 })
    await installMocks(auth)
    await auth.addInitScript(() => localStorage.setItem('javachat.token', 'demo-token'))
    const page = await auth.newPage()

    await page.goto(`${appOrigin}/friends`, { waitUntil: 'networkidle' })
    await page.getByPlaceholder('按用户名或昵称搜索').fill('test')
    await page.getByRole('button', { name: '搜索' }).click()
    await page.waitForLoadState('networkidle')
    await page.screenshot({ path: path.join(screenshots, '03-friends.png'), fullPage: true })

    await screenshot(page, '/chat', '04-public-chat.png')
    await page.locator('aside button').filter({ hasText: 'Alice' }).click()
    await page.waitForLoadState('networkidle')
    await page.screenshot({ path: path.join(screenshots, '05-private-chat.png'), fullPage: true })
    await screenshot(page, '/profile', '06-profile.png')
    await screenshot(page, '/dashboard', '07-dashboard.png')

    await auth.close()
    await browser.close()
  } finally {
    stopDevServer(vite)
  }
}

main().catch((error) => {
  console.error(error)
  process.exit(1)
})
