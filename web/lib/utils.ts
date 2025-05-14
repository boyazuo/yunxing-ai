import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

/**
 * 格式化日期
 * @param date 日期对象或日期字符串
 * @param format 格式，默认 yyyy-MM-dd
 * @returns 格式化后的日期字符串
 */
export function formatDate(date: Date | string, format = 'yyyy-MM-dd'): string {
  const d = typeof date === 'string' ? new Date(date) : date

  const year = d.getFullYear()
  const month = d.getMonth() + 1
  const day = d.getDate()
  const hour = d.getHours()
  const minute = d.getMinutes()
  const second = d.getSeconds()

  const pad = (n: number): string => (n < 10 ? `0${n}` : `${n}`)

  return format
    .replace('yyyy', `${year}`)
    .replace('MM', pad(month))
    .replace('dd', pad(day))
    .replace('HH', pad(hour))
    .replace('mm', pad(minute))
    .replace('ss', pad(second))
}
