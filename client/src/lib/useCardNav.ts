import { useState } from 'react'

/** 闪卡堆导航（对应原型 [data-card-nav]：prev/next 边界内移动 + 「n / total」计数） */
export function useCardNav(total: number) {
  const [index, setIndex] = useState(0)
  const go = (dir: 'prev' | 'next') =>
    setIndex((i) => Math.max(0, Math.min(total - 1, i + (dir === 'next' ? 1 : -1))))
  return { index, go, label: `${Math.min(index + 1, total)} / ${total}` }
}
