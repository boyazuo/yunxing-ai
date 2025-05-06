'use client'

import type { Session } from 'next-auth'
import { SessionProvider } from 'next-auth/react'
import type React from 'react'

export default function NextAuthSessionProvider({
  children,
  session,
}: {
  children: React.ReactNode
  session?: Session | null
}) {
  return <SessionProvider session={session}>{children}</SessionProvider>
}
