import type { DefaultSession } from 'next-auth'
import 'next-auth/jwt'
import type { Tenant, User as UserType } from './account'

declare module 'next-auth' {
  interface Session {
    accessToken: string
    user: UserType & DefaultSession['user']
    tenant: Tenant
  }

  interface User {
    id: string | undefined
    accessToken: string
    user: UserType & DefaultSession['user']
    tenant: Tenant
  }
}

declare module 'next-auth/jwt' {
  interface JWT {
    accessToken: string
    user: UserType
    tenant: Tenant
  }
}
