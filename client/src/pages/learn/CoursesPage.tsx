import { useNavigate } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import type { Language } from '../../lib/sdk'
import { sdk } from '../../lib/sdk'
import { useApi, Loading, ErrorState, PageTitle } from '../../lib/ui'
import { toast } from '../../lib/toast'
import { useState } from 'react'

/** P09 课程中心：三语种卡片，选学/进入课程树；L4–L6 coming soon。 */
export default function CoursesPage({ meta: _meta }: { meta: PageMeta }) {
  const navigate = useNavigate()
  const { data, error, loading, reload } = useApi(() => sdk.courses(), [])
  const [busyCode, setBusyCode] = useState<string | null>(null)

  async function enroll(lang: Language) {
    setBusyCode(lang.code)
    try {
      await sdk.enroll(lang.code)
      toast(`已加入${lang.nameZh}学习`)
      navigate(`/course-map?lang=${lang.code}`)
    } catch {
      toast('操作失败，请重试')
    } finally {
      setBusyCode(null)
    }
  }

  return (
    <div className="wrap" style={{ paddingBottom: 96 }}>
      <PageTitle zh="课程中心" sub="选一个主修语种开始盖章之旅，随时可以加入新的语种。" />

      {loading && <Loading />}
      {error && <ErrorState message={error} onRetry={reload} />}

      {data && (
        <div className="grid mt-lg" style={{ gridTemplateColumns: 'repeat(auto-fill,minmax(280px,1fr))', gap: 18 }}>
          {data.map((l) => (
            <section key={l.code} className="card stamp-card">
              <div className="row-between">
                <span className="rubber ink" style={{ ['--s' as string]: '64px', color: l.color }}>
                  <span className="r-main">{l.code.toUpperCase()}</span>
                  <span className="r-date">{l.nameNative}</span>
                </span>
                {l.enrolled && (
                  <span className="tiny" style={{ color: 'var(--stamp)', fontWeight: 700 }}>
                    ✓ 学习中{l.placementLevel ? ` · L${l.placementLevel}` : ''}
                  </span>
                )}
              </div>
              <h3 className="serif-cn mt-md" style={{ fontSize: 20 }}>
                {l.nameZh}
              </h3>
              <p className="small muted mt-sm">
                {l.levels.filter((x) => x.released).length} 个级别已开放 · 共{' '}
                {l.levels.reduce((n, lv) => n + lv.units.reduce((m, u) => m + u.lessons.length, 0), 0)} 课时
              </p>
              <div className="row gap-sm mt-md">
                {l.enrolled ? (
                  <button className="btn btn-primary" onClick={() => navigate(`/course-map?lang=${l.code}`)}>
                    继续学习
                  </button>
                ) : (
                  <button className="btn btn-stamp" disabled={busyCode === l.code} onClick={() => enroll(l)}>
                    {busyCode === l.code ? '加入中…' : '加入学习'}
                  </button>
                )}
                <button className="btn btn-ghost" onClick={() => navigate(`/course-map?lang=${l.code}`)}>
                  预览地图
                </button>
              </div>
            </section>
          ))}
        </div>
      )}
    </div>
  )
}
