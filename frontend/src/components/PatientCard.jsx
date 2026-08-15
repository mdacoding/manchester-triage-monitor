import { TRIAGE_CONFIG } from '../config/triageConfig'
import { formatTime, formatWaitingTime } from '../utils/timeUtils'
import { apiFetch } from '../utils/apiClient'

/**
 * PatientCard
 *
 * Renders a single patient case with a triage-level color accent
 * applied as a left border — calm and clinical, not alarming.
 *
 * Animates in with a soft fade-slide-up transition when first rendered
 * (controlled by the `animate-fade-slide-in` Tailwind keyframe).
 *
 * @param {{ patient: PatientCase, position: number }} props
 */
export function PatientCard({ patient, position }) {
  const triageConfig = TRIAGE_CONFIG[patient.triageLevel] ?? TRIAGE_CONFIG.BLUE
  const waitingTime  = formatWaitingTime(patient.admissionTime)
  const arrivalTime  = formatTime(patient.admissionTime)
  const treatmentBy  = formatTime(patient.estimatedTreatmentTime)

  return (
    <article
      className={[
        // Layout & shape
        'relative flex items-stretch gap-0',
        'bg-white rounded-2xl overflow-hidden',
        // Soft shadow — Apple-style depth
        'shadow-[0_2px_12px_rgba(0,0,0,0.06)] hover:shadow-[0_4px_20px_rgba(0,0,0,0.10)]',
        // Triage color accent: left border (4px)
        'border-l-4', triageConfig.borderColor,
        // Smooth entrance animation
        'animate-fade-slide-in',
        // Hover micro-interaction
        'transition-all duration-300 ease-out hover:-translate-y-0.5',
      ].join(' ')}
    >
      {/* ── Queue position badge ──────────────────────────────────────── */}
      <div className="flex items-center justify-center w-10 sm:w-12 shrink-0 bg-stone-50 border-r border-stone-100">
        <span className="text-xs font-semibold text-stone-500 tabular-nums">
          #{position}
        </span>
      </div>

      {/* ── Main content ─────────────────────────────────────────────── */}
      <div className="flex flex-col justify-between flex-1 min-w-0 px-4 sm:px-5 py-4 gap-3">
        {/* Top row: name + triage badge */}
        <div className="flex items-start justify-between gap-3 flex-wrap">
          <h3 className="text-[15px] font-semibold text-stone-900 leading-tight break-words">
            {patient.patientName}
          </h3>

          {/* Triage level badge */}
          <span
            className={[
              'inline-flex items-center gap-1.5 shrink-0',
              'px-2.5 py-1 rounded-full text-xs font-medium',
              triageConfig.badgeBg, triageConfig.badgeText,
            ].join(' ')}
          >
            {/* Status dot */}
            <span className={`w-1.5 h-1.5 rounded-full ${triageConfig.dotColor}`} />
            {triageConfig.label}
          </span>
        </div>

        {/* Symptoms */}
        {patient.symptoms && (
          <p className="text-[13px] text-stone-500 leading-snug line-clamp-2">
            {patient.symptoms}
          </p>
        )}

        {/* Bottom row: time meta & actions */}
        <div className="flex items-center justify-between gap-3 flex-wrap pt-1 border-t border-stone-100">
          <div className="flex items-center gap-3 sm:gap-5 flex-wrap">
            <TimeMetaItem label="Ankunft"    value={arrivalTime}  />
            <TimeMetaItem label="Wartezeit"  value={waitingTime}  />
            <TimeMetaItem label="Behandlung bis" value={treatmentBy} highlight />
          </div>
          
          {/* Action: Archive */}
          <button
            onClick={() => {
              if (window.confirm(`${patient.patientName} wirklich entlassen / archivieren?`)) {
                apiFetch(`/api/triage/patient/${patient.id}/archive`, { method: 'PATCH' })
                  .catch(err => console.error("Error archiving patient", err));
              }
            }}
            className="text-[11px] font-medium text-stone-500 hover:text-stone-700 hover:bg-stone-100 px-2 py-1 rounded transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-stone-900/40"
          >
            Entlassen
          </button>
        </div>
      </div>
    </article>
  )
}

/**
 * Small inline label/value pair for the card's meta row.
 */
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
