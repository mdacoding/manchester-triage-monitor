/**
 * Formats a LocalDateTime string from the backend into a human-readable time.
 * Example: "2024-06-20T14:35:00" → "14:35"
 *
 * @param {string} dateTimeString ISO-8601 datetime string
 * @returns {string} Formatted time string, or '–' if invalid
 */
export function formatTime(dateTimeString) {
  if (!dateTimeString) return '–'
  try {
    const date = new Date(dateTimeString)
    return date.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })
  } catch {
    return '–'
  }
}

/**
 * Calculates the elapsed waiting time since admission.
 * Returns a compact label like "12 min" or "1 h 5 min".
 *
 * @param {string} admissionTime ISO-8601 datetime string
 * @returns {string} Human-readable elapsed time
 */
export function formatWaitingTime(admissionTime) {
  if (!admissionTime) return '–'
  try {
    const admissionDate  = new Date(admissionTime)
    const elapsedMinutes = Math.max(0, Math.floor((Date.now() - admissionDate.getTime()) / 60_000))

    if (elapsedMinutes < 60) return `${elapsedMinutes} min`

    const hours   = Math.floor(elapsedMinutes / 60)
    const minutes = elapsedMinutes % 60
    return minutes > 0 ? `${hours} h ${minutes} min` : `${hours} h`
  } catch {
    return '–'
  }
}
