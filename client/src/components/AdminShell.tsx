import { useEffect } from 'react'
import { Link, Outlet, useMatches } from 'react-router-dom'

const ITEMS = [
  { key: 'dash', label: '数据看板', path: '/admin-dashboard' },
  { key: 'users', label: '用户管理', path: '/admin-users' },
  { key: 'courses', label: '课程内容', path: '/admin-courses' },
  { key: 'mod', label: '内容审核', path: '/admin-moderation' },
]

interface RouteHandle {
  nav?: string
}

/** 管理后台外壳：232px 深色侧边栏 + admin-main（≤860px 折为顶部横滚） */
export default function AdminShell() {
  const matches = useMatches()
  const active = (matches[matches.length - 1]?.handle as RouteHandle | undefined)?.nav

  useEffect(() => {
    document.body.classList.add('admin-body')
    return () => document.body.classList.remove('admin-body')
  }, [])

  return (
    <>
      <aside className="admin-side">
        <div className="brand" style={{ color: '#fff', margin: '6px 10px 22px', fontSize: 18 }}>
          <span className="brand-mark">L</span>LV 后台
        </div>
        {ITEMS.map((i) => (
          <Link key={i.key} to={i.path} className={i.key === active ? 'active' : ''}>
            {i.label}
          </Link>
        ))}
        <div style={{ position: 'absolute', bottom: 18, left: 14, right: 14 }}>
          <Link to="/archive">← 返回视图索引</Link>
        </div>
      </aside>
      <main className="admin-main">
        <Outlet />
      </main>
    </>
  )
}
