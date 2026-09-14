import { useEffect, useMemo, useState } from 'react'
import { problemService } from '../../services/problemService'
import ProblemCard from '../../components/ProblemCard'
import EmptyState from '../../components/EmptyState'
import Spinner from '../../components/Spinner'
import { categoryLabels } from '../../utils/format'

export default function BrowseProblems() {
  const [problems, setProblems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [filters, setFilters] = useState({ category: '', priority: '', technology: '', maxBudget: '' })

  useEffect(() => {
    (async () => {
      try {
        setProblems(await problemService.getAllOpen())
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  const filtered = useMemo(() => {
    return problems.filter((p) => {
      if (filters.category && p.category !== filters.category) return false
      if (filters.priority && p.priority !== filters.priority) return false
      if (filters.technology && !(p.technology || '').toLowerCase().includes(filters.technology.toLowerCase())) return false
      if (filters.maxBudget && p.budget > Number(filters.maxBudget)) return false
      return true
    })
  }, [problems, filters])

  if (loading) return <Spinner page />

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Job Board</span>
        <h1>Browse open problems</h1>
        <p>Filter by technology, category, priority, or budget to find the right fit.</p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="card mb-24">
        <div className="grid grid-4">
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label>Category</label>
            <select className="select" value={filters.category} onChange={(e) => setFilters({ ...filters, category: e.target.value })}>
              <option value="">All</option>
              {Object.entries(categoryLabels).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
          </div>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label>Priority</label>
            <select className="select" value={filters.priority} onChange={(e) => setFilters({ ...filters, priority: e.target.value })}>
              <option value="">All</option>
              {['LOW', 'MEDIUM', 'HIGH', 'EMERGENCY'].map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
          </div>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label>Technology</label>
            <input className="input" placeholder="e.g. React" value={filters.technology} onChange={(e) => setFilters({ ...filters, technology: e.target.value })} />
          </div>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label>Max budget ($)</label>
            <input type="number" className="input" placeholder="500" value={filters.maxBudget} onChange={(e) => setFilters({ ...filters, maxBudget: e.target.value })} />
          </div>
        </div>
      </div>

      {filtered.length === 0 ? (
        <EmptyState title="No matching problems" subtitle="Try widening your filters." />
      ) : (
        <div className="grid grid-3">
          {filtered.map((p) => (
            <ProblemCard key={p.id} problem={p} viewPath={`/freelancer/problems/${p.id}`} />
          ))}
        </div>
      )}
    </div>
  )
}
