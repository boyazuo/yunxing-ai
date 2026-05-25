export interface NormalizedPage<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

/** MyBatis-Flex Page 与前端约定的字段名差异 */
interface FlexPage<T> {
  records?: T[]
  total?: number
  totalRow?: number
  current?: number
  pageNumber?: number
  size?: number
  pageSize?: number
  pages?: number
  totalPage?: number
}

export function normalizePage<T>(data: FlexPage<T>, fallbackCurrent = 1, fallbackSize = 10): NormalizedPage<T> {
  const size = data.size ?? data.pageSize ?? fallbackSize
  const current = data.current ?? data.pageNumber ?? fallbackCurrent
  const total = data.total ?? data.totalRow ?? 0
  const pages = data.pages ?? data.totalPage ?? (size > 0 ? Math.ceil(total / size) : 0)

  return {
    records: data.records ?? [],
    total,
    current,
    size,
    pages,
  }
}
