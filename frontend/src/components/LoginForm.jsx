import { useState } from 'react'
import { useAuth } from '../hooks/useAuth'

/**
 * LoginForm
 *
 * Full-screen gate shown whenever no valid session exists. Demo credentials
 * are surfaced directly in the UI — this is a portfolio/demo build, not a
 * production clinical system (see README for details).
 */
export function LoginForm() {
  const { login } = useAuth()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMsg, setErrorMsg] = useState(null)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsSubmitting(true)
    setErrorMsg(null)

    try {
      await login(username, password)
    } catch (err) {
      setErrorMsg(err.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  const fillDemo = async (demoUsername, demoPassword) => {
    setUsername(demoUsername)
    setPassword(demoPassword)
    setIsSubmitting(true)
    setErrorMsg(null)
    try {
      await login(demoUsername, demoPassword)
    } catch (err) {
      setErrorMsg(err.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#FAFAF7] flex items-center justify-center p-4">
      <div className="w-full max-w-sm">
        <div className="flex flex-col items-center mb-8">
          <div className="w-10 h-10 rounded-xl bg-stone-900 flex items-center justify-center mb-3">
            <span className="text-white text-sm font-bold tracking-tight">T</span>
          </div>
          <h1 className="text-[16px] font-semibold text-stone-900">Triage Dashboard</h1>
          <p className="text-[12px] text-stone-500 mt-0.5">Anmeldung erforderlich</p>
        </div>

        <div className="bg-white rounded-2xl shadow-[0_2px_12px_rgba(0,0,0,0.06)] p-6">
          {errorMsg && (
            <div className="p-3 mb-4 text-sm text-red-800 bg-red-50 rounded-lg">
              {errorMsg}
            </div>
          )}

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div>
              <label className="block text-sm font-medium text-stone-700 mb-1">Benutzername</label>
              <input
                type="text"
                required
                autoFocus
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full px-3 py-2 border border-stone-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-stone-900/10 focus:border-stone-900 transition-all text-sm"
                placeholder="z. B. pflege"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-stone-700 mb-1">Passwort</label>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-3 py-2 border border-stone-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-stone-900/10 focus:border-stone-900 transition-all text-sm"
                placeholder="••••••••"
              />
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full px-4 py-2 text-sm font-medium text-white bg-stone-900 rounded-lg hover:bg-stone-800 active:bg-stone-950 disabled:opacity-50 transition-colors mt-1 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-stone-900/40 focus-visible:ring-offset-2"
            >
              {isSubmitting ? 'Anmelden …' : 'Anmelden'}
            </button>
          </form>
        </div>

        {/* Demo credentials helper — remove for a real deployment */}
        <div className="mt-5 text-center">
          <p className="text-[11px] text-stone-500 mb-2">Demo-Zugangsdaten (Portfolio-Build):</p>
          <div className="flex flex-wrap justify-center gap-2">
            <button
              type="button"
              disabled={isSubmitting}
              onClick={() => fillDemo('pflege', 'pflege123!')}
              className="text-[11px] font-medium text-stone-500 hover:text-stone-900 bg-stone-100 hover:bg-stone-200 px-2.5 py-1 rounded-md transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-stone-900/40"
            >
              pflege / pflege123!
            </button>
            <button
              type="button"
              disabled={isSubmitting}
              onClick={() => fillDemo('admin', 'admin123!')}
              className="text-[11px] font-medium text-stone-500 hover:text-stone-900 bg-stone-100 hover:bg-stone-200 px-2.5 py-1 rounded-md transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-stone-900/40"
            >
              admin / admin123!
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
