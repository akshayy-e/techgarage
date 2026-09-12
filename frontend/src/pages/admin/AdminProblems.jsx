import { useEffect, useState } from 'react'
import { adminService } from '../../services/adminService'
import Spinner from '../../components/Spinner'
import { categoryLabels, formatCurrency, formatDate, humanStatus, statusBadgeClass } from '../../utils/format'

export default function AdminProblems() {
  const [problems, setProblems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

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

  if (loading) return <Spinner page />

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Admin</span>
        <h1>All problems</h1>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Title</th><th>Client</th><th>Category</th><th>Priority</th><th>Budget</th><th>Status</th><th>Posted</th></tr>
          </thead>
          <tbody>
            {problems.map((p) => (
              <tr key={p.id}>
                <td>{p.title}</td>
                <td>{p.clientName}</td>
                <td>{categoryLabels[p.category] || p.category}</td>
                <td>{p.priority}</td>
                <td>{formatCurrency(p.budget)}</td>
                <td><span className={`badge ${statusBadgeClass(p.status)}`}>{humanStatus(p.status)}</span></td>
                <td>{formatDate(p.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
