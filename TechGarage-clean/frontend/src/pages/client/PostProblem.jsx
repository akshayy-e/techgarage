import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { problemService } from '../../services/problemService'
import { fileService } from '../../services/fileService'

const CATEGORIES = ['FRONTEND', 'BACKEND', 'DATABASE', 'API', 'CLOUD', 'DEVOPS', 'MOBILE', 'UI_UX', 'SECURITY', 'AI_ML', 'OTHER']
const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'EMERGENCY']

export default function PostProblem() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    title: '', description: '', category: '', technology: '', priority: 'MEDIUM',
    budget: '', expectedCompletionDate: '',
  })
  const [file, setFile] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const [aiLoading, setAiLoading] = useState(false)
  const [aiError, setAiError] = useState('')
  const [aiSuggestion, setAiSuggestion] = useState(null)

  const handleChange = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  const handleAnalyze = async () => {
    setAiError('')
    if (!form.title.trim() || !form.description.trim()) {
      setAiError('Add a title and description first so the AI has something to analyze.')
      return
    }
    setAiLoading(true)
    setAiSuggestion(null)
    try {
      const suggestion = await problemService.aiSuggest({ title: form.title, description: form.description })
      setAiSuggestion(suggestion)
    } catch (err) {
      setAiError(err.message)
    } finally {
      setAiLoading(false)
    }
  }

  const applyAiSuggestion = () => {
    if (!aiSuggestion) return
    const midBudget = aiSuggestion.suggestedBudgetMin != null && aiSuggestion.suggestedBudgetMax != null
      ? Math.round(((aiSuggestion.suggestedBudgetMin + aiSuggestion.suggestedBudgetMax) / 2) / 5) * 5
      : form.budget
    setForm({
      ...form,
      category: aiSuggestion.category || form.category,
      technology: aiSuggestion.technology || form.technology,
      priority: aiSuggestion.priority || form.priority,
      budget: midBudget || form.budget,
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      let attachmentUrl = null
      if (file) {
        const uploaded = await fileService.upload(file)
        attachmentUrl = uploaded.url
      }
      const payload = {
        title: form.title,
        description: form.description,
        category: form.category || null,
        technology: form.technology || null,
        priority: form.priority || null,
        budget: form.budget ? Number(form.budget) : null,
        expectedCompletionDate: form.expectedCompletionDate ? new Date(form.expectedCompletionDate).toISOString() : null,
        attachmentUrl,
      }
      const created = await problemService.create(payload)
      navigate(`/client/problems/${created.id}`)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container-narrow">
      <div className="page-header">
        <span className="eyebrow">New Diagnostic Ticket</span>
        <h1>Post a technical problem</h1>
        <p>Leave category/technology/priority blank and our classifier will suggest them from your description.</p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <form onSubmit={handleSubmit} className="card">
        <div className="form-group">
          <label htmlFor="title">Problem title</label>
          <input id="title" className="input" required placeholder="e.g. React app crashes on checkout page"
            value={form.title} onChange={handleChange('title')} />
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea id="description" className="textarea" required
            placeholder="Describe the bug, what you expected, and any error messages…"
            value={form.description} onChange={handleChange('description')} />
        </div>

        <div className="ai-panel">
          <div className="flex-between">
            <div>
              <strong>✨ AI Assistant</strong>
              <p style={{ margin: '2px 0 0', fontSize: '0.85rem', color: 'var(--color-text-muted, #667085)' }}>
                Get a suggested category, priority, budget, and questions freelancers will ask.
              </p>
            </div>
            <button type="button" className="btn btn-outline btn-sm" onClick={handleAnalyze} disabled={aiLoading}>
              {aiLoading ? 'Analyzing…' : 'Analyze with AI'}
            </button>
          </div>

          {aiError && <div className="alert alert-error" style={{ marginTop: 12 }}>{aiError}</div>}

          {aiSuggestion && (
            <div style={{ marginTop: 14 }}>
              <div className="flex gap-8" style={{ flexWrap: 'wrap' }}>
                <span className="badge badge-neutral">Category: {aiSuggestion.category?.replace('_', '/')}</span>
                <span className="badge badge-neutral">Tech: {aiSuggestion.technology}</span>
                <span className="badge badge-neutral">Priority: {aiSuggestion.priority}</span>
                {aiSuggestion.suggestedBudgetMin != null && (
                  <span className="badge badge-neutral">
                    Budget: ${aiSuggestion.suggestedBudgetMin} – ${aiSuggestion.suggestedBudgetMax}
                  </span>
                )}
              </div>

              {aiSuggestion.summary && (
                <p style={{ marginTop: 10, fontSize: '0.9rem' }}><em>"{aiSuggestion.summary}"</em></p>
              )}

              {aiSuggestion.clarifyingQuestions?.length > 0 && (
                <div style={{ marginTop: 10 }}>
                  <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>Freelancers will likely ask:</span>
                  <ul style={{ margin: '4px 0 0', paddingLeft: 20, fontSize: '0.85rem' }}>
                    {aiSuggestion.clarifyingQuestions.map((q, i) => <li key={i}>{q}</li>)}
                  </ul>
                </div>
              )}

              <div className="flex-between" style={{ marginTop: 12 }}>
                <span style={{ fontSize: '0.78rem', color: 'var(--color-text-muted, #667085)' }}>
                  {aiSuggestion.aiGenerated ? 'Powered by Claude' : 'Using built-in heuristics — add an API key for smarter suggestions'}
                </span>
                <button type="button" className="btn btn-primary btn-sm" onClick={applyAiSuggestion}>
                  Apply suggestions
                </button>
              </div>
            </div>
          )}
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="category">Category (optional)</label>
            <select id="category" className="select" value={form.category} onChange={handleChange('category')}>
              <option value="">Auto-detect</option>
              {CATEGORIES.map((c) => <option key={c} value={c}>{c.replace('_', '/')}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="technology">Technology (optional)</label>
            <input id="technology" className="input" placeholder="e.g. React, Spring Boot, MySQL"
              value={form.technology} onChange={handleChange('technology')} />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="priority">Priority</label>
            <select id="priority" className="select" value={form.priority} onChange={handleChange('priority')}>
              {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="budget">Budget (USD)</label>
            <input id="budget" type="number" min="1" step="0.01" className="input" required placeholder="150"
              value={form.budget} onChange={handleChange('budget')} />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="expected">Expected completion date (optional)</label>
          <input id="expected" type="date" className="input" value={form.expectedCompletionDate} onChange={handleChange('expectedCompletionDate')} />
        </div>

        <div className="form-group">
          <label htmlFor="attachment">Attachment — screenshot, log, or PDF (optional)</label>
          <input id="attachment" type="file" className="input" onChange={(e) => setFile(e.target.files[0])} />
        </div>

        <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
          {loading ? 'Posting…' : 'Post Problem'}
        </button>
      </form>
    </div>
  )
}
