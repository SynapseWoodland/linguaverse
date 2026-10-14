import { useState } from 'react'
import { Link, Navigate, useSearchParams } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import PhaseBadge from '../../components/PhaseBadge'
import { sdk, type ListeningResult } from '../../lib/sdk'
import { ApiError } from '../../lib/api'
import { useApi, Loading, ErrorState } from '../../lib/ui'
import { speak } from '../../lib/speech'

/** P16 听力：对话稿 + 朗读（浏览器 speechSynthesis）+ 单选判题。 */
export default function ListeningPage({ meta }: { meta: PageMeta }) {
  const [params] = useSearchParams()
  const id = Number(params.get('id'))
  const { data: lesson, error, loading, reload } = useApi(() => sdk.lesson(id), [id])
  const [picked, setPicked] = useState<number | null>(null)
  const [result, setResult] = useState<ListeningResult | null>(null)
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)
  const [showScript, setShowScript] = useState(false)

  if (!id) return <Navigate to="/courses" replace />

  function playDialog() {
    if (!lesson) return
    speak(lesson.listening.lines.map((l) => l.text).join('. '), lesson.langCode, 0.92)
  }

  async function submit() {
    if (picked == null) return
    setBusy(true)
    setErr(null)
    try {
      const r = await sdk.listeningSubmit(id, picked)
      setResult(r)
      await sdk.stageProgress(id, 'LISTENING', r.score)
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : '提交失败')
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <div style={{ maxWidth: 720, margin: '0 auto', padding: '32px 24px 80px' }}>
        <Link className="small muted" to={`/lesson?id=${id}`}>
          ← 返回课时
        </Link>
        {loading && <Loading />}
        {error && <ErrorState message={error} onRetry={reload} />}

        {lesson && (
          <>
            <h1 className="serif-cn mt-md" style={{ fontSize: 26 }}>
              ③ 听力训练 · {lesson.listening.title}
            </h1>

            <section className="card mt-lg center">
              <button className="btn btn-primary btn-lg" onClick={playDialog}>
                ▶ 播放对话
              </button>
              <button className="btn btn-ghost mt-md" onClick={() => setShowScript((v) => !v)}>
                {showScript ? '隐藏对话稿' : '查看对话稿（降级）'}
              </button>
              {showScript && (
                <div className="mt-lg" style={{ textAlign: 'left', width: '100%', display: 'grid', gap: 10 }}>
                  {lesson.listening.lines.map((l, i) => (
                    <div key={i} className="small">
                      <span className="tiny muted" style={{ marginRight: 8 }}>
                        {l.speaker}
                      </span>
                      {l.text}
                      <span className="tiny muted"> （{l.zh}）</span>
                    </div>
                  ))}
                </div>
              )}
            </section>

            <section className="card mt-lg">
              <p className="small" style={{ fontWeight: 700 }}>
                {lesson.listening.questionStem}
              </p>
              <div className="mt-md" style={{ display: 'grid', gap: 8 }}>
                {lesson.listening.options.map((opt, oi) => {
                  const isCorrect = result && oi === result.correctIndex
                  const isWrongPick = result && picked === oi && !result.correct
                  return (
                    <button
                      key={oi}
                      disabled={!!result || busy}
                      onClick={() => setPicked(oi)}
                      aria-pressed={picked === oi}
                      className="card"
                      style={{
                        margin: 0,
                        textAlign: 'left',
                        padding: '10px 14px',
                        cursor: result ? 'default' : 'pointer',
                        borderColor: isCorrect
                          ? 'rgba(58,127,90,.6)'
                          : isWrongPick
                            ? 'rgba(194,69,47,.6)'
                            : picked === oi
                              ? 'var(--stamp)'
                              : undefined,
                        background: isCorrect ? 'rgba(58,127,90,.08)' : isWrongPick ? 'rgba(194,69,47,.06)' : undefined,
                      }}
                    >
                      <span className="tiny muted" style={{ marginRight: 8 }}>
                        {String.fromCharCode(65 + oi)}
                      </span>
                      {opt}
                    </button>
                  )
                })}
              </div>
              {result && (
                <div className="mt-md">
                  <p className="small" style={{ color: result.correct ? '#3a7f5a' : '#c2452f', fontWeight: 700 }}>
                    {result.correct ? '✓ 回答正确 +100' : `✗ 正确答案是 ${String.fromCharCode(65 + result.correctIndex)}`}
                  </p>
                  <p className="tiny muted mt-sm">✦ {result.explanation}</p>
                </div>
              )}
              {err && <p className="small text-stamp mt-md">✗ {err}</p>}
            </section>

            <div className="center mt-lg">
              {!result ? (
                <button className="btn btn-primary btn-lg" disabled={busy || picked == null} onClick={submit}>
                  {busy ? '判题中…' : '提交答案'}
                </button>
              ) : (
                <Link className="btn btn-stamp" to={`/lesson?id=${id}`}>
                  返回课时 · 听力 {result.score} 分
                </Link>
              )}
            </div>
          </>
        )}
      </div>
      <PhaseBadge phase={meta.phase} />
    </>
  )
}
