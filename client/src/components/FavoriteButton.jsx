import { useState } from 'react'
import { Button } from 'react-bootstrap'
import { useLocation, useNavigate } from 'react-router-dom'
import { addFavorite, removeFavorite } from '../api.js'

function FavoriteButton({ productId, isFavorite, currentUser, onFavoriteChange, className = '' }) {
  const [updating, setUpdating] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()

  async function toggleFavorite(event) {
    event.preventDefault()
    event.stopPropagation()
    if (!currentUser) {
      navigate('/sign-in', { state: { from: location } })
      return
    }

    setUpdating(true)
    try {
      if (isFavorite) await removeFavorite(productId)
      else await addFavorite(productId)
      onFavoriteChange(productId, !isFavorite)
    } finally {
      setUpdating(false)
    }
  }

  const label = isFavorite ? 'Remove from favorites' : 'Add to favorites'
  return <Button type="button" variant="light" className={`favorite-button ${className}`} onClick={toggleFavorite} disabled={updating} aria-label={label} title={label}>
    <i className={`bi ${isFavorite ? 'bi-heart-fill' : 'bi-heart'}`} aria-hidden="true" />
  </Button>
}

export default FavoriteButton
