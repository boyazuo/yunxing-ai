import { api } from '@/lib/api'
import type { CommonStatus, Model, ModelType } from '@/types/ai'

// 获取提供商下的模型列表
export const getModelsByProviderId = async (providerId: string): Promise<Model[]> => {
  try {
    const response = await api.get<Model[]>(`/models/provider/${providerId}`)
    return response.data || []
  } catch (error) {
    console.error('获取模型列表失败', error)
    throw error
  }
}

// 获取模型类型列表
export const getModelsByType = async (modelType: ModelType): Promise<Model[]> => {
  try {
    const response = await api.get<Model[]>(`/models/type/${modelType}`)
    return response.data || []
  } catch (error) {
    console.error('获取模型类型列表失败', error)
    throw error
  }
}

// 创建模型
export const createModel = async (data: {
  tenantId: string
  providerId: string
  modelName: string
  displayName: string
  modelType: ModelType
  contextLength: number
  maxTokens: number
  inputPrice: number
  outputPrice: number
}): Promise<Model> => {
  try {
    const response = await api.post<Model>('/models', data)
    return response.data
  } catch (error) {
    console.error('创建模型失败', error)
    throw error
  }
}

// 更新模型
export const updateModel = async (
  modelId: string,
  data: {
    tenantId?: string
    modelName?: string
    displayName?: string
    modelType?: ModelType
    contextLength?: number
    maxTokens?: number
    inputPrice?: number
    outputPrice?: number
    status?: CommonStatus
  },
) => {
  try {
    const response = await api.put<Model>(`/models/${modelId}`, data)
    return response.data
  } catch (error) {
    console.error('更新模型失败', error)
    throw error
  }
}

// 获取模型详情
export const getModelById = async (modelId: string): Promise<Model> => {
  try {
    const response = await api.get<Model>(`/models/${modelId}`)
    return response.data
  } catch (error) {
    console.error('获取模型详情失败', error)
    throw error
  }
}

export const modelService = {
  getModelsByProviderId,
  getModelsByType,
  createModel,
  updateModel,
  getModelById,
}
