'use client'

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'

interface ConfirmDialogProps {
  /**
   * 对话框是否打开
   */
  open: boolean
  /**
   * 对话框状态变化回调
   */
  onOpenChange: (open: boolean) => void
  /**
   * 对话框标题
   */
  title: string
  /**
   * 对话框描述内容
   */
  description: string
  /**
   * 确认按钮文本
   */
  confirmText?: string
  /**
   * 取消按钮文本
   */
  cancelText?: string
  /**
   * 确认按钮变体样式
   * @default "destructive"
   */
  confirmVariant?: 'destructive' | 'default' | 'outline' | 'secondary' | 'ghost' | 'link'
  /**
   * 确认操作回调
   */
  onConfirm: () => void | Promise<void>
  /**
   * 是否正在执行确认操作
   */
  isConfirming?: boolean
}

/**
 * 通用确认对话框组件
 */
export function ConfirmDialog({
  open,
  onOpenChange,
  title,
  description,
  confirmText = '确认',
  cancelText = '取消',
  confirmVariant = 'destructive',
  onConfirm,
  isConfirming = false,
}: ConfirmDialogProps) {
  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent
        onCloseAutoFocus={(event) => {
          // 阻止默认的焦点处理
          event.preventDefault()
          document.body.style.pointerEvents = ''
        }}
      >
        <AlertDialogHeader>
          <AlertDialogTitle>{title}</AlertDialogTitle>
          <AlertDialogDescription>{description}</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isConfirming}>{cancelText}</AlertDialogCancel>
          <AlertDialogAction
            className={`${
              confirmVariant === 'destructive'
                ? 'bg-destructive text-destructive-foreground hover:bg-destructive/90'
                : ''
            }`}
            onClick={(e) => {
              e.preventDefault()
              onConfirm()
            }}
            disabled={isConfirming}
          >
            {confirmText}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
