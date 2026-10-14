import { useEffect, useState } from 'react'

/**
 * 全局离线提示条：浏览器 online/offline 事件驱动。
 * 离线时固定在顶部（z 高于 topbar），恢复联网后自动消失。
 */
export default function OfflineBanner() {
  const [online, setOnline] = useState(navigator.onLine)

  useEffect(() => {
    const on = () => setOnline(true)
    const off = () => setOnline(false)
    window.addEventListener('online', on)
    window.addEventListener('offline', off)
    return () => {
      window.removeEventListener('online', on)
      window.removeEventListener('offline', off)
    }
  }, [])

  if (online) return null
  return (
    <div
      role="status"
      aria-live="assertive"
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        zIndex: 100,
        background: 'var(--stamp)',
        color: '#fff',
        textAlign: 'center',
        fontSize: 13,
        fontWeight: 600,
        padding: '7px 12px',
        letterSpacing: '0.02em',
      }}
    >
      ⚠ 网络连接已断开，当前操作可能失败，请检查网络
    </div>
  )
}
