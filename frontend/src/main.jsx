import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App.jsx'
import { AuthProvider } from './context/AuthContext.jsx'
import './index.css'

// Non-blocking backend warm-up: helps reduce first-request latency on sleeping hosted services.
const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
fetch(`${apiBase.replace(/\/api\/?$/, '')}/actuator/health`, { method: 'GET', cache: 'no-store' }).catch(() => {})

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>,
)
