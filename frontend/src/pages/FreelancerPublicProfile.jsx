import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { freelancerService } from '../services/freelancerService'
import { reviewService } from '../services/reviewService'
import Spinner from '../components/Spinner'
import EmptyState from '../components/EmptyState'
import { formatDateTime } from '../utils/format'

export default function FreelancerPublicProfile() {
  const { userId } = useParams()
  const [profile, setProfile] = useState(null)
  const [reviews, setReviews] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

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
