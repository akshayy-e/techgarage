import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const dashboardFor = (role) =>
    role === 'CLIENT' ? '/client/dashboard' : role === 'FREELANCER' ? '/freelancer/dashboard' : '/admin/dashboard'

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const user = await login(form.email, form.password)
      const redirectTo = location.state?.from || dashboardFor(user.role)
      navigate(redirectTo)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-wrap">
      <div className="auth-card card">
        <span className="eyebrow" style={{ fontFamily: 'var(--font-mono)', fontSize: 12, color: 'var(--color-blueprint)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>Welcome back</span>
        <h1 style={{ fontSize: 24, marginTop: 4 }}>Log in to TechGarage</h1>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input id="email" type="email" className="input" required
              value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </div>
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input id="password" type="password" className="input" required
              value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Logging in…' : 'Log In'}
          </button>
        </form>

        <p style={{ marginTop: 18, fontSize: 13.5, textAlign: 'center' }}>
          Don't have an account? <Link to="/register">Create one</Link>
        </p>

        <div className="alert alert-info mt-16" style={{ fontSize: 12.5 }}>
          <strong>Demo accounts</strong> (see README): admin@techgarage.com / Admin@123 · client1@techgarage.com / Client@123 · java.dev@techgarage.com / Freelancer@123
        </div>
      </div>
    </div>
  )
}
