import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import { sdk, type WordCard } from '../../lib/sdk'
import { ApiError } from '../../lib/api'
import { useApi, Loading, ErrorState, PageTitle, LANG_LABEL } from '../../lib/ui'
import { toast } from '../../lib/toast'
import SpeakButton from '../../components/SpeakButton'
import { getAutoSpeak, setAutoSpeak, speak } from '../../lib/speech'
import { useAutoSpeak } from '../../lib/useAutoSpeak'

/** P19 复习中心：到期队列逐张评分（UNKNOWN 当天再来 / VAGUE 明天 / KNOWN 三天后）。 */
export default function ReviewPage({ meta: _meta }: { meta: PageMeta }) {
  const [params, setParams] = useSearchParams()
  const me = useApi(() => sdk.me(), [])
  const lang = params.get('lang') ?? me.data?.enrollments[0]?.langCode ?? 'en'
  const queue = useApi(() => (me.data ? sdk.due(lang) : Promise.resolve(null)), [lang, !!me.data])
  const [cards, setCards] = useState<WordCard[] | null>(null)
  const [idx, setIdx] = useState(0)
  const [busy, setBusy] = useState(false)
  const [doneCount, setDoneCount] = useState(0)
  const [autoSpeak, setAutoSpeakOn] = useState(getAutoSpeak())

  const visible = cards ?? queue.data?.cards ?? []
  const currentCard = idx < visible.length ? visible[idx] : undefined
  useAutoSpeak(currentCard?.word, lang)

  // 键盘 P 重播当前词读音
  useEffect(() => {
    if (!currentCard) return
    const onKey = (e: KeyboardEvent) => {
      if (e.key.toLowerCase() === 'p' && !(e.target instanceof HTMLInputElement)) speak(currentCard.word, lang, 0.85)
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [currentCard, lang])

  async function rate(rating: 'UNKNOWN' | 'VAGUE' | 'KNOWN') {
    const card = visible[idx]
    if (!card) return
    setBusy(true)
    try {
      await sdk.rate(lang, card.word, rating)
      setDoneCount((n) => n + 1)
      setIdx((i) => i + 1)
    } catch (e) {
      toast(e instanceof ApiError ? e.message : '评分失败')
    } finally {
      setBusy(false)
    }
  }

  function switchLang(l: string) {
    setCards(null)
    setIdx(0)
    setDoneCount(0)
    setParams(l ? { lang: l } : {})
  }

  const enrollLangs = me.data?.enrollments.map((e) => e.langCode) ?? ['en', 'ja', 'ko']

  return (
    <div className="wrap" style={{ paddingBottom: 96 }}>
      <PageTitle zh="复习中心" sub="按遗忘曲线到期的单词，刷完继续推进新课程。" />

      <div className="row gap-sm mt-md">
        {enrollLangs.map((l) => (
          <button key={l} className={`btn ${l === lang ? 'btn-stamp' : 'btn-ghost'}`} onClick={() => switchLang(l)}>
            {LANG_LABEL[l]}
          </button>
        ))}
      </div>

      {me.loading && <Loading />}
      {me.error && <ErrorState message={me.error} onRetry={me.reload} />}
      {queue.loading && me.data && <Loading tip="正在取到期卡片…" />}
      {queue.error && <ErrorState message={queue.error} onRetry={queue.reload} />}

      {me.data && queue.data && (
        <>
          {visible.length === 0 || idx >= visible.length ? (
            <section className="card stamp-card center mt-lg">
              <div className="rubber ink" style={{ ['--s' as string]: '90px' }}>
                <span className="r-main">✓</span>
                <span className="r-date">CLEAR</span>
              </div>
              <h2 className="serif-cn mt-md">
                {doneCount === 0 ? '今天没有到期单词' : `本轮复习完成，共 ${doneCount} 张`}
              </h2>
              <p className="small muted mt-sm">UNKNOWN 的卡片当天会再次出现，去课程地图解锁新词吧。</p>
            </section>
          ) : (
            <section className="card mt-lg">
              <div className="row-between">
                <span className="tiny muted">
                  {LANG_LABEL[lang]} · 到期 {queue.data.total} 张 · 本轮第 {Math.min(idx + 1, visible.length)} 张
                </span>
                <span className="tiny">stage {visible[idx].stage}</span>
              </div>
              <div className="center mt-lg">
                <p className="display serif-cn" style={{ fontSize: 44 }}>
                  {visible[idx].word}
                </p>
                <div className="row gap-sm mt-md" style={{ alignItems: 'center', justifyContent: 'center' }}>
                  <span className="muted small">{visible[idx].phonetic}</span>
                  <SpeakButton text={visible[idx].word} langCode={lang} rate={0.85} label="朗读单词（快捷键 P）" />
                </div>
                <p className="serif-cn mt-lg" style={{ fontSize: 24 }}>
                  {visible[idx].meaningZh}
                </p>
              </div>
              <div className="row gap-sm mt-lg" style={{ justifyContent: 'center', alignItems: 'center' }}>
                <button className="btn btn-ghost" disabled={busy} onClick={() => rate('UNKNOWN')}>
                  不认识 · 今天再来
                </button>
                <button className="btn btn-ghost" disabled={busy} onClick={() => rate('VAGUE')}>
                  模糊 · 明天
                </button>
                <button className="btn btn-primary" disabled={busy} onClick={() => rate('KNOWN')}>
                  认识 · 三天后
                </button>
                <button
                  className="btn btn-ghost"
                  aria-pressed={autoSpeak}
                  title="切换到新卡片时自动朗读"
                  onClick={() => {
                    const next = !autoSpeak
                    setAutoSpeakOn(next)
                    setAutoSpeak(next)
                    if (next) speak(visible[idx].word, lang, 0.85)
                  }}
                >
                  {autoSpeak ? '🔊 自动发音 开' : '🔇 自动发音 关'}
                </button>
              </div>
            </section>
          )}
        </>
      )}
    </div>
  )
}
