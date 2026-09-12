import { useEffect, useState } from 'react'
import { notificationService } from '../services/notificationService'
import EmptyState from '../components/EmptyState'
import Spinner from '../components/Spinner'
import { formatDateTime } from '../utils/format'

export default function Notifications() {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = async () => {
    try {
      setNotifications(await notificationService.getMine())
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleRead = async (id) => {
    try {
      await notificationService.markAsRead(id)
      setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)))
    } catch {
      // non-critical
    }
  }

  if (loading) return <Spinner page />

  return (
    <div className="container-narrow">
      <div className="page-header">
        <span className="eyebrow">Inbox</span>
        <h1>Notifications</h1>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {notifications.length === 0 ? (
        <EmptyState glyph="🔔" title="You're all caught up" subtitle="New activity on your problems and jobs will show up here." />
      ) : (
        <div className="stack gap-8">
          {notifications.map((n) => (
            <div key={n.id} className="card" style={{ opacity: n.isRead ? 0.6 : 1, cursor: n.isRead ? 'default' : 'pointer', padding: 16 }}
              onClick={() => !n.isRead && handleRead(n.id)}>
              <div className="flex-between">
                <p style={{ margin: 0, fontSize: 14, color: 'var(--color-ink)' }}>{n.message}</p>
                {!n.isRead && <span className="badge badge-open">New</span>}
              </div>
              <div style={{ fontSize: 11.5, color: 'var(--color-ink-faint)', marginTop: 6 }}>{formatDateTime(n.createdAt)}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
