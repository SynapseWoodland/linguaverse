import { create } from 'zustand'
import { persist } from 'zustand/middleware'

/** 与 server/CONTRACT.md 注册/登录响应体对齐 */
export interface SessionUser {
  id: number
  username: string
  nickname: string
  avatarSeed: string
  dailyGoalXp: number
  streak: number
  onboarded?: boolean
}

interface SessionState {
  token: string | null
  user: SessionUser | null
  setSession: (token: string, user: SessionUser) => void
  clear: () => void
}

/** 会话持久化（localStorage），刷新保持登录态 */
export const useSession = create<SessionState>()(
  persist(
    (set) => ({
      token: null,
      user: null,
      setSession: (token, user) => set({ token, user }),
      clear: () => set({ token: null, user: null }),
    }),
    { name: 'lv-session' },
  ),
)
