import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { problemService } from '../services/problemService'
import Spinner from '../components/Spinner'
import { formatCurrency, formatDateTime, ticketNumber } from '../utils/format'
export default function EmergencyJobs(){
 const [items,setItems]=useState([]),[loading,setLoading]=useState(true),[error,setError]=useState('')
 useEffect(()=>{problemService.getEmergency().then(setItems).catch(e=>setError(e.message)).finally(()=>setLoading(false))},[])
 if(loading)return <Spinner page/>
 return <div className="container page-section"><div className="page-header"><div><span className="eyebrow">Priority dispatch</span><h1>Emergency Fixes</h1><p>Production-critical software problems that need a fast response.</p></div></div>{error&&<div className="alert alert-error">{error}</div>}{items.length===0?<div className="empty-state"><div className="glyph">🚨</div><h3>No emergency repairs right now</h3><p>Check back soon or browse normal repair requests.</p></div>:<div className="grid grid-2">{items.map(p=><div className="card" key={p.id}><div className="flex-between"><span className="badge badge-danger">EMERGENCY</span><span className="mono">{ticketNumber(p.id)}</span></div><h2 style={{fontSize:18}}>{p.title}</h2><p>{p.description}</p><div className="muted">{p.technology||'Technology not specified'} · {formatCurrency(p.budget)} · {formatDateTime(p.createdAt)}</div><div className="mt-16"><Link className="btn btn-primary btn-sm" to={`/freelancer/problems/${p.id}`}>Open Repair</Link></div></div>)}</div>}</div>
}
