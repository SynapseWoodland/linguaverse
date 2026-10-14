import { useToast } from '../lib/toast'

/** 全局 toast 渲染器（.toast 单例，挂在 RouterProvider 旁，全站可用） */
export default function Toaster() {
  const msg = useToast((s) => s.msg)
  return (
    <div className="toast" style={{ display: msg ? 'block' : 'none' }} role="status" aria-live="polite">
      {msg}
    </div>
  )
}
