import './index.css'
import { TriageDashboard } from './pages/TriageDashboard'
import { LoginForm } from './components/LoginForm'
import { useAuth } from './hooks/useAuth'

export default function App() {
  const { isAuthenticated } = useAuth()

  return isAuthenticated ? <TriageDashboard /> : <LoginForm />
}
