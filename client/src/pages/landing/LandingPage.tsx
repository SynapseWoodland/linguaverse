import { Link } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import type { CSSVars } from '../../lib/css'
import PhaseBadge from '../../components/PhaseBadge'

/** P01 品牌落地页：护照封面 hero · 三语词排版 · 三邮戳（纯展示，无数据依赖） */
export default function LandingPage({ meta }: { meta: PageMeta }) {
  return (
    <>
      <TopBar />
      <Hero />
      <LangStamps />
      <Modules />
      <PlanPreview />
      <Community />
      <FinalCta />
      <PhaseBadge phase={meta.phase} />
    </>
  )
}

/* ---- 顶部细栏 ---- */
function TopBar() {
  return (
    <header className="cover" style={{ position: 'sticky', top: 0, zIndex: 40, borderBottom: '1px solid rgba(255,255,255,.1)' }}>
      <div className="wrap row-between" style={{ height: 60 }}>
        <div className="brand" style={{ color: '#ECE8DB' }}>
          <span className="brand-mark">L</span>LinguaVerse
        </div>
        <div className="row gap-md">
          <Link className="btn btn-light btn-sm" to="/login">
            登录
          </Link>
          <Link className="btn btn-stamp btn-sm" to="/register">
            免费启程
          </Link>
        </div>
      </div>
    </header>
  )
}

