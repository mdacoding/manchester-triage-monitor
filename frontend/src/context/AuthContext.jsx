import { useCallback, useEffect, useState } from 'react'
import { getStoredAuth, setStoredAuth, clearStoredAuth } from '../utils/authStorage'
import { UNAUTHORIZED_EVENT } from '../utils/apiClient'
import { API_BASE_URL } from '../utils/apiBaseUrl'
import { AuthContext } from './authContextValue'

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
    const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })

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
