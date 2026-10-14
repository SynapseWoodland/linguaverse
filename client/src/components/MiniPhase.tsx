import type { Phase } from '../lib/routes'

const MAP: Record<Phase, [string, string]> = {
  m: ['MVP', 'mini-m'],
  '1': ['P1', 'mini-1'],
  '2': ['P2', 'mini-2'],
}

/** /archive 索引条目用的小号阶段角标 */
export default function MiniPhase({ phase }: { phase: Phase }) {
  const [label, cls] = MAP[phase]
  return <span className={`mini-phase ${cls}`}>{label}</span>
}
