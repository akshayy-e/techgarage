import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { problemService } from '../../services/problemService'
import { jobService } from '../../services/jobService'
import { freelancerService } from '../../services/freelancerService'
import { proposalService } from '../../services/proposalService'
import ProblemCard from '../../components/ProblemCard'
import EmptyState from '../../components/EmptyState'
import Spinner from '../../components/Spinner'

export default function FreelancerDashboard() {
  const [problems, setProblems] = useState([])
  const [jobs, setJobs] = useState([])
  const [profile, setProfile] = useState(null)
  const [proposals, setProposals] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    (async () => {
      try {
        const [p, j, prof, props] = await Promise.all([
          problemService.getAllOpen(),
          jobService.getMine(),
          freelancerService.getMine(),
          proposalService.getMine(),
        ])
        setProblems(p)
        setJobs(j)
        setProfile(prof)
        setProposals(props)
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

  const stats = [
    { label: 'Available Problems', value: problems.length },
    { label: 'Active Jobs', value: activeJobs.length },
    { label: 'Completed Jobs', value: completedJobs.length },
    { label: 'Total Earnings', value: `$${(profile?.totalEarnings || 0).toFixed(2)}` },
  ]

  return (
    <div className="container">
      <div className="page-header flex-between">
        <div>
          <span className="eyebrow">Freelancer Dashboard</span>
          <h1>Your workbench</h1>
          <p>
            ★ {profile?.rating?.toFixed(1) || '0.0'} rating · {profile?.totalReviews || 0} reviews
            {profile?.verified ? <span className="badge badge-completed" style={{ marginLeft: 8 }}>Verified</span> : <span className="badge badge-neutral" style={{ marginLeft: 8 }}>Unverified</span>}
          </p>
        </div>
        <Link to="/freelancer/problems" className="btn btn-primary">Browse Problems</Link>
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

      <div className="flex-between mb-16">
        <h2 style={{ fontSize: 19, margin: 0 }}>Latest open problems</h2>
        <Link to="/freelancer/proposals" style={{ fontSize: 13.5, fontWeight: 700 }}>View my proposals ({proposals.length}) →</Link>
      </div>

      {problems.length === 0 ? (
        <EmptyState title="No open problems right now" subtitle="Check back soon — new problems appear here as clients post them." />
      ) : (
        <div className="grid grid-3">
          {problems.slice(0, 6).map((p) => (
            <ProblemCard key={p.id} problem={p} viewPath={`/freelancer/problems/${p.id}`} />
          ))}
        </div>
      )}
    </div>
  )
}
