import type { AxiosInstance, AxiosRequestConfig } from 'axios'
import axios from 'axios'
import { getSession } from 'next-auth/react'
import { toast } from 'sonner'

export interface ApiResponse<T> {
  code: number
  data: T
  msg: string
}

// 获取API基础URL
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/v1/api'

class ApiClient {
  private client: AxiosInstance

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
      withCredentials: true,
      timeout: 30000, // 30秒超时
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    this.client.interceptors.request.use(async (config) => {
      try {
        const session = await getSession()
        if (session?.accessToken) {
          config.headers.Authorization = `${session.accessToken}`
        }

        return config
      } catch (error) {
        return config
      }
    })

    this.client.interceptors.response.use(
      (response) => {
        if (response.data.code === 401) {
          if (typeof window !== 'undefined') {
            // 提示用户重新登录
            toast.error('您尚未登录或登录已过期，请重新登录')
            window.location.href = '/login'
          }
        }
        return response
      },
      (error) => {
        return Promise.reject(error)
      },
    )
  }

  async get<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.get<ApiResponse<T>>(url, config)
    return response.data
  }

  async post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.post<ApiResponse<T>>(url, data, config)
    return response.data
  }

  async put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.put<ApiResponse<T>>(url, data, config)
    return response.data
  }

  async patch<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.patch<ApiResponse<T>>(url, data, config)
    return response.data
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.delete<ApiResponse<T>>(url, config)
    return response.data
  }
}

export const api = new ApiClient()
