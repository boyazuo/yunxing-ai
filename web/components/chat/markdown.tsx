'use client'

import Link from 'next/link'
import { memo } from 'react'
import { Streamdown } from 'streamdown'
import 'streamdown/styles.css'
import { CodeBlock } from './code-block'

const components = {
  code: ({ inline, className, children, ...props }: { inline?: boolean; className?: string; children?: React.ReactNode }) => {
    const match = /language-(\w+)/.exec(className || '')
    return !inline && match ? (
      <CodeBlock inline={false} className={className} {...props}>
        {children}
      </CodeBlock>
    ) : (
      <CodeBlock inline className={className} {...props}>
        {children}
      </CodeBlock>
    )
  },
  pre: ({ children }: { children?: React.ReactNode }) => <>{children}</>,
  ol: ({ children, ...props }: React.ComponentProps<'ol'>) => (
    <ol className="list-decimal list-outside ml-4 my-2" {...props}>
      {children}
    </ol>
  ),
  li: ({ children, ...props }: React.ComponentProps<'li'>) => (
    <li className="py-1" {...props}>
      {children}
    </li>
  ),
  ul: ({ children, ...props }: React.ComponentProps<'ul'>) => (
    <ul className="list-disc list-outside ml-4 my-2" {...props}>
      {children}
    </ul>
  ),
  strong: ({ children, ...props }: React.ComponentProps<'span'>) => (
    <span className="font-semibold" {...props}>
      {children}
    </span>
  ),
  a: ({ children, href, ...props }: React.ComponentProps<'a'>) => (
    <Link className="text-blue-500 hover:underline" href={href || '#'} target="_blank" rel="noreferrer" {...props}>
      {children}
    </Link>
  ),
  h1: ({ children, ...props }: React.ComponentProps<'h1'>) => (
    <h1 className="text-2xl font-semibold mt-6 mb-2 first:mt-0" {...props}>
      {children}
    </h1>
  ),
  h2: ({ children, ...props }: React.ComponentProps<'h2'>) => (
    <h2 className="text-xl font-semibold mt-5 mb-2 first:mt-0" {...props}>
      {children}
    </h2>
  ),
  h3: ({ children, ...props }: React.ComponentProps<'h3'>) => (
    <h3 className="text-lg font-semibold mt-4 mb-2 first:mt-0" {...props}>
      {children}
    </h3>
  ),
  h4: ({ children, ...props }: React.ComponentProps<'h4'>) => (
    <h4 className="text-base font-semibold mt-4 mb-2 first:mt-0" {...props}>
      {children}
    </h4>
  ),
  p: ({ children, ...props }: React.ComponentProps<'p'>) => (
    <p className="my-2 leading-relaxed" {...props}>
      {children}
    </p>
  ),
}

interface MarkdownProps {
  children: string
  isStreaming?: boolean
}

function MarkdownContent({ children, isStreaming = false }: MarkdownProps) {
  return (
    <Streamdown
      mode={isStreaming ? 'streaming' : 'static'}
      isAnimating={isStreaming}
      parseIncompleteMarkdown={isStreaming}
      components={components}
      className="text-sm leading-relaxed text-foreground"
    >
      {children}
    </Streamdown>
  )
}

export const Markdown = memo(MarkdownContent, (prev, next) => prev.children === next.children && prev.isStreaming === next.isStreaming)
