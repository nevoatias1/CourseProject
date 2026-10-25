import { useCallback, useEffect, useState } from 'react'
import { Alert, Badge, Button, Col, Container, Row, Spinner } from 'react-bootstrap'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { addToCart, ApiError, getProduct } from '../api.js'
import FavoriteButton from '../components/FavoriteButton.jsx'
import ProductVisual from '../components/ProductVisual.jsx'
import { formatCurrency } from '../utils/formatters.js'

function ProductSkeleton() {
  return (
    <Container className="product-page">
      <div className="skeleton skeleton-back" />
      <Row className="g-5 align-items-center">
        <Col lg={6}><div className="skeleton product-image-skeleton" /></Col>
        <Col lg={6}><div className="skeleton skeleton-line skeleton-short" /><div className="skeleton skeleton-title" /><div className="skeleton skeleton-line" /><div className="skeleton skeleton-line skeleton-medium" /></Col>
      </Row>
    </Container>
  )
}

function ProductPage({ currentUser, onCartChange, favoriteIds, onFavoriteChange }) {
  const { id } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const [product, setProduct] = useState(null)
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [cartMessage, setCartMessage] = useState('')
  const [adding, setAdding] = useState(false)

  const loadProduct = useCallback(() => {
    setStatus('loading')
    setError('')
    getProduct(id)
      .then((result) => { setProduct(result); setQuantity(1); setStatus('ready') })
      .catch((requestError) => {
        setError(requestError.message)
        setStatus(requestError instanceof ApiError && requestError.status === 404 ? 'not-found' : 'error')
      })
  }, [id])

  useEffect(() => { loadProduct() }, [loadProduct])

  if (status === 'loading') return <ProductSkeleton />
  if (status === 'not-found') {
    return <EmptyProductState title="Product not found" message="We couldn't find that product. It may have been removed or the link may be out of date." />
  }
  if (status === 'error') {
    return <ProductError message={error} onRetry={loadProduct} />
  }

  async function handleAddToCart() {
    if (!currentUser) {
      navigate('/sign-in', { state: { from: location } })
      return
    }
    setAdding(true)
    setCartMessage('')
    try {
      await addToCart(product.id, quantity)
      await onCartChange()
      setCartMessage(`${product.name} was added to your cart.`)
    } catch (requestError) {
      setCartMessage(requestError.message)
    } finally {
      setAdding(false)
    }
  }

  return (
    <Container className="product-page">
      <Link to="/" className="back-link">← Back to catalog</Link>
      <Row className="g-5 align-items-center">
        <Col lg={6}>
          <div className="product-detail-image-wrap">
            <ProductVisual categoryName={product.categoryName} productName={product.name} imageUrl={product.imageUrl} detail />
          </div>
        </Col>
        <Col lg={6}>
          <div className="product-detail-heading"><div><div className="eyebrow">{product.categoryName}</div><h1 className="product-detail-title">{product.name}</h1></div><FavoriteButton productId={product.id} isFavorite={favoriteIds.has(product.id)} currentUser={currentUser} onFavoriteChange={onFavoriteChange} className="product-detail-favorite" /></div>
          <div className="product-detail-price">{formatCurrency(product.price)}</div>
          <p className="product-description">{product.description}</p>
          {product.stock > 0 ? (
            <><Badge bg="success" className="stock-badge">In stock · {product.stock} available</Badge><div className="add-to-cart-row"><label htmlFor="productQuantity">Quantity</label><select id="productQuantity" value={quantity} onChange={(event) => setQuantity(Number(event.target.value))}>{Array.from({ length: product.stock }, (_, index) => <option key={index + 1} value={index + 1}>{index + 1}</option>)}</select><Button variant="primary" onClick={handleAddToCart} disabled={adding}>{adding ? 'Adding...' : 'Add to cart'}</Button></div>{cartMessage && <Alert variant={cartMessage.includes('was added') ? 'success' : 'danger'} className="cart-feedback">{cartMessage}</Alert>}</>
          ) : (
            <><Badge bg="secondary" className="stock-badge">Out of stock</Badge><div className="add-to-cart-row"><Button variant="primary" disabled>Add to cart</Button></div></>
          )}
        </Col>
      </Row>
    </Container>
  )
}

function ProductError({ message, onRetry }) {
  return (
    <Container className="catalog-state">
      <Alert variant="warning"><strong>We couldn't reach the catalog.</strong><br />{message}</Alert>
      <Button variant="primary" onClick={onRetry}><Spinner animation="border" size="sm" className="d-none" /> Try again</Button>
    </Container>
  )
}

function EmptyProductState({ title, message }) {
  return (
    <Container className="catalog-state">
      <div className="empty-state">
        <div className="empty-state-icon">○</div>
        <h2>{title}</h2>
        <p>{message}</p>
        <Button as={Link} to="/" variant="primary">Back to catalog</Button>
      </div>
    </Container>
  )
}

export default ProductPage
