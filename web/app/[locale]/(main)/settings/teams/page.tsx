'use client'

import { invitationService } from '@/api/invitation'
import { teamService, UserInTenant } from '@/api/team'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { cn } from '@/lib/utils'
import { Tenant, TenantUserRole } from '@/types/account'
import { zodResolver } from '@hookform/resolvers/zod'
import {
  CheckCircle2,
  ChevronDown,
  Crown,
  MoreVertical,
  PencilIcon,
  Search,
  ShieldCheck,
  UserPlus,
  UserX,
  Users,
} from 'lucide-react'
import { useSession } from 'next-auth/react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import * as z from 'zod'

// 团队成员表单验证
const memberFormSchema = z.object({
  email: z.string().email({
    message: '请输入有效的邮箱地址',
  }),
  role: z.enum(['owner', 'admin', 'normal'], {
    required_error: '请选择角色',
  }),
})

// 团队名称表单验证
const teamNameFormSchema = z.object({
  teamName: z
    .string()
    .min(2, {
      message: '团队名称至少需要 2 个字符',
    })
    .max(50, {
      message: '团队名称最多 50 个字符',
    }),
})

// 角色信息
const roles = [
  { value: 'owner', label: '所有者', icon: <Crown className="mr-2 h-4 w-4" /> },
  {
    value: 'admin',
    label: '管理员',
    icon: <ShieldCheck className="mr-2 h-4 w-4" />,
  },
  { value: 'normal', label: '成员', icon: <Users className="mr-2 h-4 w-4" /> },
]

// 测试数据 - 多个团队信息
const initialTeams: Tenant[] = [{ tenantId: '', tenantName: '' }]

// 团队成员
const initialMembers: UserInTenant[] = []

