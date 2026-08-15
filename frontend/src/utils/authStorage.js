const AUTH_STORAGE_KEY = 'triage_auth'

/**
 * Reads the persisted auth object ({ token, username, role }) from localStorage.
 * Returns null if nothing is stored or the stored value is corrupted.
 */
export function getStoredAuth() {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function setStoredAuth(auth) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth))
}

export function clearStoredAuth() {
  localStorage.removeItem(AUTH_STORAGE_KEY)
}
