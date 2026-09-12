import { useEffect, useRef, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { jobService } from '../services/jobService'
import { messageService } from '../services/messageService'
import { reviewService } from '../services/reviewService'
import { useAuth } from '../context/AuthContext'
import JobStatusTracker from '../components/JobStatusTracker'
import StarRating from '../components/StarRating'
import Spinner from '../components/Spinner'
import { formatCurrency, formatDateTime, humanStatus, statusBadgeClass, ticketNumber } from '../utils/format'

export default function JobDetails() {
  const { id } = useParams()
  const { user } = useAuth()
  const [job, setJob] = useState(null)
  const [messages, setMessages] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionError, setActionError] = useState('')
  const [newMessage, setNewMessage] = useState('')
  const [solutionNotes, setSolutionNotes] = useState('')
  const [revisionNotes, setRevisionNotes] = useState('')
  const [rating, setRating] = useState(5)
  const [comment, setComment] = useState('')
  const [reviewed, setReviewed] = useState(false)
  const chatEndRef = useRef(null)

  const isClient = user.role === 'CLIENT'
  const isFreelancer = user.role === 'FREELANCER'

  const load = async () => {
    try {
      const j = await jobService.getById(id)
      setJob(j)
      const m = await messageService.getForJob(id)
      setMessages(m)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [id])
  useEffect(() => { chatEndRef.current?.scrollIntoView({ behavior: 'smooth' }) }, [messages])

  const handleSendMessage = async (e) => {
    e.preventDefault()
    if (!newMessage.trim()) return
    setActionError('')
    try {
      await messageService.send(id, { message: newMessage })
      setNewMessage('')
      const m = await messageService.getForJob(id)
      setMessages(m)
    } catch (err) {
      setActionError(err.message)
    }
  }

  const handleStatusChange = async (status) => {
    setActionError('')
    try {
      await jobService.updateStatus(id, { status })
      await load()
    } catch (err) {
      setActionError(err.message)
    }
  }

  const handleSubmitSolution = async (e) => {
    e.preventDefault()
    setActionError('')
    try {
      await jobService.submitSolution(id, { solutionNotes })
      setSolutionNotes('')
      await load()
    } catch (err) {
      setActionError(err.message)
    }
  }

  const handleRequestRevision = async (e) => {
    e.preventDefault()
    setActionError('')
    try {
      await jobService.requestRevision(id, { revisionNotes })
      setRevisionNotes('')
      await load()
    } catch (err) {
      setActionError(err.message)
    }
  }

  const handleComplete = async () => {
    setActionError('')
    try {
      await jobService.complete(id)
      await load()
    } catch (err) {
      setActionError(err.message)
    }
  }

  const handleReview = async (e) => {
    e.preventDefault()
    setActionError('')
    try {
      await reviewService.submit(id, { rating, comment })
      setReviewed(true)
    } catch (err) {
      setActionError(err.message)
    }
  }

  if (loading) return <Spinner page />
  if (error) return <div className="container"><div className="alert alert-error">{error}</div></div>
  if (!job) return null

  const dashboardPath = isClient ? '/client/dashboard' : isFreelancer ? '/freelancer/dashboard' : '/admin/dashboard'

  return (
    <div className="container">
      <div className="mb-16"><Link to={dashboardPath}>← Back to dashboard</Link></div>

      <div className="flex-between mb-16" style={{ alignItems: 'flex-start' }}>
        <div>
          <span className="eyebrow">Active Job</span>
          <h1 style={{ fontSize: 22 }} className="mono">{ticketNumber(job.id)} — {job.problemTitle}</h1>
        </div>
        <span className={`badge ${statusBadgeClass(job.status)}`} style={{ fontSize: 13 }}>{humanStatus(job.status)}</span>
      </div>

      <div className="card mb-24">
        <JobStatusTracker status={job.status} />
        <div className="grid grid-4">
          <div>
            <div style={{ fontSize: 11, color: 'var(--color-ink-faint)', fontFamily: 'var(--font-mono)' }}>CLIENT</div>
            <div style={{ fontWeight: 700 }}>{job.clientName}</div>
          </div>
          <div>
            <div style={{ fontSize: 11, color: 'var(--color-ink-faint)', fontFamily: 'var(--font-mono)' }}>FREELANCER</div>
            <div style={{ fontWeight: 700 }}>{job.freelancerName}</div>
          </div>
          <div>
            <div style={{ fontSize: 11, color: 'var(--color-ink-faint)', fontFamily: 'var(--font-mono)' }}>AGREED PRICE</div>
            <div style={{ fontWeight: 700 }}>{formatCurrency(job.agreedPrice)}</div>
          </div>
          <div>
            <div style={{ fontSize: 11, color: 'var(--color-ink-faint)', fontFamily: 'var(--font-mono)' }}>PAYMENT</div>
            <div style={{ fontWeight: 700 }}>{humanStatus(job.paymentStatus)}</div>
          </div>
        </div>
      </div>

      {actionError && <div className="alert alert-error">{actionError}</div>}

      <div className="grid" style={{ gridTemplateColumns: '1.3fr 1fr', gap: 24, alignItems: 'flex-start' }}>
        <div className="card">
          <h3 style={{ fontSize: 16 }}>Chat with {isClient ? job.freelancerName : job.clientName}</h3>
          <div className="chat-window">
            {messages.length === 0 && <div style={{ color: 'var(--color-ink-faint)', fontSize: 13, textAlign: 'center', margin: 'auto' }}>No messages yet — say hello!</div>}
            {messages.map((m) => (
              <div key={m.id} className={`chat-bubble ${m.senderId === user.id ? 'mine' : 'theirs'}`}>
                <div>{m.message}</div>
                <div className="meta">{m.senderName} · {formatDateTime(m.createdAt)}</div>
              </div>
            ))}
            <div ref={chatEndRef} />
          </div>
          <form onSubmit={handleSendMessage} className="chat-input-row">
            <input className="input" placeholder="Type a message…" value={newMessage} onChange={(e) => setNewMessage(e.target.value)} />
            <button type="submit" className="btn btn-dark">Send</button>
          </form>
        </div>

        <div className="stack gap-16">
          {isFreelancer && job.status === 'ASSIGNED' && (
            <div className="card">
              <h3 style={{ fontSize: 15 }}>Start work</h3>
              <p style={{ fontSize: 13 }}>Mark this job in progress once you begin diagnosing the issue.</p>
              <button className="btn btn-primary btn-block" onClick={() => handleStatusChange('IN_PROGRESS')}>Move to In Progress</button>
            </div>
          )}

          {isFreelancer && (job.status === 'IN_PROGRESS' || job.status === 'REVISION_REQUESTED') && (
            <div className="card">
              <h3 style={{ fontSize: 15 }}>{job.status === 'REVISION_REQUESTED' ? 'Resubmit solution' : 'Submit solution'}</h3>
              {job.revisionNotes && job.status === 'REVISION_REQUESTED' && (
                <div className="alert alert-info" style={{ fontSize: 13 }}><strong>Client feedback:</strong> {job.revisionNotes}</div>
              )}
              <form onSubmit={handleSubmitSolution}>
                <div className="form-group">
                  <textarea className="textarea" required placeholder="Describe the fix you made…" value={solutionNotes} onChange={(e) => setSolutionNotes(e.target.value)} />
                </div>
                <button type="submit" className="btn btn-primary btn-block">Submit Solution</button>
              </form>
            </div>
          )}

          {isClient && job.status === 'SUBMITTED' && (
            <div className="card">
              <h3 style={{ fontSize: 15 }}>Review the solution</h3>
              {job.solutionNotes && <div className="alert alert-info" style={{ fontSize: 13 }}><strong>Freelancer notes:</strong> {job.solutionNotes}</div>}
              <button className="btn btn-primary btn-block mb-16" onClick={handleComplete}>Approve &amp; Complete Job</button>
              <div style={{ fontSize: 12, color: 'var(--color-ink-faint)', textAlign: 'center', margin: '4px 0' }}>— or —</div>
              <form onSubmit={handleRequestRevision}>
                <div className="form-group">
                  <textarea className="textarea" required placeholder="What needs to change?" value={revisionNotes} onChange={(e) => setRevisionNotes(e.target.value)} />
                </div>
                <button type="submit" className="btn btn-outline btn-block">Request Revision</button>
              </form>
            </div>
          )}

          {isClient && job.status === 'COMPLETED' && !reviewed && (
            <div className="card">
              <h3 style={{ fontSize: 15 }}>Rate {job.freelancerName}</h3>
              <form onSubmit={handleReview}>
                <div className="form-group">
                  <StarRating value={rating} onChange={setRating} />
                </div>
                <div className="form-group">
                  <textarea className="textarea" placeholder="How was your experience?" value={comment} onChange={(e) => setComment(e.target.value)} />
                </div>
                <button type="submit" className="btn btn-primary btn-block">Submit Review</button>
              </form>
            </div>
          )}

          {isClient && job.status === 'COMPLETED' && reviewed && (
            <div className="alert alert-success">Thanks for your review!</div>
          )}

          {job.status === 'COMPLETED' && (
            <div className="alert alert-success">🎉 Job completed and payment released.</div>
          )}
        </div>
      </div>
    </div>
  )
}
