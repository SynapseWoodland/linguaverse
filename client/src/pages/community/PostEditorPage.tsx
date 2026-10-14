import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import { sdk } from '../../lib/sdk'
import { ApiError } from '../../lib/api'
import { PageTitle, LANG_LABEL } from '../../lib/ui'

/** P33 发帖编辑器：可选语种/标题，正文必填；首帖将解锁 social 成就。 */
export default function PostEditorPage({ meta: _meta }: { meta: PageMeta }) {
  const navigate = useNavigate()
  const [langCode, setLangCode] = useState('en')
  const [noLang, setNoLang] = useState(false)
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function submit(e: FormEvent) {
    e.preventDefault()
    if (!content.trim()) {
      setError('内容不能为空')
      return
    }
    setBusy(true)
    setError(null)
    try {
      const r = await sdk.createPost({
        langCode: noLang ? undefined : langCode,
        title: title.trim() || undefined,
        content: content.trim(),
      })
      navigate('/post-detail', { state: { justCreated: r.id } })
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '发布失败，请重试')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="wrap" style={{ paddingBottom: 96, maxWidth: 680 }}>
      <PageTitle zh="发布新帖" sub="分享学习心得、提问或打卡，发布第一条帖子可解锁「社区新星」。" />

      <form className="card mt-lg" onSubmit={submit}>
        <div className="field">
          <span id="post-lang-label" className="hint" style={{ display: 'block', marginBottom: 6 }}>
            关联语种
          </span>
          <div className="row gap-sm" role="group" aria-labelledby="post-lang-label">
            {['en', 'ja', 'ko'].map((l) => (
              <button
                type="button"
                key={l}
                className={`btn ${!noLang && langCode === l ? 'btn-stamp' : 'btn-ghost'}`}
                disabled={noLang}
                aria-pressed={!noLang && langCode === l}
                onClick={() => setLangCode(l)}
              >
                {LANG_LABEL[l]}
              </button>
            ))}
            <button
              type="button"
              className={`btn ${noLang ? 'btn-stamp' : 'btn-ghost'}`}
              aria-pressed={noLang}
              onClick={() => setNoLang(true)}
            >
              闲聊（不关联语种）
            </button>
            {noLang && (
              <button type="button" className="btn btn-ghost tiny" onClick={() => setNoLang(false)}>
                重新选语种
              </button>
            )}
          </div>
        </div>
        <div className="field">
          <label htmlFor="post-title">标题（可选）</label>
          <input id="post-title" className="input" maxLength={128} value={title} onChange={(e) => setTitle(e.target.value)} placeholder="一句话标题" />
        </div>
        <div className="field">
          <label htmlFor="post-content">内容</label>
          <textarea
            id="post-content"
            className="input"
            rows={8}
            maxLength={2000}
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="说点什么…"
            style={{ resize: 'vertical' }}
          />
          <span className="hint">{content.length}/2000</span>
        </div>
        {error && <p className="small text-stamp">✗ {error}</p>}
        <div className="row gap-md mt-md">
          <button className="btn btn-stamp btn-lg" type="submit" disabled={busy}>
            {busy ? '发布中…' : '发布'}
          </button>
          <Link className="btn btn-ghost" to="/community">
            取消
          </Link>
        </div>
      </form>
    </div>
  )
}
