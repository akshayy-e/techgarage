import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { authService } from '../services/authService'

export default function VerifyEmail(){
 const [params]=useSearchParams(); const [state,setState]=useState('loading'); const [error,setError]=useState('')
 useEffect(()=>{const token=params.get('token'); if(!token){setState('error');setError('Verification token is missing.');return} authService.verifyEmail(token).then(()=>setState('success')).catch(e=>{setState('error');setError(e.message)})},[params])
 return <div className="auth-wrap"><div className="auth-card card"><span className="eyebrow">TechGarage account</span><h1 style={{fontSize:24}}>Email verification</h1>{state==='loading'&&<div className="alert alert-info">Verifying your email…</div>}{state==='success'&&<><div className="alert alert-success">Your email has been verified successfully.</div><Link className="btn btn-primary btn-block" to="/login">Continue to login</Link></>}{state==='error'&&<><div className="alert alert-error">{error}</div><Link className="btn btn-outline btn-block" to="/login">Back to login</Link></>}</div></div>
}
