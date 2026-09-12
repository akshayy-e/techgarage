import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { notificationService } from '../services/notificationService'
import { initials } from '../utils/format'

export default function Navbar() {
  const { user, logout, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [unread, setUnread] = useState(0)

  useEffect(() => {
    if (!isAuthenticated) return
    let cancelled = false
    const load = async () => {
      try {
        const res = await notificationService.unreadCount()
        if (!cancelled) setUnread(res.count)
      } catch {
        // ignore, non-critical
      }
    }
    load()
    const interval = setInterval(load, 20000)
    return () => { cancelled = true; clearInterval(interval) }
  }, [isAuthenticated])

  const dashboardPath = user?.role === 'CLIENT' ? '/client/dashboard'
    : user?.role === 'FREELANCER' ? '/freelancer/dashboard'
    : user?.role === 'ADMIN' ? '/admin/dashboard' : '/'

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <Link to="/" className="brand">
          <span className="brand-mark">TG</span>
          TechGarage
        </Link>

        {isAuthenticated ? (
          <nav className="nav-links">
            <NavLink to={dashboardPath} className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Dashboard</NavLink>
            {user.role === 'CLIENT' && (
              <NavLink to="/client/post-problem" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Post Problem</NavLink>
            )}
            {user.role === 'FREELANCER' && (
              <NavLink to="/freelancer/problems" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Browse Problems</NavLink>
            )}
            <NavLink to="/notifications" className={({ isActive }) => `nav-link nav-badge${isActive ? ' active' : ''}`}>
              Notifications
              {unread > 0 && <span className="dot" />}
            </NavLink>
          </nav>
        ) : null}

        <div className="nav-user">
          {isAuthenticated ? (
            <>
              <div className="avatar" title={user.name}>{initials(user.name)}</div>
              <button className="btn btn-outline btn-sm" style={{ borderColor: 'rgba(255,255,255,0.3)', color: '#fff' }} onClick={handleLogout}>
                Log out
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="nav-link">Log in</Link>
              <Link to="/register" className="btn btn-primary btn-sm">Get Started</Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
