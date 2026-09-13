import { createContext, useContext, useState, useCallback, useEffect } from 'react'
import { authService } from '../services/authService'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('tg_user')
    return stored ? JSON.parse(stored) : null
  })

  useEffect(() => {
    const syncProfile = () => {
      const stored = localStorage.getItem('tg_user')
      if (stored) setUser(JSON.parse(stored))
    }
    window.addEventListener('tg-profile-updated', syncProfile)
    return () => window.removeEventListener('tg-profile-updated', syncProfile)
  }, [])

  const persist = (authResponse) => {
    localStorage.setItem('tg_token', authResponse.token)
    const userData = {
      id: authResponse.userId,
      name: authResponse.name,
      email: authResponse.email,
      role: authResponse.role,
    }
    localStorage.setItem('tg_user', JSON.stringify(userData))
    setUser(userData)
    return userData
  }

  const login = useCallback(async (email, password) => {
    const res = await authService.login({ email, password })
    return persist(res)
  }, [])

  const register = useCallback(async (payload) => {
    const res = await authService.register(payload)
    return persist(res)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('tg_token')
    localStorage.removeItem('tg_user')
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, login, register, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
