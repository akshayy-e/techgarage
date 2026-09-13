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
import { paymentService, loadRazorpay } from '../services/paymentService'

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
  const [disputeReason, setDisputeReason] = useState('')
  const [disputeDescription, setDisputeDescription] = useState('')
  const [changeRequests, setChangeRequests] = useState([])
  const [changeDescription, setChangeDescription] = useState('')
  const [changePrice, setChangePrice] = useState('')
  const [changeDays, setChangeDays] = useState('0')
  const [paymentLoading, setPaymentLoading] = useState(false)
  const [actionLoading, setActionLoading] = useState('')
  const chatEndRef = useRef(null)

  const isClient = user.role === 'CLIENT'
  const isFreelancer = user.role === 'FREELANCER'
  const isAdmin = user.role === 'ADMIN'

  const load = async () => {
    try {
      const [j, m, cr] = await Promise.all([jobService.getById(id), messageService.getForJob(id), jobService.getChangeRequests(id)])
      setJob(j); setMessages(m); setChangeRequests(cr)
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
      const [m, cr] = await Promise.all([messageService.getForJob(id), jobService.getChangeRequests(id)])
      setMessages(m); setChangeRequests(cr)
    } catch (err) {
      setActionError(err.message)
    }
  }

  const handleStatusChange = async (status) => {
    setActionError(''); setActionLoading(`status-${status}`)
    try { await jobService.updateStatus(id, { status }); await load() }
    catch (err) { setActionError(err.message) }
    finally { setActionLoading('') }
  }

  const handleCancel = async () => {
    if (!window.confirm('Cancel this job? If payment has been held, the configured refund process will apply.')) return
    setActionError(''); setActionLoading('cancel')
    try { await jobService.cancel(id); await load() }
    catch (err) { setActionError(err.message) }
    finally { setActionLoading('') }
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

  const handleRaiseDispute = async (e) => {
    e.preventDefault()
    setActionError('')
    try {
      await jobService.raiseDispute(id, { reason: disputeReason, description: disputeDescription })
      setDisputeReason('')
      setDisputeDescription('')
      await load()
    } catch (err) {
      setActionError(err.message)
    }
  }

  const handleCreateChange = async (e) => {
    e.preventDefault(); setActionError('')
    try {
      await jobService.createChangeRequest(id, { description: changeDescription, additionalPrice: Number(changePrice || 0), additionalDays: Number(changeDays || 0) })
      setChangeDescription(''); setChangePrice(''); setChangeDays('0'); await load()
    } catch (err) { setActionError(err.message) }
  }

  const handleChangeResponse = async (changeRequestId, accept) => {
    setActionError('')
    try { await jobService.respondToChangeRequest(changeRequestId, accept); await load() } catch (err) { setActionError(err.message) }
  }

  const handlePay = async () => {
    setActionError(''); setPaymentLoading(true)
    try {
      const order = await paymentService.createOrder(id)
      await loadRazorpay()
      const razorpay = new window.Razorpay({
        key: order.keyId,
        amount: Math.round(order.amount * 100),
        currency: order.currency,
        name: 'TechGarage',
        description: `Payment for Job #${order.jobId}`,
        order_id: order.orderId,
        prefill: { name: order.customerName, email: order.customerEmail },
        theme: { color: '#111111' },
        handler: async (response) => {
          try { await paymentService.verify({ razorpayOrderId: response.razorpay_order_id, razorpayPaymentId: response.razorpay_payment_id, razorpaySignature: response.razorpay_signature }); await load() }
          catch (err) { setActionError(err.message) }
          finally { setPaymentLoading(false) }
        },
        modal: { ondismiss: () => setPaymentLoading(false) }
      })
      razorpay.on('payment.failed', response => { setActionError(response.error?.description || 'Payment failed'); setPaymentLoading(false) })
      razorpay.open()
    } catch (err) { setActionError(err.message); setPaymentLoading(false) }
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

      <div className="grid job-details-grid" style={{ gridTemplateColumns: '1.3fr 1fr', gap: 24, alignItems: 'flex-start' }}>
        <div className="card">
          <h3 style={{ fontSize: 16 }}>{isAdmin ? 'Job chat (read-only)' : `Chat with ${isClient ? job.freelancerName : job.clientName}`}</h3>
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
          {!isAdmin && <form onSubmit={handleSendMessage} className="chat-input-row">
            <input className="input" maxLength={4000} placeholder="Type a message…" value={newMessage} onChange={(e) => setNewMessage(e.target.value)} />
            <button type="submit" className="btn btn-dark">Send</button>
          </form>}
        </div>

        <div className="stack gap-16">
          {isClient && job.status === 'ASSIGNED' && job.paymentStatus === 'PENDING' && (
            <div className="card">
              <h3 style={{ fontSize: 15 }}>Fund this job</h3>
              <p style={{ fontSize: 13 }}>Your payment is processed securely through Razorpay. Work can begin only after payment is verified.</p>
              <div className="alert alert-info" style={{ fontSize: 13 }}><strong>{formatCurrency(job.agreedPrice)}</strong> total job price</div>
              <button className="btn btn-primary btn-block" onClick={handlePay} disabled={paymentLoading}>{paymentLoading ? 'Opening secure checkout…' : 'Pay & Fund Job'}</button>
            </div>
          )}

          {isFreelancer && job.status === 'ASSIGNED' && (
            <div className="card">
              <h3 style={{ fontSize: 15 }}>Start work</h3>
              <p style={{ fontSize: 13 }}>Mark this job in progress once you begin diagnosing the issue.</p>
              <button className="btn btn-primary btn-block" disabled={actionLoading === 'status-IN_PROGRESS'} onClick={() => handleStatusChange('IN_PROGRESS')}>{actionLoading === 'status-IN_PROGRESS' ? 'Starting…' : 'Move to In Progress'}</button>
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

          {(isClient || isFreelancer) && !['COMPLETED', 'CANCELLED', 'DISPUTED'].includes(job.status) && (
            <div className="card">
              <h3 style={{ fontSize: 15 }}>Scope &amp; change requests</h3>
              <p style={{ fontSize: 13 }}>Keep extra work explicit. A change can add price and/or time and must be approved by the other party.</p>
              {changeRequests.map(cr => <div key={cr.id} className="alert alert-info" style={{ fontSize: 13, marginBottom: 8 }}>
                <strong>{cr.status}</strong> · {cr.requestedByName}: {cr.description}<br/>
                +{formatCurrency(cr.additionalPrice)} · +{cr.additionalDays} day(s)
                {cr.status === 'PENDING' && cr.requestedById !== user.id && <div className="flex gap-8" style={{ marginTop: 8 }}>
                  <button className="btn btn-primary btn-sm" onClick={() => handleChangeResponse(cr.id, true)}>Accept change</button>
                  <button className="btn btn-outline btn-sm" onClick={() => handleChangeResponse(cr.id, false)}>Reject</button>
                </div>}
              </div>)}
              {!changeRequests.some(cr => cr.status === 'PENDING') && (
                <form onSubmit={handleCreateChange}>
                  <div className="form-group"><label>Additional work</label><textarea className="textarea" required maxLength={2000} value={changeDescription} onChange={e=>setChangeDescription(e.target.value)} placeholder="Describe what changed in the requested scope…" /></div>
                  <div className="grid grid-2">
                    <div className="form-group"><label>Additional price</label><input className="input" type="number" min="0" step="0.01" value={changePrice} onChange={e=>setChangePrice(e.target.value)} /></div>
                    <div className="form-group"><label>Additional days</label><input className="input" type="number" min="0" max="365" value={changeDays} onChange={e=>setChangeDays(e.target.value)} /></div>
                  </div>
                  <button className="btn btn-outline btn-block" type="submit">Propose Scope Change</button>
                </form>
              )}
            </div>
          )}

          {(isClient || isFreelancer) && ['ASSIGNED', 'IN_PROGRESS', 'SUBMITTED', 'REVISION_REQUESTED'].includes(job.status) && (
            <div className="card">
              <h3 style={{ fontSize: 15 }}>Cancel job</h3>
              <p style={{ fontSize: 13 }}>Cancel only if you no longer want this repair to continue. A held payment may be refunded according to the payment configuration.</p>
              <button className="btn btn-danger btn-block" disabled={actionLoading === 'cancel'} onClick={handleCancel}>{actionLoading === 'cancel' ? 'Cancelling…' : 'Cancel Job'}</button>
            </div>
          )}

          {(isClient || isFreelancer) && ['ASSIGNED', 'IN_PROGRESS', 'SUBMITTED', 'REVISION_REQUESTED'].includes(job.status) && (
            <div className="card">
              <h3 style={{ fontSize: 15 }}>Need help? Raise a dispute</h3>
              <p style={{ fontSize: 13 }}>Use a dispute when you cannot resolve the issue directly. An admin will review the case.</p>
              <form onSubmit={handleRaiseDispute}>
                <div className="form-group">
                  <label>Reason</label>
                  <input className="input" required maxLength={200} value={disputeReason} onChange={(e) => setDisputeReason(e.target.value)} placeholder="e.g. Solution does not match the agreed scope" />
                </div>
                <div className="form-group">
                  <label>Details</label>
                  <textarea className="textarea" maxLength={3000} value={disputeDescription} onChange={(e) => setDisputeDescription(e.target.value)} placeholder="Provide the facts and evidence an admin should review." />
                </div>
                <button type="submit" className="btn btn-outline btn-block">Raise Dispute</button>
              </form>
            </div>
          )}

          {job.status === 'DISPUTED' && (
            <div className="alert alert-info">This job is under dispute review. Cancellation is disabled until an admin resolves the dispute.</div>
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
