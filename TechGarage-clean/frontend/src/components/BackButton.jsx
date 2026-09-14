import { useLocation, useNavigate } from 'react-router-dom'

export default function BackButton() {
  const navigate = useNavigate()
  const location = useLocation()

  if (location.pathname === '/') return null

  const goBack = () => {
    if (window.history.length > 1) navigate(-1)
    else navigate('/')
  }

  return (
    <div className="back-button-wrap">
      <button type="button" className="btn btn-secondary back-button" onClick={goBack} aria-label="Go back">
        <span aria-hidden="true">←</span> Back
      </button>
    </div>
  )
}
