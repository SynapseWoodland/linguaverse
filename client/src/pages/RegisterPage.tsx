import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { PageMeta } from '../lib/routes'
import type { CSSVars } from '../lib/css'
import PhaseBadge from '../components/PhaseBadge'
import { sdk } from '../lib/sdk'
import { ApiError } from '../lib/api'
import { useSession } from '../lib/session'

/** P02 注册：左侧深色封皮 + 右侧表单，接 POST /auth/register，成功 → onboarding。 */
export default function RegisterPage({ meta }: { meta: PageMeta }) {
  const navigate = useNavigate()
  const setSession = useSession((s) => s.setSession)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function submit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    if (username.trim().length < 2) return setError('用户名至少 2 个字符')
    if (password.length < 6) return setError('密码至少需要 6 位')
    if (password !== confirm) return setError('两次输入的密码不一致')
    setBusy(true)
    try {
      const res = await sdk.register(username.trim(), password)
      setSession(res.token, res.user)
      navigate('/onboarding')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '注册失败，请稍后重试')
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <div className="lv-auth-grid" style={{ minHeight: '100vh', display: 'grid', gridTemplateColumns: '1fr 1fr' }}>
        {/* 左侧深色封皮 */}
        <div className="cover" style={{ padding: 48, display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
          <Link className="brand" style={{ color: '#ECE8DB' }} to="/">
            <span className="brand-mark">L</span>LinguaVerse
          </Link>
          <div>
            <div className="rubber stamp-anim" style={{ '--s': '130px' } as CSSVars}>
              <span className="r-main">
                NEW
                <br />
                PASSPORT
              </span>
              <span className="r-date">2026</span>
            </div>
            <h2 className="display mt-lg" style={{ fontSize: 38, color: '#F4F1E8', maxWidth: 360 }}>
              给自己签发一本语言护照
            </h2>
            <p className="muted mt-lg" style={{ maxWidth: 340, lineHeight: 1.9 }}>
              注册后先做 5 分钟定级测试，我们会告诉你该从哪一站上车。
            </p>
          </div>
          <p className="tiny muted">
            已有护照？
            <Link to="/login" style={{ color: '#E7AEB4', textDecoration: 'underline' }}>
              直接登录
            </Link>
          </p>
        </div>

        {/* 右侧表单 */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '48px 28px' }}>
          <div style={{ width: '100%', maxWidth: 380 }}>
            <h1 className="serif-cn" style={{ fontSize: 27 }}>
              创建账户
            </h1>
            <p className="muted small mt-md">密码会以 BCrypt 加盐加密存储。</p>
            {error && (
              <div className="card mt-md" style={{ borderColor: 'rgba(194,69,47,.4)', background: 'rgba(194,69,47,.05)' }}>
                <p className="small text-stamp" style={{ fontWeight: 600 }}>
                  ✗ {error}
                </p>
              </div>
            )}
            <form className="mt-lg" onSubmit={submit}>
              <div className="field">
                <label htmlFor="reg-username">用户名</label>
                <input
                  id="reg-username"
                  className="input"
                  placeholder="2–16 位，字母或数字"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  autoComplete="username"
                  required
                />
              </div>
              <div className="field">
                <label htmlFor="reg-password">密码</label>
                <input
                  id="reg-password"
                  className="input"
                  type="password"
                  placeholder="至少 6 位"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="new-password"
                  required
                />
              </div>
              <div className="field">
                <label htmlFor="reg-confirm">再输一次密码</label>
                <input
                  id="reg-confirm"
                  className="input"
                  type="password"
                  value={confirm}
                  onChange={(e) => setConfirm(e.target.value)}
                  autoComplete="new-password"
                  required
                />
              </div>
              <button className="btn btn-stamp btn-block btn-lg" type="submit" disabled={busy}>
                {busy ? '创建中…' : '注册并开始'}
              </button>
            </form>
          </div>
        </div>
      </div>
      <PhaseBadge phase={meta.phase} />
    </>
  )
}
