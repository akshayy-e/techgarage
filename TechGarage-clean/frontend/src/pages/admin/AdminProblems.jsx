import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { adminService } from '../../services/adminService'
import Spinner from '../../components/Spinner'
import { categoryLabels, formatCurrency, formatDate, humanStatus, statusBadgeClass } from '../../utils/format'

export default function AdminProblems() {
  const [problems, setProblems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('')
  const [priority, setPriority] = useState('')

  useEffect(() => {
    (async () => {
      try {
        setProblems(await adminService.getProblems())
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  const filtered = useMemo(() => problems.filter(p => {
    const q = query.trim().toLowerCase()
    return (!q || [p.title, p.clientName, p.technology, p.category].filter(Boolean).some(v => String(v).toLowerCase().includes(q)))
      && (!status || p.status === status)
      && (!priority || p.priority === priority)
  }), [problems, query, status, priority])

  if (loading) return <Spinner page />

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Admin</span>
        <h1>All problems</h1>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="card mb-24">
        <div className="filter-grid">
          <div className="form-group"><label>Search</label><input className="input" value={query} onChange={e => setQuery(e.target.value)} placeholder="Title, client, technology…" /></div>
          <div className="form-group"><label>Status</label><select className="select" value={status} onChange={e => setStatus(e.target.value)}><option value="">All statuses</option>{[...new Set(problems.map(p=>p.status).filter(Boolean))].map(v=><option key={v} value={v}>{humanStatus(v)}</option>)}</select></div>
          <div className="form-group"><label>Priority</label><select className="select" value={priority} onChange={e => setPriority(e.target.value)}><option value="">All priorities</option>{[...new Set(problems.map(p=>p.priority).filter(Boolean))].map(v=><option key={v} value={v}>{v}</option>)}</select></div>
          <div className="form-group"><label>Results</label><div className="input" style={{background:'var(--color-bg)'}}>{filtered.length} of {problems.length}</div></div>
        </div>
      </div>

      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Title</th><th>Client</th><th>Category</th><th>Priority</th><th>Budget</th><th>Status</th><th>Posted</th><th>Action</th></tr>
          </thead>
          <tbody>
            {filtered.map((p) => (
              <tr key={p.id}>
                <td>{p.title}</td>
                <td>{p.clientName}</td>
                <td>{categoryLabels[p.category] || p.category}</td>
                <td>{p.priority}</td>
                <td>{formatCurrency(p.budget)}</td>
                <td><span className={`badge ${statusBadgeClass(p.status)}`}>{humanStatus(p.status)}</span></td>
                <td>{formatDate(p.createdAt)}</td>
                <td><Link to={`/client/problems/${p.id}`} className="btn btn-outline btn-sm">View</Link></td>
              </tr>
            ))}
          </tbody>
        </table>
        {filtered.length === 0 && <div className="table-empty">No problems match the current filters.</div>}
      </div>
    </div>
  )
}
