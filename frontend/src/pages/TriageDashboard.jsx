import { useEffect, useState } from 'react'
import { useTriageWebSocket, ConnectionStatus } from '../hooks/useTriageWebSocket'
import { PatientCard }          from '../components/PatientCard'
import { ConnectionIndicator }  from '../components/ConnectionIndicator'
import { TriageSummaryBar }     from '../components/TriageSummaryBar'
import { ErrorBoundary }        from '../components/ErrorBoundary'
import { PatientFormModal }     from '../components/PatientFormModal'
import { useAuth }              from '../hooks/useAuth'
import { apiFetch }             from '../utils/apiClient'

/**
 * TriageDashboard
 *
 * Main view of the Echtzeit-Triage-Dashboard.
 *
 * On mount it fetches the current queue via REST (initial load before WebSocket
 * delivers its first message), then live-updates via STOMP subscription.
 *
 * Layout:
 *   ┌──────────────────────────────────────────────┐
 *   │  Header: Logo · Title · ConnectionIndicator  │
 *   │  TriageSummaryBar (level counts)             │
 *   ├──────────────────────────────────────────────┤
 *   │  PatientCard list (scrollable)               │
 *   └──────────────────────────────────────────────┘
 */
export function TriageDashboard() {
  const { auth, logout }                     = useAuth()
  const { queue: wsQueue, connectionStatus } = useTriageWebSocket(auth?.token)
  const [displayQueue, setDisplayQueue]      = useState([])
  const [lastUpdated, setLastUpdated]        = useState(null)
  const [isModalOpen, setIsModalOpen]        = useState(false)

  // ── Initial REST fetch ───────────────────────────────────────────────────
  // One-off fetch on mount to bridge the gap before the WebSocket delivers its
  // first push — not a store subscription, so setState here is intentional.
  useEffect(() => {
    apiFetch('/api/triage/queue')
      .then((res) => res.json())
      .then((data) => {
        setDisplayQueue(data)
        setLastUpdated(new Date())
      })
      .catch((err) => console.error('[Dashboard] Initialer Abruf fehlgeschlagen:', err))
  }, [])

  // ── Live WebSocket updates ───────────────────────────────────────────────
  // Mirrors the external WebSocket store (wsQueue) into local display state.
  useEffect(() => {
    if (wsQueue.length > 0 || connectionStatus === ConnectionStatus.CONNECTED) {
      // eslint-disable-next-line react-hooks/set-state-in-effect -- mirrors the external WS store into local state
      setDisplayQueue(wsQueue)
      setLastUpdated(new Date())
    }
  }, [wsQueue, connectionStatus])

  const formattedLastUpdated = lastUpdated
    ? lastUpdated.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    : null

  return (
    <div className="min-h-screen bg-[#FAFAF7]">
      {/* ── Header ──────────────────────────────────────────────────────── */}
      <header className="sticky top-0 z-10 bg-[#FAFAF7]/90 backdrop-blur-md border-b border-stone-200/60">
        <div className="max-w-5xl mx-auto px-6 py-4 flex items-center justify-between gap-4">
          {/* Left: brand + title */}
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-xl bg-stone-900 flex items-center justify-center shrink-0">
              <span className="text-white text-xs font-bold tracking-tight">T</span>
            </div>
            <div>
              <h1 className="text-[15px] font-semibold text-stone-900 leading-none">
                Triage Dashboard
              </h1>
              <p className="text-[11px] text-stone-400 mt-0.5 leading-none">
                Notaufnahme · Echtzeit
              </p>
            </div>
          </div>

          {/* Right: last updated + connection status */}
          <div className="flex items-center gap-5">
            <button 
              onClick={() => setIsModalOpen(true)}
              className="text-[12px] font-semibold text-white bg-stone-900 hover:bg-stone-800 px-3 py-1.5 rounded-lg transition-colors"
            >
              + Neuer Patient
            </button>
            {formattedLastUpdated && (
              <span className="hidden sm:block text-[11px] text-stone-400">
                Aktualisiert {formattedLastUpdated}
              </span>
            )}
            <ConnectionIndicator status={connectionStatus} />
            <div className="flex items-center gap-2 pl-4 border-l border-stone-200">
              <span className="hidden sm:block text-[11px] text-stone-500">
                {auth?.username} · {auth?.role}
              </span>
              <button
                onClick={logout}
                className="text-[11px] font-medium text-stone-400 hover:text-stone-700 hover:bg-stone-100 px-2 py-1 rounded transition-colors"
              >
                Abmelden
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* ── Page body ───────────────────────────────────────────────────── */}
      <main className="max-w-5xl mx-auto px-6 py-8 space-y-8">

        {/* Summary bar */}
        <section aria-label="Triage-Übersicht">
          <TriageSummaryBar queue={displayQueue} />
        </section>

        {/* Queue heading */}
        <section aria-label="Warteliste">
          <div className="flex items-baseline justify-between mb-4">
            <h2 className="text-[13px] font-semibold text-stone-400 uppercase tracking-widest">
              Warteliste
            </h2>
            <span className="text-[13px] text-stone-400 tabular-nums">
              {displayQueue.length} {displayQueue.length === 1 ? 'Patient' : 'Patienten'}
            </span>
          </div>

          {/* Patient list */}
          <ErrorBoundary>
            {displayQueue.length === 0 ? (
              <EmptyQueuePlaceholder connectionStatus={connectionStatus} />
            ) : (
              <ol className="space-y-3">
                {displayQueue.map((patient, index) => (
                  <li key={patient.id}>
                    <PatientCard patient={patient} position={index + 1} />
                  </li>
                ))}
              </ol>
            )}
          </ErrorBoundary>
        </section>
      </main>

      <PatientFormModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} />
    </div>
  )
}

// ── Sub-component: empty state ─────────────────────────────────────────────

function EmptyQueuePlaceholder({ connectionStatus }) {
  const isConnecting = connectionStatus === ConnectionStatus.CONNECTING

  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="w-12 h-12 rounded-2xl bg-stone-100 flex items-center justify-center mb-4">
        <span className="text-2xl">🏥</span>
      </div>
      <p className="text-[15px] font-medium text-stone-700">
        {isConnecting ? 'Verbinde mit Triage-System …' : 'Keine aktiven Patienten'}
      </p>
      <p className="text-[13px] text-stone-400 mt-1">
        {isConnecting
          ? 'Warte auf Backend-Verbindung'
          : 'Die Warteliste ist derzeit leer'}
      </p>
    </div>
  )
}
