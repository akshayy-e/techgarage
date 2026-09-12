import { useEffect, useState } from 'react'
import { freelancerService } from '../../services/freelancerService'
import Spinner from '../../components/Spinner'

export default function FreelancerProfile() {
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  useEffect(() => {
    (async () => {
      try {
        const p = await freelancerService.getMine()
        setProfile(p)
        setForm({
          bio: p.bio || '', experienceYears: p.experienceYears || 0, skills: p.skills || '',
          portfolio: p.portfolio || '', hourlyRate: p.hourlyRate || 0, availability: p.availability,
        })
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess(false)
    setSaving(true)
    try {
      const updated = await freelancerService.updateProfile({
        ...form,
        experienceYears: Number(form.experienceYears),
        hourlyRate: Number(form.hourlyRate),
      })
      setProfile(updated)
      setSuccess(true)
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  if (loading || !form) return <Spinner page />

  return (
    <div className="container-narrow">
      <div className="page-header">
        <span className="eyebrow">Your Workshop</span>
        <h1>Freelancer profile</h1>
        <p>
          ★ {profile.rating?.toFixed(1) || '0.0'} ({profile.totalReviews || 0} reviews) · Total earnings ${profile.totalEarnings?.toFixed(2) || '0.00'}
          {profile.verified ? <span className="badge badge-completed" style={{ marginLeft: 8 }}>Verified</span> : <span className="badge badge-neutral" style={{ marginLeft: 8 }}>Pending Verification</span>}
        </p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">Profile updated.</div>}

      <form onSubmit={handleSubmit} className="card">
        <div className="form-group">
          <label htmlFor="bio">Bio</label>
          <textarea id="bio" className="textarea" value={form.bio} onChange={(e) => setForm({ ...form, bio: e.target.value })} placeholder="Tell clients about your expertise…" />
        </div>
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="experience">Years of experience</label>
            <input id="experience" type="number" min="0" className="input" value={form.experienceYears} onChange={(e) => setForm({ ...form, experienceYears: e.target.value })} />
          </div>
          <div className="form-group">
            <label htmlFor="rate">Hourly rate (USD)</label>
            <input id="rate" type="number" min="0" step="0.01" className="input" value={form.hourlyRate} onChange={(e) => setForm({ ...form, hourlyRate: e.target.value })} />
          </div>
        </div>
        <div className="form-group">
          <label htmlFor="skills">Skills (comma-separated)</label>
          <input id="skills" className="input" placeholder="React, Node.js, AWS" value={form.skills} onChange={(e) => setForm({ ...form, skills: e.target.value })} />
        </div>
        <div className="form-group">
          <label htmlFor="portfolio">Portfolio links</label>
          <textarea id="portfolio" className="textarea" placeholder="GitHub, personal site, case studies…" value={form.portfolio} onChange={(e) => setForm({ ...form, portfolio: e.target.value })} />
        </div>
        <div className="form-group">
          <label>
            <input type="checkbox" checked={form.availability} onChange={(e) => setForm({ ...form, availability: e.target.checked })} style={{ marginRight: 8 }} />
            Available for new jobs
          </label>
        </div>
        <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save Profile'}</button>
      </form>
    </div>
  )
}
