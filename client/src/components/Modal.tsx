import type { ReactNode } from 'react'

interface ModalProps {
  open: boolean
  onClose: () => void
  children: ReactNode
  /** 额外挂在 .modal 上的类名 */
  className?: string
}

/** 受控弹层（对应原型 .modal-mask + .modal，点遮罩关闭） */
export default function Modal({ open, onClose, children, className }: ModalProps) {
  return (
    <div
      className={`modal-mask${open ? ' open' : ''}`}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose()
      }}
    >
      <div className={`modal${className ? ` ${className}` : ''}`} role="dialog" aria-modal="true">
        {children}
      </div>
    </div>
  )
}
