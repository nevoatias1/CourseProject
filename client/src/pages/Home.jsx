import { useEffect, useState } from 'react'
import { Alert, Button, Card, Col, Container, Form, Nav, Row } from 'react-bootstrap'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { ApiError, getCategories, getProducts } from '../api.js'
import FavoriteButton from '../components/FavoriteButton.jsx'
import ProductVisual from '../components/ProductVisual.jsx'
import { formatCurrency } from '../utils/formatters.js'

function CatalogSkeleton() {
  return <Row xs={1} sm={2} lg={4} className="g-4">{Array.from({ length: 8 }, (_, index) => <Col key={index}><div className="skeleton product-card-skeleton"><div className="skeleton-image" /><div className="skeleton-content"><div className="skeleton-line skeleton-short" /><div className="skeleton-line" /><div className="skeleton-line skeleton-price" /></div></div></Col>)}</Row>
}

function Home({ currentUser, favoriteIds, onFavoriteChange }) {
  const [searchParams, setSearchParams] = useSearchParams()
  const categoryId = searchParams.get('categoryId') || ''
  const search = searchParams.get('search') || ''
  const sort = searchParams.get('sort') || 'featured'
  const [categories, setCategories] = useState([])
  const [products, setProducts] = useState([])
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let cancelled = false
    setStatus('loading')
    setError('')
    Promise.all([getCategories(), getProducts({ categoryId, search })])
      .then(([categoryResults, productResults]) => {
        if (cancelled) return
        setCategories(categoryResults)
        setProducts(productResults)
        setStatus('ready')
      })
      .catch((requestError) => {
        if (cancelled) return
        setError(requestError.message)
        setStatus(requestError instanceof ApiError && requestError.status === 0 ? 'unreachable' : 'error')
      })
    return () => { cancelled = true }
  }, [categoryId, search, reloadKey])

  function chooseCategory(nextCategoryId) {
    const nextParams = new URLSearchParams(searchParams)
    if (nextCategoryId) nextParams.set('categoryId', nextCategoryId)
    else nextParams.delete('categoryId')
    setSearchParams(nextParams)
  }

  function clearFilters() {
    setSearchParams(new URLSearchParams())
  }

  function chooseSort(nextSort) {
    const nextParams = new URLSearchParams(searchParams)
    if (nextSort === 'featured') nextParams.delete('sort')
    else nextParams.set('sort', nextSort)
    setSearchParams(nextParams)
  }

  const sortedProducts = [...products].sort((first, second) => {
    if (sort === 'price-asc') return Number(first.price) - Number(second.price)
    if (sort === 'price-desc') return Number(second.price) - Number(first.price)
    return 0
  })

  return (
    <Container className="page-shell">
      <section className="catalog-hero">
        <div>
          <div className="eyebrow">ATIAS EXPRESS · EVERYDAY TECH</div>
          <h1>Useful technology, delivered simply.</h1>
          <p className="hero-copy">Explore reliable electronics and smart accessories chosen to keep up with your day.</p>
          {currentUser && <p className="hero-note">Welcome back, {currentUser.username}.</p>}
        </div>
      </section>

      <section aria-label="Product catalog">
        <div className="catalog-heading"><div><div className="eyebrow">BROWSE THE COLLECTION</div><h2>Find your next essential</h2></div>{search && <span className="filter-summary">Results for “{search}”</span>}</div>
        <div className="catalog-sort"><Form.Label htmlFor="catalogSort">Sort products</Form.Label><Form.Select id="catalogSort" value={sort} onChange={(event) => chooseSort(event.target.value)}><option value="featured">Featured</option><option value="price-asc">Price: low to high</option><option value="price-desc">Price: high to low</option></Form.Select></div>
        <Nav className="category-pills" aria-label="Product categories">
          <Nav.Item><Button variant={categoryId ? 'outline-primary' : 'primary'} className="category-pill" onClick={() => chooseCategory('')}>All</Button></Nav.Item>
          {categories.map((category) => <Nav.Item key={category.id}><Button variant={String(category.id) === categoryId ? 'primary' : 'outline-primary'} className="category-pill" onClick={() => chooseCategory(String(category.id))}>{category.name}</Button></Nav.Item>)}
        </Nav>

        {status === 'loading' && <CatalogSkeleton />}
        {status === 'unreachable' && <CatalogError message={error} onRetry={() => setReloadKey((value) => value + 1)} />}
        {status === 'error' && <CatalogError message={error} onRetry={() => setReloadKey((value) => value + 1)} />}
        {status === 'ready' && products.length === 0 && <EmptyCatalogState onClear={clearFilters} />}
        {status === 'ready' && products.length > 0 && <Row xs={1} sm={2} lg={4} className="g-4">{sortedProducts.map((product) => <ProductCard key={product.id} product={product} currentUser={currentUser} isFavorite={favoriteIds.has(product.id)} onFavoriteChange={onFavoriteChange} />)}</Row>}
      </section>
    </Container>
  )
}

function ProductCard({ product, currentUser, isFavorite, onFavoriteChange }) {
  const navigate = useNavigate()
  function openProduct(event) {
    if (event.target.closest('button')) return
    navigate(`/products/${product.id}`)
  }
  return <Col><Card className="product-card product-card-interactive" onClick={openProduct}><div className="product-image-wrap"><ProductVisual categoryName={product.categoryName} productName={product.name} imageUrl={product.imageUrl} /><FavoriteButton productId={product.id} isFavorite={isFavorite} currentUser={currentUser} onFavoriteChange={onFavoriteChange} className="product-card-favorite" />{product.stock === 0 && <span className="out-of-stock-badge">Out of stock</span>}</div><Card.Body><div className="product-category">{product.categoryName}</div><Card.Title>{product.name}</Card.Title><div className="product-price">{formatCurrency(product.price)}</div></Card.Body></Card></Col>
}

function CatalogError({ message, onRetry }) {
  return <div className="catalog-state"><Alert variant="warning"><strong>We couldn't load the catalog.</strong><br />{message}</Alert><Button variant="primary" onClick={onRetry}>Try again</Button></div>
}

function EmptyCatalogState({ onClear }) {
  return <div className="empty-state"><div className="empty-state-icon">⌕</div><h2>No products found</h2><p>Try another search or browse all categories.</p><Button variant="primary" onClick={onClear}>Clear filters</Button></div>
}

export default Home
