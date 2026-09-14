import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="notfound">
      <div className="code">404</div>
      <h2>This bay is empty</h2>
      <p>The page you're looking for isn't on the lot. Let's get you back on track.</p>
      <Link to="/" className="btn btn-primary">Back to TechGarage</Link>
    </div>
  )
}
