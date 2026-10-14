import { Link, Outlet, useMatches } from 'react-router-dom'
import { useSession } from '../lib/session'
import { signOut } from '../lib/sdk'

const NAV = [
  { key: 'home', label: '今日', icon: '✦', path: '/home' },
  { key: 'courses', label: '课程', icon: '⛰', path: '/courses' },
  { key: 'review', label: '复习', icon: '◍', path: '/review' },
  { key: 'community', label: '社区', icon: '✉', path: '/community' },
  { key: 'achievements', label: '成就', icon: '★', path: '/achievements' },
  { key: 'dashboard', label: '我的', icon: '☻', path: '/dashboard' },
]

interface RouteHandle {
  nav?: string
}

/** 学员端外壳：sticky 顶栏 + 移动端底部 tabbar（≤860px 显示） */
export default function LearnerShell() {
  const matches = useMatches()
  const user = useSession((s) => s.user)
  const active = (matches[matches.length - 1]?.handle as RouteHandle | undefined)?.nav
  return (
    <>
      <header className="topbar">
        <div className="wrap topbar-inner">
          <Link className="brand" to="/home">
            <span className="brand-mark">L</span>LinguaVerse
          </Link>
          <nav className="nav" aria-label="主导航">
            {NAV.map((n) => (
              <Link
                key={n.key}
                to={n.path}
                className={n.key === active ? 'active' : ''}
                aria-current={n.key === active ? 'page' : undefined}
              >
                <span aria-hidden="true">{n.icon}</span>
                {n.label}
              </Link>
            ))}
          </nav>
          <Link
            className="row gap-sm"
            to="/profile"
            style={{ marginLeft: 'auto' }}
            title="个人资料"
            aria-label="个人资料"
          >
            <span className="avatar" aria-hidden="true">
              {user?.avatarSeed ?? user?.username?.slice(0, 1) ?? '我'}
            </span>
          </Link>
          <button
            className="btn btn-ghost tiny"
            style={{ marginLeft: 8 }}
            onClick={signOut}
            title="退出登录"
          >
            退出
          </button>
        </div>
      </header>
      <Outlet />
      <nav className="mobile-tabbar" aria-label="主导航（移动端）">
        {NAV.map((n) => (
          <Link
            key={n.key}
            to={n.path}
            className={n.key === active ? 'active' : ''}
            aria-current={n.key === active ? 'page' : undefined}
          >
            <span aria-hidden="true">{n.icon}</span>
            {n.label}
          </Link>
        ))}
      </nav>
    </>
  )
}
