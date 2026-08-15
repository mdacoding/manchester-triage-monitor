import { ConnectionStatus } from '../hooks/useTriageWebSocket'

/**
 * ConnectionIndicator
 *
 * Displays the live WebSocket connection status as a small, pulsing dot
 * with a descriptive label — keeps the header uncluttered but informative.
 *
 * @param {{ status: string }} props
 */
export function ConnectionIndicator({ status }) {
  // Text colors use the -700 shade (rather than -600) to meet WCAG AA
  // contrast (≥ 4.5:1) for small text on the light background.
  const config = {
    [ConnectionStatus.CONNECTED]:  { dot: 'bg-emerald-400 animate-pulse-dot', label: 'Live',         textColor: 'text-emerald-700' },
    [ConnectionStatus.CONNECTING]: { dot: 'bg-amber-400  animate-pulse-dot', label: 'Verbinde …',   textColor: 'text-amber-700'   },
    [ConnectionStatus.ERROR]:      { dot: 'bg-red-400',                       label: 'Verbindungsfehler', textColor: 'text-red-700' },
  }[status] ?? { dot: 'bg-stone-400', label: status, textColor: 'text-stone-600' }

  return (
    <div className="flex items-center gap-2">
      <span className={`w-2 h-2 rounded-full shrink-0 ${config.dot}`} />
      <span className={`text-xs font-medium ${config.textColor}`}>
        {config.label}
      </span>
    </div>
  )
}
