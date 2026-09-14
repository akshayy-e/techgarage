import { useEffect, useState } from 'react'
import { freelancerService } from '../../services/freelancerService'
import { userService } from '../../services/userService'
import Spinner from '../../components/Spinner'
import { initials } from '../../utils/format'

export default function FreelancerProfile() {
  const [profile, setProfile] = useState(null)
  const [account, setAccount] = useState(null)
  const [form, setForm] = useState(null)
  const [skillInput, setSkillInput] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [savingAccount, setSavingAccount] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    Promise.all([freelancerService.getMine(), userService.getMine()])
      .then(([p, a]) => {
        setProfile(p); setAccount(a)
        setForm({ bio: p.bio || '', experienceYears: p.experienceYears || 0, skills: p.skills || '', portfolio: p.portfolio || '', hourlyRate: p.hourlyRate || 0, availability: p.availability ?? true })
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  const skills = form?.skills ? form.skills.split(',').map(s => s.trim()).filter(Boolean) : []

  const addSkill = () => {
    const value = skillInput.trim().replace(/,/g, '')
    if (!value || skills.some(s => s.toLowerCase() === value.toLowerCase())) return
    setForm({ ...form, skills: [...skills, value].join(', ') })
    setSkillInput('')
  }
  const removeSkill = (skill) => setForm({ ...form, skills: skills.filter(s => s !== skill).join(', ') })
  const editSkill = (skill) => {
    const value = window.prompt('Edit skill', skill)?.trim().replace(/,/g, '')
    if (!value || skills.some(s => s !== skill && s.toLowerCase() === value.toLowerCase())) return
    setForm({ ...form, skills: skills.map(s => s === skill ? value : s).join(', ') })
  }

  const saveAccount = async (e) => {
    e.preventDefault(); setSavingAccount(true); setError(''); setSuccess('')
    try {
      const updated = await userService.update({ name: account.name, phone: account.phone || '' })
      setAccount(updated)
      const stored = JSON.parse(localStorage.getItem('tg_user') || '{}')
      localStorage.setItem('tg_user', JSON.stringify({ ...stored, name: updated.name, email: updated.email, role: updated.role }))
      window.dispatchEvent(new Event('tg-profile-updated'))
      setSuccess('Account details updated.')
    } catch (err) { setError(err.message) } finally { setSavingAccount(false) }
  }

  const saveProfile = async (e) => {
    e.preventDefault(); setSaving(true); setError(''); setSuccess('')
    try {
      const updated = await freelancerService.updateProfile({ ...form, experienceYears: Number(form.experienceYears), hourlyRate: Number(form.hourlyRate) })
      setProfile(updated); setForm({ ...form, skills: updated.skills || '' }); setSuccess('Freelancer profile updated.')
    } catch (err) { setError(err.message) } finally { setSaving(false) }
  }

  if (loading || !form || !account) return <Spinner page />

  return (
    <div className="container-narrow">
      <div className="page-header">
        <span className="eyebrow">Your Workshop</span>
        <h1>Freelancer profile</h1>
        <p>Keep your account and professional skills ready for clients to review.</p>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="card mb-16 flex gap-16" style={{ alignItems: 'center' }}>
        <div className="avatar" style={{ width: 72, height: 72, fontSize: 24 }}>{initials(account.name)}</div>
        <div>
          <div style={{ fontWeight: 800, fontSize: 20 }}>{account.name}</div>
          <div style={{ color: 'var(--color-ink-faint)' }}>{account.email}</div>
          <span className="badge badge-open mt-8">FREELANCER</span>
        </div>
        <div style={{ marginLeft: 'auto', textAlign: 'right' }}>
          <div style={{ fontWeight: 800 }}>★ {profile.rating?.toFixed(1) || '0.0'}</div>
          <div style={{ fontSize: 12, color: 'var(--color-ink-faint)' }}>{profile.totalReviews || 0} reviews</div>
        </div>
      </div>

      <form onSubmit={saveAccount} className="card mb-16">
        <h2 style={{ fontSize: 18, marginTop: 0 }}>Account details</h2>
        <div className="form-row">
          <div className="form-group"><label>Name</label><input className="input" required value={account.name || ''} onChange={(e) => setAccount({ ...account, name: e.target.value })} /></div>
          <div className="form-group"><label>Phone</label><input className="input" value={account.phone || ''} onChange={(e) => setAccount({ ...account, phone: e.target.value })} /></div>
        </div>
        <div className="form-group"><label>Email</label><input className="input" value={account.email || ''} disabled /><div className="form-hint">Email is your login identity and cannot be changed here.</div></div>
        <button className="btn btn-outline" disabled={savingAccount}>{savingAccount ? 'Saving…' : 'Save Account Details'}</button>
      </form>

      <form onSubmit={saveProfile} className="card">
        <div className="flex-between" style={{ alignItems: 'flex-start' }}>
          <div><h2 style={{ fontSize: 18, marginTop: 0 }}>Professional profile</h2><p style={{ fontSize: 13, color: 'var(--color-ink-faint)' }}>This information appears on your public freelancer profile.</p></div>
          {profile.verified ? <span className="badge badge-completed">Verified</span> : <span className="badge badge-neutral">Pending Verification</span>}
        </div>
        <div className="form-group"><label>Bio</label><textarea className="textarea" value={form.bio} onChange={(e) => setForm({ ...form, bio: e.target.value })} placeholder="Tell clients about your expertise…" /></div>
        <div className="form-row">
          <div className="form-group"><label>Years of experience</label><input type="number" min="0" className="input" value={form.experienceYears} onChange={(e) => setForm({ ...form, experienceYears: e.target.value })} /></div>
          <div className="form-group"><label>Hourly rate (USD)</label><input type="number" min="0" step="0.01" className="input" value={form.hourlyRate} onChange={(e) => setForm({ ...form, hourlyRate: e.target.value })} /></div>
        </div>

        <div className="form-group">
          <label>Skills</label>
          <div className="flex gap-8" style={{ marginBottom: 10 }}>
            <input className="input" value={skillInput} onChange={(e) => setSkillInput(e.target.value)} onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addSkill() } }} placeholder="e.g. React, Java, Spring Boot" />
            <button type="button" className="btn btn-outline" onClick={addSkill}>Add</button>
          </div>
          {skills.length > 0 ? <div className="flex gap-8 flex-wrap">{skills.map(skill => <span key={skill} className="badge badge-neutral" style={{ cursor: 'default' }}>{skill}<button type="button" onClick={() => editSkill(skill)} title="Edit skill" style={{ border: 0, background: 'transparent', cursor: 'pointer', padding: 0 }}>✎</button><button type="button" onClick={() => removeSkill(skill)} title="Delete skill" style={{ border: 0, background: 'transparent', cursor: 'pointer', padding: 0 }}>×</button></span>)}</div> : <div className="form-hint">Add your strongest skills. You can edit or delete each skill before saving.</div>}
        </div>
        <div className="form-group"><label>Portfolio links</label><textarea className="textarea" value={form.portfolio} onChange={(e) => setForm({ ...form, portfolio: e.target.value })} placeholder="GitHub, personal site, case studies…" /></div>
        <div className="form-group"><label><input type="checkbox" checked={form.availability} onChange={(e) => setForm({ ...form, availability: e.target.checked })} style={{ marginRight: 8 }} />Available for new jobs</label></div>
        <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save Professional Profile'}</button>
      </form>
    </div>
  )
}
