'use client'

import { zodResolver } from '@hookform/resolvers/zod'
import { useSession } from 'next-auth/react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import * as z from 'zod'

import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { updateCurrentUser } from '@/api/user'
import { authService } from '@/api/auth'
import { signOut } from 'next-auth/react'
import { fileService } from '@/api/file'
import { SysFile } from '@/types/system'

// 基本信息表单验证
const profileFormSchema = z.object({
  username: z.string().min(2, {
    message: '姓名至少需要 2 个字符',
  }),
})

// 密码表单验证
const passwordFormSchema = z
  .object({
    currentPassword: z.string().min(8, {
      message: '密码至少需要 8 个字符',
    }),
    newPassword: z.string().min(8, {
      message: '密码至少需要 8 个字符',
    }),
    confirmPassword: z.string().min(8, {
      message: '密码至少需要 8 个字符',
    }),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: '两次输入的密码不一致',
    path: ['confirmPassword'],
  })

export default function AccountSettingsPage() {
  const { data: session, update } = useSession()
  const [isUpdating, setIsUpdating] = useState(false)
  const [isChangingPassword, setIsChangingPassword] = useState(false)
  const [avatarFile, setAvatarFile] = useState<File | null>(null)
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null)
  const [avatarId, setAvatarId] = useState<number | null>(null)

  // 基本信息表单
  const profileForm = useForm<z.infer<typeof profileFormSchema>>({
    resolver: zodResolver(profileFormSchema),
    defaultValues: {
      username: session?.user?.username || '',
    },
  })

  // 密码表单
  const passwordForm = useForm<z.infer<typeof passwordFormSchema>>({
    resolver: zodResolver(passwordFormSchema),
    defaultValues: {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    },
  })

  // 设置初始表单数据
  useEffect(() => {
    if (session?.user) {
      profileForm.reset({
        username: session.user.username || '',
      })
    }
  }, [session, profileForm])

  // 处理头像上传
  const handleAvatarChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (file) {
      try {
        // 调用上传头像的API
        fileService.updateFile(file).then((res) => {
          const sysFile: SysFile = res
          setAvatarFile(file)
          // 生成预览
          const reader = new FileReader()
          reader.onload = (e) => {
            setAvatarPreview(e.target?.result as string)
          }
          reader.readAsDataURL(file)
          // 更新 头像ID
          setAvatarId(sysFile.fileId)
          toast.success('头像上传成功')
        })
      } catch (error) {
        toast.error('头像上传失败')
      }
    }
  }

  // 处理基本信息提交（合并头像上传逻辑）
  const onProfileSubmit = async (data: z.infer<typeof profileFormSchema>) => {
    setIsUpdating(true)
    try {

      // 更新用户名 和 头像 ID 到数据库
      await updateCurrentUser({ username: data.username, avatarId: avatarId })

      // 更新session中的用户名
      await update({
        ...session,
        user: {
          ...session?.user,
          username: data.username,
        },
      })

      toast.success('个人信息及头像更新成功')
    } catch (error) {
      toast.error('更新失败')
      console.error(error)
    } finally {
      setIsUpdating(false)
      // 清空头像选择状态
      setAvatarFile(null)
      setAvatarPreview(null)
    }
  }

  // 处理密码修改提交
  const onPasswordSubmit = async (data: z.infer<typeof passwordFormSchema>) => {
    setIsChangingPassword(true)
    try {
      // 调用 API
      const res: any = await authService.changePassword(data)
      if (res.code === 0) {
        toast.success('密码修改成功')
        passwordForm.reset()

        // 退出登录
        signOut({ callbackUrl: '/login' })
      } else {
        toast.error(res.msg)
      }
    } catch (error) {
      toast.error('密码修改失败')
      console.error(error)
    } finally {
      setIsChangingPassword(false)
    }
  }

  const userInitials = session?.user?.username
    ? session.user.username
        .split(' ')
        .map((n) => n[0])
        .join('')
    : '用户'

  return (
    <div className="space-y-6 max-w-3xl py-6">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight">账户设置</h2>
        <p className="text-muted-foreground mt-1">管理您的账户信息和安全设置</p>
      </div>

      <Tabs defaultValue="profile" className="w-full">
        <TabsList className="mb-4">
          <TabsTrigger value="profile">个人资料</TabsTrigger>
          <TabsTrigger value="password">密码设置</TabsTrigger>
        </TabsList>

        <TabsContent value="profile">
          <Card>
            <CardContent className="p-6 space-y-6">
              {/* 头像设置 */}
              <div className="space-y-4">
                <h3 className="text-lg font-medium">个人信息</h3>
                <Separator />
                <div className="flex items-center gap-6">
                  <Label htmlFor="avatar" className="cursor-pointer">
                    {' '}
                    {/* 将 Label 包裹头像 */}
                    <Avatar className="w-20 h-20">
                      <AvatarImage
                        src={avatarPreview || session?.user?.avatar || undefined}
                      />
                      <AvatarFallback className="text-lg">
                        {userInitials}
                      </AvatarFallback>
                    </Avatar>
                    <Input
                      id="avatar"
                      type="file"
                      accept="image/*"
                      className="hidden"
                      onChange={handleAvatarChange}
                    />
                  </Label>

                  <div className="space-y-2">
                    {/* 移除独立的上传按钮，仅保留格式提示 */}
                    <p className="text-xs text-muted-foreground">
                      支持JPG, PNG格式, 建议尺寸 400x400 像素
                    </p>
                  </div>
                </div>
              </div>

              {/* 个人信息表单 */}
              <div className="space-y-4">
                <Form {...profileForm}>
                  <form
                    onSubmit={profileForm.handleSubmit(onProfileSubmit)}
                    className="space-y-4"
                  >
                    <FormField
                      control={profileForm.control}
                      name="username"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>用户名</FormLabel>
                          <FormControl>
                            <Input {...field} />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />

                    <div>
                      <FormLabel>邮箱</FormLabel>
                      <Input
                        value={session?.user?.email || ''}
                        disabled
                        className="bg-muted/30 text-muted-foreground"
                      />
                      <p className="text-xs text-muted-foreground mt-1">
                        邮箱地址不可修改
                      </p>
                    </div>

                    <Button
                      type="submit"
                      disabled={isUpdating}
                      className="mt-2"
                    >
                      {isUpdating ? '保存中...' : '保存更改'}
                    </Button>
                  </form>
                </Form>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="password">
          <Card>
            <CardContent className="p-6 space-y-4">
              <h3 className="text-lg font-medium">修改密码</h3>
              <Separator />
              <Form {...passwordForm}>
                <form
                  onSubmit={passwordForm.handleSubmit(onPasswordSubmit)}
                  className="space-y-4"
                >
                  <FormField
                    control={passwordForm.control}
                    name="currentPassword"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>当前密码</FormLabel>
                        <FormControl>
                          <Input type="password" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <FormField
                    control={passwordForm.control}
                    name="newPassword"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>新密码</FormLabel>
                        <FormControl>
                          <Input type="password" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <FormField
                    control={passwordForm.control}
                    name="confirmPassword"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>确认密码</FormLabel>
                        <FormControl>
                          <Input type="password" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <Button
                    type="submit"
                    disabled={isChangingPassword}
                    className="mt-2"
                  >
                    {isChangingPassword ? '更新中...' : '更新密码'}
                  </Button>
                </form>
              </Form>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
