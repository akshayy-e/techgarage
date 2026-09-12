import { useEffect, useState } from 'react'
import { adminService } from '../../services/adminService'
import Spinner from '../../components/Spinner'
import { formatDate } from '../../utils/format'

export default function AdminUsers() {
  const [users, setUsers] = useState([])
  const [tab, setTab] = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = async () => {
    try {
      setUsers(await adminService.getUsers())
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleVerify = async (userId) => {
    try {
      await adminService.verifyFreelancer(userId)
      await load()
    } catch (err) {
      setError(err.message)
    }
  }

  const handleToggleSuspend = async (user) => {
    try {
      if (user.enabled) await adminService.suspendUser(user.id)
      else await adminService.reactivateUser(user.id)
      await load()
    } catch (err) {
      setError(err.message)
    }
  }

  if (loading) return <Spinner page />

  const filtered = tab === 'ALL' ? users : users.filter((u) => u.role === tab)

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Admin</span>
        <h1>Users</h1>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="tabs">
        {['ALL', 'CLIENT', 'FREELANCER', 'ADMIN'].map((t) => (
          <button key={t} className={`tab${tab === t ? ' active' : ''}`} onClick={() => setTab(t)}>{t}</button>
        ))}
      </div>

      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Name</th><th>Email</th><th>Role</th><th>Joined</th><th>Status</th><th>Actions</th></tr>
          </thead>
          <tbody>
            {filtered.map((u) => (
              <tr key={u.id}>
                <td>
                  {u.name}
                  {u.role === 'FREELANCER' && (
                    <span className={`badge ${u.verified ? 'badge-completed' : 'badge-neutral'}`} style={{ marginLeft: 8 }}>
                      {u.verified ? '✓ Verified' : 'Unverified'}
                    </span>
                  )}
                </td>
                <td>{u.email}</td>
                <td><span className="badge badge-neutral">{u.role}</span></td>
                <td>{formatDate(u.createdAt)}</td>
                <td>
                  <span className={`badge ${u.enabled ? 'badge-completed' : 'badge-danger'}`}>{u.enabled ? 'Active' : 'Suspended'}</span>
                </td>
                <td>
                  <div className="flex gap-8">
                    {u.role === 'FREELANCER' && (
                      u.verified
                        ? <button className="btn btn-outline btn-sm" disabled>Verified</button>
                        : <button className="btn btn-outline btn-sm" onClick={() => handleVerify(u.id)}>Verify</button>
                    )}
                    {u.role !== 'ADMIN' && (
                      <button className="btn btn-sm" style={{ background: u.enabled ? 'var(--color-danger-bg)' : 'var(--color-success-bg)', color: u.enabled ? 'var(--color-danger)' : 'var(--color-success)' }}
                        onClick={() => handleToggleSuspend(u)}>
                        {u.enabled ? 'Suspend' : 'Reactivate'}
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
