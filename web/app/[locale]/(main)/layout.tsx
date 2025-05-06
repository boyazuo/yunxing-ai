import Header from '@/components/layouts/header'
import { getServerSession } from 'next-auth'
import { redirect } from 'next/navigation'

export default async function MainLayout({
  children,
}: {
  children: React.ReactNode
}) {
  // 未登录用户，重定向至登录页面
  const session = await getServerSession()
  if (!session) {
    redirect('/login')
  }

  return (
    <div className="relative flex min-h-screen flex-col">
      <Header />
      <main className="flex-1">{children}</main>
    </div>
  )
}
