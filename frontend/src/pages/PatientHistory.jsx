import { useEffect, useState } from 'react'
import { apiFetch } from '../utils/apiClient'
import { formatTime } from '../utils/timeUtils'
import { TRIAGE_CONFIG } from '../config/triageConfig'

const PAGE_SIZE = 20
const TRIAGE_LEVELS = ['RED', 'ORANGE', 'YELLOW', 'GREEN', 'BLUE']

/**
 * PatientHistory
 *
 * Read-only archive view: lists discharged/transferred patient cases
 * (`GET /api/triage/history`), newest archived first, with backend-side
 * pagination and an optional triage-level filter.
 *
 * Deliberately self-contained — does not touch the active queue's state
 * or WebSocket subscription; each filter/page change triggers its own
 * one-off REST fetch, mirroring how `TriageDashboard`'s initial load works.
 */
export function PatientHistory() {
  const [page, setPage]               = useState(0)
  const [levelFilter, setLevelFilter] = useState('')
  const [pageData, setPageData]       = useState(null)
  const [isLoading, setIsLoading]     = useState(true)
  const [error, setError]             = useState(null)

  useEffect(() => {
    let isCurrent = true
    // eslint-disable-next-line react-hooks/set-state-in-effect -- kicks off a fresh REST fetch whenever page/filter change, mirroring the one-off pattern used for the initial queue load
    setIsLoading(true)
    setError(null)

    const query = new URLSearchParams({ page: String(page), size: String(PAGE_SIZE) })
    if (levelFilter) query.set('triageLevel', levelFilter)

    apiFetch(`/api/triage/history?${query.toString()}`)
      .then((res) => {
        if (!res.ok) throw new Error('Historie konnte nicht geladen werden')
        return res.json()
      })
      .then((data) => {
        if (!isCurrent) return
        setPageData(data)
        setIsLoading(false)
      })
      .catch((err) => {
        if (!isCurrent) return
        console.error('[PatientHistory] Abruf fehlgeschlagen:', err)
        setError('Die Historie konnte nicht geladen werden.')
        setIsLoading(false)
      })

    return () => { isCurrent = false }
  }, [page, levelFilter])

  const cases        = pageData?.content ?? []
  const totalPages   = pageData?.totalPages ?? 0
  const totalElements = pageData?.totalElements ?? 0
  const isFirstPage  = pageData?.first ?? true
  const isLastPage   = pageData?.last ?? true

  return (
    <section aria-label="Patientenhistorie">
      <div className="flex items-baseline justify-between mb-4 gap-3 flex-wrap">
        <h2 className="text-[13px] font-semibold text-stone-500 uppercase tracking-widest">
          Historie
        </h2>
        {!isLoading && !error && (
          <span className="text-[13px] text-stone-500 tabular-nums">
            {totalElements} {totalElements === 1 ? 'archivierter Fall' : 'archivierte Fälle'}
          </span>
        )}
      </div>

      {/* Filter */}
      <div className="flex items-center gap-2 mb-4">
        <label htmlFor="history-level-filter" className="text-[12px] font-medium text-stone-600">
          Triagestufe
        </label>
        <select
          id="history-level-filter"
          value={levelFilter}
          onChange={(e) => {
            setLevelFilter(e.target.value)
            setPage(0)
          }}
          className="text-[13px] bg-white border border-stone-200 rounded-lg px-2.5 py-1.5 focus:outline-none focus-visible:ring-2 focus-visible:ring-stone-900/40 focus:border-stone-900 transition-all"
        >
          <option value="">Alle</option>
          {TRIAGE_LEVELS.map((level) => (
            <option key={level} value={level}>{TRIAGE_CONFIG[level].label} ({level})</option>
          ))}
        </select>
      </div>

      {error ? (
        <ErrorState message={error} />
      ) : isLoading ? (
        <HistorySkeleton />
      ) : cases.length === 0 ? (
        <EmptyHistoryPlaceholder hasFilter={Boolean(levelFilter)} />
      ) : (
        <>
          <ol className="space-y-2">
            {cases.map((patientCase) => (
              <li key={patientCase.id}>
                <HistoryRow patientCase={patientCase} />
              </li>
            ))}
          </ol>

          <PaginationControls
            page={page}
            totalPages={totalPages}
            isFirstPage={isFirstPage}
            isLastPage={isLastPage}
            onPrevious={() => setPage((p) => Math.max(0, p - 1))}
            onNext={() => setPage((p) => p + 1)}
          />
        </>
      )}
    </section>
  )
}

// ── Sub-component: single archived case row ─────────────────────────────────

