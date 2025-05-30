import { api } from '@/lib/api'
import type { InvitationRequest } from '@/types/invitation'

export const invitationService = {
  apiRoot: '/invitations',
  async createInvitations(data: InvitationRequest) {
    try {
      const response = await api.post(`${this.apiRoot}`, data)
      return response
    } catch (error) {
      console.error('发送邀请失败', error)
      throw error
    }
  },

  async getInvitations(token: string) {
    try {
      const response = await api.get(`${this.apiRoot}/${token}`)
      return response
    } catch (error) {
      console.error('获取邀请失败', error)
      throw error
    }
  },
}
