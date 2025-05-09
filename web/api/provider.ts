import { api } from '@/lib/api'

// 获取提供商列表
export const getProviders = async (tenantId: string) => {
  try {
    const response = await api.get(`/providers?tenantId=${tenantId}`)
    return response.data || []
  } catch (error) {
    console.error('获取提供商列表失败', error)
    throw error
  }
}

// 获取提供商详情
export const getProviderById = async (providerId: string) => {
  try {
    const response = await api.get(`/providers/${providerId}`)
    return response.data
  } catch (error) {
    console.error('获取提供商详情失败', error)
    throw error
  }
}

// 添加提供商
export const createProvider = async (data: {
  tenantId: string
  providerName: string
  logo?: string
  apiKey: string
  endpoint?: string
}) => {
  try {
    const response = await api.post('/providers', data)
    return response.data
  } catch (error) {
    console.error('添加提供商失败', error)
    throw error
  }
}

// 更新提供商
export const updateProvider = async (
  providerId: string,
  data: {
    providerName?: string
    logo?: string
    apiKey?: string
    endpoint?: string
    status?: string
  },
) => {
  try {
    const response = await api.put(`/providers/${providerId}`, data)
    return response.data
  } catch (error) {
    console.error('更新提供商失败', error)
    throw error
  }
}

// 删除提供商
export const deleteProvider = async (providerId: string) => {
  try {
    const response = await api.delete(`/providers/${providerId}`)
    return response.data
  } catch (error) {
    console.error('删除提供商失败', error)
    throw error
  }
}

// 获取提供商下的模型列表
export const getModelsByProviderId = async (providerId: string) => {
  try {
    const response = await api.get(`/models/provider/${providerId}`)
    return response.data || []
  } catch (error) {
    console.error('获取模型列表失败', error)
    throw error
  }
}

export const providerService = {
  getProviders,
  getProviderById,
  createProvider,
  updateProvider,
  deleteProvider,
  getModelsByProviderId,
}
