import { getStoredAuth } from './authStorage'
import { API_BASE_URL } from './apiBaseUrl'

/**
 * Fired on `window` whenever a request comes back 401 Unauthorized —
 * lets AuthContext react by clearing the session and returning to the
 * login screen, without every call site needing to check the status itself.
 */
export const UNAUTHORIZED_EVENT = 'triage-auth:unauthorized'

/**
 * fetch() wrapper that automatically attaches the JWT (if present) as a
 * Bearer Authorization header and broadcasts a global event on 401 so the
 * app can log the user out (e.g. after token expiry).
 */
export async function apiFetch(path, options = {}) {
  const auth = getStoredAuth()
  const headers = { ...(options.headers || {}) }

  if (auth?.token) {
    headers['Authorization'] = `Bearer ${auth.token}`
  }

  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers })

  if (response.status === 401) {
    window.dispatchEvent(new Event(UNAUTHORIZED_EVENT))
  }

  return response
}
