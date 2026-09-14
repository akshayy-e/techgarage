import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const CATEGORIES = [
  { icon: '🖥️', label: 'Frontend' },
  { icon: '⚙️', label: 'Backend' },
  { icon: '🗄️', label: 'Database' },
  { icon: '☁️', label: 'Cloud' },
  { icon: '📱', label: 'Mobile' },
  { icon: '🔁', label: 'DevOps' },
  { icon: '🔌', label: 'API' },
  { icon: '🤖', label: 'AI/ML' },
]

const STEPS = [
  { title: 'Post Your Problem', body: 'Describe the bug or issue. Our classifier tags category, tech stack, and priority automatically.' },
  { title: 'Get Expert Proposals', body: 'Verified tech mechanics review your problem and send in fixed-price proposals.' },
  { title: 'Choose Your Tech Mechanic', body: 'Compare ratings, skills, and pricing, then accept the proposal that fits.' },
  { title: 'Get It Fixed', body: 'Track progress, chat directly, review the fix, and release payment when you approve.' },
]

const TRUST = [
  { icon: '✅', label: 'Verified Experts' },
  { icon: '🔒', label: 'Secure Payments' },
  { icon: '⚡', label: 'Fast Support' },
  { icon: '⭐', label: 'Ratings & Reviews' },
]

export default function Landing() {
  const { isAuthenticated, user } = useAuth()

  const fixLink = isAuthenticated
    ? (user.role === 'CLIENT' ? '/client/post-problem' : '/register')
    : '/register'
  const mechanicLink = isAuthenticated
    ? (user.role === 'FREELANCER' ? '/freelancer/problems' : '/register')
    : '/register'

  return (
    <div>
      <section className="hero">
        <div className="container hero-inner">
          <span className="hero-stamp">● Diagnostics Open 24/7</span>
          <h1>Something Broken?<br />Bring It To <span>TechGarage</span>.</h1>
          <p className="lead">
            Get your website, application, API, database, or software problem fixed by verified tech
            experts — post the issue, compare proposals, and watch it get repaired.
          </p>
          <div className="hero-actions">
            <Link to={fixLink} className="btn btn-primary">Fix My Problem</Link>
            <Link to={mechanicLink} className="btn btn-outline" style={{ borderColor: 'rgba(255,255,255,0.4)', color: '#fff' }}>
              Become a Tech Mechanic
            </Link>
          </div>
        </div>
      </section>
      <div className="hazard-divider" />

      <section className="section">
        <div className="container">
          <div className="section-heading">
            <span className="eyebrow">Diagnostic Categories</span>
            <h2>Whatever's broken, there's a bay for it</h2>
          </div>
          <div className="grid grid-4">
            {CATEGORIES.map((c) => (
              <div key={c.label} className="card category-card card-hover">
                <div className="category-icon">{c.icon}</div>
                <div style={{ fontWeight: 700 }}>{c.label}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="section" style={{ background: 'var(--color-surface)', borderTop: '1px solid var(--color-border)', borderBottom: '1px solid var(--color-border)' }}>
        <div className="container">
          <div className="section-heading">
            <span className="eyebrow">How It Works</span>
            <h2>From ticket to fix in four steps</h2>
          </div>
          <div className="grid grid-4">
            {STEPS.map((s, i) => (
              <div key={s.title} className="step">
                <div className="step-num">{i + 1}</div>
                <div>
                  <h4>{s.title}</h4>
                  <p>{s.body}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="section">
        <div className="container">
          <div className="grid grid-4">
            {TRUST.map((t) => (
              <div key={t.label} className="trust-item">
                <span className="ico">{t.icon}</span>
                {t.label}
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  )
}
