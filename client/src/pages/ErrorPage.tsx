import { Link } from 'react-router-dom'
import type { PageMeta } from '../lib/routes'
import PhaseBadge from '../components/PhaseBadge'
import { toast } from '../lib/toast'

/** P48 断网 / 服务异常：重试按钮态 */
export default function ErrorPage({ meta }: { meta: PageMeta }) {
  return (
    <>
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px 20px', textAlign: 'center' }}>
        <div style={{ maxWidth: 440 }}>
          <div style={{ fontSize: 54 }}>✉︎</div>
          <h1 className="serif-cn mt-md" style={{ fontSize: 27 }}>
            暂时连不上学习服务器
          </h1>
          <p className="muted mt-md">
            你的学习记录都保存在服务器上，不会因为这次断线丢失。请检查网络后重试；如果多次出现同样问题，可以稍后再来。
          </p>
          <div className="card mt-lg small" style={{ textAlign: 'left' }}>
            <div className="kv">
              <b>错误代码</b>
              <span className="mono">ERR_NETWORK_UNREACHABLE</span>
            </div>
            <div className="kv">
              <b>发生时间</b>
              <span>2026-10-13 14:22</span>
            </div>
            <div className="kv">
              <b>建议</b>
              <span>检查网络 / 稍后重试</span>
            </div>
          </div>
          <div className="row gap-md mt-xl" style={{ justifyContent: 'center' }}>
            <button className="btn btn-stamp btn-lg" onClick={() => toast('正在重新连接…')}>
              重新连接
            </button>
            <Link className="btn btn-ghost btn-lg" to="/home">
              先回首页
            </Link>
          </div>
        </div>
      </div>
      <PhaseBadge phase={meta.phase} />
    </>
  )
}
