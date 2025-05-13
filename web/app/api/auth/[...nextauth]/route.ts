import { authService } from '@/api/auth'
import { getUserById } from '@/api/user'
import type { Tenant, User } from '@/types/account'
import type { AuthOptions } from 'next-auth'
import NextAuth from 'next-auth'
import CredentialsProvider from 'next-auth/providers/credentials'

export const authOptions: AuthOptions = {
  providers: [
    CredentialsProvider({
      name: 'Credentials',
      credentials: {
        email: { label: 'Email', type: 'email' },
        password: { label: 'Password', type: 'password' },
      },
      async authorize(credentials) {
        if (!credentials?.email || !credentials?.password) {
          return null
        }

        try {
          // 这里需要连接到您的后端API进行用户验证
          const response = await authService.login({
            email: credentials.email,
            password: credentials.password,
          })

          if (!response) {
            return null
          }

          if (!response.token) {
            return null
          }

          // 返回用户信息
          return {
            id: response.user.userId,
            accessToken: response.token,
            user: response.user,
            tenant: response.tenant,
          }
        } catch (error) {
          return null
        }
      },
    }),
  ],
  session: {
    strategy: 'jwt',
    maxAge: 30 * 24 * 60 * 60, // 30天
  },
  callbacks: {
    async jwt({ token, user, session, trigger }) {
      // 首次登录时，user对象中包含accessToken和其他信息
      if (user) {
        // 使用扩展运算符将所有用户属性复制到token中
        return {
          ...token,
          ...user,
        }
      }

      if (trigger === 'update') {
        if (session.user) {
          const user = session.user
          return {
            ...token,
            user
          }
        }
      }

      return token
    },
    async session({ session, token }) {
      // 将token中的信息传递到session中，前端可以访问
      session.accessToken = token.accessToken as string
      session.user = token.user as User
      session.tenant = token.tenant as Tenant
      return session
    },
  },
  pages: {
    signIn: '/login',
    signOut: '/login',
    error: '/login', // 错误页面
  },
  secret: process.env.NEXTAUTH_SECRET,
}

const handler = NextAuth(authOptions)
export { handler as GET, handler as POST }
