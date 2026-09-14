import { formatCurrency, formatDateTime } from '../utils/format'

export default function ProposalCard({ proposal, onAccept, onReject, showActions }) {
  return (
    <div className="card mb-16">
      <div className="flex-between mb-8" style={{ alignItems: 'flex-start' }}>
        <div className="flex gap-12" style={{ alignItems: 'center' }}>
          <div className="avatar">{proposal.freelancerName?.[0] || '?'}</div>
          <div>
            <div style={{ fontWeight: 700 }}>{proposal.freelancerName}</div>
            <div style={{ fontSize: 12.5, color: 'var(--color-ink-faint)' }}>
              ★ {proposal.freelancerRating?.toFixed(1) || '0.0'} · {proposal.freelancerExperience || 0} yrs exp
            </div>
          </div>
        </div>
        <span className="badge badge-open">{proposal.status}</span>
      </div>

      {proposal.freelancerSkills && (
        <div className="mb-8" style={{ fontSize: 12.5, color: 'var(--color-ink-soft)' }}>
          <strong>Skills:</strong> {proposal.freelancerSkills}
        </div>
      )}

      <p style={{ fontSize: 14 }}>{proposal.message}</p>

      <div className="flex-between mt-16" style={{ alignItems: 'center' }}>
        <div className="flex gap-24">
          <div>
            <div style={{ fontSize: 11, color: 'var(--color-ink-faint)', fontFamily: 'var(--font-mono)' }}>PRICE</div>
            <div style={{ fontWeight: 700, fontSize: 16 }}>{formatCurrency(proposal.price)}</div>
          </div>
          <div>
            <div style={{ fontSize: 11, color: 'var(--color-ink-faint)', fontFamily: 'var(--font-mono)' }}>ETA</div>
            <div style={{ fontWeight: 700, fontSize: 16 }}>{proposal.estimatedDays} day(s)</div>
          </div>
        </div>
        {showActions && proposal.status === 'PENDING' && (
          <div className="flex gap-8">
            <button className="btn btn-outline btn-sm" onClick={() => onReject(proposal.id)}>Reject</button>
            <button className="btn btn-primary btn-sm" onClick={() => onAccept(proposal.id)}>Accept</button>
          </div>
        )}
      </div>
      <div style={{ fontSize: 11, color: 'var(--color-ink-faint)', marginTop: 8 }}>Submitted {formatDateTime(proposal.createdAt)}</div>
    </div>
  )
}
