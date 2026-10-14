import { useEffect, useState } from 'react'
import type { PageMeta } from '../../lib/routes'
import { sdk } from '../../lib/sdk'
import { ApiError } from '../../lib/api'
import { Loading, ErrorState, PageTitle, LANG_LABEL } from '../../lib/ui'
import { toast } from '../../lib/toast'
import { useSession, type SessionUser } from '../../lib/session'
import { signOut } from '../../lib/sdk'

const GOAL_TIERS = [10, 30, 60, 100]

/** P40 个人资料 + P42 每日目标四档（MVP 合并一页）。 */
export default function ProfilePage({ meta: _meta }: { meta: PageMeta }) {
  const sessionUser = useSession((s) => s.user)
  const setSession = useSession((s) => s.setSession)
  const token = useSession((s) => s.token)
  const [me, setMe] = useState<Awaited<ReturnType<typeof sdk.me>> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [nickname, setNickname] = useState('')
  const [bio, setBio] = useState('')
  const [avatarSeed, setAvatarSeed] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    sdk
      .me()
      .then((d) => {
        setMe(d)
        setNickname(d.user.nickname ?? '')
        setBio(d.user.bio ?? '')
        setAvatarSeed(d.user.avatarSeed ?? '')
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : '加载失败'))
      .finally(() => setLoading(false))
  }, [])

  async function save(patch: { nickname?: string; bio?: string; avatarSeed?: string; dailyGoalXp?: number }) {
    setBusy(true)
    try {
      const d = await sdk.updateMe(patch)
      setMe((prev) => (prev ? { ...prev, user: { ...prev.user, ...d } } : prev))
      if (sessionUser && token) {
        const u: SessionUser = {
          id: d.id!,
          username: d.username,
          nickname: d.nickname,
          avatarSeed: d.avatarSeed,
          dailyGoalXp: d.dailyGoalXp ?? 30,
          streak: d.streak ?? sessionUser.streak,
          onboarded: d.onboarded ?? sessionUser.onboarded,
        }
        setSession(token, u)
      }
      toast('已保存')
    } catch (e) {
      toast(e instanceof ApiError ? e.message : '保存失败')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="wrap" style={{ paddingBottom: 96, maxWidth: 680 }}>
      <PageTitle zh="个人资料" sub="资料、头像与每日学习目标。" />

      {loading && <Loading />}
      {error && <ErrorState message={error} />}

      {me && (
        <>
          <section className="card stamp-card mt-lg row gap-md">
            <span className="avatar" style={{ width: 56, height: 56, fontSize: 24 }}>
              {me.user.avatarSeed}
            </span>
            <div>
              <p className="serif-cn" style={{ fontSize: 20 }}>
                {me.user.nickname}
              </p>
              <p className="tiny muted">@{me.user.username}</p>
              <p className="tiny muted mt-sm">
                连续 {me.user.streak ?? 0} 天 · 已加入 {me.enrollments.length} 个语种（
                {me.enrollments.map((e) => LANG_LABEL[e.langCode]).join(' / ') || '暂无'}）
              </p>
            </div>
          </section>

          <section className="card mt-lg">
            <h2 className="serif-cn" style={{ fontSize: 18 }}>
              资料
            </h2>
            <div className="field mt-md">
              <label htmlFor="profile-nickname">昵称</label>
              <input id="profile-nickname" className="input" value={nickname} onChange={(e) => setNickname(e.target.value)} maxLength={24} />
            </div>
            <div className="field">
              <label htmlFor="profile-avatar">头像种子（1 个字符，显示为圆形头像）</label>
              <input id="profile-avatar" className="input" value={avatarSeed} onChange={(e) => setAvatarSeed(e.target.value.slice(0, 2))} maxLength={2} />
            </div>
            <div className="field">
              <label htmlFor="profile-bio">简介</label>
              <textarea id="profile-bio" className="input" rows={3} value={bio} onChange={(e) => setBio(e.target.value)} maxLength={200} style={{ resize: 'vertical' }} />
            </div>
            <button className="btn btn-primary" disabled={busy} onClick={() => save({ nickname: nickname.trim(), bio: bio.trim(), avatarSeed: avatarSeed.trim() })}>
              保存资料
            </button>
          </section>

          <section className="card mt-lg">
            <h2 className="serif-cn" style={{ fontSize: 18 }}>
              每日目标
            </h2>
            <p className="small muted mt-sm">当前目标：{me.user.dailyGoalXp} XP / 天，仅支持四档。</p>
            <div className="row gap-sm mt-md" role="group" aria-label="每日目标档位">
              {GOAL_TIERS.map((g) => (
                <button
                  key={g}
                  className={`btn ${me.user.dailyGoalXp === g ? 'btn-stamp' : 'btn-ghost'}`}
                  disabled={busy}
                  aria-pressed={me.user.dailyGoalXp === g}
                  onClick={() => save({ dailyGoalXp: g })}
                >
                  {g} XP
                </button>
              ))}
            </div>
          </section>

          <div className="center mt-lg">
            <button className="btn btn-ghost" onClick={signOut}>
              退出登录
            </button>
          </div>
        </>
      )}
    </div>
  )
}
