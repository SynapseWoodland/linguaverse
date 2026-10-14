import { Link } from 'react-router-dom'
import type { PageMeta } from '../lib/routes'
import MiniPhase from '../components/MiniPhase'
import PhaseBadge from '../components/PhaseBadge'

/** 视图占位页：证明路由/外壳/角标链路可用，页面本体按 Task 2.5 后续批次迁移 */
export default function PlaceholderPage({ meta }: { meta: PageMeta }) {
  return (
    <>
      <div className="wrap page" style={{ maxWidth: 720 }}>
        <div className="stamp-card center">
          <div className="display" style={{ fontSize: 44 }}>
            {meta.pid}
          </div>
          <h1 className="serif-cn" style={{ marginTop: 10 }}>
            {meta.title}
          </h1>
          {meta.desc && <p className="muted mt-md">{meta.desc}</p>}
          <div className="row mt-lg" style={{ justifyContent: 'center' }}>
            <MiniPhase phase={meta.phase} />
            <span className="chip">待迁移 · 与静态原型逐页比对中</span>
          </div>
          <p className="muted small mt-lg">
            本视图为 React 工程占位，对应静态原型
            <span className="nowrap"> prototype/pages{meta.path === '/' ? '/landing.html' : `${meta.path}.html`}</span>
          </p>
          <div className="mt-lg">
            <Link className="btn btn-ghost" to="/archive">
              ← 返回视图索引
            </Link>
          </div>
        </div>
      </div>
      <PhaseBadge phase={meta.phase} />
    </>
  )
}
