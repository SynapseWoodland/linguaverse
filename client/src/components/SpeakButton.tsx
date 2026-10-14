import { useEffect, useRef, useState } from 'react'
import { speak } from '../lib/speech'

/** 🔊 单词/例句朗读钮：点击阻止冒泡（闪卡翻面），朗读期间呈播放态，重复点按重新播放。 */
export default function SpeakButton({
  text,
  langCode,
  rate = 0.9,
  label,
  className,
}: {
  text: string
  langCode: string
  rate?: number
  label: string
  className?: string
}) {
  const [playing, setPlaying] = useState(false)
  const timer = useRef<ReturnType<typeof setTimeout>>(undefined)
  useEffect(() => () => clearTimeout(timer.current), [])

  return (
    <button
      type="button"
      aria-label={label}
      title={label}
      className={className}
      style={{
        border: '1px solid var(--line, #ddd)',
        background: 'transparent',
        borderRadius: '50%',
        width: 34,
        height: 34,
        fontSize: 15,
        cursor: 'pointer',
        opacity: playing ? 1 : 0.75,
      }}
      onClick={(e) => {
        e.stopPropagation()
        if (!text) return
        setPlaying(true)
        speak(text, langCode, rate)
        clearTimeout(timer.current)
        // 按词数估算时长，到期复原播放态
        timer.current = setTimeout(() => setPlaying(false), Math.max(1200, text.length * 130))
      }}
    >
      {playing ? '❚❚' : '🔊'}
    </button>
  )
}
