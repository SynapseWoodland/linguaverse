import { Link, useSearchParams } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import { sdk, type LessonNode } from '../../lib/sdk'
import { useApi, Loading, ErrorState, LANG_LABEL } from '../../lib/ui'

/** P10 课程地图：级别 → 单元 → 课时站点三态（completed/unlocked/locked）。 */
export default function CourseMapPage({ meta: _meta }: { meta: PageMeta }) {
  const [params] = useSearchParams()
  const lang = params.get('lang') ?? 'en'
  const { data, error, loading, reload } = useApi(() => sdk.courseTree(lang), [lang])

  return (
    <div className="wrap" style={{ paddingBottom: 96 }}>
      <div className="row-between mt-lg">
        <div>
          <p className="tiny muted" style={{ letterSpacing: '.2em' }}>
            COURSE MAP
          </p>
          <h1 className="serif-cn" style={{ fontSize: 28 }}>
            {LANG_LABEL[lang]}课程地图
          </h1>
        </div>
        <Link className="btn btn-ghost" to="/courses">
          切换语种
        </Link>
      </div>

      {loading && <Loading />}
      {error && <ErrorState message={error} onRetry={reload} />}

      {data && (
        <div style={{ marginTop: 24, display: 'grid', gap: 28 }}>
          {data.levels.map((lv) => (
            <section key={lv.levelNo} className={lv.released ? '' : 'is-locked-level'}>
              <div className="row-between">
                <h2 className="serif-cn" style={{ fontSize: 20, opacity: lv.released ? 1 : 0.5 }}>
                  L{lv.levelNo} · {lv.title}
                </h2>
                {!lv.released && (
                  <span className="tiny" style={{ border: '1px dashed var(--line)', borderRadius: 999, padding: '2px 10px', color: 'var(--muted)' }}>
                    COMING SOON
                  </span>
                )}
              </div>
              {lv.units.map((u) => (
                <div key={u.unitNo} className="card mt-md" style={{ opacity: lv.released ? 1 : 0.55 }}>
                  <p className="small" style={{ fontWeight: 700 }}>
                    单元 {u.unitNo} · {u.title}
                    <span className="muted" style={{ fontWeight: 400 }}>
                      {' '}
                      / {u.titleNative}
                    </span>
                  </p>
                  <div className="route mt-md" style={{ flexWrap: 'wrap', justifyContent: 'flex-start' }}>
                    {u.lessons.map((lesson) => (
                      <LessonStop key={lesson.id} lesson={lesson} />
                    ))}
                  </div>
                </div>
              ))}
            </section>
          ))}
        </div>
      )}
    </div>
  )
}

function LessonStop({ lesson }: { lesson: LessonNode }) {
  const cls = lesson.completed ? 'done' : lesson.unlocked ? 'current' : ''
  const inner = (
    <>
      <span className={`dot ${cls}`} style={{ width: 40, height: 40 }}>
        {lesson.completed ? '✓' : lesson.unlocked ? lesson.lessonNo : '🔒'}
      </span>
      <span className="tiny" style={{ maxWidth: 96, textAlign: 'center' }}>
        {lesson.title}
      </span>
    </>
  )
  if (!lesson.unlocked) {
    return (
      <span className="stop" style={{ opacity: 0.45 }}>
        {inner}
      </span>
    )
  }
  return (
    <Link className="stop" to={`/lesson?id=${lesson.id}`}>
      {inner}
    </Link>
  )
}
