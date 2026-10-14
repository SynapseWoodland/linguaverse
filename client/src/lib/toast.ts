import { create } from 'zustand'

interface ToastState {
  msg: string | null
  show: (msg: string) => void
}

let timer: ReturnType<typeof setTimeout> | undefined

/** 全局 toast（对应原型 window.lvToast：单例、1800ms 自动隐藏、重复调用重置计时） */
export const useToast = create<ToastState>()((set) => ({
  msg: null,
  show: (msg) => {
    clearTimeout(timer)
    set({ msg })
    timer = setTimeout(() => set({ msg: null }), 1800)
  },
}))

/** 非组件环境也可调用：toast('已复制') */
export const toast = (msg: string) => useToast.getState().show(msg)
