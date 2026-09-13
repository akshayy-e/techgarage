import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { problemService } from '../../services/problemService'
import { jobService } from '../../services/jobService'
import { freelancerService } from '../../services/freelancerService'
import { proposalService } from '../../services/proposalService'
import { invitationService } from '../../services/invitationService'
import ProblemCard from '../../components/ProblemCard'
import EmptyState from '../../components/EmptyState'
import Spinner from '../../components/Spinner'

function InvitationCard({ inv, onRefresh }) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const respond = async (accepted) => {
    setBusy(true); setError('')
    try { await invitationService.respond(inv.id, accepted); await onRefresh() }
    catch (err) { setError(err?.message || 'Unable to update invitation. Please try again.') }
    finally { setBusy(false) }
  }
  return <div className="card"><div className="flex-between"><span className="eyebrow">INVITATION</span><span className="badge badge-neutral">{inv.status.replaceAll('_',' ')}</span></div><h3 style={{fontSize:16}}>{inv.problemTitle}</h3><p style={{fontSize:13}}>From {inv.clientName}</p>{inv.message && <p style={{fontSize:13}}>{inv.message}</p>}{error && <div className="alert alert-error">{error}</div>}{inv.status === 'PENDING' && <div className="flex gap-8"><button className="btn btn-primary btn-sm" disabled={busy} onClick={() => respond(true)}>{busy ? 'Updating…' : 'Accept'}</button><button className="btn btn-outline btn-sm" disabled={busy} onClick={() => respond(false)}>Decline</button></div>}</div>
}

export default function FreelancerDashboard() {
  const [problems, setProblems] = useState([])
  const [jobs, setJobs] = useState([])
  const [profile, setProfile] = useState(null)
  const [proposals, setProposals] = useState([])
  const [invitations, setInvitations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    (async () => {
      try {
        const [p, j, prof, props, invites] = await Promise.all([
          problemService.getAllOpen(),
          jobService.getMine(),
          freelancerService.getMine(),
          proposalService.getMine(),
          invitationService.getMine(),
        ])
        setProblems(p)
        setJobs(j)
        setProfile(prof)
        setProposals(props)
        setInvitations(invites)
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
        <h2 style={{ fontSize: 19, margin: 0 }}>Active jobs</h2>
        <span style={{ fontSize: 13.5, color: 'var(--color-ink-faint)' }}>{activeJobs.length} in progress</span>
      </div>

      {activeJobs.length === 0 ? (
        <EmptyState
          title="No active jobs yet"
          subtitle="When a client accepts your proposal, the job will appear here with its status, chat, and work controls."
          action={<Link to="/freelancer/proposals" className="btn btn-outline mt-8">View My Proposals</Link>}
        />
      ) : (
        <div className="grid grid-3 mb-32">
          {activeJobs.map((job) => (
            <Link key={job.id} to={`/jobs/${job.id}`} className="card" style={{ textDecoration: 'none', color: 'inherit' }}>
              <div className="flex-between mb-8">
                <span className="eyebrow">JOB #{job.id}</span>
                <span className="badge badge-neutral">{job.status.replaceAll('_', ' ')}</span>
              </div>
              <h3 style={{ fontSize: 16, marginBottom: 8 }}>{job.problemTitle}</h3>
              <p style={{ fontSize: 13, color: 'var(--color-ink-muted)', marginBottom: 12 }}>
                Client: {job.clientName}
              </p>
              <div style={{ fontSize: 13, fontWeight: 700 }}>
                Open job → Chat · Status · Submit solution
              </div>
            </Link>
          ))}
        </div>
      )}

      <div className="flex-between mb-16">
        <h2 style={{ fontSize: 19, margin: 0 }}>Client invitations</h2>
        <span style={{ fontSize: 13.5, color: 'var(--color-ink-faint)' }}>{invitations.filter(i => i.status === 'PENDING').length} pending</span>
      </div>
      {invitations.length === 0 ? <EmptyState glyph="✉️" title="No invitations yet" subtitle="Clients can invite you directly from your public mechanic profile." /> : (
        <div className="grid grid-3 mb-32">
          {invitations.slice(0, 6).map(inv => <InvitationCard key={inv.id} inv={inv} onRefresh={async () => setInvitations(await invitationService.getMine())} />)}
        </div>
      )}

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
