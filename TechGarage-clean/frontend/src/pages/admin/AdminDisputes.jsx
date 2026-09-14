import { useEffect, useState } from 'react'
import { adminService } from '../../services/adminService'
import EmptyState from '../../components/EmptyState'
import Spinner from '../../components/Spinner'
import { formatDateTime, ticketNumber } from '../../utils/format'

export default function AdminDisputes() {
  const [disputes, setDisputes] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [responses, setResponses] = useState({})
  const [actions, setActions] = useState({})
  const [workingId, setWorkingId] = useState(null)

  const load = async () => {
    try {
      setDisputes(await adminService.getDisputes())
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleResolve = async (id, status) => {
    const action = status === 'REJECTED' ? 'RESUME' : (actions[id] || 'RESUME')
    const verb = status === 'REJECTED' ? 'reject this dispute' : 'resolve this dispute'
    if (!window.confirm(`Are you sure you want to ${verb}? This may change the job and payment state.`)) return
    setWorkingId(id); setError('')
    try {
      await adminService.resolveDispute(id, { status, action, adminResponse: responses[id] || '' })
      await load()
    } catch (err) {
      setError(err.message)
    } finally { setWorkingId(null) }
  }

  if (loading) return <Spinner page />

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Admin</span>
        <h1>Disputes</h1>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {disputes.length === 0 ? (
        <EmptyState glyph="⚖️" title="No disputes raised" subtitle="Disputes from clients or freelancers will appear here for review." />
      ) : (
        disputes.map((d) => (
          <div key={d.id} className="card mb-16">
            <div className="flex-between mb-8">
              <strong>Job {ticketNumber(d.job.id)} — {d.reason}</strong>
              <span className="badge badge-danger">{d.status}</span>
            </div>
            <p style={{ fontSize: 13.5 }}>{d.description}</p>
            <div style={{ fontSize: 12, color: 'var(--color-ink-faint)', marginBottom: 12 }}>
              Raised by {d.raisedBy.name} on {formatDateTime(d.createdAt)}
            </div>
            {d.adminResponse && <div className="alert alert-info" style={{ fontSize: 13 }}><strong>Admin response:</strong> {d.adminResponse}</div>}
            {d.status !== 'RESOLVED' && d.status !== 'REJECTED' && (
              <>
                <textarea className="textarea mb-8" placeholder="Write your resolution notes…"
                  value={responses[d.id] || ''} onChange={(e) => setResponses({ ...responses, [d.id]: e.target.value })} />
                <div className="form-group">
                  <label>Resolution action</label>
                  <select className="input" value={actions[d.id] || 'RESUME'} onChange={(e) => setActions({ ...actions, [d.id]: e.target.value })}>
                    <option value="RESUME">Resume job</option>
                    <option value="REFUND_AND_CANCEL">Refund client &amp; cancel job</option>
                    <option value="RELEASE_AND_COMPLETE">Release payment &amp; complete job</option>
                  </select>
                </div>
                <div className="flex gap-8">
                  <button className="btn btn-primary btn-sm" disabled={workingId === d.id} onClick={() => handleResolve(d.id, 'RESOLVED')}>{workingId === d.id ? 'Saving…' : 'Mark Resolved'}</button>
                  <button className="btn btn-outline btn-sm" disabled={workingId === d.id} onClick={() => handleResolve(d.id, 'REJECTED')}>{workingId === d.id ? 'Saving…' : 'Reject Dispute'}</button>
                </div>
              </>
            )}
          </div>
        ))
      )}
    </div>
  )
}