/* ---- HERO：护照封面 ---- */
function Hero() {
  return (
    <section className="cover">
      <div
        className="wrap"
        style={{ display: 'grid', gridTemplateColumns: '1.15fr .85fr', gap: 48, alignItems: 'center', padding: '72px 28px 88px' }}
      >
        <div>
          <p className="serif-cn muted" style={{ fontSize: 15, letterSpacing: '.3em', marginBottom: 22 }}>
            一本护照，三种世界
          </p>
          <h1 className="display" style={{ fontSize: 54, color: '#F4F1E8' }}>
            你的语言
            <br />
            护照，<span className="text-stamp" style={{ fontStyle: 'italic' }}>等你盖章</span>。
          </h1>
          <div
            className="mt-lg row gap-lg"
            style={{ fontFamily: 'var(--font-display)', fontSize: 26, color: '#ECE8DB', flexWrap: 'wrap', gap: '18px 30px' }}
          >
            <span style={{ color: '#9DB6D6' }}>Hello.</span>
            <span style={{ color: '#E7AEB4' }}>こんにちは。</span>
            <span style={{ color: '#9CC7B4' }}>안녕하세요.</span>
          </div>
          <p className="muted mt-lg" style={{ maxWidth: 440, fontSize: 16, lineHeight: 1.8 }}>
            英语、日语、韩语，从第一站 L1 到精通 L6。背词、语法、听力、口语都在一次旅途里完成——每学完一课，就在护照上盖一枚章。
          </p>
          <div className="row gap-md mt-xl">
            <Link className="btn btn-stamp btn-lg" to="/register">
              翻开护照，开始第一站
            </Link>
            <Link className="btn btn-light btn-lg" to="/course-map">
              先看看课程路线
            </Link>
          </div>
        </div>

        {/* 护照本插画 */}
        <div style={{ position: 'relative', height: 430 }}>
          <div
            style={{
              position: 'absolute',
              inset: '30px 10px 0 40px',
              background: 'linear-gradient(150deg,#2C4360,#16263A)',
              borderRadius: '8px 14px 14px 8px',
              boxShadow: '0 30px 60px rgba(0,0,0,.45)',
              border: '1px solid rgba(184,137,59,.5)',
            }}
          />
          <div
            style={{
              position: 'absolute',
              inset: '48px 28px 18px 58px',
              background: '#EFEDE4',
              borderRadius: '3px 8px 8px 3px',
              padding: '26px 22px',
              backgroundImage: 'linear-gradient(rgba(27,42,61,.05) 1px,transparent 1px)',
              backgroundSize: '100% 30px',
            }}
          >
            <p className="tiny muted" style={{ letterSpacing: '.25em' }}>
              LINGUAVERSE · PASSPORT
            </p>
            <div className="row-between mt-md">
              <div>
                <div className="serif-cn" style={{ fontWeight: 700, fontSize: 18 }}>
                  学习护照
                </div>
                <p className="tiny muted mt-md">编号 LV-2026-1013</p>
              </div>
              <div className="rubber sm ink" style={{ '--s': '56px' } as CSSVars}>
                <span className="r-main">
                  L1
                  <br />
                  DONE
                </span>
                <span className="r-date">10.13</span>
              </div>
            </div>
            <div className="mt-lg" style={{ borderTop: '2px solid var(--ink)', paddingTop: 10 }}>
              <p className="tiny muted">旅程进度 · 英语</p>
              <div className="bar mt-md">
                <i style={{ width: '42%', background: 'var(--lang-en)' }} />
              </div>
              <div className="row-between tiny muted mt-md">
                <span>L2 · 初级</span>
                <span>42%</span>
              </div>
            </div>
            <div className="row gap-md" style={{ marginTop: 20 }}>
              <span className="rubber sm" style={{ '--s': '52px' } as CSSVars}>
                <span className="r-main">
                  WORD
                  <br />
                  ×8
                </span>
                <span className="r-date">今日</span>
              </span>
              <span className="rubber sm muted-stamp" style={{ '--s': '52px' } as CSSVars}>
                <span className="r-main">
                  14
                  <br />
                  DAYS
                </span>
                <span className="r-date">STREAK</span>
              </span>
              <span className="rubber sm" style={{ '--s': '52px', color: 'var(--gold)' } as CSSVars}>
                <span className="r-main">LV.3</span>
                <span className="r-date">XP 640</span>
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}

/* ---- 三语邮戳 ---- */
function LangStamps() {
  const langs = [
    { cls: 'ink', main: 'ENGLISH', date: '英语 · 6 级', title: '英语 English', desc: '从 Good morning 到读懂原版小说，六站走完。' },
    { color: 'var(--lang-ja)', main: '日本語', date: '日语 · 6 级', title: '日语 日本語', desc: '假名起步，跟着真实生活会话一路到站。' },
    { color: 'var(--lang-ko)', main: '한국어', date: '韩语 · 6 级', title: '韩语 한국어', desc: '从韩文字母到刷剧不看字幕的旅行。' },
  ]
  return (
    <section className="wrap page" style={{ paddingTop: 64 }}>
      <div className="row-between" style={{ flexWrap: 'wrap', gap: 30 }}>
        {langs.map((l) => (
          <div key={l.title} style={{ flex: 1, minWidth: 240 }} className="row gap-lg">
            <span className={`rubber${l.cls ? ` ${l.cls}` : ''}`} style={{ '--s': '96px', ...(l.color ? { color: l.color } : {}) } as CSSVars}>
              <span className="r-main">{l.main}</span>
              <span className="r-date">{l.date}</span>
            </span>
            <div>
              <h3 className="serif-cn">{l.title}</h3>
              <p className="muted small mt-md">{l.desc}</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}

/* ---- 四模块：护照内页四格 ---- */
function Modules() {
  const mods = [
    { icon: '◍', title: '单词记忆', desc: '翻转卡片，认识、模糊、不认识三档安排复习，到期自动回到你面前。' },
    { icon: '✎', title: '语法练习', desc: '讲解只讲最必要的规则，随后立刻做题，错一题立刻看到为什么。' },
    { icon: '◔', title: '听力训练', desc: '真实语速与慢速随时切换，逐句重播，原文先藏起来让耳朵先上场。' },
    { icon: '♪', title: '口语跟读', desc: '逐句跟读即时打分，设备不支持识别时也能自评完成，不掉链子。' },
  ]
  return (
    <section className="wrap page">
      <p className="serif-cn muted center" style={{ letterSpacing: '.3em', fontSize: 13 }}>
        每节课的四枚印记
      </p>
      <h2 className="serif-cn center mt-md" style={{ fontSize: 30 }}>
        听说读写，一页一站
      </h2>
      <div className="grid mt-xl" style={{ gridTemplateColumns: 'repeat(4,1fr)', gap: 18 }}>
        {mods.map((m) => (
          <div className="stamp-card" key={m.title}>
            <div className="display" style={{ fontSize: 30 }}>
              {m.icon}
            </div>
            <h3 className="serif-cn mt-md">{m.title}</h3>
            <p className="small muted mt-md">{m.desc}</p>
          </div>
        ))}
      </div>
    </section>
  )
}

/* ---- 路线与推荐 ---- */
function PlanPreview() {
  const tips = [
    { cls: '', main: 'L3', text: '「你有 12 个单词今天到期，先花 5 分钟复习」' },
    { cls: 'ink', main: '82%', text: '「语法是你正确率最低的模块，建议练一组 L2 语法」' },
    { cls: 'muted-stamp', main: '14D', text: '「再学 1 节，就能保住连续 14 天的记录」' },
  ]
  const plans = [
    { title: '复习到期单词 ×12', reason: '因你标记过的 12 个词今天到期' },
    { title: '继续：L2 第 3 课《车站》', reason: '上节课完成了 40%' },
    { title: '语法专项 · て形', reason: '你最近的语法正确率 61%' },
  ]
  return (
    <section className="page" style={{ background: 'var(--ink)', color: 'var(--paper)' }}>
      <div className="wrap" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 56, alignItems: 'center' }}>
        <div>
          <h2 className="serif-cn" style={{ fontSize: 30 }}>
            今天该学什么，护照替你想好
          </h2>
          <p className="muted mt-lg" style={{ lineHeight: 1.9, fontSize: 15.5 }}>
            定级测试告诉你从哪一站上车；到期的词、没学完的课、正确率最低的模块，都会变成首页上一条条带理由的任务。连续学习天数断了？它会在临断前提醒你。
          </p>
          <div className="col gap-md mt-xl">
            {tips.map((t) => (
              <div className="row gap-md" key={t.main}>
                <span className={`rubber sm${t.cls ? ` ${t.cls}` : ''}`} style={{ '--s': '48px', flex: 'none' } as CSSVars}>
                  <span className="r-main">{t.main}</span>
                </span>
                <span className="small">{t.text}</span>
              </div>
            ))}
          </div>
        </div>
        <div className="card" style={{ background: '#F7F5ED', color: 'var(--ink)', transform: 'rotate(1.2deg)' }}>
          <p className="tiny muted">今日学习计划 · 10 月 13 日</p>
          <div className="mt-md col gap-md">
            {plans.map((p) => (
              <div className="row-between stamp-card" style={{ padding: '14px 16px' }} key={p.title}>
                <div>
                  <b className="small">{p.title}</b>
                  <p className="tiny muted">{p.reason}</p>
                </div>
                <span className="text-stamp">→</span>
              </div>
            ))}
          </div>
          <div className="ring stamp-ring mt-lg" style={{ '--p': 66, '--size': '92px', marginInline: 'auto' } as CSSVars}>
            <span>66%</span>
          </div>
          <p className="tiny muted center mt-md">今日目标 30 XP · 已得 20</p>
        </div>
      </div>
    </section>
  )
}

/* ---- 社区与成就 ---- */
function Community() {
  const badges = [
    { cls: '', main: ['FIRST', 'STEP'], date: '初学者' },
    { cls: 'ink', main: ['10', 'WORDS'], date: '词汇新星' },
    { cls: '', color: 'var(--gold)', main: ['PERFECT'], date: '满分达人' },
    { cls: '', color: 'var(--lang-ko)', main: ['3', 'DAYS'], date: '勤学不辍' },
  ]
  return (
    <section className="wrap page">
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 40, alignItems: 'center' }}>
        <div>
          <h2 className="serif-cn" style={{ fontSize: 30 }}>
            旅伴与徽章
          </h2>
          <p className="muted mt-lg" style={{ lineHeight: 1.9 }}>
            在社区用明信片记录学习日常，给同好点个赞。八枚徽章等你收集：第一节课、十个单词、一次满分、连续三天……每一枚都是护照上真实的印章。
          </p>
          <div className="row gap-md mt-xl" style={{ flexWrap: 'wrap' }}>
            {badges.map((b) => (
              <span
                key={b.date}
                className={`rubber sm${b.cls ? ` ${b.cls}` : ''}`}
                style={{ '--s': '70px', ...(b.color ? { color: b.color } : {}) } as CSSVars}
              >
                <span className="r-main">
                  {b.main.map((line, i) => (
                    <span key={i}>
                      {line}
                      {i < b.main.length - 1 && <br />}
                    </span>
                  ))}
                </span>
                <span className="r-date">{b.date}</span>
              </span>
            ))}
          </div>
        </div>
        <div className="stamp-card" style={{ transform: 'rotate(-1deg)' }}>
          <div className="row gap-md">
            <span className="avatar ja">芽</span>
            <div>
              <b className="small">樱芽めい</b>
              <p className="tiny muted">日本語 · 2 小时前</p>
            </div>
            <span className="chip chip-lang-ja active" style={{ marginLeft: 'auto' }}>
              日本語
            </span>
          </div>
          <p className="mt-md" style={{ fontSize: 14.5, lineHeight: 1.8 }}>
            今天终于搞懂は和が的区别了！盖了三个章， Streak 第 14 天 ✦ 给还在 L1 的朋友打气：がんばって！
          </p>
          <div className="row gap-lg muted tiny mt-lg">
            <span>♡ 48</span>
            <span>◔ 12 条评论</span>
          </div>
        </div>
      </div>
    </section>
  )
}

/* ---- 最终 CTA + 页脚 ---- */
function FinalCta() {
  return (
    <>
      <section className="wrap" style={{ padding: '20px 28px 90px', textAlign: 'center' }}>
        <div className="visa inline" style={{ display: 'inline-block', padding: '30px 48px', transform: 'rotate(-1deg)' }}>
          <h2 className="serif-cn" style={{ fontSize: 30 }}>
            第一枚章，今天就盖
          </h2>
          <p className="muted mt-md">免费开始，无需付费，定级测试只要 5 分钟。</p>
          <Link className="btn btn-stamp btn-lg mt-lg" to="/register">
            免费创建我的护照
          </Link>
        </div>
      </section>
      <footer className="cover">
        <div className="wrap row-between tiny muted" style={{ padding: '26px 28px' }}>
          <span>© 2026 LinguaVerse · 多语种学习平台原型</span>
          <span>English · 日本語 · 한국어</span>
        </div>
      </footer>
    </>
  )
}
