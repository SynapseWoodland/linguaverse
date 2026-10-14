import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { useSession, type SessionUser } from './session'

const flush = () => new Promise((r) => setTimeout(r, 0))

const user: SessionUser = {
  id: 7,
  username: 'tester',
  nickname: '测试员',
  avatarSeed: 'seed-7',
  dailyGoalXp: 30,
  streak: 2,
}

describe('useSession 会话存储', () => {
  beforeEach(() => {
    localStorage.clear()
    useSession.getState().clear()
  })
  afterEach(() => localStorage.clear())

  it('初始状态为未登录', () => {
    expect(useSession.getState().token).toBeNull()
    expect(useSession.getState().user).toBeNull()
  })

  it('setSession 后内存态与 localStorage（lv-session）均写入 token/用户', async () => {
    useSession.getState().setSession('jwt-abc', user)
    await flush()

    expect(useSession.getState().token).toBe('jwt-abc')
    expect(useSession.getState().user).toEqual(user)

    const persisted = JSON.parse(localStorage.getItem('lv-session') ?? 'null')
    expect(persisted.state.token).toBe('jwt-abc')
    expect(persisted.state.user.id).toBe(7)
  })

  it('clear 清空内存态并同步到持久化层（刷新后仍为登出态）', async () => {
    useSession.getState().setSession('jwt-abc', user)
    await flush()
    useSession.getState().clear()
    await flush()

    expect(useSession.getState().token).toBeNull()
    expect(useSession.getState().user).toBeNull()
    const persisted = JSON.parse(localStorage.getItem('lv-session') ?? 'null')
    expect(persisted.state.token).toBeNull()
    expect(persisted.state.user).toBeNull()
  })
})
