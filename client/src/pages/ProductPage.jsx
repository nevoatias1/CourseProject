import { useCallback, useEffect, useState } from 'react'
import { Alert, Badge, Button, Col, Container, Row, Spinner } from 'react-bootstrap'
import { Link, useParams } from 'react-router-dom'
import { ApiError, getProduct } from '../api.js'
import ProductVisual from '../components/ProductVisual.jsx'

const currency = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' })

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

function ProductPage() {
  const { id } = useParams()
  const [product, setProduct] = useState(null)
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState('')

  const loadProduct = useCallback(() => {
    setStatus('loading')
    setError('')
    getProduct(id)
      .then((result) => { setProduct(result); setStatus('ready') })
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

  return (
    <Container className="product-page">
      <Link to="/" className="back-link">← Back to catalog</Link>
      <Row className="g-5 align-items-center">
        <Col lg={6}>
          <div className="product-detail-image-wrap">
            <ProductVisual categoryName={product.categoryName} productName={product.name} detail />
          </div>
        </Col>
        <Col lg={6}>
          <div className="eyebrow">{product.categoryName}</div>
          <h1 className="product-detail-title">{product.name}</h1>
          <div className="product-detail-price">{currency.format(product.price)}</div>
          <p className="product-description">{product.description}</p>
          {product.stock > 0 ? (
            <Badge bg="success" className="stock-badge">In stock · {product.stock} available</Badge>
          ) : (
            <Badge bg="secondary" className="stock-badge">Out of stock</Badge>
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
