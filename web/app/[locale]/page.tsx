import { getServerSession } from 'next-auth'
import { redirect } from 'next/navigation'

export default async function RootPage() {
  const session = await getServerSession()

  // 根据用户登录状态重定向
  if (session) {
    // 已登录用户重定向到 home
    redirect('/home')
  } else {
    // 未登录用户重定向到 login
    redirect('/login')
  }
}
