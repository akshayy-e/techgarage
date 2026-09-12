import { useAuth } from '../../context/AuthContext'
import { initials } from '../../utils/format'

export default function ClientProfile() {
  const { user } = useAuth()
  return (
    <div className="container-narrow">
      <div className="page-header">
        <span className="eyebrow">Your Account</span>
        <h1>Client profile</h1>
      </div>
      <div className="card flex gap-16" style={{ alignItems: 'center' }}>
        <div className="avatar" style={{ width: 64, height: 64, fontSize: 22 }}>{initials(user.name)}</div>
        <div>
          <div style={{ fontWeight: 800, fontSize: 18 }}>{user.name}</div>
          <div style={{ color: 'var(--color-ink-faint)' }}>{user.email}</div>
          <span className="badge badge-open mt-8">CLIENT</span>
        </div>
      </div>
    </div>
  )
}