function HistoryRow({ patientCase }) {
  const triageConfig  = TRIAGE_CONFIG[patientCase.triageLevel] ?? TRIAGE_CONFIG.BLUE
  const admissionTime = formatTime(patientCase.admissionTime)
  const archivedTime  = formatTime(patientCase.archivedAt)

  return (
    <article
      className={[
        'flex items-stretch gap-0',
        'bg-white rounded-2xl overflow-hidden',
        'shadow-[0_2px_12px_rgba(0,0,0,0.06)]',
        'border-l-4', triageConfig.borderColor,
        'animate-fade-slide-in',
      ].join(' ')}
    >
      <div className="flex flex-col justify-center flex-1 min-w-0 px-4 sm:px-5 py-3.5 gap-2">
        <div className="flex items-start justify-between gap-3 flex-wrap">
          <h3 className="text-[14px] font-semibold text-stone-900 leading-tight break-words">
            {patientCase.patientName}
          </h3>
          <span
            className={[
              'inline-flex items-center gap-1.5 shrink-0',
              'px-2.5 py-1 rounded-full text-xs font-medium',
              triageConfig.badgeBg, triageConfig.badgeText,
            ].join(' ')}
          >
            <span className={`w-1.5 h-1.5 rounded-full ${triageConfig.dotColor}`} />
            {triageConfig.label}
          </span>
        </div>

        <div className="flex items-center gap-5 flex-wrap">
          <TimeMetaItem label="Aufnahme" value={admissionTime} />
          <TimeMetaItem label="Archiviert" value={archivedTime} highlight />
        </div>
      </div>
    </article>
  )
}

function TimeMetaItem({ label, value, highlight = false }) {
  return (
    <div className="flex flex-col gap-0.5">
      <span className="text-[10px] uppercase tracking-widest text-stone-500 font-medium">
        {label}
      </span>
      <span
        className={[
          'text-[13px] font-semibold tabular-nums',
          highlight ? 'text-stone-800' : 'text-stone-600',
        ].join(' ')}
      >
        {value}
      </span>
    </div>
  )
}

// ── Sub-component: pagination ────────────────────────────────────────────────

function PaginationControls({ page, totalPages, isFirstPage, isLastPage, onPrevious, onNext }) {
  if (totalPages <= 1) return null

  return (
    <div className="flex items-center justify-between mt-4 pt-4 border-t border-stone-200/60">
      <button
        onClick={onPrevious}
        disabled={isFirstPage}
        className="text-[12px] font-medium text-stone-600 hover:text-stone-900 hover:bg-stone-100 disabled:opacity-40 disabled:hover:bg-transparent disabled:cursor-not-allowed px-3 py-1.5 rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-stone-900/40"
      >
        ← Zurück
      </button>
      <span className="text-[12px] text-stone-500 tabular-nums">
        Seite {page + 1} von {totalPages}
      </span>
      <button
        onClick={onNext}
        disabled={isLastPage}
        className="text-[12px] font-medium text-stone-600 hover:text-stone-900 hover:bg-stone-100 disabled:opacity-40 disabled:hover:bg-transparent disabled:cursor-not-allowed px-3 py-1.5 rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-stone-900/40"
      >
        Weiter →
      </button>
    </div>
  )
}

// ── Sub-component: empty state ────────────────────────────────────────────────

function EmptyHistoryPlaceholder({ hasFilter }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center animate-fade-slide-in">
      <div className="w-14 h-14 rounded-2xl bg-stone-100 flex items-center justify-center mb-4">
        <span className="text-3xl" role="img" aria-label="Archiv">🗄️</span>
      </div>
      <p className="text-[15px] font-medium text-stone-700">
        {hasFilter ? 'Keine archivierten Fälle für diese Triagestufe' : 'Noch keine archivierten Fälle'}
      </p>
      <p className="text-[13px] text-stone-500 mt-1 max-w-xs">
        {hasFilter
          ? 'Versuchen Sie einen anderen Filter oder wählen Sie „Alle“.'
          : 'Entlassene oder verlegte Patient:innen erscheinen hier, sobald sie archiviert wurden.'}
      </p>
    </div>
  )
}

// ── Sub-component: error state ────────────────────────────────────────────────

function ErrorState({ message }) {
  return (
    <div className="p-4 text-sm text-red-800 bg-red-50 rounded-xl animate-fade-slide-in">
      {message}
    </div>
  )
}

// ── Sub-component: loading skeleton ──────────────────────────────────────────

function HistorySkeleton() {
  return (
    <ol className="space-y-2" aria-label="Historie wird geladen" aria-busy="true">
      {[0, 1, 2, 3].map((i) => (
        <li
          key={i}
          className="bg-white rounded-2xl overflow-hidden shadow-[0_2px_12px_rgba(0,0,0,0.06)] border-l-4 border-stone-200 animate-pulse"
          style={{ animationDelay: `${i * 100}ms` }}
        >
          <div className="flex flex-col justify-center px-5 py-3.5 gap-2">
            <div className="flex items-center justify-between gap-4">
              <div className="h-4 w-32 bg-stone-200 rounded" />
              <div className="h-5 w-20 bg-stone-100 rounded-full" />
            </div>
            <div className="flex items-center gap-5">
              <div className="h-6 w-14 bg-stone-100 rounded" />
              <div className="h-6 w-14 bg-stone-100 rounded" />
            </div>
          </div>
        </li>
      ))}
    </ol>
  )
}
