import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { freelancerService } from '../services/freelancerService'
import { reviewService } from '../services/reviewService'
import Spinner from '../components/Spinner'
import EmptyState from '../components/EmptyState'
import { formatDateTime } from '../utils/format'
import { useAuth } from '../context/AuthContext'
import { problemService } from '../services/problemService'
import { invitationService } from '../services/invitationService'

export default function FreelancerPublicProfile() {
  const { userId } = useParams()
  const [profile, setProfile] = useState(null)
  const [reviews, setReviews] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [inviteOpen, setInviteOpen] = useState(false)
  const [problems, setProblems] = useState([])
  const [problemId, setProblemId] = useState('')
  const [message, setMessage] = useState('')
  const [inviteState, setInviteState] = useState({ loading: false, error: '', success: '' })
  const { user } = useAuth()

  useEffect(() => {
    (async () => {
      try {
        const [p, r] = await Promise.all([
          freelancerService.getByUserId(userId),
          reviewService.getForFreelancer(userId),
        ])
        setProfile(p)
        setReviews(r)
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    })()
  }, [userId])

  if (loading) return <Spinner page />
  if (error) return <div className="container"><div className="alert alert-error">{error}</div></div>
  if (!profile) return null

  const openInvite = async () => {
    setInviteState({ loading: true, error: '', success: '' })
    try {
      const mine = await problemService.getMine()
      const open = mine.filter(p => ['OPEN', 'PROPOSALS_RECEIVED'].includes(p.status))
      setProblems(open)
      setProblemId(open[0]?.id || '')
      setInviteOpen(true)
    } catch (err) { setInviteState({ loading: false, error: err.message, success: '' }); return }
    setInviteState({ loading: false, error: '', success: '' })
  }

  const sendInvite = async () => {
    if (!problemId) return
    setInviteState({ loading: true, error: '', success: '' })
    try {
      await invitationService.invite(profile.userId, { problemId: Number(problemId), message })
      setInviteState({ loading: false, error: '', success: 'Invitation sent. The freelancer will see it in their notifications.' })
      setMessage('')
    } catch (err) { setInviteState({ loading: false, error: err.message, success: '' }) }
  }

  return (
    <div className="container-narrow">
      <div className="card mb-24">
        <div className="flex-between" style={{ alignItems: 'flex-start' }}>
          <div>
            <h1 style={{ fontSize: 22 }}>{profile.name}</h1>
            <p>★ {profile.rating?.toFixed(1) || '0.0'} ({profile.totalReviews || 0} reviews) · {profile.experienceYears || 0} yrs experience</p>
          </div>
          {profile.verified && <span className="badge badge-completed">Verified</span>}
        </div>
        <p>{profile.bio}</p>
        <div className="flex gap-8" style={{ marginTop: 16 }}>
          {user?.role === 'CLIENT' && profile.availability && <button className="btn btn-primary btn-sm" onClick={openInvite}>Invite to a problem</button>}
          {profile.portfolio && <a className="btn btn-outline btn-sm" href={profile.portfolio.startsWith('http') ? profile.portfolio : `https://${profile.portfolio}`} target="_blank" rel="noreferrer">View portfolio</a>}
        </div>
        {inviteOpen && user?.role === 'CLIENT' && (
          <div className="card" style={{ marginTop: 18, background: 'var(--color-bg)' }}>
            <div className="flex-between"><strong>Invite {profile.name}</strong><button className="btn btn-outline btn-sm" onClick={() => setInviteOpen(false)}>Close</button></div>
            {inviteState.error && <div className="alert alert-error" style={{ marginTop: 12 }}>{inviteState.error}</div>}
            {inviteState.success && <div className="alert alert-success" style={{ marginTop: 12 }}>{inviteState.success}</div>}
            {problems.length === 0 ? <p style={{ marginTop: 12 }}>You have no open problems to invite this mechanic to. <Link to="/client/post-problem">Post a problem</Link> first.</p> : (
              <><div className="form-group" style={{ marginTop: 14 }}><label>Problem</label><select className="select" value={problemId} onChange={e => setProblemId(e.target.value)}>{problems.map(p => <option value={p.id} key={p.id}>#{p.id} — {p.title}</option>)}</select></div><div className="form-group"><label>Message (optional)</label><textarea className="textarea" value={message} onChange={e => setMessage(e.target.value)} maxLength={1000} placeholder="Tell the mechanic why this problem may be a good fit..." /></div><button className="btn btn-primary" disabled={inviteState.loading} onClick={sendInvite}>{inviteState.loading ? 'Sending...' : 'Send invitation'}</button></>
            )}
          </div>
        )}
        {profile.skills && <div className="flex gap-8 flex-wrap">{profile.skills.split(',').map((s) => <span key={s} className="badge badge-neutral">{s.trim()}</span>)}</div>}
      </div>

      <h2 style={{ fontSize: 18 }}>Reviews</h2>
      {reviews.length === 0 ? (
        <EmptyState glyph="⭐" title="No reviews yet" />
      ) : (
        reviews.map((r) => (
          <div key={r.id} className="card mb-16">
            <div className="flex-between">
              <strong>{r.clientName}</strong>
              <span>{'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}</span>
            </div>
            <p style={{ marginTop: 6 }}>{r.comment}</p>
            <div style={{ fontSize: 11.5, color: 'var(--color-ink-faint)' }}>{formatDateTime(r.createdAt)}</div>
          </div>
        ))
      )}
    </div>
  )
}
