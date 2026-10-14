import type { Phase } from '../lib/routes'

const MAP: Record<Phase, [string, string]> = {
  m: ['MVP', 'phase-m'],
  '1': ['P1', 'phase-1'],
  '2': ['P2', 'phase-2'],
}

/** 右下角阶段角标（原型 body[data-phase] 的 React 形态） */
export default function PhaseBadge({ phase }: { phase: Phase }) {
  const [label, cls] = MAP[phase]
  return <div className={`phase ${cls}`}>{label} · React</div>
}
