import { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { userService } from '../../services/userService'
import Spinner from '../../components/Spinner'
import { initials } from '../../utils/format'

export default function ClientProfile() {
  const { user } = useAuth()
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState({ name: '', phone: '' })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState({ type: '', text: '' })

  useEffect(() => {
    userService.getMine().then((p) => {
      setProfile(p)
      setForm({ name: p.name || '', phone: p.phone || '' })
    }).catch((err) => setMessage({ type: 'error', text: err.message }))
      .finally(() => setLoading(false))
  }, [])

  const save = async (e) => {
    e.preventDefault()
    setSaving(true); setMessage({ type: '', text: '' })
    try {
      const updated = await userService.update(form)
      setProfile(updated)
      setMessage({ type: 'success', text: 'Account details updated successfully.' })
      localStorage.setItem('tg_user', JSON.stringify({ ...user, name: updated.name, email: updated.email, role: updated.role }))
      window.dispatchEvent(new Event('tg-profile-updated'))
    } catch (err) {
      setMessage({ type: 'error', text: err.message })
    } finally { setSaving(false) }
  }

  if (loading) return <Spinner page />

  return (
    <div className="container-narrow">
      <div className="page-header">
        <span className="eyebrow">Your Account</span>
        <h1>Client profile</h1>
        <p>Manage the account information clients share with TechGarage.</p>
      </div>
      {message.text && <div className={`alert alert-${message.type}`}>{message.text}</div>}
      <div className="card mb-16 flex gap-16" style={{ alignItems: 'center' }}>
        <div className="avatar" style={{ width: 72, height: 72, fontSize: 24 }}>{initials(profile?.name || user.name)}</div>
        <div>
          <div style={{ fontWeight: 800, fontSize: 20 }}>{profile?.name}</div>
          <div style={{ color: 'var(--color-ink-faint)' }}>{profile?.email}</div>
          <span className="badge badge-open mt-8">CLIENT</span>
        </div>
      </div>
      <form onSubmit={save} className="card">
        <h2 style={{ fontSize: 18, marginTop: 0 }}>Account details</h2>
        <div className="form-group">
          <label htmlFor="client-name">Full name</label>
          <input id="client-name" className="input" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        </div>
        <div className="form-group">
          <label htmlFor="client-email">Email</label>
          <input id="client-email" className="input" value={profile?.email || ''} disabled />
          <div className="form-hint">Email is your login identity and cannot be changed here.</div>
        </div>
        <div className="form-group">
          <label htmlFor="client-phone">Phone</label>
          <input id="client-phone" className="input" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} placeholder="Optional contact number" />
        </div>
        <button className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save Account'}</button>
      </form>
    </div>
  )
}
