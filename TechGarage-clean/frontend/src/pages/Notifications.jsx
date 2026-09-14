import { useEffect, useState } from 'react'
import { notificationService } from '../services/notificationService'
import EmptyState from '../components/EmptyState'
import Spinner from '../components/Spinner'
import { formatDateTime } from '../utils/format'

export default function Notifications() {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [working, setWorking] = useState(false)
  const [error, setError] = useState('')

  const load = async () => {
    try { setNotifications(await notificationService.getMine()) }
    catch (err) { setError(err.message) }
    finally { setLoading(false) }
  }
  useEffect(() => { load() }, [])

  const handleRead = async (id) => {
    try {
      await notificationService.markAsRead(id)
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, isRead: true } : n))
    } catch (err) { setError(err.message) }
  }

  const markAllRead = async () => {
    setWorking(true); setError('')
    try {
      const unread = notifications.filter(n => !n.isRead)
      await Promise.all(unread.map(n => notificationService.markAsRead(n.id)))
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })))
    } catch (err) { setError(err.message) }
    finally { setWorking(false) }
  }

  const clearAll = async () => {
    if (!notifications.length || !window.confirm('Clear all notifications? This cannot be undone.')) return
    setWorking(true); setError('')
    try { await notificationService.clearAll(); setNotifications([]) }
    catch (err) { setError(err.message) }
    finally { setWorking(false) }
  }

  if (loading) return <Spinner page />
  const unread = notifications.filter(n => !n.isRead).length

  return (
    <div className="container-narrow">
      <div className="page-header">
        <div className="flex-between" style={{ alignItems: 'flex-end', gap: 16 }}>
          <div><span className="eyebrow">Inbox</span><h1>Notifications</h1><p>{unread ? `${unread} unread notification${unread === 1 ? '' : 's'}` : 'You are all caught up.'}</p></div>
          {notifications.length > 0 && <div className="flex gap-8"><button className="btn btn-outline btn-sm" onClick={markAllRead} disabled={working || unread === 0}>✓ Mark all read</button><button className="btn btn-outline btn-sm" onClick={clearAll} disabled={working}>Clear all</button></div>}
        </div>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {notifications.length === 0 ? <EmptyState glyph="🔔" title="You're all caught up" subtitle="New activity on your problems and jobs will show up here." /> : (
        <div className="stack gap-8">
          {notifications.map(n => (
            <div key={n.id} className="card" style={{ opacity: n.isRead ? 0.65 : 1, padding: 16 }}>
              <div className="flex-between" style={{ gap: 12 }}>
                <p style={{ margin: 0, fontSize: 14, color: 'var(--color-ink)' }}>{n.message}</p>
                {!n.isRead ? <button className="btn btn-outline btn-sm" onClick={() => handleRead(n.id)}>Mark read</button> : <span className="badge badge-completed">Read</span>}
              </div>
              <div style={{ fontSize: 11.5, color: 'var(--color-ink-faint)', marginTop: 8 }}>{formatDateTime(n.createdAt)}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
