import { api } from '@/lib/api'
import type { AppConfig } from '@/types/appConfig'

/**
 * 应用配置服务
 */
export const appConfigService = {
  /**
   * 获取应用配置
   * @param appId 应用ID
   * @returns 应用配置
   */
  async getAppConfig(appId: string): Promise<AppConfig> {
    try {
      const response = await api.get<AppConfig>(`/app-configs/app/${appId}`)
      return response.data
    } catch (error) {
      throw new Error('获取应用配置失败')
    }
  },

  /**
   * 创建应用配置
   * @param appConfig 应用配置
   * @returns 创建的应用配置
   */
  async createAppConfig(appConfig: AppConfig): Promise<AppConfig> {
    try {
      const response = await api.post<AppConfig>('/app-configs', appConfig)
      return response.data
    } catch (error) {
      throw new Error('创建应用配置失败')
    }
  },

  /**
   * 更新应用配置
   * @param appId 应用ID
   * @param appConfig 应用配置
   * @returns 更新后的应用配置
   */
  async updateAppConfig(appId: string, appConfig: Partial<AppConfig>): Promise<AppConfig> {
    try {
      const response = await api.put<AppConfig>(`/app-configs/${appId}`, appConfig)
      return response.data
    } catch (error) {
      throw new Error('更新应用配置失败')
    }
  },
}
