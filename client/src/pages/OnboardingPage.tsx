import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { PageMeta } from '../lib/routes'
import type { CSSVars } from '../lib/css'
import PhaseBadge from '../components/PhaseBadge'
import { sdk } from '../lib/sdk'
import { toast } from '../lib/toast'

type Lang = 'en' | 'ja' | 'ko'

const LANGS: { key: Lang; color?: string; main: string; date: string; title: string; desc: string }[] = [
  { key: 'en', main: 'ENGLISH', date: '英语', title: '英语', desc: '使用最广的旅途通用语' },
  { key: 'ja', color: 'var(--lang-ja)', main: '日本語', date: '日语', title: '日语', desc: '从假名到动漫日剧' },
  { key: 'ko', color: 'var(--lang-ko)', main: '한국어', date: '韩语', title: '韩语', desc: '从韩文字母到追星刷剧' },
]

/** P05 入门引导：欢迎 → 选语种 → 定级/直学 三步向导 */
export default function OnboardingPage({ meta }: { meta: PageMeta }) {
  const navigate = useNavigate()
  const [step, setStep] = useState(0)
  const [lang, setLang] = useState<Lang>('en')
  const [busy, setBusy] = useState(false)

  async function choose(mode: 'PLACEMENT' | 'DIRECT' | 'SKIP') {
    setBusy(true)
    try {
      await sdk.onboarding(mode, lang)
      if (mode === 'PLACEMENT') {
        navigate(`/placement?lang=${lang}`)
      } else if (mode === 'DIRECT') {
        navigate(`/course-map?lang=${lang}`)
      } else {
        navigate('/home')
      }
    } catch {
      toast('操作失败，请重试')
    } finally {
      setBusy(false)
    }
  }

  /* 顶部路线指示：随步骤推进 */
  const stops = [
    { dot: '✦', label: '欢迎' },
    { dot: '地', label: '选语种' },
    { dot: '测', label: '怎么开始' },
  ]

  return (
    <>
      <div style={{ maxWidth: 820, margin: '0 auto', padding: '40px 24px 80px' }}>
        <div className="center">
          <div className="brand" style={{ justifyContent: 'center', fontSize: 22 }}>
            <span className="brand-mark">L</span>LinguaVerse
          </div>
          <div className="route mt-lg" style={{ justifyContent: 'center' }}>
            {stops.map((s, i) => (
              <span key={s.label} style={{ display: 'contents' }}>
                {i > 0 && <span className={`line${i <= step ? ' done' : ''}`} />}
                <span className="stop">
                  <span className={`dot${i < step ? ' done' : i === step ? ' current' : ''}`}>{s.dot}</span>
                  <span className={`tiny${i < step ? ' muted' : ''}`}>{s.label}</span>
                </span>
              </span>
            ))}
          </div>
        </div>

        {/* Step 1 · 欢迎 */}
        {step === 0 && (
          <section className="card mt-xl">
            <div className="center">
              <p className="serif-cn muted tiny" style={{ letterSpacing: '.3em' }}>
                第一步 · 欢迎
              </p>
              <h1 className="display serif-cn" style={{ fontSize: 34, marginTop: 10 }}>
                你的语言旅程，从这里检票
              </h1>
              <p className="muted mt-md" style={{ maxWidth: 520, marginInline: 'auto', lineHeight: 1.9 }}>
                接下来两件事：挑一个你最想去的国度，再决定是先做定级测试还是直接从 L1 出发。全程不到 5 分钟。
              </p>
              <div className="mt-lg">
                <button className="btn btn-primary btn-lg" onClick={() => setStep(1)}>
                  好，开始选语种
                </button>
              </div>
            </div>
          </section>
        )}

        {/* Step 2 · 选择首个目的地 */}
        {step === 1 && (
          <section className="mt-xl">
            <p className="serif-cn muted tiny" style={{ letterSpacing: '.3em' }}>
              第二步 · 选择首个目的地
            </p>
            <h2 className="serif-cn mt-md" style={{ fontSize: 24 }}>
              你最想先学哪一门语言？
            </h2>
            <p className="small muted mt-md">先选一个主修语种，以后随时可以在课程中心加入另外两门。</p>
            <div className="grid mt-lg" style={{ gridTemplateColumns: 'repeat(3,1fr)', gap: 18 }}>
              {LANGS.map((l) => {
                const active = lang === l.key
                return (
                  <div
                    key={l.key}
                    className="stamp-card"
                    style={{ cursor: 'pointer', ...(active ? { border: '2px solid var(--lang-en)' } : {}) }}
                    onClick={() => setLang(l.key)}
                  >
                    <div className={`rubber${l.key === 'en' ? ' ink' : ''}`} style={{ '--s': '76px', margin: '0 auto', ...(l.color ? { color: l.color } : {}) } as CSSVars}>
                      <span className="r-main">{l.main}</span>
                      <span className="r-date">{l.date}</span>
                    </div>
                    <h3 className="serif-cn center mt-md">{l.title}</h3>
                    <p className="tiny muted center mt-md">{l.desc}</p>
                    {active && (
                      <p className="tiny center mt-md" style={{ color: 'var(--lang-en)', fontWeight: 700 }}>
                        已选择 ✓
                      </p>
                    )}
                  </div>
                )
              })}
            </div>
            <div className="row-between mt-xl">
              <button className="btn btn-ghost" onClick={() => setStep(0)}>
                返回
              </button>
              <button className="btn btn-primary btn-lg" onClick={() => setStep(2)}>
                下一步：怎么开始
              </button>
            </div>
          </section>
        )}

        {/* Step 3 · 决定上车方式 */}
        {step === 2 && (
          <section className="mt-xl">
            <p className="serif-cn muted tiny" style={{ letterSpacing: '.3em' }}>
              第三步 · 决定上车方式
            </p>
            <h2 className="serif-cn mt-md" style={{ fontSize: 24 }}>
              想先知道自己的水平吗？
            </h2>
            <div className="grid mt-lg" style={{ gridTemplateColumns: '1fr 1fr', gap: 18 }}>
              <div className="stamp-card" style={{ border: '2px solid var(--stamp)' }}>
                <span className="rubber" style={{ '--s': '70px' } as CSSVars}>
                  <span className="r-main">TEST</span>
                  <span className="r-date">5 MIN</span>
                </span>
                <h3 className="serif-cn mt-md">做定级测试（推荐）</h3>
                <p className="small muted mt-md">10 道词汇与语法小题，5 分钟。完成后我们建议你从最合适的级别开始，不浪费时间在已经会的内容上。</p>
                <button className="btn btn-stamp btn-block mt-lg" disabled={busy} onClick={() => choose('PLACEMENT')}>
                  {busy ? '请稍候…' : '开始定级测试'}
                </button>
              </div>
              <div className="stamp-card">
                <span className="rubber ink" style={{ '--s': '70px' } as CSSVars}>
                  <span className="r-main">START</span>
                  <span className="r-date">L1</span>
                </span>
                <h3 className="serif-cn mt-md">从 L1 直接开始</h3>
                <p className="small muted mt-md">零基础或想打牢基础？直接进入入门第一课，从字母、发音和第一句问候学起。</p>
                <button className="btn btn-ghost btn-block mt-lg" disabled={busy} onClick={() => choose('DIRECT')}>
                  进入 L1 第一课
                </button>
              </div>
            </div>
            <div className="center mt-lg">
              <button className="small muted" style={{ background: 'none', border: 0, cursor: 'pointer', textDecoration: 'underline' }} disabled={busy} onClick={() => choose('SKIP')}>
                稍后再决定，先去首页看看
              </button>
            </div>
          </section>
        )}
      </div>
      <PhaseBadge phase={meta.phase} />
    </>
  )
}
