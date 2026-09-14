import { Link } from 'react-router-dom'
import { categoryLabels, formatCurrency, formatDate, problemTicketNumber, statusBadgeClass, humanStatus } from '../utils/format'
import { resolveFileUrl } from '../services/api'

export default function ProblemCard({ problem, viewPath }) {
  return (
    <div className="ticket-card card-hover">
      <div className="ticket-stub">
        <span>PROBLEM <span className="ticket-id">#{problemTicketNumber(problem.id)}</span></span>
        <span className={`priority-stripe priority-${problem.priority}`} style={{ width: 34, height: 4 }} />
      </div>
      <div className="ticket-body">
        <div className="flex-between mb-8" style={{ alignItems: 'flex-start' }}>
          <h3 style={{ fontSize: 16, margin: 0 }}>{problem.title}</h3>
          <span className={`badge ${statusBadgeClass(problem.status)}`}>{humanStatus(problem.status)}</span>
        </div>
        <p style={{ fontSize: 13.5, margin: '0 0 14px' }}>
          {problem.description?.length > 130 ? problem.description.slice(0, 130) + '…' : problem.description}
        </p>
        <div className="flex gap-8 flex-wrap mb-16">
          <span className="badge badge-neutral">{categoryLabels[problem.category] || problem.category}</span>
          {problem.technology && <span className="badge badge-neutral">{problem.technology}</span>}
          <span className="badge badge-neutral">{problem.priority}</span>
          {problem.attachmentUrl && <a href={resolveFileUrl(problem.attachmentUrl)} target="_blank" rel="noreferrer" className="badge badge-neutral">📎</a>}
        </div>
        <div className="flex-between">
          <div>
            <div className="mono" style={{ fontSize: 17, fontWeight: 700, color: 'var(--color-brand)' }}>
              {formatCurrency(problem.budget)}
            </div>
            <div style={{ fontSize: 11.5, color: 'var(--color-ink-faint)' }}>Posted {formatDate(problem.createdAt)} · {problem.proposalCount || 0} proposal(s)</div>
          </div>
          <Link to={viewPath || `/problems/${problem.id}`} className="btn btn-dark btn-sm">View Details</Link>
        </div>
      </div>
    </div>
  )
}
