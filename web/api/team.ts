import { api } from '@/lib/api'
import type { Tenant, TenantPlan, TenantUserRole } from '@/types/account'

export interface TenantRequest {
  tenantId?: string
  tenantName: string
  plan?: TenantPlan
}

export interface UserInTenant {
  userId: string
  username?: string
  email: string
  avatar: string
  role: TenantUserRole | string
  isActive: boolean
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

  async getUserInTeam(tenantId: string) {
    try {
      const response = await api.get<UserInTenant[]>(`${this.apiRoot}/${tenantId}/users`)
      return response.data
    } catch (error) {
      console.error('获取团队成员失败', error)
      throw error
    }
  },

  async updupdateTenantUserRole(role: TenantUserRole, tenantId: string, userId: string) {
    try {
      const response = await api.put<null>(`${this.apiRoot}/${tenantId}/users/${userId}`, { role })
      return response
    } catch (error) {
      console.error('更新团队成员角色失败', error)
      throw error
    }
  },

  async deleteTenantUser(tenantId: string, userId: string) {
    try {
      const response = await api.delete<null>(`${this.apiRoot}/${tenantId}/users/${userId}`)
      return response
    } catch (error) {
      console.error('删除团队成员失败', error)
      throw error
    }
  },
}