export default function TeamsPage() {
  const { data: session, update } = useSession()
  const [isOpenAddDialog, setIsOpenAddDialog] = useState(false)
  const [isOpenEditNameDialog, setIsOpenEditNameDialog] = useState(false)
  const [isTeamSwitcherOpen, setIsTeamSwitcherOpen] = useState(false)
  const [members, setMembers] = useState(initialMembers)
  const [teams, setTeams] = useState(initialTeams)
  const [currentTeam, setCurrentTeam] = useState(
    initialTeams.find((team) => team.isActive) || initialTeams[0]
  )
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')
  const [activeTab, setActiveTab] = useState('all')

  const [
    currentUserInTenant,
    setCurrentUserInTenant,
  ] = useState<UserInTenant | null>()
  // 加载当前用户所涉及的团队
  useEffect(() => {
    // 这里添加获取当前用户所涉及的团队的API调用
    teamService.getTeams().then((data) => {
      setTeams(data)
      setCurrentTeam(data.find((team) => team.isActive) || data[0])
    })
  }, [])

  // 加载当前团队所具有的人员
  useEffect(() => {
    if (currentTeam.tenantId) {
      teamService.getUserInTeam(currentTeam.tenantId).then((data) => {
        data.find((userInTenant) => {
          if (userInTenant.userId === session?.user.userId) {
            setCurrentUserInTenant(userInTenant)
          }
        })
        setMembers(data)
      })
    }
  }, [currentTeam])

  // 成员邀请表单
  const memberForm = useForm<z.infer<typeof memberFormSchema>>({
    resolver: zodResolver(memberFormSchema),
    defaultValues: {
      email: '',
      role: 'normal',
    },
  })

  // 团队名称修改表单
  const teamNameForm = useForm<z.infer<typeof teamNameFormSchema>>({
    resolver: zodResolver(teamNameFormSchema),
    defaultValues: {
      teamName: currentTeam.tenantName,
    },
  })

  // 设置团队名称表单默认值
  useEffect(() => {
    teamNameForm.reset({ teamName: currentTeam.tenantName })
  }, [currentTeam, teamNameForm])

  // 处理添加团队成员提交
  const onSubmitMember = async (data: z.infer<typeof memberFormSchema>) => {
    setIsSubmitting(true)
    try {
      // 这里添加邀请团队成员的API调用
      console.log('邀请团队成员:', data)

      // 调用邀请 并 发送邮件的API
      await invitationService.createInvitations({
        inviterTenantId: currentTeam.tenantId,
        inviteeEmail: data.email,
        inviteeRole: data.role,
      })

      toast.success('团队成员邀请已发送')
      memberForm.reset()
     
    } catch (error) {
      toast.error('邀请团队成员失败')
      console.error(error)
    } finally {
      setIsOpenAddDialog(false)
      setIsSubmitting(false)
    }
  }

  // 处理团队名称修改提交
  const onSubmitTeamName = async (data: z.infer<typeof teamNameFormSchema>) => {
    setIsSubmitting(true)
    try {
      // 修改团队名称的API调用
      const res: any = await teamService.updateTeam({
        tenantId: currentTeam.tenantId,
        tenantName: data.teamName,
      })

      if (res.code === 0) {
        const updatedTeams = teams.map((team) =>
          team.tenantId === currentTeam.tenantId
            ? { ...team, tenantName: data.teamName }
            : team
        )

        setTeams(updatedTeams)
        setCurrentTeam({ ...currentTeam, tenantName: data.teamName })
        toast.success('团队名称已更新')
        setIsOpenEditNameDialog(false)
      } else {
        toast.error(res.msg)
      }
    } catch (error) {
      toast.error('更新团队名称失败')
      console.error(error)
    } finally {
      setIsSubmitting(false)
    }
  }

  // 处理团队切换
  const handleTeamChange = (teamId: string | undefined) => {
    const selectedTeam = teams.find((team) => team.tenantId === teamId)
    if (selectedTeam) {
      // 更新所有团队的活跃状态
      const updatedTeams = teams.map((team) => ({
        ...team,
        isActive: team.tenantId === teamId,
      }))

      setTeams(updatedTeams)
      setCurrentTeam(selectedTeam)
      setIsTeamSwitcherOpen(false)

      // 这里可以添加API调用来切换活跃团队
      console.log('切换到团队:', selectedTeam)
    }
  }

  // 处理删除团队成员
  const handleDeleteMember = (id: string) => {
    try {
      // 这里添加删除团队成员的API调用
      teamService.deleteTenantUser(currentTeam.tenantId, id).then((res) => {
        if (res.code === 0) {
          setMembers(members.filter((member) => member.userId !== id))
          toast.success('团队成员已移除')
        } else {
          toast.error('移除团队成员失败')
        }
      })
    } catch (error) {
      toast.error('移除团队成员失败')
      console.error(error)
    }
  }

  // 处理修改团队成员角色
  const handleChangeRole = (userId: string, newRole: TenantUserRole) => {
    try {
      // 这里添加修改团队成员角色的API调用
      teamService
        .updupdateTenantUserRole(newRole, currentTeam.tenantId, userId)
        .then((res) => {
          setMembers(
            members.map((member) =>
              member.userId === userId ? { ...member, role: newRole } : member
            )
          )
          toast.success('团队成员角色已更新')
        })
    } catch (error) {
      toast.error('更新团队成员角色失败')
      console.error(error)
    }
  }

  // 获取角色显示名称
  const getRoleLabel = (role: string | undefined) => {
    return roles.find((r) => r.value === role)?.label || role
  }

  // 获取角色图标
  const getRoleIcon = (role: string | undefined) => {
    return roles.find((r) => r.value === role)?.icon
  }

  // 生成头像文本
  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
  }

  // 获取计划名称显示
  const getPlanDisplay = (plan: string | undefined) => {
    plan = plan || 'free'
    const planMap: Record<string, string> = {
      free: '免费版',
      basic: '基础版',
      pro: '专业版',
    }
    return planMap[plan] || plan
  }

  // 筛选成员
  const filteredMembers = members.filter((member) => {
    // 搜索筛选
    const matchesSearch =
      member.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
      member.email.toLowerCase().includes(searchQuery.toLowerCase())

    // 标签筛选
    const matchesTab =
      activeTab === 'all' ||
      (activeTab === 'active' && member.isActive) ||
      (activeTab === 'admin' &&
        (member.role === 'admin' || member.role === 'owner'))

    return matchesSearch && matchesTab
  })

  return (
    <div className="px-1 md:px-6 pb-0 max-w-7xl mx-auto flex flex-col">
      <div className="flex-none space-y-5 pb-5">
        {/* 团队信息 */}
        <Card className="py-4 border-none shadow-sm bg-gradient-to-br from-slate-50 to-slate-100 dark:from-slate-900 dark:to-slate-950">
          <CardContent className="p-4">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
              <div className="flex-1 space-y-3">
                {/* 团队切换器 */}
                <div className="relative">
                  <div className="flex items-center gap-2">
                    <Button
                      variant="outline"
                      onClick={() => setIsTeamSwitcherOpen(!isTeamSwitcherOpen)}
                      className="w-full md:w-auto justify-between border-dashed"
                    >
                      <div className="flex items-center gap-2">
                        <Avatar className="h-5 w-5">
                          <AvatarFallback className="bg-primary/10 text-primary text-xs">
                            {getInitials(currentTeam.tenantName)}
                          </AvatarFallback>
                        </Avatar>
                        <span className="text-sm font-medium">
                          {currentTeam.tenantName}
                        </span>
                      </div>
                      <ChevronDown className="ml-2 h-4 w-4 opacity-50" />
                    </Button>

                    {currentUserInTenant?.role === 'owner' ? (
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 rounded-full"
                        onClick={() => setIsOpenEditNameDialog(true)}
                      >
                        <PencilIcon className="h-3.5 w-3.5" />
                        <span className="sr-only">编辑团队名称</span>
                      </Button>
                    ) : (
                      ''
                    )}
                  </div>

                  {isTeamSwitcherOpen && (
                    <div className="absolute top-full left-0 z-10 mt-1 w-full md:w-[240px] rounded-md border bg-popover p-1 shadow-md">
                      <div className="py-2 px-2 text-xs font-medium text-muted-foreground">
                        团队列表
                      </div>
                      {teams.map((team) => (
                        <button
                          type="button"
                          key={team.tenantId}
                          onClick={() => handleTeamChange(team.tenantId)}
                          className={cn(
                            'flex items-center gap-2 w-full rounded-md p-2 text-left text-sm transition-colors',
                            'hover:bg-accent',
                            team.isActive ? 'bg-accent' : ''
                          )}
                        >
                          <Avatar className="h-5 w-5">
                            <AvatarFallback className="bg-primary/10 text-primary text-xs">
                              {getInitials(team.tenantName)}
                            </AvatarFallback>
                          </Avatar>
                          <div className="flex-1 truncate">
                            <p className="font-medium">{team.tenantName}</p>
                            <p className="text-xs text-muted-foreground">
                              {getRoleLabel(team.role)}
                            </p>
                          </div>
                          {team.isActive && (
                            <CheckCircle2 className="h-4 w-4 text-primary" />
                          )}
                        </button>
                      ))}
                    </div>
                  )}
                </div>

                <div className="flex flex-wrap items-center gap-3 text-sm text-muted-foreground">
                  <Badge variant="outline" className="font-normal">
                    {getPlanDisplay(currentTeam.plan)}
                  </Badge>
                  <div className="flex items-center">
                    <Users className="h-3.5 w-3.5 mr-1.5" />
                    {currentTeam.memberCount} 名成员
                  </div>
                  <div className="flex items-center">
                    {getRoleIcon(currentTeam.role)}
                    {getRoleLabel(currentTeam.role)}
                  </div>
                </div>
              </div>
              <Dialog open={isOpenAddDialog} onOpenChange={setIsOpenAddDialog}>
                <DialogTrigger asChild>
                  <Button size="sm">
                    <UserPlus className="mr-2 h-4 w-4" />
                    邀请成员
                  </Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>邀请新团队成员</DialogTitle>
                    <DialogDescription>
                      输入团队成员信息并发送邀请。团队成员将收到邀请邮件。
                    </DialogDescription>
                  </DialogHeader>
                  <Form {...memberForm}>
                    <form
                      onSubmit={memberForm.handleSubmit(onSubmitMember)}
                      className="space-y-4"
                    >
                      <FormField
                        control={memberForm.control}
                        name="email"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>邮箱</FormLabel>
                            <FormControl>
                              <Input placeholder="请输入邮箱" {...field} />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                      <FormField
                        control={memberForm.control}
                        name="role"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>角色</FormLabel>
                            <Select
                              onValueChange={field.onChange}
                              defaultValue={field.value}
                            >
                              <FormControl>
                                <SelectTrigger>
                                  <SelectValue placeholder="选择角色" />
                                </SelectTrigger>
                              </FormControl>
                              <SelectContent>
                                {roles.filter((role) => role.value !== 'owner').map((role) => (
                                  <SelectItem
                                    key={role.value}
                                    value={role.value}
                                  >
                                    <div className="flex items-center">
                                      {role.icon}
                                      {role.label}
                                    </div>
                                  </SelectItem>
                                ))}
                              </SelectContent>
                            </Select>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                      <DialogFooter>
                        <Button type="submit" disabled={isSubmitting}>
                          {isSubmitting ? '发送中...' : '发送邀请'}
                        </Button>
                      </DialogFooter>
                    </form>
                  </Form>
                </DialogContent>
              </Dialog>

              {/* 修改团队名称对话框 */}
              <Dialog
                open={isOpenEditNameDialog}
                onOpenChange={setIsOpenEditNameDialog}
              >
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>修改团队名称</DialogTitle>
                    <DialogDescription>
                      更新您的团队名称，这将显示在所有成员的界面中。
                    </DialogDescription>
                  </DialogHeader>
                  <Form {...teamNameForm}>
                    <form
                      onSubmit={teamNameForm.handleSubmit(onSubmitTeamName)}
                      className="space-y-4"
                    >
                      <FormField
                        control={teamNameForm.control}
                        name="teamName"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>团队名称</FormLabel>
                            <FormControl>
                              <Input placeholder="请输入团队名称" {...field} />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                      <DialogFooter>
                        <Button type="submit" disabled={isSubmitting}>
                          {isSubmitting ? '更新中...' : '更新名称'}
                        </Button>
                      </DialogFooter>
                    </form>
                  </Form>
                </DialogContent>
              </Dialog>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 团队成员列表 */}
      <Card className="flex flex-col py-4 gap-2 overflow-hidden">
        <CardHeader className="p-4 pb-0 flex-none">
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
            <div>
              <CardTitle>团队成员</CardTitle>
              <CardDescription className="text-[12px]">
                管理团队中的成员及其权限。
              </CardDescription>
            </div>
            <div className="relative w-full sm:w-auto">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="搜索成员..."
                className="pl-8 w-full sm:w-[200px] lg:w-[250px]"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>

          <div className="mt-3 pb-3">
            <div className="inline-flex h-9 items-center justify-center rounded-lg bg-muted p-1 text-muted-foreground">
              <button
                type="button"
                className={cn(
                  'inline-flex items-center justify-center whitespace-nowrap rounded-md px-3 py-1 text-sm font-medium ring-offset-background transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50',
                  activeTab === 'all'
                    ? 'bg-background text-foreground shadow-sm'
                    : 'hover:bg-background/50'
                )}
                onClick={() => setActiveTab('all')}
              >
                所有成员
              </button>
              <button
                type="button"
                className={cn(
                  'inline-flex items-center justify-center whitespace-nowrap rounded-md px-3 py-1 text-sm font-medium ring-offset-background transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50',
                  activeTab === 'active'
                    ? 'bg-background text-foreground shadow-sm'
                    : 'hover:bg-background/50'
                )}
                onClick={() => setActiveTab('active')}
              >
                活跃成员
              </button>
              <button
                type="button"
                className={cn(
                  'inline-flex items-center justify-center whitespace-nowrap rounded-md px-3 py-1 text-sm font-medium ring-offset-background transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50',
                  activeTab === 'admin'
                    ? 'bg-background text-foreground shadow-sm'
                    : 'hover:bg-background/50'
                )}
                onClick={() => setActiveTab('admin')}
              >
                管理员
              </button>
            </div>
          </div>
        </CardHeader>

        <CardContent className="p-4 pt-0 overflow-auto grow-0">
          <div className="space-y-1 min-h-0">
            {filteredMembers.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-6 text-center">
                <Users className="h-12 w-12 text-muted-foreground/50 mb-2" />
                <p className="text-muted-foreground mb-2">
                  {searchQuery ? '没有找到匹配的成员' : '还没有团队成员'}
                </p>
                {searchQuery && (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setSearchQuery('')}
                  >
                    清除搜索
                  </Button>
                )}
              </div>
            ) : (
              filteredMembers.map((member) => (
                <div
                  key={member.userId}
                  className={cn(
                    'flex items-center justify-between p-2 rounded-lg transition-colors',
                    'hover:bg-accent/50 group'
                  )}
                >
                  <div className="flex items-center space-x-4">
                    <Avatar className="border">
                      <AvatarImage src={member.avatar} alt={member.username} />
                      <AvatarFallback className="bg-primary/10 text-primary">
                        {getInitials(member.username)}
                      </AvatarFallback>
                    </Avatar>
                    <div>
                      <div className="flex items-center gap-2">
                        <div className="font-medium">{member.username}</div>
                        {member.isActive && (
                          <CheckCircle2 className="text-green-500 h-3.5 w-3.5" />
                        )}
                      </div>
                      <div className="text-sm text-muted-foreground">
                        {member.email}
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Badge
                      variant="outline"
                      className="hidden md:flex items-center gap-1 px-2"
                    >
                      {getRoleIcon(member.role)}
                      <span>{getRoleLabel(member.role)}</span>
                    </Badge>
                    {(currentUserInTenant?.role === 'owner' ||
                      currentUserInTenant?.role === 'admin') &&
                    currentUserInTenant.userId != member.userId ? (
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="opacity-70 group-hover:opacity-100"
                          >
                            <MoreVertical className="h-4 w-4" />
                            <span className="sr-only">操作</span>
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem
                            onClick={() =>
                              handleChangeRole(
                                member.userId,
                                TenantUserRole.ADMIN
                              )
                            }
                            className="flex items-center"
                          >
                            <ShieldCheck className="mr-2 h-4 w-4" />
                            设为管理员
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            onClick={() =>
                              handleChangeRole(
                                member.userId,
                                TenantUserRole.NORMAL
                              )
                            }
                            className="flex items-center"
                          >
                            <Users className="mr-2 h-4 w-4" />
                            设为成员
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            className="text-destructive focus:text-destructive flex items-center"
                            onClick={() => handleDeleteMember(member.userId)}
                          >
                            <UserX className="mr-2 h-4 w-4" />
                            移除团队成员
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    ) : (
                      ''
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
