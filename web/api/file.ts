import { api } from '@/lib/api'
import type { SysFile } from '@/types/system'

export const fileService = {
  apiRoot: '/system/file',
  // 修改后的头像上传方法（支持文件上传）
  async updateFile(file: File): Promise<SysFile> {
    try {
      const formData = new FormData()
      formData.append('file', file) // 将文件添加到FormData
      // 发送PUT请求，使用multipart/form-data格式
      const response = await api.post<SysFile>(this.apiRoot, formData, {
        headers: {
          'Content-Type': 'multipart/form-data', // 指定内容类型
        },
      })
      return response.data // 返回上传后的文件信息
    } catch (error) {
      console.error('上传文件失败', error)
      throw error
    }
  },
}
