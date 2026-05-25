import { authService } from '@/api/auth'
import type { Tenant, User } from '@/types/account'
import type { AuthOptions } from 'next-auth'
import CredentialsProvider from 'next-auth/providers/credentials'

export const authOptions: AuthOptions = {
  providers: [
    CredentialsProvider({
      name: 'Credentials',
      credentials: {
        email: { label: 'Email', type: 'email' },
        password: { label: 'Password', type: 'password' },
        token: { label: 'Token', type: 'text' },
      },
      async authorize(credentials) {
        if (!credentials?.email || !credentials?.password) {
          return null
        }

        try {
          const response = await authService.login({
            email: credentials.email,
            password: credentials.password,
            token: credentials.token,
          })

          if (!response?.token) {
            return null
          }

          return {
            id: response.user.userId,
            accessToken: response.token,
            user: response.user,
            tenant: response.tenant,
          }
        } catch {
          return null
        }
      },
    }),
  ],
  session: {
    strategy: 'jwt',
    maxAge: 30 * 24 * 60 * 60,
  },
  callbacks: {
    async jwt({ token, user, session, trigger }) {
      if (user) {
        return {
          ...token,
          ...user,
        }
      }

      if (trigger === 'update' && session.user) {
        return {
          ...token,
          user: session.user,
        }
      }

      return token
    },
    async session({ session, token }) {
      session.accessToken = token.accessToken as string
      session.user = token.user as User
      session.tenant = token.tenant as Tenant
      return session
    },
  },
  pages: {
    signIn: '/login',
    signOut: '/login',
    error: '/login',
  },
  secret: process.env.NEXTAUTH_SECRET,
}
