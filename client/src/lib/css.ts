import type { CSSProperties } from 'react'

/** 允许内联 CSS 自定义属性（原型中的 --s / --p / --size 等变量） */
export type CSSVars = CSSProperties & Record<`--${string}`, string | number>
