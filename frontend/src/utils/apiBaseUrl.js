/**
 * REST vs WebSocket base URLs.
 *
 * REST is always same-origin (`''`):
 *   – local: Vite proxy `/api` → localhost:8080 (vite.config.js)
 *   – prod:  Vercel rewrite `/api` → Render (vercel.json)
 * Same-origin requests skip browser CORS, so Login nicht an einer
 * veralteten Render-CORS-Liste scheitert.
 *
 * WebSockets kann Vercel nicht proxyen. In Production geht SockJS
 * deshalb direkt an das Backend (dort ist WS-CORS bereits `*`).
 */
const configuredBackend = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
const DEFAULT_PROD_BACKEND = 'https://triage-dashboard-api-dr0z.onrender.com'

export const API_BASE_URL = ''
export const BACKEND_ORIGIN = configuredBackend || DEFAULT_PROD_BACKEND
export const WS_BASE_URL = import.meta.env.PROD ? BACKEND_ORIGIN : ''
