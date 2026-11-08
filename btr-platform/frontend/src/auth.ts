import { reactive } from 'vue'
import { api } from './api/http'

export type User = {
  id: number
  account: string
  nickname: string
  role: string
  platformRole?: string
}

export type Tenant = {
  id: number
  code: string
  name: string
  type: string
  status: string
  contactName?: string
  contactEmail?: string
  createdAt?: string
}

type LoginResponse = {
  accessToken: string
  user: User
  tenant: Tenant
}

type StoredAuth = {
  token: string
  user: User
  tenant: Tenant
}

const STORAGE_KEY = 'btr.auth'

function loadStored(): StoredAuth | null {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as StoredAuth
  } catch {
    localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

const stored = loadStored()

export const auth = reactive({
  token: stored?.token ?? '',
  user: stored?.user ?? null,
  tenant: stored?.tenant ?? null,

  async login(account: string, password: string, sliderTicket: string) {
    const result = await api.post<LoginResponse>('/api/v1/auth/login', {
      account,
      password,
      sliderTicket
    })
    this.apply(result)
  },

  async register(email: string, password: string, nickname: string, code: string) {
    const result = await api.post<LoginResponse>('/api/v1/auth/register', {
      email,
      password,
      nickname,
      code
    })
    this.apply(result)
  },

  async sendRegisterCode(email: string, sliderTicket: string) {
    await api.post('/api/v1/auth/send-register-code', { tenantCode: 'demo', email, sliderTicket })
  },

  async forgotPassword(email: string, sliderTicket: string) {
    await api.post('/api/v1/auth/forgot-password', { tenantCode: 'demo', email, sliderTicket })
  },

  async resetPassword(email: string, code: string, newPassword: string) {
    await api.post('/api/v1/auth/reset-password', { email, code, newPassword })
  },

  apply(result: LoginResponse) {
    this.token = result.accessToken
    this.user = result.user
    this.tenant = result.tenant
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      token: result.accessToken,
      user: result.user,
      tenant: result.tenant
    }))
  },

  logout() {
    this.token = ''
    this.user = null
    this.tenant = null
    localStorage.removeItem(STORAGE_KEY)
  }
})
