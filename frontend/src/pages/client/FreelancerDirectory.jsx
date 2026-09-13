import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { freelancerService } from '../../services/freelancerService'
import Spinner from '../../components/Spinner'
import EmptyState from '../../components/EmptyState'

export default function FreelancerDirectory() {
  const [freelancers, setFreelancers] = useState([])
  const [query, setQuery] = useState('')
  const [technology, setTechnology] = useState('')
  const [verified, setVerified] = useState(false)
  const [available, setAvailable] = useState(false)
  const [minRating, setMinRating] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [searching, setSearching] = useState(false)

  const load = async () => {
    setLoading(true); setSearching(true); setError('')
    try {
      const params = {}
      if (query.trim()) params.query = query.trim()
      if (technology.trim()) params.technology = technology.trim()
      if (verified) params.verified = true
      if (available) params.available = true
      if (minRating) params.minRating = Number(minRating)
      setFreelancers(await freelancerService.getAll(params))
    } catch (err) { setError(err.message) } finally { setLoading(false); setSearching(false) }
  }

  useEffect(() => { load() }, [])

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Tech Mechanic Directory</span>
        <h1>Find the right mechanic</h1>
        <p>Search verified, available freelancers by skill, technology, and reputation.</p>
      </div>

      <div className="card mb-24">
        <div className="form-row">
          <div className="form-group"><label>Search</label><input className="input" value={query} onChange={e => setQuery(e.target.value)} onKeyDown={e => e.key === 'Enter' && load()} placeholder="React, Spring Boot, API, developer..." /></div>
          <div className="form-group"><label>Technology</label><input className="input" value={technology} onChange={e => setTechnology(e.target.value)} onKeyDown={e => e.key === 'Enter' && load()} placeholder="e.g. Java" /></div>
        </div>
        <div className="flex gap-16 flex-wrap" style={{ alignItems: 'center' }}>
          <label className="flex gap-8" style={{ alignItems: 'center' }}><input type="checkbox" checked={verified} onChange={e => setVerified(e.target.checked)} /> Verified only</label>
          <label className="flex gap-8" style={{ alignItems: 'center' }}><input type="checkbox" checked={available} onChange={e => setAvailable(e.target.checked)} /> Available now</label>
          <select className="select" style={{ width: 'auto' }} value={minRating} onChange={e => setMinRating(e.target.value)}><option value="">Any rating</option><option value="4">4.0+</option><option value="4.5">4.5+</option><option value="4.8">4.8+</option></select>
          <button className="btn btn-primary" onClick={load} disabled={searching}>{searching ? 'Searching…' : 'Search mechanics'}</button>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {loading ? <Spinner page /> : freelancers.length === 0 ? <EmptyState glyph="🔧" title="No mechanics found" subtitle="Try a broader skill, technology, or rating filter." /> : (
        <div className="grid grid-3">
          {freelancers.map(f => (
            <div className="card card-hover" key={f.userId}>
              <div className="flex-between" style={{ alignItems: 'flex-start' }}>
                <div><h3 style={{ fontSize: 17 }}>{f.name}</h3><div style={{ fontSize: 13 }}>★ {(f.rating ?? 0).toFixed(1)} · {f.totalReviews ?? 0} reviews</div></div>
                <div className="flex gap-8 flex-wrap" style={{ justifyContent: 'flex-end' }}>
                  {f.verified && <span className="badge badge-completed">✓ Verified</span>}
                  <span className={`badge ${f.availability ? 'badge-open' : 'badge-cancelled'}`}>{f.availability ? 'Available' : 'Busy'}</span>
                </div>
              </div>
              <p style={{ minHeight: 48 }}>{f.bio || 'No bio added yet.'}</p>
              {f.skills && <div className="flex gap-8 flex-wrap mb-16">{f.skills.split(',').slice(0, 6).map(s => <span className="badge badge-neutral" key={s}>{s.trim()}</span>)}</div>}
              <div className="flex-between" style={{ alignItems: 'center' }}><strong>{f.hourlyRate ? `₹${f.hourlyRate}/hr` : 'Rate not set'}</strong><Link to={`/freelancers/${f.userId}`} className="btn btn-outline btn-sm">View profile</Link></div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
