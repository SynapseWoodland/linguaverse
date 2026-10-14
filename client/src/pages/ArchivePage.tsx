import { Link } from 'react-router-dom'
import { groupPages, TOTAL_VIEWS } from '../lib/routes'
import MiniPhase from '../components/MiniPhase'

/** 原型索引页：分组列出全部视图，计数 = 52 页面 + 本索引 = 53 */
export default function ArchivePage() {
  return (
    <div className="wrap" style={{ maxWidth: 980 }}>
      <header className="row-between" style={{ padding: '34px 0 8px' }}>
        <div>
          <div className="brand" style={{ fontSize: 26 }}>
            <span className="brand-mark">L</span>LinguaVerse
          </div>
          <p className="muted small mt-md">多语种在线学习平台 · React 工程视图索引 · 共 {TOTAL_VIEWS} 个视图</p>
        </div>
        <Link className="btn btn-primary" to="/">
          从落地页开始 →
        </Link>
      </header>

      <div className="card mt-lg" style={{ background: 'var(--ink)', color: 'var(--paper)', border: 0 }}>
        <div className="row-between" style={{ flexWrap: 'wrap', gap: 16 }}>
          <div>
            <div className="display" style={{ fontSize: 24 }}>
              设计概念：语言护照 The Linguistic Passport
            </div>
            <p className="muted small mt-md" style={{ maxWidth: 520 }}>
              每节课是旅途一站，每个词是一枚印章，每完成一级，护照上多一枚签证。色：亚麻纸 · 蓝墨黑 · 护照靛蓝 ·
              邮戳朱红 · 签证烫金。
            </p>
          </div>
          <div className="row gap-md">
            <MiniPhase phase="m" />
            <MiniPhase phase="1" />
            <MiniPhase phase="2" />
          </div>
        </div>
      </div>

      {groupPages().map(([group, pages]) => (
        <section className="archive-group" key={group}>
          <h2 className="serif-cn">{group}</h2>
          <div className="mt-md">
            {pages.map((p) => (
              <Link className="archive-item" to={p.path} key={p.path}>
                <span className="pid">{p.pid}</span>
                <span style={{ flex: 1 }}>
                  <span className="pname">{p.title}</span>
                  {p.desc && (
                    <>
                      <br />
                      <span className="pdesc">{p.desc}</span>
                    </>
                  )}
                </span>
                <MiniPhase phase={p.phase} />
              </Link>
            ))}
          </div>
        </section>
      ))}

      <p className="muted small center mt-xl" style={{ paddingBottom: 40 }}>
        React 工程骨架 · 视图逐批迁移中 · 页面右下角角标标识交付阶段
      </p>
    </div>
  )
}
