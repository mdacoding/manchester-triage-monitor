import { TRIAGE_CONFIG } from '../config/triageConfig'

/**
 * TriageSummaryBar
 *
 * Compact row of per-level patient counts at the top of the dashboard.
 * Provides an at-a-glance overview of the current queue composition.
 *
 * @param {{ queue: PatientCase[] }} props
 */
export function TriageSummaryBar({ queue }) {
  const levels = ['RED', 'ORANGE', 'YELLOW', 'GREEN', 'BLUE']

  const counts = levels.reduce((acc, level) => {
    acc[level] = queue.filter((p) => p.triageLevel === level).length
    return acc
  }, {})

  return (
    <div className="flex items-center gap-3 flex-wrap">
      {levels.map((level) => {
        const config = TRIAGE_CONFIG[level]
        return (
          <div
            key={level}
            className="flex items-center gap-2 bg-white rounded-xl px-3 py-2 shadow-[0_1px_6px_rgba(0,0,0,0.05)] transition-shadow duration-300 hover:shadow-[0_2px_10px_rgba(0,0,0,0.08)]"
          >
            <span className={`w-2 h-2 rounded-full ${config.dotColor}`} />
            <span className="text-[11px] font-medium text-stone-500">{config.label}</span>
            <span className="text-[13px] font-bold text-stone-800 tabular-nums ml-1">
              {counts[level]}
            </span>
          </div>
        )
      })}
    </div>
  )
}
