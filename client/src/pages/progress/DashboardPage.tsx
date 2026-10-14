import { Link } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import { sdk } from '../../lib/sdk'
import { useApi, Loading, ErrorState, PageTitle, LANG_LABEL } from '../../lib/ui'

/** P26 进度仪表盘：等级、周 XP 柱、语种进度、四模块、词汇分布、每日目标入口。 */
export default function DashboardPage({ meta: _meta }: { meta: PageMeta }) {
  const { data, error, loading, reload } = useApi(() => sdk.dashboard(), [])

  return (
    <div className="wrap" style={{ paddingBottom: 96 }}>
      <PageTitle zh="进度仪表盘" sub="你的全部学习数据，实时聚合。" />

      {loading && <Loading />}
      {error && <ErrorState message={error} onRetry={reload} />}

      {data && (
        <div style={{ display: 'grid', gap: 18, marginTop: 20 }}>
          {/* 等级 + streak + 今日 */}
          <div className="grid" style={{ gridTemplateColumns: 'repeat(auto-fit,minmax(200px,1fr))', gap: 14 }}>
            <section className="card stamp-card center">
              <p className="tiny muted">等级</p>
              <p className="display serif-cn" style={{ fontSize: 40 }}>
                Lv{data.level.level}
              </p>
              <p className="tiny muted mt-sm">
                {data.level.xpIntoLevel}/{data.level.xpForNext} XP
              </p>
              <div style={{ width: '100%', height: 6, background: 'var(--line)', borderRadius: 3, marginTop: 8 }}>
                <div style={{ width: `${data.level.progressPercent}%`, height: '100%', background: 'var(--stamp)', borderRadius: 3 }} />
              </div>
              <p className="tiny muted mt-sm">累计 {data.level.totalXp} XP</p>
            </section>
            <section className="card center">
              <p className="tiny muted">连续学习</p>
              <p className="display serif-cn" style={{ fontSize: 40 }}>
                🔥 {data.streak}
              </p>
              <p className="tiny muted mt-sm">天</p>
            </section>
            <section className="card center">
              <p className="tiny muted">今日</p>
              <p className="display serif-cn" style={{ fontSize: 40 }}>
                {data.today.xp}
                <span className="muted" style={{ fontSize: 16 }}>/{data.today.goalXp}</span>
              </p>
              <p className="tiny muted mt-sm">
                {data.today.minutes} 分钟 · {data.today.completedLessons} 课
                {data.today.goalReached ? ' · 🎉 已达标' : ''}
              </p>
            </section>
            <section className="card center">
              <p className="tiny muted">词汇</p>
              <p className="display serif-cn" style={{ fontSize: 40 }}>
                {data.vocab.mastered}
              </p>
              <p className="tiny muted mt-sm">
                已掌握 · 学习中 {data.vocab.learning} · 到期 {data.vocab.dueToday}
              </p>
            </section>
          </div>

          {/* 近 7 天 XP 柱 */}
          <section className="card">
            <p className="small" style={{ fontWeight: 700 }}>
              近 7 天 XP
            </p>
            <div style={{ display: 'flex', gap: 10, alignItems: 'flex-end', height: 120, marginTop: 16 }}>
              {data.weekXp.map((d) => {
                const max = Math.max(1, ...data.weekXp.map((x) => x.xp))
                return (
                  <div key={d.date} style={{ flex: 1, textAlign: 'center' }}>
                    <span className="tiny muted">{d.xp}</span>
                    <div
                      style={{
                        height: `${Math.max(3, (d.xp / max) * 80)}px`,
                        background: 'var(--stamp)',
                        opacity: 0.75,
                        borderRadius: '4px 4px 0 0',
                        margin: '4px 0',
                      }}
                    />
                    <span className="tiny muted">{d.date.slice(5)}</span>
                  </div>
                )
              })}
            </div>
          </section>

          {/* 语种进度 */}
          <section className="card">
            <p className="small" style={{ fontWeight: 700 }}>
              语种进度
            </p>
            <div style={{ display: 'grid', gap: 14, marginTop: 12 }}>
              {data.languages.length === 0 && <p className="small muted">还没有选学语种，去课程中心加入吧。</p>}
              {data.languages.map((l) => (
                <div key={l.langCode}>
                  <div className="row-between small">
                    <span>
                      {LANG_LABEL[l.langCode] ?? l.langCode}
                      {l.placementLevel ? ` · 定级 L${l.placementLevel}` : ''}
                    </span>
                    <span className="muted">
                      {l.completedLessons}/{l.totalLessons} 课（{l.percent}%）
                      {l.avgAccuracy != null && ` · 均分 ${l.avgAccuracy}`}
                    </span>
                  </div>
                  <div style={{ height: 8, background: 'var(--line)', borderRadius: 4, marginTop: 6 }}>
                    <div style={{ width: `${l.percent}%`, height: '100%', background: l.color, borderRadius: 4 }} />
                  </div>
                </div>
              ))}
            </div>
          </section>

          {/* 四模块均分 */}
          <section className="card">
            <p className="small" style={{ fontWeight: 700 }}>
              四模块均分
            </p>
            <div className="grid mt-md" style={{ gridTemplateColumns: 'repeat(4,1fr)', gap: 10 }}>
              {data.modules.map((m) => (
                <div key={m.code} className="center" style={{ border: '1px solid var(--line)', borderRadius: 12, padding: '14px 6px' }}>
                  <p className="display serif-cn" style={{ fontSize: 26, color: (m.avgScore ?? 0) < 70 && m.attempts > 0 ? '#c2452f' : undefined }}>
                    {m.avgScore ?? '—'}
                  </p>
                  <p className="tiny muted mt-sm">
                    {m.labelZh} · {m.attempts} 次
                  </p>
                </div>
              ))}
            </div>
          </section>

          <div className="row gap-md">
            <Link className="btn btn-stamp" to="/achievements">
              成就墙
            </Link>
            <Link className="btn btn-ghost" to="/profile">
              资料与每日目标
            </Link>
          </div>
        </div>
      )}
    </div>
  )
}
