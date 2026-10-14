import { useEffect, useMemo, useState } from 'react'
import { Link, Navigate, useSearchParams } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import PhaseBadge from '../../components/PhaseBadge'
import FlipCard from '../../components/FlipCard'
import SpeakButton from '../../components/SpeakButton'
import { sdk } from '../../lib/sdk'
import { ApiError } from '../../lib/api'
import { useApi, Loading, ErrorState } from '../../lib/ui'
import { toast } from '../../lib/toast'
import { getAutoSpeak, setAutoSpeak, speak } from '../../lib/speech'
import { useAutoSpeak } from '../../lib/useAutoSpeak'

const STAGE_LABEL: Record<string, string> = {
  VOCAB: '单词闪卡',
  GRAMMAR: '语法练习',
  LISTENING: '听力训练',
  SPEAKING: '口语跟读',
}

/** P13/P14 课时对页：四环节枢纽，闪卡在此直接刷（SRS rating），其余三环节进子页。 */
export default function LessonPage({ meta }: { meta: PageMeta }) {
  const [params] = useSearchParams()
  const id = Number(params.get('id'))
  const { data: lesson, error, loading, reload } = useApi(() => sdk.lesson(id), [id])
  const [cardIdx, setCardIdx] = useState(0)
  const [ratedCount, setRatedCount] = useState(0)
  const [busy, setBusy] = useState(false)
  const [vocabDone, setVocabDone] = useState(false)
  const [autoSpeak, setAutoSpeakOn] = useState(getAutoSpeak())

  const doneStages = useMemo(() => new Set(lesson?.completedStages ?? []), [lesson])

  const currentCard = lesson && cardIdx < lesson.vocab.length ? lesson.vocab[cardIdx] : undefined
  useAutoSpeak(currentCard?.word, lesson?.langCode)

  // 键盘 P 重播当前词读音
  useEffect(() => {
    if (!currentCard) return
    const onKey = (e: KeyboardEvent) => {
      if (e.key.toLowerCase() === 'p' && !(e.target instanceof HTMLInputElement)) {
        speak(currentCard.word, lesson!.langCode, 0.85)
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [currentCard, lesson])

  // 进入课时即记录开始时刻，供结算页计算 durationSec
  useMemo(() => {
    if (lesson && !sessionStorage.getItem(`lv-start-${id}`)) {
      sessionStorage.setItem(`lv-start-${id}`, String(Date.now()))
    }
  }, [lesson, id])

  if (!id) return <Navigate to="/courses" replace />

  async function rate(rating: 'UNKNOWN' | 'VAGUE' | 'KNOWN') {
    if (!lesson) return
    const card = lesson.vocab[cardIdx]
    setBusy(true)
    try {
      await sdk.rate(lesson.langCode, card.word, rating)
      const next = ratedCount + 1
      setRatedCount(next)
      if (cardIdx < lesson.vocab.length - 1) {
        setCardIdx(cardIdx + 1)
      } else if (!doneStages.has('VOCAB') && !vocabDone) {
        await sdk.stageProgress(id, 'VOCAB', rating === 'UNKNOWN' ? 60 : 100)
        setVocabDone(true)
        toast('单词环节已完成')
      }
    } catch (e) {
      toast(e instanceof ApiError ? e.message : '评分失败')
    } finally {
      setBusy(false)
    }
  }

  const allStages =
    lesson != null &&
    ['VOCAB', 'GRAMMAR', 'LISTENING', 'SPEAKING'].every((s) => doneStages.has(s) || (s === 'VOCAB' && vocabDone))

  return (
    <>
      <div style={{ maxWidth: 860, margin: '0 auto', padding: '32px 24px 80px' }}>
        <Link className="small muted" to={`/course-map?lang=${lesson?.langCode ?? 'en'}`}>
          ← 返回课程地图
        </Link>

        {loading && <Loading tip="正在翻开护照对页…" />}
        {error && <ErrorState message={error} onRetry={reload} />}

        {lesson && (
          <>
            <div className="row-between mt-md">
              <div>
                <p className="tiny muted" style={{ letterSpacing: '.2em' }}>
                  LESSON {lesson.seqNo}
                </p>
                <h1 className="serif-cn" style={{ fontSize: 28 }}>
                  {lesson.title}
                </h1>
                <p className="small muted">{lesson.titleNative} · {lesson.scene}</p>
              </div>
              {lesson.completed && (
                <span className="rubber ink" style={{ ['--s' as string]: '56px' }}>
                  <span className="r-main">✓</span>
                  <span className="r-date">DONE</span>
                </span>
              )}
            </div>

            {/* 四环节进度 */}
            <div className="grid mt-lg" style={{ gridTemplateColumns: 'repeat(4,1fr)', gap: 10 }}>
              {(['VOCAB', 'GRAMMAR', 'LISTENING', 'SPEAKING'] as const).map((s) => {
                const finished = doneStages.has(s) || (s === 'VOCAB' && vocabDone)
                return (
                  <div key={s} className="card center" style={{ padding: '14px 8px', opacity: finished ? 1 : 0.65 }}>
                    <span style={{ fontSize: 20 }}>{finished ? '✅' : '◯'}</span>
                    <span className="tiny mt-sm">{STAGE_LABEL[s]}</span>
                  </div>
                )
              })}
            </div>

            {/* 单词闪卡环节 */}
            <section className="card mt-lg">
              <div className="row-between">
                <h2 className="serif-cn" style={{ fontSize: 20 }}>
                  ① 单词闪卡
                </h2>
                <span className="tiny muted">
                  {Math.min(cardIdx + 1, lesson.vocab.length)} / {lesson.vocab.length}
                </span>
              </div>

              {cardIdx < lesson.vocab.length ? (
                <>
                  <div className="center mt-lg">
                    <FlipCard
                      front={
                        <div className="center" style={{ padding: 24 }}>
                          <p className="display serif-cn" style={{ fontSize: 40 }}>
                            {lesson.vocab[cardIdx].word}
                          </p>
                          <div className="row gap-sm mt-md" style={{ alignItems: 'center', justifyContent: 'center' }}>
                            <span className="muted small">{lesson.vocab[cardIdx].phonetic}</span>
                            <SpeakButton
                              text={lesson.vocab[cardIdx].word}
                              langCode={lesson.langCode}
                              rate={0.85}
                              label="朗读单词（快捷键 P）"
                            />
                          </div>
                          <p className="tiny muted mt-md">点击卡片翻面看释义</p>
                        </div>
                      }
                      back={
                        <div className="center" style={{ padding: 24 }}>
                          <p className="serif-cn" style={{ fontSize: 26 }}>
                            {lesson.vocab[cardIdx].meaningZh}
                          </p>
                          <div className="row gap-sm mt-md" style={{ alignItems: 'center', justifyContent: 'center' }}>
                            <p className="small muted" style={{ fontStyle: 'italic', margin: 0 }}>
                              {lesson.vocab[cardIdx].example}
                            </p>
                            <SpeakButton
                              text={lesson.vocab[cardIdx].example}
                              langCode={lesson.langCode}
                              rate={0.92}
                              label="朗读例句"
                            />
                          </div>
                          <p className="tiny muted mt-sm">{lesson.vocab[cardIdx].exampleZh}</p>
                        </div>
                      }
                    />
                  </div>
                  <div className="row gap-sm mt-lg" style={{ justifyContent: 'center', alignItems: 'center' }}>
                    <button className="btn btn-ghost" disabled={busy} onClick={() => rate('UNKNOWN')}>
                      不认识
                    </button>
                    <button className="btn btn-ghost" disabled={busy} onClick={() => rate('VAGUE')}>
                      模糊
                    </button>
                    <button className="btn btn-primary" disabled={busy} onClick={() => rate('KNOWN')}>
                      认识
                    </button>
                    <button
                      className="btn btn-ghost"
                      aria-pressed={autoSpeak}
                      title="切换到新卡片时自动朗读"
                      onClick={() => {
                        const next = !autoSpeak
                        setAutoSpeakOn(next)
                        setAutoSpeak(next)
                        if (next) speak(lesson.vocab[cardIdx].word, lesson.langCode, 0.85)
                      }}
                    >
                      {autoSpeak ? '🔊 自动发音 开' : '🔇 自动发音 关'}
                    </button>
                  </div>
                </>
              ) : (
                <div className="center mt-lg">
                  <p className="serif-cn">本轮 {ratedCount} 张卡片已刷完 ✓</p>
                  <button className="btn btn-ghost mt-md" onClick={() => setCardIdx(0)}>
                    再刷一轮
                  </button>
                </div>
              )}
            </section>

            {/* 其余三环节 */}
            <div className="grid mt-lg" style={{ gridTemplateColumns: 'repeat(3,1fr)', gap: 14 }}>
              <Link className="card stamp-card center" to={`/grammar?id=${id}`}>
                <h3 className="serif-cn">② 语法练习</h3>
                <p className="tiny muted mt-sm">{lesson.grammar.title} · {lesson.grammar.questions.length} 题</p>
                {doneStages.has('GRAMMAR') && <p className="tiny mt-sm" style={{ color: 'var(--stamp)' }}>已完成 ✓</p>}
              </Link>
              <Link className="card stamp-card center" to={`/listening?id=${id}`}>
                <h3 className="serif-cn">③ 听力训练</h3>
                <p className="tiny muted mt-sm">{lesson.listening.title}</p>
                {doneStages.has('LISTENING') && <p className="tiny mt-sm" style={{ color: 'var(--stamp)' }}>已完成 ✓</p>}
              </Link>
              <Link className="card stamp-card center" to={`/speaking?id=${id}`}>
                <h3 className="serif-cn">④ 口语跟读</h3>
                <p className="tiny muted mt-sm">{lesson.speaking.length} 句跟读</p>
                {doneStages.has('SPEAKING') && <p className="tiny mt-sm" style={{ color: 'var(--stamp)' }}>已完成 ✓</p>}
              </Link>
            </div>

            {/* 结算 */}
            <div className="center mt-xl">
              {allStages ? (
                <Link className="btn btn-stamp btn-lg" to={`/lesson-complete?id=${id}`}>
                  {lesson.completed ? '查看盖章结果' : '完成课时 · 盖章'}
                </Link>
              ) : (
                <p className="small muted">完成全部四个环节后即可盖章结算 XP</p>
              )}
            </div>
          </>
        )}
      </div>
      <PhaseBadge phase={meta.phase} />
    </>
  )
}
