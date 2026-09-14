const STEPS = [
  { key: 'ASSIGNED', label: 'Assigned' },
  { key: 'IN_PROGRESS', label: 'In Progress' },
  { key: 'SUBMITTED', label: 'Submitted' },
  { key: 'COMPLETED', label: 'Completed' },
]

export default function JobStatusTracker({ status }) {
  const effectiveStatus = status === 'REVISION_REQUESTED' ? 'SUBMITTED' : status
  const currentIndex = STEPS.findIndex((s) => s.key === effectiveStatus)

  return (
    <div className="tracker">
      {STEPS.map((step, idx) => {
        let cls = 'tracker-step'
        if (idx < currentIndex) cls += ' done'
        else if (idx === currentIndex) cls += status === 'REVISION_REQUESTED' ? '' : ' current'
        if (status === 'REVISION_REQUESTED' && step.key === 'SUBMITTED') cls += ' current'
        return (
          <div key={step.key} className={cls}>
            <div className="dot">{idx < currentIndex ? '✓' : idx + 1}</div>
            <div className="label">{step.label}</div>
          </div>
        )
      })}
    </div>
  )
}
