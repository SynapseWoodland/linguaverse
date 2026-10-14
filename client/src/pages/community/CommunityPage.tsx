
import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import { sdk, type Post } from '../../lib/sdk'
import { ApiError } from '../../lib/api'
import { Loading, ErrorState, PageTitle, timeAgo, LANG_LABEL } from '../../lib/ui'
import { toast } from '../../lib/toast'
import { useSession } from '../../lib/session'

/** P32 社区信息流：语种过滤、分页、点赞 toggle、作者删帖。 */
export default function CommunityPage({ meta: _meta }: { meta: PageMeta }) {
  const navigate = useNavigate()
  const userId = useSession((s) => s.user?.id)
  const [lang, setLang] = useState<string>('')
  const [page, setPage] = useState(1)
  const [data, setData] = useState<{ items: Post[]; totalPages: number; total: number } | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [likeBusy, setLikeBusy] = useState<number | null>(null)

  async function load(l: string, p: number) {
    setLoading(true)
    setError(null)
    try {
      const r = await sdk.posts({ lang: l || undefined, page: p, size: 10 })
      setData({ items: r.items, totalPages: r.totalPages, total: r.total })
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '加载失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load(lang, page)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [lang, page])

  function switchLang(l: string) {
    setLang(l)
    setPage(1)
  }

  async function toggleLike(p: Post) {
    setLikeBusy(p.id)
    try {
      const r = await sdk.toggleLike(p.id)
      setData((d) =>
        d
          ? {
              ...d,
              items: d.items.map((x) => (x.id === p.id ? { ...x, liked: r.liked, likeCount: r.likeCount } : x)),
            }
          : d,
      )
    } catch (e) {
      toast(e instanceof ApiError ? e.message : '操作失败')
    } finally {
      setLikeBusy(null)
    }
  }

  async function remove(p: Post) {
    if (!window.confirm('确定删除这条帖子？评论也会一并删除。')) return
    try {
      await sdk.deletePost(p.id)
      toast('帖子已删除')
      load(lang, page)
    } catch (e) {
      toast(e instanceof ApiError ? e.message : '删除失败')
    }
  }

  return (
    <div className="wrap" style={{ paddingBottom: 96 }}>
      <div className="row-between mt-lg">
        <PageTitle zh="社区信息流" sub={data ? `${data.total} 条帖子` : undefined} />
        <Link className="btn btn-stamp" to="/post-editor">
          ✎ 发帖
        </Link>
      </div>

      <div className="row gap-sm mt-md">
        <button className={`btn ${lang === '' ? 'btn-stamp' : 'btn-ghost'}`} onClick={() => switchLang('')}>
          全部
        </button>
        {['en', 'ja', 'ko'].map((l) => (
          <button key={l} className={`btn ${lang === l ? 'btn-stamp' : 'btn-ghost'}`} onClick={() => switchLang(l)}>
            {LANG_LABEL[l]}
          </button>
        ))}
      </div>

      {loading && <Loading />}
      {error && <ErrorState message={error} onRetry={() => load(lang, page)} />}

      {data && data.items.length === 0 && !loading && (
        <section className="card center mt-lg">
          <p className="muted small">这里还没有帖子，来发第一帖吧。</p>
          <Link className="btn btn-stamp mt-md" to="/post-editor">
            写帖子
          </Link>
        </section>
      )}

      <div style={{ display: 'grid', gap: 14, marginTop: 18 }}>
        {data?.items.map((p) => (
          <article key={p.id} className="card">
            <div className="row-between">
              <div className="row gap-sm">
                <span className="avatar tiny">{p.author.avatarSeed}</span>
                <div>
                  <p className="small" style={{ fontWeight: 700 }}>
                    {p.author.nickname}
                  </p>
                  <p className="tiny muted">
                    {p.langCode ? LANG_LABEL[p.langCode] : '闲聊'} · {timeAgo(p.createdAt)}
                  </p>
                </div>
              </div>
              {p.author.id === userId && (
                <button className="btn btn-ghost tiny" onClick={() => remove(p)}>
                  删除
                </button>
              )}
            </div>
            {p.title && (
              <h3 className="serif-cn mt-md" style={{ fontSize: 19 }}>
                {p.title}
              </h3>
            )}
            <p className="small mt-sm" style={{ lineHeight: 1.8, whiteSpace: 'pre-wrap' }}>
              {p.content}
            </p>
            <div className="row gap-md mt-md">
              <button
                className={`btn btn-ghost tiny ${p.liked ? 'is-liked' : ''}`}
                disabled={likeBusy === p.id}
                onClick={() => toggleLike(p)}
                aria-pressed={p.liked}
                aria-label={`${p.liked ? '取消点赞' : '点赞'}（当前 ${p.likeCount} 赞）`}
                style={p.liked ? { color: 'var(--stamp)', borderColor: 'var(--stamp)' } : undefined}
              >
                <span aria-hidden="true">{p.liked ? '♥' : '♡'}</span> {p.likeCount}
              </button>
              <button className="btn btn-ghost tiny" onClick={() => navigate('/post-detail', { state: { post: p } })}>
                ✉ {p.commentCount} 条评论
              </button>
            </div>
          </article>
        ))}
      </div>

      {data && data.totalPages > 1 && (
        <div className="row gap-sm center mt-lg">
          <button className="btn btn-ghost" disabled={page <= 1} onClick={() => { const p = page - 1; setPage(p); load(lang, p) }}>
            上一页
          </button>
          <span className="small muted">
            {page} / {data.totalPages}
          </span>
          <button className="btn btn-ghost" disabled={page >= data.totalPages} onClick={() => { const p = page + 1; setPage(p); load(lang, p) }}>
            下一页
          </button>
        </div>
      )}
    </div>
  )
}
