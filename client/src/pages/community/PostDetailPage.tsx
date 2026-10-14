import { useEffect, useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import type { PageMeta } from '../../lib/routes'
import { sdk, type Post, type Comment } from '../../lib/sdk'
import { ApiError } from '../../lib/api'
import { Loading, timeAgo, LANG_LABEL } from '../../lib/ui'
import { toast } from '../../lib/toast'
import { useSession } from '../../lib/session'

/** P34 帖子详情：正文 + 盖楼评论 + 点赞 + 作者删除。 */
export default function PostDetailPage({ meta: _meta }: { meta: PageMeta }) {
  const location = useLocation()
  const navigate = useNavigate()
  const userId = useSession((s) => s.user?.id)
  const statePost = (location.state as { post?: Post; justCreated?: number } | null)?.post
  const justCreated = (location.state as { justCreated?: number } | null)?.justCreated
  const [post, setPost] = useState<Post | null>(statePost ?? null)
  const [comments, setComments] = useState<Comment[]>([])
  const [loading, setLoading] = useState(!statePost)
  const [error, setError] = useState<string | null>(null)
  const [text, setText] = useState('')
  const [busy, setBusy] = useState(false)

  const postId = post?.id ?? justCreated

  useEffect(() => {
    let alive = true
    async function boot() {
      try {
        if (!post && postId) {
          // 无 GET 单帖端点：从信息流中定位（MVP 语料规模足够）
          const feed = await sdk.posts({ size: 50 })
          const found = feed.items.find((p) => p.id === postId)
          if (!found) {
            if (alive) setError('帖子不存在或已被删除')
            return
          }
          if (alive) setPost(found)
        }
        if (postId) {
          const cs = await sdk.comments(postId)
          if (alive) setComments(cs)
        }
      } catch (e) {
        if (alive) setError(e instanceof ApiError ? e.message : '加载失败')
      } finally {
        if (alive) setLoading(false)
      }
    }
    boot()
    return () => {
      alive = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [postId])

  async function submitComment(e: FormEvent) {
    e.preventDefault()
    if (!text.trim() || !post) return
    setBusy(true)
    try {
      const c = await sdk.addComment(post.id, text.trim())
      setComments((cs) => [...cs, c])
      setPost((p) => (p ? { ...p, commentCount: p.commentCount + 1 } : p))
      setText('')
    } catch (err) {
      toast(err instanceof ApiError ? err.message : '评论失败')
    } finally {
      setBusy(false)
    }
  }

  async function like() {
    if (!post) return
    try {
      const r = await sdk.toggleLike(post.id)
      setPost({ ...post, liked: r.liked, likeCount: r.likeCount })
    } catch (e) {
      toast(e instanceof ApiError ? e.message : '操作失败')
    }
  }

  async function remove() {
    if (!post || !window.confirm('确定删除这条帖子？')) return
    try {
      await sdk.deletePost(post.id)
      toast('帖子已删除')
      navigate('/community')
    } catch (e) {
      toast(e instanceof ApiError ? e.message : '删除失败')
    }
  }

  if (loading) return <Loading tip="帖子加载中…" />
  if (error || !post)
    return (
      <div className="wrap center" style={{ paddingTop: 80 }}>
        <p className="text-stamp">✗ {error ?? '帖子不存在'}</p>
        <Link className="btn btn-ghost mt-md" to="/community">
          回社区
        </Link>
      </div>
    )

  return (
    <div className="wrap" style={{ paddingBottom: 96, maxWidth: 720 }}>
      <Link className="small muted" to="/community">
        ← 社区信息流
      </Link>

      <article className="card mt-md">
        <div className="row-between">
          <div className="row gap-sm">
            <span className="avatar">{post.author.avatarSeed}</span>
            <div>
              <p className="small" style={{ fontWeight: 700 }}>
                {post.author.nickname}
              </p>
              <p className="tiny muted">
                {post.langCode ? LANG_LABEL[post.langCode] : '闲聊'} · {timeAgo(post.createdAt)}
              </p>
            </div>
          </div>
          {post.author.id === userId && (
            <button className="btn btn-ghost tiny" onClick={remove}>
              删除
            </button>
          )}
        </div>
        {post.title && (
          <h1 className="serif-cn mt-md" style={{ fontSize: 26 }}>
            {post.title}
          </h1>
        )}
        <p className="small mt-md" style={{ lineHeight: 1.9, whiteSpace: 'pre-wrap' }}>
          {post.content}
        </p>
        <button
          className="btn btn-ghost mt-md"
          onClick={like}
          aria-pressed={post.liked}
          aria-label={`${post.liked ? '取消点赞' : '点赞'}（当前 ${post.likeCount} 赞）`}
          style={post.liked ? { color: 'var(--stamp)', borderColor: 'var(--stamp)' } : undefined}
        >
          <span aria-hidden="true">{post.liked ? '♥' : '♡'}</span> {post.likeCount}
        </button>
      </article>

      <h2 className="serif-cn mt-lg" style={{ fontSize: 18 }}>
        评论 {comments.length}
      </h2>
      <form className="card mt-sm" onSubmit={submitComment}>
        <textarea
          className="input"
          rows={3}
          maxLength={1000}
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="写下你的评论…"
          aria-label="评论内容"
          style={{ resize: 'vertical' }}
        />
        <div className="row-between mt-sm">
          <span className="tiny muted">{text.length}/1000</span>
          <button className="btn btn-stamp" disabled={busy || !text.trim()}>
            {busy ? '发送中…' : '发表评论'}
          </button>
        </div>
      </form>

      <div style={{ display: 'grid', gap: 10, marginTop: 14 }}>
        {comments.map((c) => (
          <div key={c.id} className="card">
            <div className="row gap-sm">
              <span className="avatar tiny">{c.author.avatarSeed}</span>
              <p className="small" style={{ fontWeight: 700 }}>
                {c.author.nickname}
              </p>
              <p className="tiny muted">{timeAgo(c.createdAt)}</p>
            </div>
            <p className="small mt-sm" style={{ lineHeight: 1.8, whiteSpace: 'pre-wrap' }}>
              {c.content}
            </p>
          </div>
        ))}
        {comments.length === 0 && <p className="small muted center mt-lg">还没有评论，抢个沙发。</p>}
      </div>
    </div>
  )
}
