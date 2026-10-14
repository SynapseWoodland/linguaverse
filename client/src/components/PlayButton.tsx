import { useRef, useState } from 'react'

interface PlayButtonProps {
  className?: string
  /** 播放态持续时长（原型为 2200ms） */
  duration?: number
}

/** 发音播放钮（对应原型 [data-play]：点击进入播放态，片刻后自动复原） */
export default function PlayButton({ className, duration = 2200 }: PlayButtonProps) {
  const [playing, setPlaying] = useState(false)
  const timer = useRef<ReturnType<typeof setTimeout>>(undefined)

  const onClick = () => {
    if (playing) return
    setPlaying(true)
    clearTimeout(timer.current)
    timer.current = setTimeout(() => setPlaying(false), duration)
  }

  return (
    <button type="button" className={className} data-play={playing ? 'on' : ''} onClick={onClick}>
      {playing ? '❚❚ 播放中…' : '▶ 播放'}
    </button>
  )
}
