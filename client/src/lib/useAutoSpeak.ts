import { useEffect } from 'react'
import { getAutoSpeak, speak } from './speech'

/** 闪卡切卡时自动朗读（开关存 localStorage，见 getAutoSpeak）。 */
export function useAutoSpeak(text: string | undefined, langCode: string | undefined) {
  useEffect(() => {
    if (text && langCode && getAutoSpeak()) speak(text, langCode, 0.85)
  }, [text, langCode])
}
