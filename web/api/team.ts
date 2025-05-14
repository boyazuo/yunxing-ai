import { api } from '@/lib/api'
import { Tenant, TenantPlan } from '@/types/account'

export interface TenantRequest {
    tenantId?: number
    tenantName: string
    plan?: TenantPlan
}

export const teamService = {
  apiRoot: '/tenants',
  /**
   * 获取当前登录用户 的团队列表
   * @returns 团队列表
   */
  async getTeams(): Promise<Tenant[]> {
    try {
      const response = await api.get<Tenant[]>(this.apiRoot)
      return response.data
    } catch (error) {
      console.error('获取团队列表失败', error)
      throw error
    }
  },

  async updateTeam(params: TenantRequest) {
    try {
      const response = await api.put<null>(this.apiRoot, params)
      return response
    } catch (error) {
      console.error('更新团队失败', error)
      throw error
    }
  },
}
