import { toast } from './toast'

const LANG_TAG: Record<string, string> = { en: 'en-US', ja: 'ja-JP', ko: 'ko-KR' }

export function langTag(langCode: string): string {
  return LANG_TAG[langCode] ?? 'en-US'
}

const AUTO_KEY = 'lv-auto-speak'

/** 闪卡切卡自动发音开关（默认开），持久化 localStorage */
export function getAutoSpeak(): boolean {
  try {
    return localStorage.getItem(AUTO_KEY) !== 'off'
  } catch {
    return true
  }
}

export function setAutoSpeak(on: boolean) {
  try {
    localStorage.setItem(AUTO_KEY, on ? 'on' : 'off')
  } catch {
    /* 忽略存储失败 */
  }
}

let voicesCache: SpeechSynthesisVoice[] = []

function refreshVoices() {
  voicesCache = window.speechSynthesis.getVoices()
}

/** 语音列表异步加载：首调可能为空，需在 voiceschanged 后再取 */
function loadVoices(): Promise<SpeechSynthesisVoice[]> {
  refreshVoices()
  if (voicesCache.length) return Promise.resolve(voicesCache)
  return new Promise((resolve) => {
    const done = () => {
      window.speechSynthesis.removeEventListener('voiceschanged', done)
      refreshVoices()
      resolve(voicesCache)
    }
    window.speechSynthesis.addEventListener('voiceschanged', done)
    setTimeout(done, 1500) // 部分环境不触发事件，超时兜底
  })
}

function pickVoice(tag: string): SpeechSynthesisVoice | null {
  const exact = voicesCache.find((v) => v.lang === tag)
  if (exact) return exact
  const prefix = tag.split('-')[0]
  return voicesCache.find((v) => v.lang.startsWith(prefix)) ?? null
}

/**
 * 浏览器 TTS 朗读。失败时 toast 明确原因（无声常见于系统无对应语种语音包）。
 * cancel 与 speak 之间留 50ms，规避 Chrome 连续 cancel/speak 吞句的已知问题。
 */
export async function speak(text: string, langCode: string, rate = 0.9): Promise<void> {
  if (!('speechSynthesis' in window)) {
    toast('当前浏览器不支持语音合成，请查看文本稿')
    return
  }
  const voices = await loadVoices()
  if (!voices.length) {
    toast('系统未安装语音包，无法朗读，请使用对话稿降级')
    return
  }
  const tag = langTag(langCode)
  const voice = pickVoice(tag)
  window.speechSynthesis.cancel()
  setTimeout(() => {
    const u = new SpeechSynthesisUtterance(text)
    u.lang = tag
    u.rate = rate
    if (voice) u.voice = voice
    u.onerror = (e) => {
      if (e.error !== 'canceled' && e.error !== 'interrupted') toast('朗读失败，请查看文本稿')
    }
    window.speechSynthesis.speak(u)
  }, 50)
}
