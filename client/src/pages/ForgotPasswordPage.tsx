import { Link } from 'react-router-dom'
import type { PageMeta } from '../lib/routes'
import type { CSSVars } from '../lib/css'
import PhaseBadge from '../components/PhaseBadge'
import { toast } from '../lib/toast'

/** P04 找回密码：邮箱验证三步（P1，提交 → toast 提示演示态） */
export default function ForgotPasswordPage({ meta }: { meta: PageMeta }) {
  return (
    <>
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px 20px' }}>
        <div style={{ width: '100%', maxWidth: 440 }}>
          <div className="center">
            <Link className="brand" style={{ justifyContent: 'center', fontSize: 23 }} to="/">
              <span className="brand-mark">L</span>LinguaVerse
            </Link>
          </div>
          <h1 className="serif-cn center mt-lg" style={{ fontSize: 24 }}>
            找回密码
          </h1>

          {/* 步骤指示（真实顺序：可用编号） */}
          <div className="row mt-lg" style={{ justifyContent: 'center', gap: 0 }}>
            <span className="chip active">1 验证邮箱</span>
            <span style={{ width: 34, borderTop: '2px solid var(--passport)' }} />
            <span className="chip">2 设置新密码</span>
            <span style={{ width: 34, borderTop: '2px solid var(--thread)' }} />
            <span className="chip">3 完成</span>
          </div>

          <form
            className="card mt-lg"
            onSubmit={(e) => {
              e.preventDefault()
              toast('演示原型：重置邮件已发送（P1 功能）')
            }}
          >
            <p className="small muted">输入注册时绑定的邮箱，我们会把重置链接发给你。链接 30 分钟内有效。</p>
            <div className="field mt-lg">
              <label>注册邮箱</label>
              <input className="input" type="email" placeholder="you@example.com" />
            </div>
            <button className="btn btn-primary btn-block btn-lg" type="submit">
              发送重置链接
            </button>
          </form>

          {/* 成功态（演示第二屏样式） */}
          <div className="card mt-md center hide" id="sent">
            <div className="rubber ink" style={{ '--s': '84px', margin: '0 auto' } as CSSVars}>
              <span className="r-main">SENT</span>
              <span className="r-date">EMAIL</span>
            </div>
            <p className="small mt-md">重置邮件已发送，请查收邮箱并在 30 分钟内完成重置。</p>
          </div>

          <p className="small muted center mt-lg">
            <Link to="/login">← 返回登录</Link>
          </p>
        </div>
      </div>
      <PhaseBadge phase={meta.phase} />
    </>
  )
}
