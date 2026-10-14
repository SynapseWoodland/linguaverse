import { useCallback, useEffect, useState, type ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useSession } from './session'
import { ApiError } from './api'

/** 数据请求三态 hook（加载 / 数据 / 错误），支持手动 reload。 */
export function useApi<T>(fn: () => Promise<T>, deps: unknown[] = []) {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(() => {
    let alive = true
    setLoading(true)
    setError(null)
    fn()
      .then((d) => alive && setData(d))
      .catch((e) => alive && setError(e instanceof ApiError ? e.message : '加载失败，请稍后重试'))
      .finally(() => alive && setLoading(false))
    return () => {
      alive = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  useEffect(() => load(), [load])

  return { data, error, loading, reload: load, setData }
}

export function Loading({ tip = '加载中…' }: { tip?: string }) {
  return (
    <div className="center" style={{ padding: '72px 0' }} role="status" aria-live="polite">
      <span className="rubber" style={{ ['--s' as string]: '54px', animation: 'lv-spin 1.6s linear infinite' }}>
        <span className="r-main">…</span>
      </span>
      <p className="muted small mt-md">{tip}</p>
    </div>
  )
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="center" style={{ padding: '72px 0' }} role="alert">
      <p style={{ color: 'var(--stamp-deep)', fontWeight: 700 }}>
        ✗ {message}
      </p>
      {onRetry && (
        <button className="btn btn-ghost mt-md" onClick={onRetry}>
          重试
        </button>
      )}
    </div>
  )
}

export function PageTitle({ zh, sub }: { zh: string; sub?: string }) {
  return (
    <div className="mt-lg">
      <h1 className="serif-cn" style={{ fontSize: 28 }}>
        {zh}
      </h1>
      {sub && <p className="muted small mt-sm">{sub}</p>}
    </div>
  )
}

/** 登录守卫：无 token 回登录页，携带 redirect 供登录后回跳原页。 */
export function RequireAuth({ children }: { children: ReactNode }) {
  const token = useSession((s) => s.token)
  const location = useLocation()
  if (!token) {
    const from = encodeURIComponent(location.pathname + location.search)
    return <Navigate to={`/login?redirect=${from}`} replace />
  }
  return <>{children}</>
}

/** 相对时间：几分钟前 / 小时前 / 天前。 */
export function timeAgo(iso: string): string {
  const t = new Date(iso).getTime()
  const diff = Math.max(0, Date.now() - t)
  const min = Math.floor(diff / 60000)
  if (min < 1) return '刚刚'
  if (min < 60) return `${min} 分钟前`
  const hr = Math.floor(min / 60)
  if (hr < 24) return `${hr} 小时前`
  const day = Math.floor(hr / 24)
  if (day < 30) return `${day} 天前`
  return new Date(iso).toLocaleDateString('zh-CN')
}

export const LANG_LABEL: Record<string, string> = { en: '英语', ja: '日语', ko: '韩语' }
