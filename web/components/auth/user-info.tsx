'use client'

import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { useSession } from 'next-auth/react'
import { LogoutButton } from './logout-button'

export function UserInfo() {
  const { data: session, status } = useSession()

  if (status === 'loading') {
    return <div className="flex items-center gap-2">Loading...</div>
  }

  if (status === 'unauthenticated' || !session) {
    return null
  }

  return (
    <div className="flex items-center gap-4">
      <div className="flex flex-col">
        <span className="text-sm font-medium">{session.user.name || session.user.email}</span>
        <span className="text-xs text-muted-foreground">{session.user.email}</span>
      </div>
      <Avatar className="h-9 w-9">
        {session.user.image && <AvatarImage src={session.user.image} alt={session.user.name || ''} />}
        <AvatarFallback>{(session.user.name || session.user.email || '').charAt(0).toUpperCase()}</AvatarFallback>
      </Avatar>
      <LogoutButton variant="ghost" className="h-8" />
    </div>
  )
}
