import { useState, type ReactNode } from 'react'

interface FlipCardProps {
  front: ReactNode
  back: ReactNode
  className?: string
  /** 受控用法（可选）：外部接管翻面状态 */
  flipped?: boolean
  onFlip?: (flipped: boolean) => void
}

/** 翻卡（对应原型 .flip > .flip-inner > .flip-face×2，点击翻面） */
export default function FlipCard({ front, back, className, flipped, onFlip }: FlipCardProps) {
  const [inner, setInner] = useState(false)
  const isFlipped = flipped ?? inner
  const toggle = () => (onFlip ? onFlip(!isFlipped) : setInner(!isFlipped))
  return (
    <div className={`flip${isFlipped ? ' flipped' : ''}${className ? ` ${className}` : ''}`} onClick={toggle}>
      <div className="flip-inner">
        <div className="flip-face">{front}</div>
        <div className="flip-face flip-back">{back}</div>
      </div>
    </div>
  )
}
