import { api } from '@/lib/api'
import type { User } from '@/types/account'
import { AxiosRequestConfig } from 'axios'

export const updateCurrentUser = async (data: User) => {
  try {
    const response = await api.put('/user', data)
    return response.data
  } catch (error) {
    console.error('更新个人资料失败', error)
    throw error
  }
}

export const getUserById = async (userId: string) => {
  try {
    const response = await api.get(`/user/${userId}`)
    return response.data
  } catch (error) {
    console.error('获取用户信息失败', error)
    throw error
  }
}
