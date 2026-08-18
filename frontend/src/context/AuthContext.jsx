import { useCallback, useEffect, useState } from 'react'
import { getStoredAuth, setStoredAuth, clearStoredAuth } from '../utils/authStorage'
import { UNAUTHORIZED_EVENT } from '../utils/apiClient'
import { AuthContext } from './authContextValue'

const LOGIN_TIMEOUT_MS = 45000

/**
 * Provides the current auth session ({ token, username, role }) to the whole
 * app, plus login()/logout() actions. Session is persisted in localStorage so
 * a page refresh doesn't force a re-login.
 */
export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(getStoredAuth)

  // Any 401 from the API (expired/invalid token) forces a clean logout.
  useEffect(() => {
    const handleUnauthorized = () => {
      clearStoredAuth()
      setAuth(null)
    }
    window.addEventListener(UNAUTHORIZED_EVENT, handleUnauthorized)
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, handleUnauthorized)
  }, [])

  const login = useCallback(async (username, password) => {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), LOGIN_TIMEOUT_MS)
    let response
    try {
      response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
        signal: controller.signal,
      })
    } catch (err) {
      if (err?.name === 'AbortError') {
        throw new Error(
          'Anmeldung dauert zu lange. Das Free-Tier-Backend startet oft 30–50 Sekunden — bitte erneut versuchen.',
          { cause: err }
        )
      }
      throw new Error(
        'Backend nicht erreichbar (Netzwerk oder CORS). Bitte Seite neu laden oder später erneut versuchen.',
        { cause: err }
      )
    } finally {
      clearTimeout(timeoutId)
    }

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      throw new Error(errorData.detail || 'Benutzername oder Passwort ist falsch.')
    }

    const data = await response.json()
    const nextAuth = { token: data.token, username: data.username, role: data.role }
    setStoredAuth(nextAuth)
    setAuth(nextAuth)
    return nextAuth
  }, [])

  const logout = useCallback(() => {
    clearStoredAuth()
    setAuth(null)
  }, [])

  return (
    <AuthContext.Provider value={{ auth, isAuthenticated: Boolean(auth?.token), login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
