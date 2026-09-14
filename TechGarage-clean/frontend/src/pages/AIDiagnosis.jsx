import { useState } from 'react'
import api, { unwrap } from '../services/api'

export default function AIDiagnosis() {
  const [form, setForm] = useState({ title: '', description: '', logs: '', environment: 'production' })
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const update = e => setForm({ ...form, [e.target.name]: e.target.value })
  const diagnose = async e => {
    e.preventDefault(); setError('')
    const title = form.title.trim()
    const description = form.description.trim()
    if (!title || !description) { setError('Problem title and description are required.'); return }
    setLoading(true)
    try { setResult(await unwrap(api.post('/ai/diagnose', { ...form, title, description, logs: form.logs.trim() }))) } catch (err) { setError(err.message) } finally { setLoading(false) }
  }
  return <div className="container page-section">
    <div className="page-header"><div><h1>AI Repair Diagnosis</h1><p>Describe the issue and get a first-pass triage before posting a repair.</p></div></div>
    <form className="card form-grid ai-diagnosis-form" onSubmit={diagnose}>
      <label>Problem title<input name="title" value={form.title} onChange={update} required maxLength={200} placeholder="Spring Boot API returns 500" /></label>
      <label>Environment<select name="environment" value={form.environment} onChange={update}><option>local</option><option>staging</option><option>production</option></select></label>
      <label className="full">Description<textarea name="description" value={form.description} onChange={update} required maxLength={8000} rows={6} placeholder="What is happening, and how can it be reproduced?" /></label>
      <label className="full">Sanitized logs (never include passwords, API keys, tokens or secrets)<textarea name="logs" value={form.logs} onChange={update} maxLength={8000} rows={5} /></label>
      <button className="btn btn-primary" disabled={loading}>{loading ? 'Diagnosing…' : 'Analyze Problem'}</button>
      {error && <p className="error full">{error}</p>}
    </form>
    {result && <div className="card diagnosis-result"><div className="diagnosis-result-head"><span className="eyebrow">DIAGNOSIS REPORT</span><span className="badge badge-neutral">{result.aiGenerated ? 'AI assisted' : 'Rule based'}</span></div><h2>{result.summary}</h2><p className="muted">First-pass technical triage. Do not include passwords, API keys, access tokens, or other secrets in logs.</p><h3>Likely causes</h3><ul>{result.likelyCauses?.map((x,i)=><li key={i}>{x}</li>)}</ul><h3>Information needed</h3><ul>{result.informationNeeded?.map((x,i)=><li key={i}>{x}</li>)}</ul><h3>Next step</h3><p>{result.suggestedNextStep}</p><p><strong>Indicative budget:</strong> ${result.budgetMin}–${result.budgetMax}</p></div>}
  </div>
}
