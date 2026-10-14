import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { apiFetch, API_BASE, ApiError } from './api'
import { useSession, type SessionUser } from './session'

const user: SessionUser = {
  id: 9,
  username: 'tester',
  nickname: '测试员',
  avatarSeed: 's',
  dailyGoalXp: 30,
  streak: 1,
}

/** jsdom 中替换 window.location（含 pathname / assign spy），测完还原原型描述符。 */
const originalLocationDesc = Object.getOwnPropertyDescriptor(window, 'location') ??
  Object.getOwnPropertyDescriptor(Window.prototype, 'location')!

function mockLocation(pathname: string) {
  const assign = vi.fn()
  const replacement: Location = { ...window.location, pathname, assign } as Location
  Object.defineProperty(window, 'location', {
    configurable: true,
    writable: true,
    value: replacement,
  })
  return assign
}

function setOnline(online: boolean) {
  Object.defineProperty(navigator, 'onLine', { configurable: true, value: online })
}

function jsonResponse(status: number, body: unknown): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: () => Promise.resolve(body),
  } as unknown as Response
}

describe('apiFetch 统一请求基座', () => {
  beforeEach(() => {
    localStorage.clear()
    useSession.getState().clear()
    vi.stubGlobal('fetch', vi.fn())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    Object.defineProperty(Window.prototype, 'location', originalLocationDesc)
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    localStorage.clear()
    useSession.getState().clear()
  })

  it('成功信封 code=0：返回 data，并携带 Bearer token 与 JSON Content-Type', async () => {
    useSession.getState().setSession('jwt-xyz', user)
    vi.mocked(fetch).mockResolvedValue(jsonResponse(200, { code: 0, message: 'ok', data: { hello: 'world' } }))

    const data = await apiFetch<{ hello: string }>('/ping')

    expect(data).toEqual({ hello: 'world' })
    expect(fetch).toHaveBeenCalledWith(
      `${API_BASE}/ping`,
      expect.objectContaining({
        headers: expect.objectContaining({
          'Content-Type': 'application/json',
          Authorization: 'Bearer jwt-xyz',
        }),
      }),
    )
  })

  it('无 token 时不发送 Authorization 头', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse(200, { code: 0, message: 'ok', data: null }))
    await apiFetch('/health')
    const headers = vi.mocked(fetch).mock.calls[0][1]!.headers as Record<string, string>
    expect(headers).not.toHaveProperty('Authorization')
  })

  it('断网（fetch reject 且 onLine=false）→ ApiError(0, 网络连接已断开...)', async () => {
    setOnline(false)
    vi.mocked(fetch).mockRejectedValue(new TypeError('Failed to fetch'))
    await expect(apiFetch('/x')).rejects.toMatchObject({ code: 0, message: expect.stringContaining('网络连接已断开') })
  })

  it('在线但服务器不可达 → ApiError(0, 无法连接服务器...)', async () => {
    setOnline(true)
    vi.mocked(fetch).mockRejectedValue(new TypeError('Failed to fetch'))
    await expect(apiFetch('/x')).rejects.toBeInstanceOf(ApiError)
    await expect(apiFetch('/x')).rejects.toMatchObject({ code: 0, message: expect.stringContaining('无法连接服务器') })
  })

  it('401（非登录页）：清空会话并跳转 /login?redirect=原路径，抛 ApiError(401)', async () => {
    useSession.getState().setSession('expired', user)
    const assign = mockLocation('/home')
    vi.mocked(fetch).mockResolvedValue(jsonResponse(401, { code: 401, message: '请先登录', data: null }))

    await expect(apiFetch('/me')).rejects.toMatchObject({ code: 401 })
    expect(useSession.getState().token).toBeNull()
    expect(assign).toHaveBeenCalledWith('/login?redirect=%2Fhome')
  })

  it('401 但当前就在 /login：不跳转，避免登录页循环', async () => {
    const assign = mockLocation('/login')
    vi.mocked(fetch).mockResolvedValue(jsonResponse(401, { code: 401, message: '请先登录', data: null }))

    await expect(apiFetch('/me')).rejects.toMatchObject({ code: 401 })
    expect(assign).not.toHaveBeenCalled()
  })

  it('响应体非 JSON → ApiError(状态码, 服务器响应异常)', async () => {
    vi.mocked(fetch).mockResolvedValue({
      ok: false,
      status: 502,
      json: () => Promise.reject(new SyntaxError('Unexpected token')),
    } as unknown as Response)
    await expect(apiFetch('/x')).rejects.toMatchObject({
      code: 502,
      message: expect.stringContaining('服务器响应异常'),
    })
  })

  it('业务错误信封 code!=0 → 抛 ApiError(业务码, 后端 message)', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse(200, { code: 409, message: '用户名已存在', data: null }))
    await expect(apiFetch('/auth/register')).rejects.toMatchObject({ code: 409, message: '用户名已存在' })
  })
})
