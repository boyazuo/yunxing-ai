'use client'

import { CHAT_STREAM_PHASE_LABELS, type ChatStreamPhase } from '@/types/chat'
import { useEffect, useState } from 'react'

interface ThinkingIndicatorProps {
  phase: ChatStreamPhase
}

export function ThinkingIndicator({ phase }: ThinkingIndicatorProps) {
  const [dotCount, setDotCount] = useState(1)

  useEffect(() => {
    const timer = setInterval(() => {
      setDotCount((prev) => (prev % 3) + 1)
    }, 450)
    return () => clearInterval(timer)
  }, [])

  const label = CHAT_STREAM_PHASE_LABELS[phase]

  return (
    <div className="py-1" aria-live="polite" aria-busy="true">
      <span className="text-sm text-muted-foreground">
        {label}
        <span className="inline-block w-[1.125rem] tabular-nums">{'.'.repeat(dotCount)}</span>
      </span>
    </div>
  )
}
