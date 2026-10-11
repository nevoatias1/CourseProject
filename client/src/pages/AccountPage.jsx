import { useCallback, useEffect, useState } from 'react'
import { Alert, Button, Container } from 'react-bootstrap'
import { Link, useSearchParams } from 'react-router-dom'
import { getFavorites } from '../api.js'
import FavoriteButton from '../components/FavoriteButton.jsx'
import ProductVisual from '../components/ProductVisual.jsx'

const currency = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' })

function AccountPage({ currentUser, favoriteIds, onFavoriteChange, onFavoritesRefresh }) {
  const [searchParams] = useSearchParams()
  const [favorites, setFavorites] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const activeTab = searchParams.get('tab') || 'favorites'

  const loadFavorites = useCallback(() => {
    setLoading(true)
    setError('')
    getFavorites()
      .then((products) => {
        setFavorites(products)
        return onFavoritesRefresh()
      })
      .catch((requestError) => setError(requestError.message))
      .finally(() => setLoading(false))
  }, [onFavoritesRefresh])

  useEffect(() => { loadFavorites() }, [loadFavorites])

  function handleFavoriteChange(productId, isFavorite) {
    onFavoriteChange(productId, isFavorite)
    if (!isFavorite) setFavorites((products) => products.filter((product) => product.id !== productId))
  }

  return <Container className="account-page">
    <div className="eyebrow">ACCOUNT</div><h1>{activeTab === 'favorites' ? 'Your favorites' : 'Your account'}</h1>
    {loading && <div className="skeleton account-skeleton" />}
    {error && <div className="catalog-state"><Alert variant="warning"><strong>We couldn't load your favorites.</strong><br />{error}</Alert><Button variant="primary" onClick={loadFavorites}>Try again</Button></div>}
    {!loading && !error && favorites.length === 0 && <div className="empty-state"><div className="empty-state-icon"><i className="bi bi-heart" /></div><h2>No favorites yet</h2><p>Save products you love to find them quickly later.</p><Button as={Link} to="/" variant="primary">Browse products</Button></div>}
    {!loading && !error && favorites.length > 0 && <div className="favorite-list">{favorites.map((product) => <article className="favorite-list-item" key={product.id}><ProductVisual categoryName={product.categoryName} productName={product.name} className="product-visual-favorite" /><div><Link to={`/products/${product.id}`}><h2>{product.name}</h2></Link><p>{product.categoryName || 'Favorite product'}</p><strong>{currency.format(product.price)}</strong></div><FavoriteButton productId={product.id} isFavorite={favoriteIds.has(product.id)} currentUser={currentUser} onFavoriteChange={handleFavoriteChange} /></article>)}</div>}
  </Container>
}

export default AccountPage
