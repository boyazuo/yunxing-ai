import type { DefaultSession } from 'next-auth'
import 'next-auth/jwt'

declare module 'next-auth' {
  interface Session {
    accessToken: string
    user: {
      userId?: string
      email?: string
      username?: string
      avatar?: string
    } & DefaultSession['user']
  }

  interface User {
    id: string
    userId: string
    email: string
    username: string
    avatar?: string
    accessToken: string
  }
}

declare module 'next-auth/jwt' {
  interface JWT {
    accessToken: string
    userId?: string
    email?: string
    username?: string
    avatar?: string
  }
}
