'use client'

import { cn } from '@/lib/utils'
import { Check, Copy, Terminal } from 'lucide-react'
import { useTheme } from 'next-themes'
import { ComponentPropsWithoutRef, ReactNode, useState } from 'react'
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter'
import { oneDark, oneLight } from 'react-syntax-highlighter/dist/esm/styles/prism'

export interface CodeBlockProps extends ComponentPropsWithoutRef<'code'> {
  inline?: boolean // react-markdown 传递的属性
  className?: string // 可能包含语言信息，例如 "language-javascript"
  children: ReactNode
}

export function CodeBlock({
  inline, // react-markdown 传递的属性
  className,
  children,
  ...props
}: CodeBlockProps) {
  const [copied, setCopied] = useState(false)
  const { theme } = useTheme()
  const isDark = theme === 'dark'

  // 从 className 中提取语言信息
  const language = className?.replace(/language-/, '') || 'text'

  // 获取代码内容为字符串
  const codeContent = typeof children === 'string'
    ? children
    : Array.isArray(children)
      ? children.map(child => typeof child === 'string' ? child : '').join('')
      : ''

  // 复制代码到剪贴板
  const copyToClipboard = () => {
    navigator.clipboard.writeText(codeContent)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  // 内联代码渲染
  if (inline) {
    return (
      <code
        className={cn(
          'text-sm font-mono bg-zinc-100/70 dark:bg-zinc-800/70 py-[0.2rem] px-1.5 rounded-md border border-zinc-200 dark:border-zinc-700'
        )}
        {...props}
      >
        {children}
      </code>
    )
  }

  // 代码块渲染
  return (
    <div className="not-prose my-4 w-full overflow-hidden rounded-lg border border-zinc-200 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-900 shadow-sm">
      <div className="flex items-center justify-between px-4 py-2 bg-zinc-100 dark:bg-zinc-800 border-b border-zinc-200 dark:border-zinc-700">
        <div className="flex items-center gap-2">
          <Terminal className="h-4 w-4 text-zinc-500 dark:text-zinc-400" />
          <span className="text-sm font-medium text-zinc-600 dark:text-zinc-300">
            {language}
          </span>
        </div>
        <button
          type="button"
          onClick={copyToClipboard}
          className="text-zinc-500 hover:text-zinc-700 dark:text-zinc-400 dark:hover:text-zinc-200 transition-colors rounded-md p-1 hover:bg-zinc-200 dark:hover:bg-zinc-700"
          aria-label="复制代码"
        >
          {copied ? (
            <Check className="h-4 w-4" />
          ) : (
            <Copy className="h-4 w-4" />
          )}
        </button>
      </div>
      <div className="relative text-sm w-full overflow-auto">
        <SyntaxHighlighter
          language={language}
          style={isDark ? oneDark : oneLight}
          customStyle={{
            margin: 0,
            padding: '1rem',
            fontSize: '0.875rem',
            lineHeight: '1.5',
          }}
        >
          {codeContent}
        </SyntaxHighlighter>
      </div>
    </div>
  )
}
