import { useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { UNAUTHORIZED_EVENT } from '../utils/apiClient'
import { API_BASE_URL } from '../utils/apiBaseUrl'

/**
 * Connection states for the WebSocket lifecycle.
 * Used by the dashboard to render the live indicator.
 */
export const ConnectionStatus = {
  CONNECTING: 'CONNECTING',
  CONNECTED:  'CONNECTED',
  ERROR:      'ERROR',
}

/**
 * Custom Hook: useTriageWebSocket
 *
 * Manages a persistent STOMP-over-WebSocket connection to the triage backend.
 * The JWT is sent as a native STOMP header on CONNECT — the backend's
 * StompAuthChannelInterceptor rejects the handshake without a valid token.
 * Automatically reconnects on dropped connections with an exponential back-off
 * strategy provided by the @stomp/stompjs Client.
 *
 * @param {string | null} token - current JWT, or null while unauthenticated
 * @returns {{ queue: PatientCase[], connectionStatus: string }}
 */
export function useTriageWebSocket(token) {
  const [queue, setQueue]                     = useState([])
  const [connectionStatus, setConnectionStatus] = useState(ConnectionStatus.CONNECTING)
  const stompClientRef                        = useRef(null)

  useEffect(() => {
    // No session yet (or logged out) — don't attempt to connect. Resets the
    // indicator when the external auth/token state changes (e.g. on logout).
    if (!token) {
      // eslint-disable-next-line react-hooks/set-state-in-effect -- syncs indicator to external token state
      setConnectionStatus(ConnectionStatus.CONNECTING)
      return undefined
    }

    const client = new Client({
      // SockJS-Factory erlaubt Fallback auf Long-Polling für ältere Browser
      webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws-triage`),

      // JWT wird als nativer STOMP-Header beim CONNECT-Frame mitgeschickt
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },

      // Automatisch alle 5 Sekunden neu verbinden, wenn die Verbindung abbricht
      reconnectDelay: 5000,

      onConnect: () => {
        setConnectionStatus(ConnectionStatus.CONNECTED)

        client.subscribe('/topic/queue', (message) => {
          try {
            const updatedQueue = JSON.parse(message.body)
            setQueue(updatedQueue)
          } catch (parseError) {
            console.error('[TriageWS] Fehler beim Parsen der Nachricht:', parseError)
          }
        })
      },

      onDisconnect: () => {
        setConnectionStatus(ConnectionStatus.CONNECTING)
      },

      onStompError: (frame) => {
        console.error('[TriageWS] STOMP-Fehler:', frame)
        setConnectionStatus(ConnectionStatus.ERROR)
        // Ein abgelehntes CONNECT (z. B. abgelaufenes Token) ist üblicherweise
        // ein Auth-Problem — Session beenden statt endlos neu zu verbinden.
        window.dispatchEvent(new Event(UNAUTHORIZED_EVENT))
      },

      onWebSocketError: (event) => {
        console.error('[TriageWS] WebSocket-Fehler:', event)
        setConnectionStatus(ConnectionStatus.ERROR)
      },
    })

    client.activate()
    stompClientRef.current = client

    // Cleanup: Verbindung beim Unmounten oder Token-Wechsel sauber schließen
    return () => {
      client.deactivate()
    }
  }, [token])

  return { queue, connectionStatus }
}
