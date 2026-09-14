import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { adminService } from '../../services/adminService'
import Spinner from '../../components/Spinner'

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    (async () => {
      try {
        setStats(await adminService.getStats())
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  if (loading) return <Spinner page />

  const cards = stats ? [
    { label: 'Total Users', value: stats.totalUsers },
    { label: 'Total Freelancers', value: stats.totalFreelancers },
    { label: 'Total Clients', value: stats.totalClients },
    { label: 'Total Problems', value: stats.totalProblems },
    { label: 'Active Jobs', value: stats.activeJobs },
    { label: 'Completed Jobs', value: stats.completedJobs },
    { label: 'Disputes', value: stats.totalDisputes },
    { label: 'Open Disputes', value: stats.openDisputes },
  ] : []

  const sections = [
    { to: '/admin/users', label: 'Users', desc: 'View, verify, and suspend clients & freelancers.' },
    { to: '/admin/problems', label: 'Problems', desc: 'Every problem posted on the platform.' },
    { to: '/admin/jobs', label: 'Jobs', desc: 'All active and completed jobs.' },
    { to: '/admin/disputes', label: 'Disputes', desc: 'Review and resolve raised disputes.' },
  ]

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Admin Console</span>
        <h1>Platform overview</h1>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="grid grid-4 mb-32">
        {cards.map((c) => (
          <div key={c.label} className="card stat-card">
            <div className="stat-label">{c.label}</div>
            <div className="stat-value">{c.value}</div>
          </div>
        ))}
      </div>

      <div className="grid grid-4">
        {sections.map((s) => (
          <Link key={s.to} to={s.to} className="card card-hover" style={{ color: 'inherit' }}>
            <h3 style={{ fontSize: 16 }}>{s.label}</h3>
            <p style={{ fontSize: 13 }}>{s.desc}</p>
          </Link>
        ))}
      </div>
    </div>
  )
}
