import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App.jsx'
import { AuthProvider } from './context/AuthContext.jsx'
import './index.css'

// Warm the backend only when an explicit API URL is configured.
// In production, this avoids accidentally calling localhost or an incorrect relative endpoint.
const configuredApiBase = (import.meta.env.VITE_API_BASE_URL || '').trim()
if (configuredApiBase) {
  fetch(`${configuredApiBase.replace(/\/api\/?$/, '')}/actuator/health`, { method: 'GET', cache: 'no-store' }).catch(() => {})
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>,
)
