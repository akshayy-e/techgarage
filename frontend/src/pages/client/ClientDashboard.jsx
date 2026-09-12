import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { problemService } from '../../services/problemService'
import { jobService } from '../../services/jobService'
import ProblemCard from '../../components/ProblemCard'
import EmptyState from '../../components/EmptyState'
import Spinner from '../../components/Spinner'

export default function ClientDashboard() {
  const [problems, setProblems] = useState([])
  const [jobs, setJobs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    (async () => {
      try {
        const [p, j] = await Promise.all([problemService.getMine(), jobService.getMine()])
        setProblems(p)
        setJobs(j)
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  if (loading) return <Spinner page />

  const activeJobs = jobs.filter((j) => !['COMPLETED', 'CANCELLED'].includes(j.status))
  const completedJobs = jobs.filter((j) => j.status === 'COMPLETED')
  const pendingProposals = problems.reduce((sum, p) => sum + (p.status === 'PROPOSALS_RECEIVED' ? (p.proposalCount || 0) : 0), 0)

  const stats = [
    { label: 'Total Problems', value: problems.length },
    { label: 'Active Jobs', value: activeJobs.length },
    { label: 'Completed Jobs', value: completedJobs.length },
    { label: 'Pending Proposals', value: pendingProposals },
  ]

  return (
    <div className="container">
      <div className="page-header flex-between">
        <div>
          <span className="eyebrow">Client Dashboard</span>
          <h1>Your repair bay</h1>
          <p>Track your problems, proposals, and jobs in one place.</p>
        </div>
        <Link to="/client/post-problem" className="btn btn-primary">+ Post Problem</Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="grid grid-4 mb-32">
        {stats.map((s) => (
          <div key={s.label} className="card stat-card">
            <div className="stat-label">{s.label}</div>
            <div className="stat-value">{s.value}</div>
          </div>
        ))}
      </div>

      <h2 style={{ fontSize: 19, marginBottom: 16 }}>Your problems</h2>
      {problems.length === 0 ? (
        <EmptyState
          title="No problems posted yet"
          subtitle="Post your first software problem and get proposals from verified tech mechanics."
          action={<Link to="/client/post-problem" className="btn btn-primary mt-8">Post Your First Problem</Link>}
        />
      ) : (
        <div className="grid grid-3">
          {problems.map((p) => (
            <ProblemCard key={p.id} problem={p} viewPath={`/client/problems/${p.id}`} />
          ))}
        </div>
      )}
    </div>
  )
}
