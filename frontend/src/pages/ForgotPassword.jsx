import { useState } from 'react'
import { Link } from 'react-router-dom'
import { authService } from '../services/authService'

export default function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const submit = async (e) => {
    e.preventDefault(); setError(''); setMessage(''); setLoading(true)
    try { await authService.forgotPassword({ email }); setMessage('If an account exists for this email, a reset link has been sent.') }
    catch (err) { setError(err.message) } finally { setLoading(false) }
  }
  return <div className="auth-wrap"><div className="auth-card card">
    <span className="eyebrow">Account recovery</span><h1 style={{fontSize:24}}>Forgot password?</h1>
    {error && <div className="alert alert-error">{error}</div>}{message && <div className="alert alert-success">{message}</div>}
    <form onSubmit={submit}><div className="form-group"><label>Email</label><input className="input" type="email" required value={email} onChange={e=>setEmail(e.target.value)} /></div>
      <button className="btn btn-primary btn-block" disabled={loading}>{loading?'Sending…':'Send reset link'}</button></form>
    <p style={{marginTop:18,textAlign:'center',fontSize:13.5}}><Link to="/login">Back to login</Link></p>
  </div></div>
}
