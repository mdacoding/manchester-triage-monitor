import { createContext } from 'react'

/**
 * Split into its own module (separate from the AuthProvider component and the
 * useAuth hook) so React Fast Refresh can reliably distinguish component
 * exports from plain value/hook exports.
 */
export const AuthContext = createContext(null)
