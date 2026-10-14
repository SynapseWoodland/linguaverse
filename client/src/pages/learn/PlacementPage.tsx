import { useState } from 'react'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import PhaseBadge from '../../components/PhaseBadge'
import { sdk } from '../../lib/sdk'
import { ApiError } from '../../lib/api'
import { useApi, LANG_LABEL } from '../../lib/ui'

/** P11 定级测试：10 题逐题作答，提交后服务端按 0-3/4-6/7-10 建议级别并完成 enrollment。 */
export default function PlacementPage({ meta }: { meta: PageMeta }) {
  const [params] = useSearchParams()
  const lang = params.get('lang') ?? 'en'
  const navigate = useNavigate()
  const { data: paper, error, loading, reload } = useApi(() => sdk.placementPaper(lang), [lang])
  const [idx, setIdx] = useState(0)
  const [answers, setAnswers] = useState<number[]>([])
  const [busy, setBusy] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  if (!['en', 'ja', 'ko'].includes(lang)) return <Navigate to="/courses" replace />

  async function choose(choiceIndex: number) {
    const next = [...answers, choiceIndex]
    setAnswers(next)
    if (!paper) return
    if (idx < paper.questions.length - 1) {
      setIdx(idx + 1)
      return
    }
    setBusy(true)
    setSubmitError(null)
    try {
      const result = await sdk.placementSubmit(lang, next)
      navigate('/placement-result', { state: { result } })
    } catch (e) {
      setSubmitError(e instanceof ApiError ? e.message : '提交失败，请重试')
      setBusy(false)
    }
  }

  const q = paper?.questions[idx]

  return (
    <>
      <div style={{ maxWidth: 640, margin: '0 auto', padding: '40px 24px 80px' }}>
        <Link className="brand" to="/home">
          <span className="brand-mark">L</span>LinguaVerse
        </Link>

        {loading && <p className="muted small mt-lg">试卷加载中…</p>}
        {error && (
          <div className="card mt-lg center">
            <p className="text-stamp">✗ {error}</p>
            <button className="btn btn-ghost mt-md" onClick={reload}>
              重试
            </button>
          </div>
        )}

        {paper && q && (
          <>
            <div className="row-between mt-lg">
              <p className="tiny muted" style={{ letterSpacing: '.2em' }}>
                {LANG_LABEL[lang]} · 定级测试
              </p>
              <p className="tiny muted">
                {idx + 1} / {paper.total}
              </p>
            </div>
            <div className="progress-track mt-sm" style={{ height: 4, background: 'var(--line)', borderRadius: 2 }}>
              <div
                style={{
                  width: `${((idx + 1) / paper.total) * 100}%`,
                  height: '100%',
                  background: 'var(--stamp)',
                  borderRadius: 2,
                  transition: 'width .3s',
                }}
              />
            </div>

            <section className="card mt-lg">
              <h2 className="serif-cn" style={{ fontSize: 22 }}>
                {q.stem}
              </h2>
              <div className="mt-lg" style={{ display: 'grid', gap: 12 }}>
                {q.options.map((opt, i) => (
                  <button key={i} className="card option-card" disabled={busy} onClick={() => choose(i)}
                    style={{ margin: 0, textAlign: 'left', cursor: 'pointer', padding: '14px 16px' }}>
                    <span className="tiny muted" style={{ marginRight: 10 }}>
                      {String.fromCharCode(65 + i)}
                    </span>
                    {opt}
                  </button>
                ))}
              </div>
              {idx > 0 && (
                <button className="btn btn-ghost mt-md" onClick={() => setIdx(idx - 1)}>
                  上一题
                </button>
              )}
            </section>
            {submitError && <p className="small text-stamp mt-md">✗ {submitError}</p>}
            <p className="tiny muted center mt-lg">答完自动提交，结果将决定你的建议起始级别。</p>
          </>
        )}
      </div>
      <PhaseBadge phase={meta.phase} />
    </>
  )
}
