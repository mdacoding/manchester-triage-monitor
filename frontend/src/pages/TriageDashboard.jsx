import { useEffect, useState } from 'react'
import { useTriageWebSocket, ConnectionStatus } from '../hooks/useTriageWebSocket'
import { PatientCard }          from '../components/PatientCard'
import { ConnectionIndicator }  from '../components/ConnectionIndicator'
import { TriageSummaryBar }     from '../components/TriageSummaryBar'
import { ErrorBoundary }        from '../components/ErrorBoundary'
import { PatientFormModal }     from '../components/PatientFormModal'
import { PatientHistory }       from './PatientHistory'
import { useAuth }              from '../hooks/useAuth'
import { apiFetch }             from '../utils/apiClient'

const VIEWS = {
  QUEUE:   'queue',
  HISTORY: 'history',
}

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
  const [activeView, setActiveView]          = useState(VIEWS.QUEUE)
  // True until the first data (REST or WebSocket) has arrived — drives the
  // skeleton loader so the queue never flashes an empty/"no patients" state
  // just because the network hasn't responded yet.
  const [isInitialLoading, setIsInitialLoading] = useState(true)

  // ── Initial REST fetch ───────────────────────────────────────────────────
  // One-off fetch on mount to bridge the gap before the WebSocket delivers its
  // first push — not a store subscription, so setState here is intentional.
  useEffect(() => {
    let cancelled = false
    apiFetch('/api/triage/queue')
      .then((res) => res.json())
      .then((data) => {
        if (cancelled) return
        setDisplayQueue(data)
        setLastUpdated(new Date())
        setIsInitialLoading(false)
      })
      .catch((err) => {
        console.error('[Dashboard] Initialer Abruf fehlgeschlagen:', err)
        if (!cancelled) setIsInitialLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  // Wenn Live-WS (CORS auf Render) fehlt: Warteliste per REST weiter aktualisieren.
  useEffect(() => {
    if (connectionStatus === ConnectionStatus.CONNECTED) return undefined
    const id = setInterval(() => {
      apiFetch('/api/triage/queue')
        .then((res) => (res.ok ? res.json() : Promise.reject(res.status)))
        .then((data) => {
          setDisplayQueue(data)
          setLastUpdated(new Date())
          setIsInitialLoading(false)
        })
        .catch(() => {})
    }, 8000)
    return () => clearInterval(id)
  }, [connectionStatus])

  // ── Live WebSocket updates ───────────────────────────────────────────────
  // Mirrors the external WebSocket store (wsQueue) into local display state.
  useEffect(() => {
    if (wsQueue.length > 0 || connectionStatus === ConnectionStatus.CONNECTED) {
      // eslint-disable-next-line react-hooks/set-state-in-effect -- mirrors the external WS store into local state
      setDisplayQueue(wsQueue)
      setLastUpdated(new Date())
      setIsInitialLoading(false)
    }
  }, [wsQueue, connectionStatus])

  const formattedLastUpdated = lastUpdated
    ? lastUpdated.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    : null

  return (
    <div className="min-h-screen bg-[#FAFAF7]">
      {/* ── Header ──────────────────────────────────────────────────────── */}
      <header className="sticky top-0 z-10 bg-[#FAFAF7]/90 backdrop-blur-md border-b border-stone-200/60">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 py-4 flex flex-wrap items-center justify-between gap-x-4 gap-y-2">
          {/* Left: brand + title */}
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-xl bg-stone-900 flex items-center justify-center shrink-0">
              <span className="text-white text-xs font-bold tracking-tight">T</span>
            </div>
            <div>
              <h1 className="text-[15px] font-semibold text-stone-900 leading-none">
                Triage Dashboard
              </h1>
              <p className="text-[11px] text-stone-500 mt-0.5 leading-none">
                Notaufnahme · Echtzeit
              </p>
            </div>
          </div>

          {/* Right: last updated + connection status */}
          <div className="flex items-center gap-2 sm:gap-5">
            <button
              onClick={() => setIsModalOpen(true)}
              className="text-[12px] font-semibold text-white bg-stone-900 hover:bg-stone-800 active:bg-stone-950 px-3 py-1.5 rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-stone-900/40 focus-visible:ring-offset-2 focus-visible:ring-offset-[#FAFAF7]"
              aria-label="Neuen Patienten aufnehmen"
            >
              <span aria-hidden="true">+</span> <span className="hidden sm:inline">Neuer Patient</span>
            </button>
            {formattedLastUpdated && (
              <span className="hidden md:block text-[11px] text-stone-500 whitespace-nowrap">
                Aktualisiert {formattedLastUpdated}
              </span>
            )}
            <ConnectionIndicator status={connectionStatus} />
            <div className="flex items-center gap-2 pl-2 sm:pl-4 border-l border-stone-200">
              <span className="hidden sm:block text-[11px] text-stone-500 whitespace-nowrap">
                {auth?.username} · {auth?.role}
              </span>
              <button
                onClick={logout}
                className="text-[11px] font-medium text-stone-500 hover:text-stone-700 hover:bg-stone-100 px-2 py-1 rounded transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-stone-900/40"
              >
                Abmelden
              </button>
            </div>
          </div>
        </div>

        {/* Tab navigation: switch between active queue and archive/history */}
        <nav
          className="max-w-5xl mx-auto px-4 sm:px-6 flex items-center gap-1 -mt-1 pb-3"
          role="tablist"
          aria-label="Ansicht wählen"
        >
          <ViewTabButton
            label="Warteliste"
            isActive={activeView === VIEWS.QUEUE}
            onClick={() => setActiveView(VIEWS.QUEUE)}
          />
          <ViewTabButton
            label="Historie"
            isActive={activeView === VIEWS.HISTORY}
            onClick={() => setActiveView(VIEWS.HISTORY)}
          />
        </nav>
      </header>

      {/* ── Page body ───────────────────────────────────────────────────── */}
      <main className="max-w-5xl mx-auto px-4 sm:px-6 py-8 space-y-8">
        {activeView === VIEWS.QUEUE ? (
          <>
            {/* Summary bar */}
            <section aria-label="Triage-Übersicht">
              <TriageSummaryBar queue={displayQueue} />
            </section>

            {/* Queue heading */}
            <section aria-label="Warteliste">
              <div className="flex items-baseline justify-between mb-4">
                <h2 className="text-[13px] font-semibold text-stone-500 uppercase tracking-widest">
                  Warteliste
                </h2>
                {!isInitialLoading && (
                  <span className="text-[13px] text-stone-500 tabular-nums">
                    {displayQueue.length} {displayQueue.length === 1 ? 'Patient' : 'Patienten'}
                  </span>
                )}
              </div>

              {/* Patient list */}
              <ErrorBoundary>
                {isInitialLoading ? (
                  <QueueSkeleton />
                ) : displayQueue.length === 0 ? (
                  <EmptyQueuePlaceholder />
                ) : (
                  <ol className="space-y-3">
                    {displayQueue.map((patient, index) => (
                      <li key={patient.id} className="transition-all duration-300 ease-out">
                        <PatientCard patient={patient} position={index + 1} />
                      </li>
                    ))}
                  </ol>
                )}
              </ErrorBoundary>
            </section>
          </>
        ) : (
          <ErrorBoundary>
            <PatientHistory />
          </ErrorBoundary>
        )}
      </main>

      <PatientFormModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} />
    </div>
  )
}

// ── Sub-component: view tab button ──────────────────────────────────────────

function ViewTabButton({ label, isActive, onClick }) {
  return (
    <button
      role="tab"
      aria-selected={isActive}
      onClick={onClick}
      className={[
        'text-[13px] font-medium px-3 py-1.5 rounded-lg transition-colors',
        'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-stone-900/40 focus-visible:ring-offset-2 focus-visible:ring-offset-[#FAFAF7]',
        isActive
          ? 'bg-stone-900 text-white'
          : 'text-stone-500 hover:text-stone-800 hover:bg-stone-100',
      ].join(' ')}
    >
      {label}
    </button>
  )
}

// ── Sub-component: empty state ─────────────────────────────────────────────

function EmptyQueuePlaceholder() {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center animate-fade-slide-in">
      <div className="w-14 h-14 rounded-2xl bg-stone-100 flex items-center justify-center mb-4">
        <span className="text-3xl" role="img" aria-label="Klinikum">🏥</span>
      </div>
      <p className="text-[15px] font-medium text-stone-700">
        Keine Patient:innen in der Warteschlange
      </p>
      <p className="text-[13px] text-stone-500 mt-1 max-w-xs">
        Sobald ein:e neue:r Patient:in aufgenommen wird, erscheint sie oder er automatisch hier.
      </p>
    </div>
  )
}

// ── Sub-component: loading skeleton ─────────────────────────────────────────

/**
 * Shown while the initial queue data is being fetched (before the REST
 * response or the first WebSocket push arrives), so the dashboard never
 * flashes an empty state that could be mistaken for "no patients waiting".
 */
function QueueSkeleton() {
  return (
    <ol className="space-y-3" aria-label="Warteliste wird geladen" aria-busy="true">
      {[0, 1, 2].map((i) => (
        <li
          key={i}
          className="flex items-stretch gap-0 bg-white rounded-2xl overflow-hidden shadow-[0_2px_12px_rgba(0,0,0,0.06)] border-l-4 border-stone-200 animate-pulse"
          style={{ animationDelay: `${i * 100}ms` }}
        >
          <div className="w-12 shrink-0 bg-stone-50 border-r border-stone-100" />
          <div className="flex flex-col justify-between flex-1 px-5 py-4 gap-3">
            <div className="flex items-center justify-between gap-4">
              <div className="h-4 w-32 bg-stone-200 rounded" />
              <div className="h-5 w-20 bg-stone-100 rounded-full" />
            </div>
            <div className="h-3 w-3/4 bg-stone-100 rounded" />
            <div className="flex items-center gap-5 pt-1 border-t border-stone-100">
              <div className="h-6 w-14 bg-stone-100 rounded" />
              <div className="h-6 w-14 bg-stone-100 rounded" />
              <div className="h-6 w-14 bg-stone-100 rounded" />
            </div>
          </div>
        </li>
      ))}
    </ol>
  )
}
