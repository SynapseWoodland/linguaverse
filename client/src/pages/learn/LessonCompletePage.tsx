import { useState } from 'react'
import { Link, Navigate, useSearchParams } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import PhaseBadge from '../../components/PhaseBadge'
import { sdk, type CompleteResult } from '../../lib/sdk'
import { ApiError } from '../../lib/api'

const ACH_NAME: Record<string, string> = {
  first_step: '初学乍练',
  streak_3: '三日不辍',
  streak_7: '一周坚持',
  lessons_10: '十课达人',
  words_50: '词汇新星',
  perfect: '满分通关',
  polyglot: '三语行者',
  social: '社区新星',
}

/** P18 课时结算：盖章时刻。durationSec 取自进入课时时记录的时间戳，结算幂等。 */
export default function LessonCompletePage({ meta }: { meta: PageMeta }) {
  const [params] = useSearchParams()
  const id = Number(params.get('id'))
  const [result, setResult] = useState<CompleteResult | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (!id) return <Navigate to="/courses" replace />

  async function stamp() {
    setBusy(true)
    setError(null)
    const start = Number(sessionStorage.getItem(`lv-start-${id}`) ?? Date.now())
    const durationSec = Math.min(14400, Math.max(0, Math.round((Date.now() - start) / 1000)))
    try {
      const r = await sdk.complete(id, durationSec)
      setResult(r)
      sessionStorage.removeItem(`lv-start-${id}`)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '结算失败，请重试')
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <div style={{ maxWidth: 560, margin: '0 auto', padding: '56px 24px 80px' }}>
        {!result && (
          <section className="card stamp-card center">
            <p className="serif-cn muted tiny" style={{ letterSpacing: '.3em' }}>
              四环节完成 · 准备盖章
            </p>
            <h1 className="display serif-cn mt-lg" style={{ fontSize: 34 }}>
              为这一站盖上邮戳
            </h1>
            <p className="muted small mt-md">结算 XP、连续学习天数与新成就，同一课时只结算一次。</p>
            {error && <p className="small text-stamp mt-md">✗ {error}</p>}
            <button className="btn btn-stamp btn-lg mt-lg" disabled={busy} onClick={stamp}>
              {busy ? '盖章中…' : '✦ 盖章结算'}
            </button>
            <Link className="small muted mt-lg" to={`/lesson?id=${id}`}>
              返回课时再看看
            </Link>
          </section>
        )}

        {result && (
          <section className="card stamp-card center">
            <div className="rubber stamp-anim" style={{ ['--s' as string]: '120px' }}>
              <span className="r-main">
                {result.accuracy}
                <br />
                SCORE
              </span>
              <span className="r-date">STAMPED</span>
            </div>
            {result.alreadyCompleted && (
              <p className="tiny muted mt-md">该课时已结算过，以下为当时的结果（未重复发放 XP）</p>
            )}

            <div className="card mt-lg" style={{ width: '100%' }}>
              <div className="row-between">
                <span className="small muted">基础 XP（词×2 + 环节×5）</span>
                <span className="small">+{result.xp.base}</span>
              </div>
              <div className="row-between mt-sm">
                <span className="small muted">准确率加成</span>
                <span className="small">+{result.xp.bonus}</span>
              </div>
              <div className="row-between mt-sm" style={{ borderTop: '1px solid var(--line)', paddingTop: 10 }}>
                <strong>本课时共获得</strong>
                <strong className="text-stamp">+{result.xp.total} XP</strong>
              </div>
              <div className="row-between mt-sm">
                <span className="small muted">连续学习</span>
                <span className="small">🔥 {result.streak} 天</span>
              </div>
              <div className="row-between mt-sm">
                <span className="small muted">学习时长</span>
                <span className="small">{Math.round(result.durationSec / 60)} 分钟</span>
              </div>
            </div>

            {result.newAchievements.length > 0 && (
              <div className="mt-lg" style={{ width: '100%' }}>
                <p className="tiny muted" style={{ letterSpacing: '.2em' }}>
                  新成就解锁
                </p>
                <div className="row gap-sm mt-sm" style={{ justifyContent: 'center', flexWrap: 'wrap' }}>
                  {result.newAchievements.map((code) => (
                    <span key={code} className="stamp-card tiny" style={{ padding: '6px 14px', color: 'var(--stamp)', fontWeight: 700 }}>
                      ★ {ACH_NAME[code] ?? code}
                    </span>
                  ))}
                </div>
              </div>
            )}

            <div className="row gap-md mt-lg" style={{ justifyContent: 'center' }}>
              {result.nextLesson ? (
                <Link className="btn btn-primary btn-lg" to={`/lesson?id=${result.nextLesson.id}`}>
                  下一课：{result.nextLesson.title}
                </Link>
              ) : (
                <Link className="btn btn-primary btn-lg" to="/dashboard">
                  去看仪表盘
                </Link>
              )}
              <Link className="btn btn-ghost" to="/home">
                回今日首页
              </Link>
            </div>
          </section>
        )}
      </div>
      <PhaseBadge phase={meta.phase} />
    </>
  )
}
