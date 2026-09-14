import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { notificationService } from '../services/notificationService'
import { initials } from '../utils/format'
import { subscribeToNotifications } from '../services/realtimeService'

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

        if (!cancelled) {
          setUnread(res.count)
        }
      } catch {
        // Ignore notification errors because they are non-critical
      }
    }

    load()

    const controller = new AbortController()
    let streamActive = true

    subscribeToNotifications(
        (notification) => {
          if (!cancelled) {
            setUnread((n) => n + (notification?.isRead ? 0 : 1))
          }
        },
        controller.signal
    ).catch(() => {
      streamActive = false
    })

    const interval = setInterval(
        load,
        streamActive ? 60000 : 20000
    )

    return () => {
      cancelled = true
      controller.abort()
      clearInterval(interval)
    }
  }, [isAuthenticated])

  const dashboardPath =
      user?.role === 'CLIENT'
          ? '/client/dashboard'
          : user?.role === 'FREELANCER'
              ? '/freelancer/dashboard'
              : user?.role === 'ADMIN'
                  ? '/admin/dashboard'
                  : '/'

  const profilePath =
      user?.role === 'CLIENT'
          ? '/client/profile'
          : user?.role === 'FREELANCER'
              ? '/freelancer/profile'
              : '/admin/users'

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
      <header className="navbar">
        <div className="container navbar-inner">

          {/* Brand */}
          <Link to="/" className="brand">
            <span className="brand-mark">TG</span>
            TechGarage
          </Link>

          {/* Navigation */}
          {isAuthenticated ? (
              <nav className="nav-links">

                {/* Dashboard */}
                <NavLink
                    to={dashboardPath}
                    className={({ isActive }) =>
                        `nav-link${isActive ? ' active' : ''}`
                    }
                >
                  Dashboard
                </NavLink>

                {/* Client Navigation */}
                {user.role === 'CLIENT' && (
                    <>
                      <NavLink
                          to="/client/post-problem"
                          className={({ isActive }) =>
                              `nav-link${isActive ? ' active' : ''}`
                          }
                      >
                        Post Problem
                      </NavLink>

                      <NavLink
                          to="/client/freelancers"
                          className={({ isActive }) =>
                              `nav-link${isActive ? ' active' : ''}`
                          }
                      >
                        Find Mechanics
                      </NavLink>

                      <NavLink
                          to="/ai-diagnosis"
                          className={({ isActive }) =>
                              `nav-link${isActive ? ' active' : ''}`
                          }
                      >
                        AI Diagnosis
                      </NavLink>
                    </>
                )}

                {/* Freelancer Navigation */}
                {user.role === 'FREELANCER' && (
                    <>
                      <NavLink
                          to="/freelancer/problems"
                          className={({ isActive }) =>
                              `nav-link${isActive ? ' active' : ''}`
                          }
                      >
                        Browse Problems
                      </NavLink>

                      <NavLink
                          to="/freelancer/emergency"
                          className={({ isActive }) =>
                              `nav-link${isActive ? ' active' : ''}`
                          }
                      >
                        🚨 Emergency
                      </NavLink>
                    </>
                )}

                {/* Payments */}
                {(user.role === 'CLIENT' || user.role === 'FREELANCER') && (
                    <NavLink
                        to="/payments"
                        className={({ isActive }) =>
                            `nav-link${isActive ? ' active' : ''}`
                        }
                    >
                      Payments
                    </NavLink>
                )}

                {/* Notifications */}
                <NavLink
                    to="/notifications"
                    className={({ isActive }) =>
                        `nav-link nav-badge${isActive ? ' active' : ''}`
                    }
                >
                  Notifications
                  {unread > 0 && <span className="dot" />}
                </NavLink>

              </nav>
          ) : null}

          {/* User Section */}
          <div className="nav-user">

            {isAuthenticated ? (
                <>
                  <button
                      className="avatar"
                      title="Open profile"
                      onClick={() => navigate(profilePath)}
                      style={{
                        border: 0,
                        cursor: 'pointer'
                      }}
                  >
                    {initials(user.name)}
                  </button>

                  <button
                      className="btn btn-outline btn-sm"
                      style={{
                        borderColor: 'rgba(255,255,255,0.3)',
                        color: '#fff'
                      }}
                      onClick={handleLogout}
                  >
                    Log out
                  </button>
                </>
            ) : (
                <>
                  <Link to="/login" className="nav-link">
                    Log in
                  </Link>

                  <Link
                      to="/register"
                      className="btn btn-primary btn-sm"
                  >
                    Get Started
                  </Link>
                </>
            )}

          </div>
        </div>
      </header>
  )
}