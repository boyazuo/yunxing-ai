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

export interface ChangePasswordParams {
  currentPassword: string
  confirmPassword: string
  newPassword: string
}

export const authService = {
  async login(params: LoginParams): Promise<LoginResponse> {
    try {
      const response = await api.post<LoginResponse>('/auth/login', params)
      return response.data as LoginResponse
    } catch (error) {
      throw new Error('登录失败，请稍后重试')
    }
  },

  async register(params: RegisterParams) {
    try {
      await api.post<null>('/auth/register', params)
    } catch (error) {
      throw new Error('注册失败，请稍后重试')
    }
  },

  async changePassword(params: ChangePasswordParams) {
    try {
      return await api.put<null>('/auth/password', params) 
    } catch (error) {
      throw new Error('修改密码失败，请稍后重试')
    }
  }
}
