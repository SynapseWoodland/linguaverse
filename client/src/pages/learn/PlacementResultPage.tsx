import { Link, Navigate, useLocation } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import PhaseBadge from '../../components/PhaseBadge'
import type { PlacementResultData } from '../../lib/sdk'
import { LANG_LABEL } from '../../lib/ui'

/** P12 定级结果：对题数 + 建议级别 + 进入课程树。 */
export default function PlacementResultPage({ meta }: { meta: PageMeta }) {
  const location = useLocation()
  const result = (location.state as { result?: PlacementResultData } | null)?.result

  if (!result) return <Navigate to="/courses" replace />

  return (
    <>
      <div style={{ maxWidth: 560, margin: '0 auto', padding: '72px 24px' }}>
        <section className="card stamp-card center">
          <p className="serif-cn muted tiny" style={{ letterSpacing: '.3em' }}>
            {LANG_LABEL[result.langCode]} · 定级完成
          </p>
          <div className="rubber stamp-anim ink mt-lg" style={{ ['--s' as string]: '110px' }}>
            <span className="r-main">L{result.suggestedLevel}</span>
            <span className="r-date">START</span>
          </div>
          <h1 className="serif-cn mt-lg" style={{ fontSize: 28 }}>
            建议你从 L{result.suggestedLevel} 开始
          </h1>
          <p className="muted mt-md">
            10 题中答对 <strong className="text-stamp">{result.correctCount}</strong> 题（0–3 → L1，4–6 → L2，7–10 → L3）。
          </p>
          <div className="row gap-md mt-lg" style={{ justifyContent: 'center' }}>
            <Link className="btn btn-primary btn-lg" to={`/course-map?lang=${result.langCode}`}>
              查看课程地图
            </Link>
            <Link className="btn btn-ghost" to="/home">
              先回首页
            </Link>
          </div>
        </section>
      </div>
      <PhaseBadge phase={meta.phase} />
    </>
  )
}
