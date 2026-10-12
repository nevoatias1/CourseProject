import { useCallback, useEffect, useState } from 'react'
import { Alert, Button, Container, Form, Modal, Nav, Spinner } from 'react-bootstrap'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { deleteCurrentUser, getFavorites, getOrders, updateCurrentUser } from '../api.js'
import FavoriteButton from '../components/FavoriteButton.jsx'
import ProductVisual from '../components/ProductVisual.jsx'
import { formatCurrency, formatDateTime as formatDate } from '../utils/formatters.js'

const currency = { format: formatCurrency }

function AccountPage({ currentUser, favoriteIds, onFavoriteChange, onFavoritesRefresh, onUserUpdate, onLogout }) {
  const [searchParams, setSearchParams] = useSearchParams()
  const activeTab = ['profile', 'orders', 'favorites'].includes(searchParams.get('tab')) ? searchParams.get('tab') : 'profile'

  function chooseTab(tab) {
    setSearchParams({ tab })
  }

  return <Container className="account-page">
    <div className="eyebrow">ACCOUNT</div><h1>My account</h1>
    <Nav variant="pills" className="account-tabs" activeKey={activeTab}>
      <Nav.Item><Nav.Link eventKey="profile" onClick={() => chooseTab('profile')}>Profile</Nav.Link></Nav.Item>
      <Nav.Item><Nav.Link eventKey="orders" onClick={() => chooseTab('orders')}>Orders</Nav.Link></Nav.Item>
      <Nav.Item><Nav.Link eventKey="favorites" onClick={() => chooseTab('favorites')}>Favorites</Nav.Link></Nav.Item>
    </Nav>
    {activeTab === 'profile' && <ProfileTab currentUser={currentUser} onUserUpdate={onUserUpdate} onLogout={onLogout} />}
    {activeTab === 'orders' && <OrdersTab />}
    {activeTab === 'favorites' && <FavoritesTab currentUser={currentUser} favoriteIds={favoriteIds} onFavoriteChange={onFavoriteChange} onFavoritesRefresh={onFavoritesRefresh} />}
  </Container>
}

function ProfileTab({ currentUser, onUserUpdate, onLogout }) {
  const navigate = useNavigate()
  const [form, setForm] = useState(profileForm(currentUser))
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [saving, setSaving] = useState(false)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => { setForm(profileForm(currentUser)) }, [currentUser])

  function updateField(event) {
    setForm((values) => ({ ...values, [event.target.name]: event.target.value }))
  }

  async function saveProfile(event) {
    event.preventDefault()
    setError('')
    setSuccess('')
    if (!form.first_name.trim() || !form.last_name.trim() || !form.email.trim()) {
      setError('First name, last name, and email are required.')
      return
    }
    if (form.password || form.confirmPassword) {
      if (form.password !== form.confirmPassword) {
        setError('New passwords do not match.')
        return
      }
    }
    const payload = { first_name: form.first_name.trim(), last_name: form.last_name.trim(), email: form.email.trim(), phone: form.phone.trim(), address: form.address.trim() }
    if (form.password) payload.password = form.password
    setSaving(true)
    try {
      const user = await updateCurrentUser(payload)
      onUserUpdate(user)
      setForm(profileForm(user))
      setSuccess('Your profile has been updated.')
    } catch (requestError) {
      setError(requestError.status === 409 ? 'This email is already in use.' : requestError.message)
    } finally {
      setSaving(false)
    }
  }

  async function deleteAccount() {
    setDeleting(true)
    setError('')
    try {
      await deleteCurrentUser()
      onLogout()
      navigate('/', { replace: true })
    } catch (requestError) {
      setError(requestError.message)
      setShowDeleteModal(false)
    } finally {
      setDeleting(false)
    }
  }

  return <section className="account-panel profile-panel">
    <h2>Profile details</h2><p className="muted-copy">Keep your contact details ready for checkout.</p>
    {error && <Alert variant="danger">{error}</Alert>}{success && <Alert variant="success">{success}</Alert>}
    <Form onSubmit={saveProfile} noValidate>
      <div className="profile-form-grid"><Form.Group controlId="profileFirstName"><Form.Label>First name</Form.Label><Form.Control name="first_name" value={form.first_name} onChange={updateField} /></Form.Group><Form.Group controlId="profileLastName"><Form.Label>Last name</Form.Label><Form.Control name="last_name" value={form.last_name} onChange={updateField} /></Form.Group><Form.Group controlId="profileEmail"><Form.Label>Email</Form.Label><Form.Control type="email" name="email" value={form.email} onChange={updateField} /></Form.Group><Form.Group controlId="profilePhone"><Form.Label>Phone</Form.Label><Form.Control name="phone" value={form.phone} onChange={updateField} /></Form.Group></div>
      <Form.Group className="mt-3" controlId="profileAddress"><Form.Label>Address</Form.Label><Form.Control name="address" value={form.address} onChange={updateField} /></Form.Group>
      <Form.Group className="mt-3" controlId="profileUsername"><Form.Label>Username</Form.Label><Form.Control value={currentUser.username} readOnly /></Form.Group>
      <div className="password-section"><h3>Change password <span>Optional</span></h3><div className="profile-form-grid"><Form.Group controlId="profilePassword"><Form.Label>New password</Form.Label><Form.Control type="password" name="password" value={form.password} onChange={updateField} autoComplete="new-password" /></Form.Group><Form.Group controlId="profileConfirmPassword"><Form.Label>Confirm new password</Form.Label><Form.Control type="password" name="confirmPassword" value={form.confirmPassword} onChange={updateField} autoComplete="new-password" /></Form.Group></div></div>
      <div className="profile-actions"><Button type="submit" variant="primary" disabled={saving}>{saving ? <><Spinner size="sm" /> Saving...</> : 'Save changes'}</Button><Button type="button" variant="outline-danger" onClick={() => setShowDeleteModal(true)}>Delete account</Button></div>
    </Form>
    <Modal show={showDeleteModal} onHide={() => !deleting && setShowDeleteModal(false)} centered><Modal.Header closeButton={!deleting}><Modal.Title>Delete your account?</Modal.Title></Modal.Header><Modal.Body>This permanently deletes your account and signs you out. This cannot be undone.</Modal.Body><Modal.Footer><Button variant="light" onClick={() => setShowDeleteModal(false)} disabled={deleting}>Cancel</Button><Button variant="danger" onClick={deleteAccount} disabled={deleting}>{deleting ? 'Deleting...' : 'Delete account'}</Button></Modal.Footer></Modal>
  </section>
}

