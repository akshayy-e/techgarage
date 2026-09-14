import { useState } from 'react'
import { Link, useSearchParams, useNavigate } from 'react-router-dom'
import { authService } from '../services/authService'

export default function ResetPassword() {
  const [params] = useSearchParams(); const navigate = useNavigate()
  const [password,setPassword]=useState(''); const [confirm,setConfirm]=useState(''); const [message,setMessage]=useState(''); const [error,setError]=useState(''); const [loading,setLoading]=useState(false)
  const token=params.get('token')||''
  const submit=async(e)=>{e.preventDefault();setError('');if(!token)return setError('Reset token is missing.');if(password!==confirm)return setError('Passwords do not match.');setLoading(true);try{await authService.resetPassword({token,newPassword:password});setMessage('Password reset successfully. You can now log in.');setTimeout(()=>navigate('/login'),1200)}catch(err){setError(err.message)}finally{setLoading(false)}}
  return <div className="auth-wrap"><div className="auth-card card"><span className="eyebrow">Account recovery</span><h1 style={{fontSize:24}}>Set a new password</h1>
    {error&&<div className="alert alert-error">{error}</div>}{message&&<div className="alert alert-success">{message}</div>}
    <form onSubmit={submit}><div className="form-group"><label>New password</label><input className="input" type="password" minLength={8} required value={password} onChange={e=>setPassword(e.target.value)}/></div><div className="form-group"><label>Confirm password</label><input className="input" type="password" minLength={8} required value={confirm} onChange={e=>setConfirm(e.target.value)}/></div><button className="btn btn-primary btn-block" disabled={loading}>{loading?'Updating…':'Reset password'}</button></form>
    <p style={{marginTop:18,textAlign:'center',fontSize:13.5}}><Link to="/login">Back to login</Link></p>
  </div></div>
}
