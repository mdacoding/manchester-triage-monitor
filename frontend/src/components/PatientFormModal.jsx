import { useState } from 'react'

export function PatientFormModal({ isOpen, onClose }) {
  const [patientName, setPatientName] = useState('')
  const [triageLevel, setTriageLevel] = useState('GREEN')
  const [symptoms, setSymptoms] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMsg, setErrorMsg] = useState(null)

  if (!isOpen) return null

  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsSubmitting(true)
    setErrorMsg(null)

    try {
      const res = await fetch('/api/triage/patient', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ patientName, triageLevel, symptoms }),
      })

      if (!res.ok) {
        const errData = await res.json()
        throw new Error(errData.detail || 'Fehler beim Speichern')
      }

      // Reset & close
      setPatientName('')
      setTriageLevel('GREEN')
      setSymptoms('')
      onClose()
    } catch (err) {
      setErrorMsg(err.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-stone-900/40 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="w-full max-w-md p-6 bg-white rounded-2xl shadow-xl shadow-black/10 relative animate-in zoom-in-95 duration-200">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-stone-400 hover:text-stone-700 transition-colors"
        >
          ✕
        </button>
        
        <h2 className="text-lg font-bold text-stone-900 mb-1">Neuer Patient</h2>
        <p className="text-sm text-stone-500 mb-5">
          Erfassen Sie hier die Daten zur initialen Triage.
        </p>

        {errorMsg && (
          <div className="p-3 mb-4 text-sm text-red-800 bg-red-50 rounded-lg">
            {errorMsg}
          </div>
        )}

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div>
            <label className="block text-sm font-medium text-stone-700 mb-1">Name des Patienten</label>
            <input
              type="text"
              required
              value={patientName}
              onChange={e => setPatientName(e.target.value)}
              className="w-full px-3 py-2 border border-stone-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-stone-900/10 focus:border-stone-900 transition-all text-sm"
              placeholder="z. B. Max Mustermann"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-stone-700 mb-1">Triage-Level (MTS)</label>
            <select
              value={triageLevel}
              onChange={e => setTriageLevel(e.target.value)}
              className="w-full px-3 py-2 border border-stone-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-stone-900/10 focus:border-stone-900 transition-all text-sm bg-white"
            >
              <option value="RED">🔴 Sofort (RED)</option>
              <option value="ORANGE">🟠 Sehr dringend (ORANGE)</option>
              <option value="YELLOW">🟡 Dringend (YELLOW)</option>
              <option value="GREEN">🟢 Normal (GREEN)</option>
              <option value="BLUE">🔵 Nicht dringend (BLUE)</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-stone-700 mb-1">Symptome / Ersteinschätzung</label>
            <textarea
              rows={3}
              value={symptoms}
              onChange={e => setSymptoms(e.target.value)}
              className="w-full px-3 py-2 border border-stone-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-stone-900/10 focus:border-stone-900 transition-all text-sm resize-none"
              placeholder="Kurze Beschreibung der Leitsymptome..."
            />
          </div>

          <div className="flex justify-end gap-3 mt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium text-stone-600 hover:text-stone-900 transition-colors"
            >
              Abbrechen
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-4 py-2 text-sm font-medium text-white bg-stone-900 rounded-lg hover:bg-stone-800 disabled:opacity-50 transition-colors"
            >
              {isSubmitting ? 'Speichere...' : 'Patient aufnehmen'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
