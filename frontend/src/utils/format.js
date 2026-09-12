export function formatCurrency(value) {
  if (value === null || value === undefined) return '—'
  return `$${Number(value).toFixed(2)}`
}

export function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  return date.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })
}

export function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  return date.toLocaleString(undefined, { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

export function initials(name) {
  if (!name) return '?'
  return name.split(' ').map((p) => p[0]).slice(0, 2).join('').toUpperCase()
}

export function ticketNumber(id) {
  return `JOB-${String(id).padStart(4, '0')}`
}

export function problemTicketNumber(id) {
  return `TG-${String(id).padStart(4, '0')}`
}

export const categoryLabels = {
  FRONTEND: 'Frontend', BACKEND: 'Backend', DATABASE: 'Database', API: 'API', CLOUD: 'Cloud',
  DEVOPS: 'DevOps', MOBILE: 'Mobile', UI_UX: 'UI/UX', SECURITY: 'Security', AI_ML: 'AI/ML', OTHER: 'Other',
}

export const statusBadgeClass = (status) => {
  switch (status) {
    case 'OPEN': case 'PROPOSALS_RECEIVED': case 'PENDING': case 'ASSIGNED':
      return 'badge-open'
    case 'IN_PROGRESS': case 'SUBMITTED': case 'REVISION_REQUESTED':
      return 'badge-progress'
    case 'COMPLETED': case 'ACCEPTED': case 'RELEASED':
      return 'badge-completed'
    case 'CANCELLED': case 'REJECTED':
      return 'badge-cancelled'
    case 'DISPUTED':
      return 'badge-danger'
    default:
      return 'badge-neutral'
  }
}

export const humanStatus = (status) => (status || '').replace(/_/g, ' ')
