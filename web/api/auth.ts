import { api } from '@/lib/api'

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

  async register(params: RegisterParams) {
    try {
      await api.post<null>('/auth/register', params)
    } catch (error: unknown) {
      throw new Error('注册失败，请稍后重试')
    }
  },
}
