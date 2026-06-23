import { useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

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
 * Automatically reconnects on dropped connections with an exponential back-off
 * strategy provided by the @stomp/stompjs Client.
 *
 * @returns {{ queue: PatientCase[], connectionStatus: string }}
 */
export function useTriageWebSocket() {
  const [queue, setQueue]                     = useState([])
  const [connectionStatus, setConnectionStatus] = useState(ConnectionStatus.CONNECTING)
  const stompClientRef                        = useRef(null)

  useEffect(() => {
    const client = new Client({
      // SockJS-Factory erlaubt Fallback auf Long-Polling für ältere Browser
      webSocketFactory: () => new SockJS('/ws-triage'),

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
      },

      onWebSocketError: (event) => {
        console.error('[TriageWS] WebSocket-Fehler:', event)
        setConnectionStatus(ConnectionStatus.ERROR)
      },
    })

    client.activate()
    stompClientRef.current = client

    // Cleanup: Verbindung beim Unmounten sauber schließen
    return () => {
      client.deactivate()
    }
  }, [])

  return { queue, connectionStatus }
}