function OrdersTab() {
  const [orders, setOrders] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const loadOrders = useCallback(() => {
    setLoading(true)
    setError('')
    getOrders().then(setOrders).catch((requestError) => setError(requestError.message)).finally(() => setLoading(false))
  }, [])
  useEffect(() => { loadOrders() }, [loadOrders])
  if (loading) return <div className="skeleton account-skeleton" />
  if (error) return <div className="catalog-state"><Alert variant="warning"><strong>We couldn't load your orders.</strong><br />{error}</Alert><Button variant="primary" onClick={loadOrders}>Try again</Button></div>
  if (orders.length === 0) return <div className="empty-state"><div className="empty-state-icon"><i className="bi bi-box-seam" /></div><h2>No orders yet</h2><p>When you place an order, it will appear here.</p><Button as={Link} to="/" variant="primary">Browse products</Button></div>
  return <section className="account-panel orders-panel"><h2>Your orders</h2><div className="orders-list">{orders.map((order) => <Link className="order-list-item" to={`/orders/${order.id}`} key={order.id}><div><strong>Order #{order.id}</strong><span>{formatDate(order.createdAt)}</span></div><span className="order-status">{order.status}</span><strong>{currency.format(order.total)}</strong><i className="bi bi-chevron-right" /></Link>)}</div></section>
}

function FavoritesTab({ currentUser, favoriteIds, onFavoriteChange, onFavoritesRefresh }) {
  const [favorites, setFavorites] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const loadFavorites = useCallback(() => {
    setLoading(true)
    setError('')
    getFavorites().then((products) => { setFavorites(products); return onFavoritesRefresh() }).catch((requestError) => setError(requestError.message)).finally(() => setLoading(false))
  }, [onFavoritesRefresh])
  useEffect(() => { loadFavorites() }, [loadFavorites])
  function handleFavoriteChange(productId, isFavorite) { onFavoriteChange(productId, isFavorite); if (!isFavorite) setFavorites((products) => products.filter((product) => product.id !== productId)) }
  if (loading) return <div className="skeleton account-skeleton" />
  if (error) return <div className="catalog-state"><Alert variant="warning"><strong>We couldn't load your favorites.</strong><br />{error}</Alert><Button variant="primary" onClick={loadFavorites}>Try again</Button></div>
  if (favorites.length === 0) return <div className="empty-state"><div className="empty-state-icon"><i className="bi bi-heart" /></div><h2>No favorites yet</h2><p>Save products you love to find them quickly later.</p><Button as={Link} to="/" variant="primary">Browse products</Button></div>
  return <section className="favorite-list">{favorites.map((product) => <article className="favorite-list-item" key={product.id}><ProductVisual categoryName={product.categoryName} productName={product.name} className="product-visual-favorite" /><div><Link to={`/products/${product.id}`}><h2>{product.name}</h2></Link><p>{product.categoryName || 'Favorite product'}</p><strong>{currency.format(product.price)}</strong></div><FavoriteButton productId={product.id} isFavorite={favoriteIds.has(product.id)} currentUser={currentUser} onFavoriteChange={handleFavoriteChange} /></article>)}</section>
}

function profileForm(user) { return { first_name: user.first_name || '', last_name: user.last_name || '', email: user.email || '', phone: user.phone || '', address: user.address || '', password: '', confirmPassword: '' } }
export default AccountPage
