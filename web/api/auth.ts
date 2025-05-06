import { api } from '@/lib/api'

export interface LoginParams {
  email: string
  password: string
}

export interface LoginResponse {
  userId: string
  username: string
  email: string
  avatar: string
  token: string
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

  async logout(): Promise<void> {
    try {
      await api.post<void>('/auth/logout')
    } catch (error: unknown) {
      // 即使登出失败也重定向到登录页
      if (typeof window !== 'undefined') {
        window.location.href = '/login'
      }
    }
  },
}
