import { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { problemService } from '../../services/problemService'
import { proposalService } from '../../services/proposalService'
import { jobService } from '../../services/jobService'
import ProposalCard from '../../components/ProposalCard'
import Spinner from '../../components/Spinner'
import EmptyState from '../../components/EmptyState'
import { categoryLabels, formatCurrency, formatDate, humanStatus, problemTicketNumber, statusBadgeClass } from '../../utils/format'

export default function ClientProblemDetails() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [problem, setProblem] = useState(null)
  const [proposals, setProposals] = useState([])
  const [job, setJob] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionError, setActionError] = useState('')

  const load = async () => {
    try {
      const p = await problemService.getById(id)
      setProblem(p)
      const props = await proposalService.getForProblem(id)
      setProposals(props)
      if (['ASSIGNED', 'IN_PROGRESS', 'SUBMITTED', 'REVISION_REQUESTED', 'COMPLETED', 'DISPUTED'].includes(p.status)) {
        const jobs = await jobService.getMine()
        const relatedJob = jobs.find((j) => j.problemId === Number(id))
        setJob(relatedJob || null)
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [id])

  const handleAccept = async (proposalId) => {
    setActionError('')
    try {
      await proposalService.accept(proposalId)
      await load()
    } catch (err) {
      setActionError(err.message)
    }
  }

  const handleReject = async (proposalId) => {
    setActionError('')
    try {
      await proposalService.reject(proposalId)
      await load()
    } catch (err) {
      setActionError(err.message)
    }
  }

  if (loading) return <Spinner page />
  if (error) return <div className="container"><div className="alert alert-error">{error}</div></div>
  if (!problem) return null

  return (
    <div className="container">
      <div className="mb-16">
        <Link to="/client/dashboard">← Back to dashboard</Link>
      </div>

      <div className="ticket-card mb-24">
        <div className="ticket-stub">
          <span>PROBLEM <span className="ticket-id">#{problemTicketNumber(problem.id)}</span></span>
          <span className={`badge ${statusBadgeClass(problem.status)}`}>{humanStatus(problem.status)}</span>
        </div>
        <div className="ticket-body">
          <h1 style={{ fontSize: 22 }}>{problem.title}</h1>
          <p>{problem.description}</p>
          <div className="flex gap-8 flex-wrap mb-16">
            <span className="badge badge-neutral">{categoryLabels[problem.category] || problem.category}</span>
            {problem.technology && <span className="badge badge-neutral">{problem.technology}</span>}
            <span className="badge badge-neutral">{problem.priority}</span>
            {problem.attachmentUrl && (
              <a href={`http://localhost:8080${problem.attachmentUrl}`} target="_blank" rel="noreferrer" className="badge badge-neutral">📎 Attachment</a>
            )}
          </div>
          <div className="grid grid-3">
            <div>
              <div style={{ fontSize: 11, color: 'var(--color-ink-faint)', fontFamily: 'var(--font-mono)' }}>BUDGET</div>
              <div style={{ fontWeight: 700 }}>{formatCurrency(problem.budget)}</div>
            </div>
            <div>
              <div style={{ fontSize: 11, color: 'var(--color-ink-faint)', fontFamily: 'var(--font-mono)' }}>POSTED</div>
              <div style={{ fontWeight: 700 }}>{formatDate(problem.createdAt)}</div>
            </div>
            <div>
              <div style={{ fontSize: 11, color: 'var(--color-ink-faint)', fontFamily: 'var(--font-mono)' }}>EXPECTED BY</div>
              <div style={{ fontWeight: 700 }}>{formatDate(problem.expectedCompletionDate)}</div>
            </div>
          </div>
        </div>
      </div>

      {job && (
        <div className="alert alert-info flex-between">
          <span>This problem now has an active job with <strong>{job.freelancerName}</strong>.</span>
          <Link to={`/jobs/${job.id}`} className="btn btn-dark btn-sm">Open Job #{job.id}</Link>
        </div>
      )}

      {actionError && <div className="alert alert-error">{actionError}</div>}

      {!job && (
        <>
          <h2 style={{ fontSize: 19, margin: '24px 0 16px' }}>Proposals ({proposals.length})</h2>
          {proposals.length === 0 ? (
            <EmptyState glyph="📭" title="No proposals yet" subtitle="Freelancers will see this problem in their dashboard and can submit proposals." />
          ) : (
            proposals.map((p) => (
              <ProposalCard key={p.id} proposal={p} showActions onAccept={handleAccept} onReject={handleReject} />
            ))
          )}
        </>
      )}
    </div>
  )
}
