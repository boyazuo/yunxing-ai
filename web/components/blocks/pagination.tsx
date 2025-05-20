import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination'

interface CustomPaginationProps {
  currentPage: number
  pageSize: number
  totalItems: number
  onPageChange: (page: number) => void
}

export function CustomPagination({ currentPage, pageSize, totalItems, onPageChange }: CustomPaginationProps) {
  const totalPages = Math.ceil(totalItems / pageSize)

  // 计算要显示哪些页码
  const getPageNumbers = (): (number | string)[] => {
    const pages: (number | string)[] = []

    if (totalPages <= 7) {
      // 少于7页，全部显示
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i)
      }
    } else {
      // 始终显示第一页
      pages.push(1)

      // 当前页附近的页码
      let startPage = Math.max(2, currentPage - 1)
      let endPage = Math.min(totalPages - 1, currentPage + 1)

      // 处理特殊情况
      if (currentPage <= 3) {
        startPage = 2
        endPage = 4
      } else if (currentPage >= totalPages - 2) {
        startPage = totalPages - 3
        endPage = totalPages - 1
      }

      // 添加省略号
      if (startPage > 2) {
        pages.push('...')
      }

      // 添加中间页码
      for (let i = startPage; i <= endPage; i++) {
        pages.push(i)
      }

      // 添加省略号
      if (endPage < totalPages - 1) {
        pages.push('...')
      }

      // 始终显示最后一页
      pages.push(totalPages)
    }

    return pages
  }

  const pageNumbers = getPageNumbers()

  return (
    <Pagination>
      <PaginationContent>
        <PaginationItem>
          <PaginationPrevious
            onClick={() => currentPage > 1 && onPageChange(currentPage - 1)}
            className={currentPage <= 1 ? 'pointer-events-none opacity-50' : ''}
            href="#"
          />
        </PaginationItem>

        {pageNumbers.map((page) => (
          <PaginationItem
            key={
              typeof page === 'number'
                ? `page-${page}`
                : `ellipsis-${page === '...' ? 'start' : 'end'}-${Math.random().toString(36).substring(2, 7)}`
            }
          >
            {typeof page === 'number' ? (
              <PaginationLink
                href="#"
                onClick={(e) => {
                  e.preventDefault()
                  onPageChange(page)
                }}
                isActive={page === currentPage}
              >
                {page}
              </PaginationLink>
            ) : (
              <span className="flex size-9 items-center justify-center">...</span>
            )}
          </PaginationItem>
        ))}

        <PaginationItem>
          <PaginationNext
            onClick={() => currentPage < totalPages && onPageChange(currentPage + 1)}
            className={currentPage >= totalPages ? 'pointer-events-none opacity-50' : ''}
            href="#"
          />
        </PaginationItem>
      </PaginationContent>
    </Pagination>
  )
}
