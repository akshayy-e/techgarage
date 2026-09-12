import { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { problemService } from '../../services/problemService'
import { proposalService } from '../../services/proposalService'
import Spinner from '../../components/Spinner'
import { categoryLabels, formatCurrency, formatDate, humanStatus, problemTicketNumber, statusBadgeClass } from '../../utils/format'

export default function FreelancerProblemDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [problem, setProblem] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [alreadyApplied, setAlreadyApplied] = useState(false)
  const [form, setForm] = useState({ price: '', estimatedDays: '', message: '' })
  const [submitting, setSubmitting] = useState(false)
  const [success, setSuccess] = useState(false)

  useEffect(() => {
    (async () => {
      try {
        const p = await problemService.getById(id)
        setProblem(p)
        const mine = await proposalService.getMine()
        setAlreadyApplied(mine.some((pr) => pr.problemId === Number(id)))
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    })()
  }, [id])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await proposalService.submit(id, {
        price: Number(form.price),
        estimatedDays: Number(form.estimatedDays),
        message: form.message,
      })
      setSuccess(true)
      setAlreadyApplied(true)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <Spinner page />
  if (!problem) return null

  return (
    <div className="container">
      <div className="mb-16">
        <Link to="/freelancer/problems">← Back to problems</Link>
      </div>

      <div className="grid" style={{ gridTemplateColumns: '1.4fr 1fr', gap: 24 }}>
        <div className="ticket-card">
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
            </div>
            <div className="grid grid-3">
              <div>
                <div style={{ fontSize: 11, color: 'var(--color-ink-faint)', fontFamily: 'var(--font-mono)' }}>BUDGET</div>
                <div style={{ fontWeight: 700 }}>{formatCurrency(problem.budget)}</div>
              </div>
              <div>
                <div style={{ fontSize: 11, color: 'var(--color-ink-faint)', fontFamily: 'var(--font-mono)' }}>CLIENT</div>
                <div style={{ fontWeight: 700 }}>{problem.clientName}</div>
              </div>
              <div>
                <div style={{ fontSize: 11, color: 'var(--color-ink-faint)', fontFamily: 'var(--font-mono)' }}>POSTED</div>
                <div style={{ fontWeight: 700 }}>{formatDate(problem.createdAt)}</div>
              </div>
            </div>
          </div>
        </div>

        <div className="card" style={{ alignSelf: 'flex-start' }}>
          <h3 style={{ fontSize: 16 }}>Submit a proposal</h3>
          {error && <div className="alert alert-error">{error}</div>}
          {success && <div className="alert alert-success">Proposal submitted! You'll be notified if it's accepted.</div>}

          {alreadyApplied ? (
            <div className="alert alert-info">You've already submitted a proposal for this problem.</div>
          ) : problem.status !== 'OPEN' && problem.status !== 'PROPOSALS_RECEIVED' ? (
            <div className="alert alert-info">This problem is no longer accepting proposals.</div>
          ) : (
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label htmlFor="price">Your price (USD)</label>
                <input id="price" type="number" min="1" step="0.01" className="input" required
                  value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} />
              </div>
              <div className="form-group">
                <label htmlFor="days">Estimated days</label>
                <input id="days" type="number" min="1" className="input" required
                  value={form.estimatedDays} onChange={(e) => setForm({ ...form, estimatedDays: e.target.value })} />
              </div>
              <div className="form-group">
                <label htmlFor="message">Message to client</label>
                <textarea id="message" className="textarea" required placeholder="Explain your approach…"
                  value={form.message} onChange={(e) => setForm({ ...form, message: e.target.value })} />
              </div>
              <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
                {submitting ? 'Submitting…' : 'Submit Proposal'}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}
