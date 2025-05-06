'use client'

import { FaGithub } from 'react-icons/fa'
import { Button } from '../../ui/button'

export default function GithubLink({ isIcon = false }: { isIcon?: boolean }) {
  return (
    <Button
      variant="ghost"
      size="icon"
      asChild
      className="border-none text-muted-foreground hover:bg-transparent focus:ring-0 focus:ring-offset-0"
    >
      <a
        href="https://github.com/boyazuo/yunxing-ai"
        target="_blank"
        rel="noopener noreferrer"
        className="flex items-center"
      >
        <FaGithub className="h-[1.2rem] w-[1.2rem]" />
        {!isIcon && <span className="ml-2 hidden md:block">GitHub</span>}
      </a>
    </Button>
  )
}
