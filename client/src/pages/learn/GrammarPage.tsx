import { useState } from 'react'
import { Link, Navigate, useSearchParams } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import PhaseBadge from '../../components/PhaseBadge'
import { sdk, type GrammarResult } from '../../lib/sdk'
import { ApiError } from '../../lib/api'
import { useApi, Loading, ErrorState } from '../../lib/ui'

/** P15 语法练习：提交后才显示对错/正确答案/解析，并上报环节成绩。 */
export default function GrammarPage({ meta }: { meta: PageMeta }) {
  const [params] = useSearchParams()
  const id = Number(params.get('id'))
  const { data: lesson, error, loading, reload } = useApi(() => sdk.lesson(id), [id])
  const [picked, setPicked] = useState<Record<number, number>>({})
  const [result, setResult] = useState<GrammarResult | null>(null)
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)

  if (!id) return <Navigate to="/courses" replace />

  async function submit() {
    if (!lesson) return
    setBusy(true)
    setErr(null)
    try {
      const answers = lesson.grammar.questions.map((q) => ({ questionId: q.id, choiceIndex: picked[q.id] ?? -1 }))
      const r = await sdk.grammarSubmit(id, answers)
      setResult(r)
      await sdk.stageProgress(id, 'GRAMMAR', r.score)
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : '提交失败')
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <div style={{ maxWidth: 760, margin: '0 auto', padding: '32px 24px 80px' }}>
        <Link className="small muted" to={`/lesson?id=${id}`}>
          ← 返回课时
        </Link>
        {loading && <Loading />}
        {error && <ErrorState message={error} onRetry={reload} />}

        {lesson && (
          <>
            <h1 className="serif-cn mt-md" style={{ fontSize: 26 }}>
              ② 语法练习 · {lesson.grammar.title}
            </h1>
            <p className="small muted mt-sm">{lesson.grammar.explanation}</p>

            <div style={{ display: 'grid', gap: 18, marginTop: 20 }}>
              {lesson.grammar.questions.map((q, qi) => {
                const item = result?.results.find((r) => r.questionId === q.id)
                return (
                  <section key={q.id} className="card">
                    <p className="small" style={{ fontWeight: 700 }}>
                      {qi + 1}. {q.stem}
                    </p>
                    <div className="mt-md" style={{ display: 'grid', gap: 8 }}>
                      {q.options.map((opt, oi) => {
                        const chosen = picked[q.id] === oi
                        const isCorrect = result && oi === item!.correctIndex
                        const isWrongPick = result && chosen && !item!.correct
                        return (
                          <button
                            key={oi}
                            disabled={!!result || busy}
                            onClick={() => setPicked((p) => ({ ...p, [q.id]: oi }))}
                            aria-pressed={chosen}
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
                                  : chosen
                                    ? 'var(--stamp)'
                                    : undefined,
                              background: isCorrect ? 'rgba(58,127,90,.08)' : isWrongPick ? 'rgba(194,69,47,.06)' : undefined,
                            }}
                          >
                            <span className="tiny muted" style={{ marginRight: 8 }}>
                              {String.fromCharCode(65 + oi)}
                            </span>
                            {opt}
                            {isCorrect && <span style={{ float: 'right', color: '#3a7f5a' }}>✓ 正确答案</span>}
                            {isWrongPick && <span style={{ float: 'right', color: '#c2452f' }}>✗ 你的选择</span>}
                          </button>
                        )
                      })}
                    </div>
                    {item && (
                      <p className="tiny muted mt-md" style={{ lineHeight: 1.8 }}>
                        ✦ {item.explanation}
                      </p>
                    )}
                  </section>
                )
              })}
            </div>

            {err && <p className="small text-stamp mt-md">✗ {err}</p>}

            <div className="center mt-lg" style={{ display: 'flex', gap: 12, justifyContent: 'center' }}>
              {!result ? (
                <button
                  className="btn btn-primary btn-lg"
                  disabled={busy || Object.keys(picked).length < lesson.grammar.questions.length}
                  onClick={submit}
                >
                  {busy ? '判题中…' : `提交答案（${Object.keys(picked).length}/${lesson.grammar.questions.length}）`}
                </button>
              ) : (
                <>
                  <div className="rubber ink" style={{ ['--s' as string]: '64px' }}>
                    <span className="r-main">{result.score}</span>
                    <span className="r-date">SCORE</span>
                  </div>
                  <Link className="btn btn-stamp" to={`/lesson?id=${id}`}>
                    返回课时（{result.correctCount}/{result.total} 正确）
                  </Link>
                </>
              )}
            </div>
          </>
        )}
      </div>
      <PhaseBadge phase={meta.phase} />
    </>
  )
}
