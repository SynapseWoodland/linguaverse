import type { PageMeta } from '../../lib/routes'
import { sdk } from '../../lib/sdk'
import { useApi, Loading, ErrorState, PageTitle } from '../../lib/ui'

/** P27 成就墙：8 枚邮戳徽章，未解锁显示 current/target 进度。 */
export default function AchievementsPage({ meta: _meta }: { meta: PageMeta }) {
  const { data, error, loading, reload } = useApi(() => sdk.achievements(), [])
  const unlockedCount = data?.filter((a) => a.unlocked).length ?? 0

  return (
    <div className="wrap" style={{ paddingBottom: 96 }}>
      <PageTitle
        zh="成就墙"
        sub={data ? `已收集 ${unlockedCount}/${data.length} 枚邮戳` : '每一枚邮戳都是旅程上的印记。'}
      />

      {loading && <Loading />}
      {error && <ErrorState message={error} onRetry={reload} />}

      {data && (
        <div className="grid mt-lg" style={{ gridTemplateColumns: 'repeat(auto-fill,minmax(240px,1fr))', gap: 16 }}>
          {data.map((a) => {
            const pct = Math.min(100, Math.round((a.current / a.target) * 100))
            return (
              <section
                key={a.code}
                className="card stamp-card center"
                style={a.unlocked ? { borderColor: 'rgba(194,69,47,.55)', background: 'rgba(194,69,47,.04)' } : { opacity: 0.92 }}
              >
                <div
                  className={`rubber ${a.unlocked ? 'stamp-anim' : ''}`}
                  style={{
                    ['--s' as string]: '86px',
                    color: a.unlocked ? 'var(--stamp)' : 'var(--muted)',
                    filter: a.unlocked ? undefined : 'grayscale(1)',
                  }}
                >
                  <span className="r-main">{a.unlocked ? '★' : a.icon.slice(0, 2).toUpperCase()}</span>
                  <span className="r-date">{a.unlocked ? 'GOT' : 'LOCKED'}</span>
                </div>
                <h3 className="serif-cn mt-md" style={{ fontSize: 18 }}>
                  {a.nameZh}
                </h3>
                <p className="tiny muted mt-sm">{a.descZh}</p>
                {a.unlocked ? (
                  <p className="tiny mt-md" style={{ color: 'var(--stamp)', fontWeight: 700 }}>
                    ✓ {new Date(a.unlockedAt!).toLocaleDateString('zh-CN')} 解锁
                  </p>
                ) : (
                  <div style={{ width: '100%', marginTop: 12 }}>
                    <div className="row-between tiny muted">
                      <span>
                        {a.current}/{a.target}
                      </span>
                      <span>{pct}%</span>
                    </div>
                    <div style={{ height: 5, background: 'var(--line)', borderRadius: 3, marginTop: 4 }}>
                      <div style={{ width: `${pct}%`, height: '100%', background: 'var(--stamp)', borderRadius: 3, opacity: 0.7 }} />
                    </div>
                  </div>
                )}
              </section>
            )
          })}
        </div>
      )}
    </div>
  )
}
