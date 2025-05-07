import { api } from '@/lib/api'
import type { Tenant, User } from '@/types/account'

export interface LoginParams {
  email: string
  password: string
}

export interface RegisterParams {
  username: string
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  user: User
  tenant: Tenant
}

export const authService = {
  async login(params: LoginParams): Promise<LoginResponse> {
    try {
      const response = await api.post<LoginResponse>('/auth/login', params)
      return response.data as LoginResponse
    } catch (error: unknown) {
      throw new Error('登录失败，请稍后重试')
    }
  },

  async register(params: RegisterParams) {
    try {
      await api.post<null>('/auth/register', params)
    } catch (error: unknown) {
      throw new Error('注册失败，请稍后重试')
    }
  },
}
