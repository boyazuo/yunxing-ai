'use client'

import { Select, SelectContent, SelectItem, SelectTrigger } from '@/components/ui/select'
import { usePathname, useRouter } from '@/i18n/routing'
import { useParams } from 'next/navigation'

import { localeNames } from '@/i18n/locale'
import { MdLanguage } from 'react-icons/md'

export default function ({ isIcon = false }: { isIcon?: boolean }) {
  const params = useParams()
  const locale = params.locale as string
  const router = useRouter()
  const pathname = usePathname()

  const handleSwitchLanguage = (value: string) => {
    if (value !== locale) {
      router.replace(pathname, { locale: value })
    }
  }

  return (
    <Select value={locale} onValueChange={handleSwitchLanguage}>
      <SelectTrigger className="flex items-center gap-x-2 border-none !shadow-none text-muted-foreground outline-none hover:bg-transparent focus:ring-0 focus:ring-offset-0 cursor-pointer">
        <MdLanguage className="text-xl" />
        {!isIcon && <span className="hidden md:block">{localeNames[locale]}</span>}
      </SelectTrigger>
      <SelectContent className="z-50">
        {Object.keys(localeNames).map((key: string) => {
          const name = localeNames[key]
          return (
            <SelectItem className="cursor-pointer" key={key} value={key}>
              {name}
            </SelectItem>
          )
        })}
      </SelectContent>
    </Select>
  )
}
