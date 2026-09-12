import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '', role: 'CLIENT' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const dashboardFor = (role) =>
    role === 'CLIENT' ? '/client/dashboard' : role === 'FREELANCER' ? '/freelancer/dashboard' : '/admin/dashboard'

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const user = await register(form)
      navigate(dashboardFor(user.role))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-wrap">
      <div className="auth-card card">
        <span style={{ fontFamily: 'var(--font-mono)', fontSize: 12, color: 'var(--color-blueprint)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>Join TechGarage</span>
        <h1 style={{ fontSize: 24, marginTop: 4 }}>Create your account</h1>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="role-toggle">
            <div className={`role-option${form.role === 'CLIENT' ? ' selected' : ''}`} onClick={() => setForm({ ...form, role: 'CLIENT' })}>
              🧑‍💻 I need something fixed
            </div>
            <div className={`role-option${form.role === 'FREELANCER' ? ' selected' : ''}`} onClick={() => setForm({ ...form, role: 'FREELANCER' })}>
              🔧 I fix software problems
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="name">Full name</label>
            <input id="name" className="input" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          </div>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input id="email" type="email" className="input" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </div>
          <div className="form-group">
            <label htmlFor="phone">Phone (optional)</label>
            <input id="phone" className="input" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          </div>
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input id="password" type="password" className="input" required minLength={6} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
            <div className="form-hint">At least 6 characters.</div>
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Creating account…' : 'Create Account'}
          </button>
        </form>

        <p style={{ marginTop: 18, fontSize: 13.5, textAlign: 'center' }}>
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </div>
    </div>
  )
}
