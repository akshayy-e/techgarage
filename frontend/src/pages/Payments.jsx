import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { paymentService } from '../services/paymentService'
import Spinner from '../components/Spinner'
import { formatCurrency, formatDateTime, humanStatus } from '../utils/format'

export default function Payments() {
  const [items, setItems] = useState([]); const [loading, setLoading] = useState(true); const [error, setError] = useState('')
  useEffect(() => { paymentService.mine().then(setItems).catch(e => setError(e.message)).finally(() => setLoading(false)) }, [])
  if (loading) return <Spinner page />
  return <div className="container">
    <div className="mb-16"><Link to="/">← Home</Link></div>
    <span className="eyebrow">Payments</span><h1>Transaction history</h1>
    {error && <div className="alert alert-error">{error}</div>}
    {items.length === 0 ? <div className="card">No payment transactions yet.</div> : <div className="stack gap-12">{items.map(p => <div className="card" key={p.id}>
      <div className="flex-between"><div><strong>Job #{p.jobId}</strong><div style={{fontSize:12,color:'var(--color-ink-faint)'}}>{formatDateTime(p.createdAt)}</div></div><span className="badge">{humanStatus(p.status)}</span></div>
      <div className="grid grid-3" style={{marginTop:14}}><div><small>Amount</small><div>{formatCurrency(p.amount)}</div></div><div><small>Platform fee</small><div>{formatCurrency(p.platformFee)}</div></div><div><small>Freelancer share</small><div>{formatCurrency(p.freelancerAmount)}</div></div></div>
      {p.gatewayPaymentId && <div className="mono" style={{fontSize:11,marginTop:12}}>Payment: {p.gatewayPaymentId}</div>}
    </div>)}</div>}
  </div>
}
