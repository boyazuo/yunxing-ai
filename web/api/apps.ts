import { api } from '@/lib/api'
import type { App, AppStatus, AppType } from '@/types/app'

/**
 * 创建/更新应用参数
 */
export interface AppRequest {
  appName: string
  intro?: string
  logo?: string
  type: AppType
  status?: AppStatus
  tenantId: string
}

/**
 * 应用服务
 */
export const appService = {
  /**
   * 获取租户下的所有应用
   * @param tenantId 租户ID
   * @returns 应用列表
   */
  async getApps(tenantId: string): Promise<App[]> {
    try {
      const response = await api.get<App[]>(`/apps?tenantId=${tenantId}`)
      return response.data
    } catch (error) {
      throw new Error('获取应用列表失败')
    }
  },

  /**
   * 根据ID获取应用详情
   * @param appId 应用ID
   * @returns 应用详情
   */
  async getAppById(appId: number): Promise<App> {
    try {
      const response = await api.get<App>(`/apps/${appId}`)
      return response.data
    } catch (error) {
      throw new Error('获取应用详情失败')
    }
  },

  /**
   * 创建应用
   * @param appRequest 应用参数
   * @returns 创建的应用
   */
  async createApp(appRequest: AppRequest): Promise<App> {
    try {
      const response = await api.post<App>('/apps', appRequest)
      return response.data
    } catch (error) {
      throw new Error('创建应用失败')
    }
  },

  /**
   * 更新应用
   * @param appId 应用ID
   * @param appRequest 应用参数
   * @returns 更新后的应用
   */
  async updateApp(appId: number, appRequest: Partial<AppRequest>): Promise<App> {
    try {
      const response = await api.put<App>(`/apps/${appId}`, appRequest)
      return response.data
    } catch (error) {
      throw new Error('更新应用失败')
    }
  },

  /**
   * 删除应用
   * @param appId 应用ID
   */
  async deleteApp(appId: number): Promise<void> {
    try {
      await api.delete<void>(`/apps/${appId}`)
    } catch (error) {
      throw new Error('删除应用失败')
    }
  },
}
