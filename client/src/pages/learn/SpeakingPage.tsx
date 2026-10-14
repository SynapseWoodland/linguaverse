import { useMemo, useRef, useState } from 'react'
import { Link, Navigate, useSearchParams } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import PhaseBadge from '../../components/PhaseBadge'
import { sdk, type SpeakingResult } from '../../lib/sdk'
import { ApiError } from '../../lib/api'
import { useApi, Loading, ErrorState } from '../../lib/ui'
import { speak as ttsSpeak, langTag } from '../../lib/speech'

type Rating = 'NEED_PRACTICE' | 'OK' | 'FLUENT'

/* Web Speech 识别器（Chrome/Edge 可用，其余浏览器降级为 SELF 自评） */
type AnyRecognition = {
  lang: string
  interimResults: boolean
  onresult: ((e: { results: ArrayLike<ArrayLike<{ transcript: string }>> }) => void) | null
  onend: (() => void) | null
  onerror: (() => void) | null
  start: () => void
  stop: () => void
}

function getRecognition(): AnyRecognition | null {
  const w = window as unknown as { SpeechRecognition?: new () => AnyRecognition; webkitSpeechRecognition?: new () => AnyRecognition }
  const Ctor = w.SpeechRecognition ?? w.webkitSpeechRecognition
  return Ctor ? new Ctor() : null
}

/** P17 口语跟读：浏览器识别走 RECOGNITION 服务端相似度判分；不支持时 SELF 三档自评。 */
export default function SpeakingPage({ meta }: { meta: PageMeta }) {
  const [params] = useSearchParams()
  const id = Number(params.get('id'))
  const { data: lesson, error, loading, reload } = useApi(() => sdk.lesson(id), [id])
  const [transcripts, setTranscripts] = useState<Record<number, string>>({})
  const [ratings, setRatings] = useState<Record<number, Rating>>({})
  const [recordingId, setRecordingId] = useState<number | null>(null)
  const [result, setResult] = useState<SpeakingResult | null>(null)
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)
  const recRef = useRef<AnyRecognition | null>(null)
  const recognitionSupported = useMemo(() => !!getRecognition(), [])

  if (!id) return <Navigate to="/courses" replace />

  function speak(text: string) {
    if (!lesson) return
    ttsSpeak(text, lesson.langCode, 0.85)
  }

  function toggleRecord(sentenceId: number) {
    if (!lesson || recordingId !== null) return
    const rec = getRecognition()
    if (!rec) return
    rec.lang = langTag(lesson.langCode)
    rec.interimResults = false
    rec.onresult = (e) => {
      const text = e.results[0][0].transcript
      setTranscripts((t) => ({ ...t, [sentenceId]: text }))
    }
    rec.onend = () => setRecordingId(null)
    rec.onerror = () => setRecordingId(null)
    recRef.current = rec
    setRecordingId(sentenceId)
    rec.start()
  }

  async function submit() {
    if (!lesson) return
    setBusy(true)
    setErr(null)
    const allTranscripts = lesson.speaking.every((s) => (transcripts[s.id] ?? '').trim().length > 0)
    const allRatings = lesson.speaking.every((s) => ratings[s.id])
    if (!allTranscripts && !allRatings) {
      setErr('请为每句话完成录音识别，或逐句选择自评档位')
      setBusy(false)
      return
    }
    try {
      const mode = allTranscripts ? 'RECOGNITION' : 'SELF'
      const sentences = lesson.speaking.map((s) =>
        allTranscripts
          ? { sentenceId: s.id, transcript: transcripts[s.id].trim() }
          : { sentenceId: s.id, rating: ratings[s.id] },
      )
      const r = await sdk.speakingSubmit(id, mode, sentences)
      setResult(r)
      await sdk.stageProgress(id, 'SPEAKING', r.score)
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
              ④ 口语跟读
            </h1>
            <p className="small muted mt-sm">
              {recognitionSupported
                ? '听示范 → 录音跟读，服务端按识别文本相似度判分；也可以直接用下方三档自评。'
                : '当前浏览器不支持语音识别，请用三档自评完成（需多练/还行/流利）。'}
            </p>

            <div style={{ display: 'grid', gap: 14, marginTop: 20 }}>
              {lesson.speaking.map((s) => {
                const item = result?.results.find((r) => r.sentenceId === s.id)
                return (
                  <section key={s.id} className="card">
                    <div className="row-between">
                      <p className="small" style={{ fontWeight: 700 }}>
                        {s.sortNo}. {s.text}
                      </p>
                      <button className="btn btn-ghost" onClick={() => speak(s.text)}>
                        🔊 示范
                      </button>
                    </div>
                    <p className="tiny muted mt-sm">{s.textZh}</p>

                    {!result && (
                      <div className="mt-md">
                        {recognitionSupported && (
                          <div className="row gap-sm">
                            <button
                              className={`btn ${recordingId === s.id ? 'btn-stamp' : 'btn-ghost'}`}
                              onClick={() => toggleRecord(s.id)}
                            >
                              {recordingId === s.id ? '● 录音中…点击停止' : '🎙 跟读录音'}
                            </button>
                            <input
                              className="input"
                              style={{ flex: 1 }}
                              placeholder="识别文本（也可手动输入）"
                              value={transcripts[s.id] ?? ''}
                              onChange={(e) => setTranscripts((t) => ({ ...t, [s.id]: e.target.value }))}
                            />
                          </div>
                        )}
                        <div className="row gap-sm mt-sm">
                          {(['NEED_PRACTICE', 'OK', 'FLUENT'] as Rating[]).map((r) => (
                            <button
                              key={r}
                              className={`btn btn-ghost ${ratings[s.id] === r ? 'is-active-rate' : ''}`}
                              style={ratings[s.id] === r ? { borderColor: 'var(--stamp)', color: 'var(--stamp)' } : undefined}
                              onClick={() => setRatings((v) => ({ ...v, [s.id]: r }))}
                            >
                              {r === 'NEED_PRACTICE' ? '需多练' : r === 'OK' ? '还行' : '流利'}
                            </button>
                          ))}
                        </div>
                      </div>
                    )}
                    {item && (
                      <p className="tiny mt-sm" style={{ color: (item.score ?? 0) >= 80 ? '#3a7f5a' : '#b07a2f' }}>
                        {item.feedback}
                        {item.score != null && ` · ${item.score} 分`}
                      </p>
                    )}
                  </section>
                )
              })}
            </div>

            {err && <p className="small text-stamp mt-md">✗ {err}</p>}

            <div className="center mt-lg" style={{ display: 'flex', gap: 12, justifyContent: 'center', alignItems: 'center' }}>
              {!result ? (
                <button className="btn btn-primary btn-lg" disabled={busy || recordingId !== null} onClick={submit}>
                  {busy ? '评分中…' : '提交跟读评分'}
                </button>
              ) : (
                <>
                  <div className="rubber ink" style={{ ['--s' as string]: '64px' }}>
                    <span className="r-main">{result.score}</span>
                    <span className="r-date">{result.mode}</span>
                  </div>
                  <Link className="btn btn-stamp" to={`/lesson?id=${id}`}>
                    返回课时
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
