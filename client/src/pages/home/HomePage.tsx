import { Link } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import { sdk, followDeeplink } from '../../lib/sdk'
import { useApi, Loading, ErrorState } from '../../lib/ui'
import { useSession } from '../../lib/session'

/** P06 学习首页：今日目标环 + streak + 规则引擎推荐卡片。 */
export default function HomePage({ meta: _meta }: { meta: PageMeta }) {
  const user = useSession((s) => s.user)
  const dash = useApi(() => sdk.dashboard(), [])
  const plan = useApi(() => sdk.todayPlan(), [])

  const today = dash.data?.today
  const goalPct = today ? Math.min(100, Math.round((today.xp / today.goalXp) * 100)) : 0

  return (
    <div className="wrap" style={{ paddingBottom: 96 }}>
      <div className="row-between mt-lg">
        <div>
          <p className="tiny muted" style={{ letterSpacing: '.2em' }}>
            TODAY · {plan.data?.date}
          </p>
          <h1 className="serif-cn" style={{ fontSize: 30 }}>
            你好，{user?.nickname ?? user?.username}
          </h1>
        </div>
        <div className="center">
          <div
            className="center"
            style={{
              width: 72,
              height: 72,
              borderRadius: '50%',
              border: '3px solid var(--stamp)',
              display: 'flex',
              flexDirection: 'column',
            }}
          >
            <strong style={{ fontSize: 20, lineHeight: 1 }}>{dash.data?.streak ?? 0}</strong>
            <span className="tiny muted">天 streak</span>
          </div>
        </div>
      </div>

      {dash.loading && <Loading />}
      {dash.error && <ErrorState message={dash.error} onRetry={dash.reload} />}

      {today && (
        <section className="card stamp-card mt-lg">
          <div className="row-between">
            <div>
              <p className="small muted">今日目标</p>
              <p className="display serif-cn mt-sm" style={{ fontSize: 34 }}>
                {today.xp}
                <span className="muted" style={{ fontSize: 16 }}> / {today.goalXp} XP</span>
              </p>
            </div>
            <div className="center" style={{ position: 'relative', width: 96, height: 96 }}>
              <svg width="96" height="96" viewBox="0 0 96 96">
                <circle cx="48" cy="48" r="40" fill="none" stroke="var(--line)" strokeWidth="8" />
                <circle
                  cx="48"
                  cy="48"
                  r="40"
                  fill="none"
                  stroke="var(--stamp)"
                  strokeWidth="8"
                  strokeLinecap="round"
                  strokeDasharray={`${(goalPct / 100) * 251.2} 251.2`}
                  transform="rotate(-90 48 48)"
                />
              </svg>
              <span className="small" style={{ position: 'absolute', fontWeight: 700 }}>
                {goalPct}%
              </span>
            </div>
          </div>
          <div className="row gap-md mt-md small muted">
            <span>⏱ {today.minutes} 分钟</span>
            <span>✦ {today.completedLessons} 节课</span>
            <span>{today.goalReached ? '🎉 今日目标已达成' : `还差 ${today.remainingXp} XP 达标`}</span>
          </div>
        </section>
      )}

      <h2 className="serif-cn mt-xl" style={{ fontSize: 20 }}>
        今日计划
      </h2>
      {plan.loading && <Loading tip="正在为你规划…" />}
      {plan.error && <ErrorState message={plan.error} onRetry={plan.reload} />}
      {plan.data && (
        <>
          <p className="small muted mt-sm">{plan.data.message}</p>
          <div className="grid mt-md" style={{ gridTemplateColumns: 'repeat(auto-fill,minmax(260px,1fr))', gap: 14 }}>
            {plan.data.recommendations.map((r) => (
              <Link key={r.key} className="card stamp-card" to={followDeeplink(r.deeplink)}>
                <span className="tiny" style={{ color: 'var(--stamp)', fontWeight: 700, letterSpacing: '.15em' }}>
                  {r.type}
                </span>
                <h3 className="serif-cn mt-sm" style={{ fontSize: 18 }}>
                  {r.title}
                </h3>
                <p className="small muted mt-sm" style={{ lineHeight: 1.7 }}>
                  {r.reason}
                </p>
                <span className="btn btn-stamp mt-md" style={{ display: 'inline-block' }}>
                  {r.ctaText} →
                </span>
              </Link>
            ))}
          </div>
        </>
      )}

      <div className="row gap-md mt-xl">
        <Link className="btn btn-ghost" to="/dashboard">
          查看进度仪表盘
        </Link>
        <Link className="btn btn-ghost" to="/community">
          去社区逛逛
        </Link>
      </div>
    </div>
  )
}
