import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { adminService } from '../../services/adminService'
import Spinner from '../../components/Spinner'
import { formatCurrency, formatDate, humanStatus, statusBadgeClass, ticketNumber } from '../../utils/format'

export default function AdminJobs() {
  const [jobs, setJobs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

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

  if (loading) return <Spinner page />

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Admin</span>
        <h1>All jobs</h1>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Job</th><th>Client</th><th>Freelancer</th><th>Price</th><th>Status</th><th>Started</th><th></th></tr>
          </thead>
          <tbody>
            {jobs.map((j) => (
              <tr key={j.id}>
                <td className="mono">{ticketNumber(j.id)}</td>
                <td>{j.clientName}</td>
                <td>{j.freelancerName}</td>
                <td>{formatCurrency(j.agreedPrice)}</td>
                <td><span className={`badge ${statusBadgeClass(j.status)}`}>{humanStatus(j.status)}</span></td>
                <td>{formatDate(j.startedAt)}</td>
                <td><Link to={`/jobs/${j.id}`} className="btn btn-outline btn-sm">View</Link></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
