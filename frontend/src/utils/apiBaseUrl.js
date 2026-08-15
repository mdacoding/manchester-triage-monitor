/**
 * Base URL for the backend API/WebSocket.
 *
 * Empty string in local dev (requests stay relative and go through the Vite
 * dev proxy defined in vite.config.js). In production (e.g. a Vercel-hosted
 * frontend talking to a separately-deployed Render backend), set
 * VITE_API_BASE_URL to the full backend origin, e.g.
 * "https://triage-dashboard-api.onrender.com".
 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''
