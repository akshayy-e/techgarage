import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { proposalService } from '../../services/proposalService'
import EmptyState from '../../components/EmptyState'
import Spinner from '../../components/Spinner'
import { formatCurrency, formatDateTime, statusBadgeClass } from '../../utils/format'

export default function MyProposals() {
  const [proposals, setProposals] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    (async () => {
      try {
        setProposals(await proposalService.getMine())
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  if (loading) return <Spinner page />

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Track Record</span>
        <h1>My proposals</h1>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {proposals.length === 0 ? (
        <EmptyState title="No proposals yet" subtitle="Browse open problems and submit your first proposal." action={<Link to="/freelancer/problems" className="btn btn-primary mt-8">Browse Problems</Link>} />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr><th>Problem</th><th>Price</th><th>ETA</th><th>Status</th><th>Submitted</th></tr>
            </thead>
            <tbody>
              {proposals.map((p) => (
                <tr key={p.id}>
                  <td><Link to={`/freelancer/problems/${p.problemId}`}>{p.problemTitle}</Link></td>
                  <td>{formatCurrency(p.price)}</td>
                  <td>{p.estimatedDays} days</td>
                  <td><span className={`badge ${statusBadgeClass(p.status)}`}>{p.status}</span></td>
                  <td>{formatDateTime(p.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
