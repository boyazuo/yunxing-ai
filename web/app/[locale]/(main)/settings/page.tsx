import { redirect } from 'next/navigation'

export default function SettingsPage() {
  // 重定向到账户设置页面
  redirect('/settings/account')
}
