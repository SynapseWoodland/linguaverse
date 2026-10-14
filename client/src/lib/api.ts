import { useSession } from './session'

/**
 * 统一 API 基座：接口签名对齐 server/CONTRACT.md。
 * mock → 真实 API 切换仅改数据层实现，页面组件不感知。
 */
export const API_BASE: string = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080/api'

export interface R<T> {
  code: number
  message: string
  data: T
}

export class ApiError extends Error {
  constructor(
    public code: number,
    message: string,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

export async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = useSession.getState().token
  let res: Response
  try {
    res = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    })
  } catch {
    // 断网 / CORS / 服务器不可达：fetch 直接 reject，给用户可读的错误
    throw new ApiError(0, navigator.onLine ? '无法连接服务器，请稍后重试' : '网络连接已断开，请检查网络后重试')
  }

  // 401 拦截：清会话并回跳登录页（登录/注册页除外，避免循环），携带 redirect 供登录后回跳原页
  if (res.status === 401) {
    useSession.getState().clear()
    const pathname = window.location.pathname
    if (pathname !== '/login' && pathname !== '/register') {
      const from = encodeURIComponent(pathname + window.location.search)
      window.location.assign(`/login?redirect=${from}`)
    }
    throw new ApiError(401, '未登录或登录已过期')
  }

  let body: R<T>
  try {
    body = (await res.json()) as R<T>
  } catch {
    throw new ApiError(res.status, '服务器响应异常，请稍后重试')
  }
  if (!res.ok || body.code !== 0) {
    throw new ApiError(body.code || res.status, body.message || '请求失败')
  }
  return body.data
}
