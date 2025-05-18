'use client'

import type { ComponentPropsWithoutRef, ReactNode } from 'react'

export interface CodeBlockProps extends ComponentPropsWithoutRef<'code'> {
  isInline?: boolean // 使用 isInline 属性代替 inline
  children: ReactNode
}

export function CodeBlock({ isInline, className, children, ...props }: CodeBlockProps) {
  if (!isInline) {
    return (
      <div className="not-prose flex flex-col">
        <pre className="text-sm w-full overflow-x-auto dark:bg-zinc-900 p-4 border border-zinc-200 dark:border-zinc-700 rounded-xl dark:text-zinc-50 text-zinc-900">
          <code className="whitespace-pre-wrap break-words" {...props}>
            {children}
          </code>
        </pre>
      </div>
    )
  }

  return (
    <code className={`${className || ''} text-sm bg-zinc-100 dark:bg-zinc-800 py-0.5 px-1 rounded-md`} {...props}>
      {children}
    </code>
  )
}
