export default function EmptyState({ glyph = '🔧', title, subtitle, action }) {
  return (
    <div className="empty-state">
      <div className="glyph">{glyph}</div>
      <h3>{title}</h3>
      {subtitle && <p>{subtitle}</p>}
      {action}
    </div>
  )
}
