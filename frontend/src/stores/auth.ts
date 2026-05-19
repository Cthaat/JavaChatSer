import { defineStore } from 'pinia'

import { authApi } from '@/api'
import { tokenStorage } from '@/api/client'
import type { AuthResponse, UserProfile } from '@/types'

interface AuthState {
  token: string | null
  user: UserProfile | null
  bootstrapped: boolean
  loading: boolean
  error: string
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: tokenStorage.get(),
    user: null,
    bootstrapped: false,
    loading: false,
    error: '',
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token && state.user),
  },
  actions: {
    applySession(session: AuthResponse) {
      this.token = session.token
      this.user = session.user
      this.error = ''
      tokenStorage.set(session.token)
    },
    async login(username: string, password: string) {
      this.loading = true
      try {
        this.applySession(await authApi.login({ username, password }))
      } finally {
        this.loading = false
      }
    },
    async register(payload: { username: string; password: string; nickname?: string; avatarUrl?: string }) {
      this.loading = true
      try {
        this.applySession(await authApi.register(payload))
      } finally {
        this.loading = false
      }
    },
    async restore() {
      if (!this.token) {
        this.bootstrapped = true
        return
      }
      try {
        this.user = await authApi.me()
      } catch {
        this.clearSession()
      } finally {
        this.bootstrapped = true
      }
    },
    async logout() {
      try {
        if (this.token) {
          await authApi.logout()
        }
      } finally {
        this.clearSession()
      }
    },
    clearSession() {
      this.token = null
      this.user = null
      this.error = ''
      tokenStorage.clear()
    },
  },
})
