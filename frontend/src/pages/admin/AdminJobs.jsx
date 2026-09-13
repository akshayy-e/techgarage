import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { adminService } from '../../services/adminService'
import Spinner from '../../components/Spinner'
import { formatCurrency, formatDate, humanStatus, statusBadgeClass, ticketNumber } from '../../utils/format'

export default function AdminJobs() {
  const [jobs, setJobs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('')

  useEffect(() => {
    (async () => {
      try {
        setJobs(await adminService.getJobs())
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  const filtered = useMemo(() => jobs.filter(j => {
    const q = query.trim().toLowerCase()
    return (!q || [j.clientName, j.freelancerName, j.problemTitle, ticketNumber(j.id)].filter(Boolean).some(v => String(v).toLowerCase().includes(q))) && (!status || j.status === status)
  }), [jobs, query, status])

  if (loading) return <Spinner page />

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Admin</span>
        <h1>All jobs</h1>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="card mb-24">
        <div className="filter-grid" style={{gridTemplateColumns:'minmax(220px,2fr) 1fr auto'}}>
          <div className="form-group"><label>Search</label><input className="input" value={query} onChange={e => setQuery(e.target.value)} placeholder="Job, client, freelancer…" /></div>
          <div className="form-group"><label>Status</label><select className="select" value={status} onChange={e => setStatus(e.target.value)}><option value="">All statuses</option>{[...new Set(jobs.map(j=>j.status).filter(Boolean))].map(v=><option key={v} value={v}>{humanStatus(v)}</option>)}</select></div>
          <div className="form-group"><label>Results</label><div className="input" style={{background:'var(--color-bg)'}}>{filtered.length} of {jobs.length}</div></div>
        </div>
      </div>

      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Job</th><th>Client</th><th>Freelancer</th><th>Price</th><th>Status</th><th>Payment</th><th>Started</th><th></th></tr>
          </thead>
          <tbody>
            {filtered.map((j) => (
              <tr key={j.id}>
                <td className="mono">{ticketNumber(j.id)}</td>
                <td>{j.clientName}</td>
                <td>{j.freelancerName}</td>
                <td>{formatCurrency(j.agreedPrice)}</td>
                <td><span className={`badge ${statusBadgeClass(j.status)}`}>{humanStatus(j.status)}</span></td>
                <td><span className="badge badge-neutral">{humanStatus(j.paymentStatus)}</span></td>
                <td>{formatDate(j.startedAt)}</td>
                <td><Link to={`/jobs/${j.id}`} className="btn btn-outline btn-sm">View</Link></td>
              </tr>
            ))}
          </tbody>
        </table>
        {filtered.length === 0 && <div className="table-empty">No jobs match the current filters.</div>}
      </div>
    </div>
  )
}
