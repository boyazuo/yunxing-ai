'use client'

import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'
import { cva, type VariantProps } from 'class-variance-authority'
import { MoreHorizontal, MoreVertical } from 'lucide-react'
import type { ComponentProps } from 'react'

const rowActionsTriggerVariants = cva(
  [
    'shrink-0 shadow-none border-0',
    'text-muted-foreground/65',
    'hover:bg-muted/80 hover:text-foreground',
    'focus-visible:bg-muted/80 focus-visible:text-foreground focus-visible:ring-2 focus-visible:ring-ring/25 focus-visible:ring-offset-0',
    'data-[state=open]:bg-muted data-[state=open]:text-foreground',
    'group-hover:bg-muted/40 group-hover:text-foreground/80',
    'transition-[color,background-color,transform] duration-150 active:scale-95',
  ].join(' '),
  {
    variants: {
      size: {
        xs: 'h-6 w-6 rounded-full [&_svg]:size-3.5',
        sm: 'h-7 w-7 rounded-full [&_svg]:size-3.5',
        md: 'h-8 w-8 rounded-full [&_svg]:size-4',
      },
    },
    defaultVariants: {
      size: 'md',
    },
  },
)

type RowActionsTriggerProps = ComponentProps<typeof Button> &
  VariantProps<typeof rowActionsTriggerVariants> & {
    icon?: 'horizontal' | 'vertical'
    label?: string
  }

function RowActionsTrigger({
  className,
  size,
  icon = 'horizontal',
  label = '操作',
  ...props
}: RowActionsTriggerProps) {
  const Icon = icon === 'vertical' ? MoreVertical : MoreHorizontal

  return (
    <Button
      variant="ghost"
      size="icon"
      className={cn(rowActionsTriggerVariants({ size }), className)}
      {...props}
    >
      <Icon />
      <span className="sr-only">{label}</span>
    </Button>
  )
}

export { RowActionsTrigger, rowActionsTriggerVariants }
