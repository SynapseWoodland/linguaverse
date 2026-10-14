import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import type { PageMeta } from '../lib/routes'
import PhaseBadge from '../components/PhaseBadge'
import { sdk } from '../lib/sdk'
import { ApiError } from '../lib/api'
import { useSession } from '../lib/session'

/** 仅允许站内相对路径回跳，拒绝协议相对（//host）与外链，防开放重定向。 */
function safeRedirect(raw: string | null): string | null {
  if (!raw) return null
  if (!raw.startsWith('/') || raw.startsWith('//')) return null
  return raw
}

/** P03 登录：接 POST /auth/login，错误态/锁定提示由服务端消息驱动。 */
export default function LoginPage({ meta }: { meta: PageMeta }) {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const setSession = useSession((s) => s.setSession)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function submit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setBusy(true)
    try {
      const res = await sdk.login(username.trim(), password)
      setSession(res.token, res.user)
      const target = safeRedirect(params.get('redirect'))
      if (res.user.onboarded === false) {
        navigate('/onboarding', { state: { from: target } })
      } else {
        navigate(target ?? '/home')
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '登录失败，请稍后重试')
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px 20px' }}>
        <div style={{ width: '100%', maxWidth: 420 }}>
          <div className="center">
            <Link className="brand" style={{ justifyContent: 'center', fontSize: 24 }} to="/">
              <span className="brand-mark">L</span>LinguaVerse
            </Link>
            <h1 className="serif-cn mt-lg" style={{ fontSize: 26 }}>
              欢迎回到你的旅程
            </h1>
            <p className="muted small mt-md">登录后继续你的语言护照。</p>
          </div>

          {error && (
            <div className="card mt-lg" style={{ borderColor: 'rgba(194,69,47,.4)', background: 'rgba(194,69,47,.05)' }}>
              <p className="small text-stamp" style={{ fontWeight: 600 }}>
                ✗ {error}
              </p>
            </div>
          )}

          <form className="card mt-lg" onSubmit={submit}>
            <div className="field">
              <label htmlFor="login-username">用户名</label>
              <input
                id="login-username"
                className="input"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
                placeholder="演示账号 demo"
                required
              />
            </div>
            <div className="field">
              <label htmlFor="login-password">密码</label>
              <input
                id="login-password"
                className="input"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                placeholder="demo1234"
                required
              />
            </div>
            <button className="btn btn-primary btn-block btn-lg" type="submit" disabled={busy}>
              {busy ? '登录中…' : '登录，继续学习'}
            </button>
          </form>

          <p className="small muted center mt-lg">
            还没有护照？
            <Link to="/register" style={{ color: 'var(--stamp)', fontWeight: 600 }}>
              免费注册
            </Link>
          </p>
        </div>
      </div>
      <PhaseBadge phase={meta.phase} />
    </>
  )
}
