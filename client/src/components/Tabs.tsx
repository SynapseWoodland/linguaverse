import { useState, type ReactNode } from 'react'

interface TabsProps {
  labels: ReactNode[]
  panels: ReactNode[]
  initial?: number
  className?: string
}

/** 分段页签（对应原型 .tabs + [data-tab] 切换面板 .hide） */
export default function Tabs({ labels, panels, initial = 0, className }: TabsProps) {
  const [cur, setCur] = useState(initial)
  return (
    <>
      <div className={`tabs${className ? ` ${className}` : ''}`}>
        {labels.map((label, i) => (
          <button key={i} type="button" className={i === cur ? 'active' : ''} onClick={() => setCur(i)}>
            {label}
          </button>
        ))}
      </div>
      {panels.map((panel, i) => (
        <div key={i} className={i === cur ? '' : 'hide'}>
          {panel}
        </div>
      ))}
    </>
  )
}
