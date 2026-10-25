import { useCallback, useEffect, useRef, useState } from 'react'
import { Alert, Badge, Button, Container, Form, Modal, Nav, Spinner, Table } from 'react-bootstrap'
import { useSearchParams } from 'react-router-dom'
import { createAdminProduct, deactivateAdminProduct, deleteAdminUser, deleteProductImage, getAdminOrder, getAdminOrders, getAdminProducts, getAdminUsers, getCategories, restoreAdminProduct, updateAdminOrderStatus, updateAdminProduct, updateAdminUserRole, uploadProductImage } from '../api.js'
import ProductVisual from '../components/ProductVisual.jsx'
import { formatCurrency, formatDateTime as formatDate } from '../utils/formatters.js'
import { formatImageSize, processProductImage } from '../utils/imageProcessing.js'

const currency = { format: formatCurrency }
const emptyProduct = { name: '', description: '', price: '', stock: '', categoryId: '' }

function AdminDashboard({ currentUser }) {
  const [searchParams, setSearchParams] = useSearchParams()
  const tab = ['products', 'orders', 'users'].includes(searchParams.get('tab')) ? searchParams.get('tab') : 'products'
  return <Container className="admin-page"><div className="eyebrow">ADMIN</div><h1>Store management</h1><Nav variant="pills" className="account-tabs" activeKey={tab}><Nav.Item><Nav.Link eventKey="products" onClick={() => setSearchParams({ tab: 'products' })}>Products</Nav.Link></Nav.Item><Nav.Item><Nav.Link eventKey="orders" onClick={() => setSearchParams({ tab: 'orders' })}>Orders</Nav.Link></Nav.Item><Nav.Item><Nav.Link eventKey="users" onClick={() => setSearchParams({ tab: 'users' })}>Users</Nav.Link></Nav.Item></Nav>{tab === 'products' && <ProductsTab />}{tab === 'orders' && <OrdersTab />}{tab === 'users' && <UsersTab currentUsername={currentUser.username} />}</Container>
}

function ProductsTab() {
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState(null)
  const [confirm, setConfirm] = useState(null)

  const load = useCallback(() => {
    setLoading(true)
    setError('')
    Promise.all([getAdminProducts(), getCategories()])
      .then(([nextProducts, nextCategories]) => { setProducts(nextProducts); setCategories(nextCategories) })
      .catch((requestError) => setError(requestError.message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { load() }, [load])

  async function toggle(product) {
    try {
      if (product.active) await deactivateAdminProduct(product.id)
      else await restoreAdminProduct(product.id)
      setConfirm(null)
      load()
    } catch (requestError) {
      setError(requestError.message)
      setConfirm(null)
    }
  }

  if (loading) return <div className="skeleton admin-skeleton" />
  if (error) return <LoadError message={error} retry={load} />

  const categoryName = (id) => categories.find((category) => category.id === id)?.name || 'Uncategorized'

  return <>
    <section className="admin-panel">
      <div className="admin-panel-heading"><h2>Products</h2><Button onClick={() => setEditing({ ...emptyProduct })}><i className="bi bi-plus-lg" /> Add product</Button></div>
      {products.length === 0 ? <Empty text="No products yet." /> : <div className="table-scroll"><Table hover className="admin-table"><thead><tr><th>Product</th><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th>Status</th><th /></tr></thead><tbody>{products.map((product) => <tr key={product.id}>
        <td><ProductVisual productName={product.name} categoryName={categoryName(product.categoryId)} imageUrl={product.imageUrl} className="product-visual-admin" /></td>
        <td>{product.name}</td><td>{categoryName(product.categoryId)}</td><td>{currency.format(product.price)}</td><td>{product.stock}</td>
        <td><Badge bg={product.active ? 'success' : 'secondary'}>{product.active ? 'Active' : 'Inactive'}</Badge></td>
        <td className="admin-actions"><Button size="sm" variant="outline-primary" onClick={() => setEditing({ ...product, price: String(product.price), stock: String(product.stock) })}>Edit</Button><Button size="sm" variant={product.active ? 'outline-danger' : 'outline-success'} onClick={() => setConfirm({ product })}>{product.active ? 'Deactivate' : 'Restore'}</Button></td>
      </tr>)}</tbody></Table></div>}
    </section>
    {editing && <ProductModal product={editing} categories={categories} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); load() }} />}
    <ConfirmModal state={confirm} title={confirm?.product.active ? 'Deactivate product?' : 'Restore product?'} body={confirm?.product.active ? 'This product will disappear from the public catalog and can no longer be added to carts.' : 'This product will become available in the public catalog again.'} actionLabel={confirm?.product.active ? 'Deactivate' : 'Restore'} variant={confirm?.product.active ? 'danger' : 'success'} onClose={() => setConfirm(null)} onConfirm={() => toggle(confirm.product)} />
  </>
}

function ProductModal({ product, categories, onClose, onSaved }) {
  const [form, setForm] = useState(product)
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const [processingImage, setProcessingImage] = useState(false)
  const [selectedImage, setSelectedImage] = useState(null)
  const [previewUrl, setPreviewUrl] = useState(null)
  const [removeImage, setRemoveImage] = useState(false)
  const inputRef = useRef(null)
  const isEdit = Boolean(product.id)
  const selectedCategory = categories.find((category) => String(category.id) === String(form.categoryId))?.name

  useEffect(() => () => { if (previewUrl) URL.revokeObjectURL(previewUrl) }, [previewUrl])

  function update(event) {
    setForm((value) => ({ ...value, [event.target.name]: event.target.value }))
  }

  async function selectImage(file) {
    if (!file) return
    setError('')
    setProcessingImage(true)
    try {
      const processed = await processProductImage(file)
      setSelectedImage(processed)
      setRemoveImage(false)
      setPreviewUrl(URL.createObjectURL(processed))
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setProcessingImage(false)
    }
  }

  function chooseImage(event) {
    selectImage(event.target.files?.[0])
  }

  function dropImage(event) {
    event.preventDefault()
    selectImage(event.dataTransfer.files?.[0])
  }

  function markImageForRemoval() {
    setSelectedImage(null)
    setPreviewUrl(null)
    setRemoveImage(true)
  }

  async function save(event) {
    event.preventDefault()
    setError('')
    if (!form.name.trim()) return setError('Product name is required.')
    if (!(Number(form.price) > 0)) return setError('Price must be greater than 0.')
    if (!Number.isInteger(Number(form.stock)) || Number(form.stock) < 0) return setError('Stock must be a whole number of at least 0.')
    if (!form.categoryId) return setError('Please choose a category.')

    setSaving(true)
    try {
      const payload = { name: form.name.trim(), description: form.description.trim(), price: Number(form.price), stock: Number(form.stock), categoryId: Number(form.categoryId), imageUrl: form.imageUrl || '' }
      const savedProduct = isEdit ? await updateAdminProduct(product.id, payload) : await createAdminProduct(payload)
      if (selectedImage) await uploadProductImage(savedProduct.id, selectedImage)
      else if (isEdit && removeImage) await deleteProductImage(savedProduct.id)
      onSaved()
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSaving(false)
    }
  }

  const imageUrl = removeImage ? null : form.imageUrl

  return <Modal show onHide={onClose} centered><Form onSubmit={save}>
    <Modal.Header closeButton><Modal.Title>{isEdit ? 'Edit product' : 'Add product'}</Modal.Title></Modal.Header>
    <Modal.Body>
      {error && <Alert variant="danger">{error}</Alert>}
      <Form.Group className="mb-3"><Form.Label>Name</Form.Label><Form.Control name="name" value={form.name} onChange={update} /></Form.Group>
      <Form.Group className="mb-3"><Form.Label>Description</Form.Label><Form.Control as="textarea" rows={3} name="description" value={form.description || ''} onChange={update} /></Form.Group>
      <div className="profile-form-grid"><Form.Group><Form.Label>Price (USD)</Form.Label><Form.Control name="price" inputMode="decimal" value={form.price} onChange={update} /></Form.Group><Form.Group><Form.Label>Stock</Form.Label><Form.Control name="stock" inputMode="numeric" value={form.stock} onChange={update} /></Form.Group></div>
      <Form.Group className="mt-3"><Form.Label>Category</Form.Label><Form.Select name="categoryId" value={form.categoryId} onChange={update}><option value="">Choose a category</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</Form.Select></Form.Group>
      <section className="product-image-upload">
        <Form.Label>Product image</Form.Label>
        <div className="product-image-preview"><ProductVisual productName={form.name || 'Product preview'} categoryName={selectedCategory} imageUrl={imageUrl} previewUrl={previewUrl} /></div>
        <div className="product-image-drop-zone" role="button" tabIndex={0} onClick={() => inputRef.current?.click()} onKeyDown={(event) => { if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); inputRef.current?.click() } }} onDragOver={(event) => event.preventDefault()} onDrop={dropImage}>
          <i className="bi bi-upload" aria-hidden="true" /><strong>Choose image or drag it here</strong><span>JPEG, PNG, or WebP. Images are resized before upload.</span>
          <Form.Control ref={inputRef} type="file" accept="image/jpeg,image/png,image/webp" className="visually-hidden" onClick={(event) => { event.currentTarget.value = '' }} onChange={chooseImage} />
        </div>
        {processingImage && <p className="product-image-meta"><Spinner animation="border" size="sm" /> Processing image...</p>}
        {selectedImage && <p className="product-image-meta">Processed image: {formatImageSize(selectedImage.size)}</p>}
        {!selectedImage && removeImage && <p className="product-image-meta">The image will be removed when you save.</p>}
        {(selectedImage || (isEdit && form.imageUrl && !removeImage)) && <Button type="button" variant="outline-danger" size="sm" onClick={markImageForRemoval}>Remove image</Button>}
      </section>
    </Modal.Body>
    <Modal.Footer><Button variant="light" onClick={onClose}>Cancel</Button><Button type="submit" disabled={saving || processingImage}>{saving ? 'Saving...' : 'Save product'}</Button></Modal.Footer>
  </Form></Modal>
}

function OrdersTab() {
  const [orders, setOrders] = useState([]); const [filter, setFilter] = useState('ALL'); const [loading, setLoading] = useState(true); const [error, setError] = useState(''); const [detail, setDetail] = useState(null); const [confirm, setConfirm] = useState(null)
  const load = useCallback(() => { setLoading(true); setError(''); getAdminOrders().then(setOrders).catch((e) => setError(e.message)).finally(() => setLoading(false)) }, [])
  useEffect(() => { load() }, [load]); const visible = filter === 'ALL' ? orders : orders.filter((order) => order.status === filter)
  async function view(id) { try { setDetail(await getAdminOrder(id)) } catch (e) { setError(e.message) } }
  async function changeStatus() { try { await updateAdminOrderStatus(confirm.order.id, confirm.status); setConfirm(null); setDetail(null); load() } catch (e) { setError(e.message); setConfirm(null) } }
  if (loading) return <div className="skeleton admin-skeleton" />; if (error) return <LoadError message={error} retry={load} />
  return <section className="admin-panel"><div className="admin-panel-heading"><h2>Orders</h2><Form.Select className="admin-filter" value={filter} onChange={(event) => setFilter(event.target.value)}><option value="ALL">All statuses</option>{['PLACED', 'SHIPPED', 'DELIVERED', 'CANCELLED'].map((status) => <option key={status}>{status}</option>)}</Form.Select></div>{visible.length === 0 ? <Empty text="No orders match this filter." /> : <div className="table-scroll"><Table hover className="admin-table"><thead><tr><th>Order</th><th>Username</th><th>Date</th><th>Total</th><th>Status</th><th /></tr></thead><tbody>{visible.map((order) => <tr key={order.id}><td>#{order.id}</td><td>{order.username}</td><td>{formatDate(order.createdAt)}</td><td>{currency.format(order.total)}</td><td><StatusBadge status={order.status} /></td><td><Button size="sm" variant="outline-primary" onClick={() => view(order.id)}>View details</Button></td></tr>)}</tbody></Table></div>}<OrderModal order={detail} onClose={() => setDetail(null)} onAction={(order, status) => setConfirm({ order, status })} /><ConfirmModal state={confirm} title={confirm?.status === 'CANCELLED' ? 'Cancel order?' : `Mark order ${confirm?.status.toLowerCase()}?`} body={confirm?.status === 'CANCELLED' ? 'Cancelling restores all ordered stock.' : 'This order status will be updated.'} actionLabel={confirm?.status === 'CANCELLED' ? 'Cancel order' : 'Confirm'} variant={confirm?.status === 'CANCELLED' ? 'danger' : 'primary'} onClose={() => setConfirm(null)} onConfirm={changeStatus} /></section>
}

function OrderModal({ order, onClose, onAction }) { if (!order) return null; const actions = order.status === 'PLACED' ? [['SHIPPED', 'Mark shipped'], ['CANCELLED', 'Cancel order']] : order.status === 'SHIPPED' ? [['DELIVERED', 'Mark delivered']] : []; return <Modal show onHide={onClose} size="lg"><Modal.Header closeButton><Modal.Title>Order #{order.id}</Modal.Title></Modal.Header><Modal.Body><p className="muted-copy">{order.username} · {formatDate(order.createdAt)} · <StatusBadge status={order.status} /></p>{order.items.map((item) => <div className="confirmation-item" key={item.productId}><ProductVisual productName={item.productName} className="product-visual-confirmation" /><div><strong>{item.productName}</strong><span>{item.quantity} × {currency.format(item.unitPrice)}</span></div><strong>{currency.format(item.lineTotal)}</strong></div>)}<div className="shipping-confirmation"><span>Shipping to</span><strong>{order.shippingAddress}</strong></div></Modal.Body><Modal.Footer>{actions.map(([status, label]) => <Button key={status} variant={status === 'CANCELLED' ? 'outline-danger' : 'primary'} onClick={() => onAction(order, status)}>{label}</Button>)}<Button variant="light" onClick={onClose}>Close</Button></Modal.Footer></Modal> }

function UsersTab({ currentUsername }) {
  const [users, setUsers] = useState([]); const [loading, setLoading] = useState(true); const [error, setError] = useState(''); const [confirm, setConfirm] = useState(null)
  const load = useCallback(() => { setLoading(true); setError(''); getAdminUsers().then(setUsers).catch((e) => setError(e.message)).finally(() => setLoading(false)) }, [])
  useEffect(() => { load() }, [load]);
  async function act() { try { if (confirm.type === 'delete') await deleteAdminUser(confirm.user.username); else await updateAdminUserRole(confirm.user.username, confirm.role); setConfirm(null); load() } catch (e) { setError(e.message); setConfirm(null) } }
  if (loading) return <div className="skeleton admin-skeleton" />; if (error) return <LoadError message={error} retry={load} />
  return <section className="admin-panel"><div className="admin-panel-heading"><h2>Users</h2></div><div className="table-scroll"><Table hover className="admin-table"><thead><tr><th>Username</th><th>Name</th><th>Email</th><th>Role</th><th /></tr></thead><tbody>{users.map((user) => <tr key={user.username}><td>{user.username}</td><td>{user.first_name} {user.last_name}</td><td>{user.email}</td><td><Badge bg={user.role === 'ADMIN' ? 'primary' : 'secondary'}>{user.role}</Badge></td><td className="admin-actions">{user.username !== currentUsername && <><Button size="sm" variant="outline-primary" onClick={() => setConfirm({ type: 'role', user, role: user.role === 'ADMIN' ? 'USER' : 'ADMIN' })}>{user.role === 'ADMIN' ? 'Make user' : 'Make admin'}</Button><Button size="sm" variant="outline-danger" onClick={() => setConfirm({ type: 'delete', user })}>Delete</Button></>}</td></tr>)}</tbody></Table></div><ConfirmModal state={confirm} title={confirm?.type === 'delete' ? 'Delete user?' : 'Change user role?'} body={confirm?.type === 'delete' ? `Delete ${confirm.user.username}'s account?` : `Make ${confirm?.user.username} a ${confirm?.role}?`} actionLabel={confirm?.type === 'delete' ? 'Delete user' : 'Confirm'} variant={confirm?.type === 'delete' ? 'danger' : 'primary'} onClose={() => setConfirm(null)} onConfirm={act} /></section>
}

function ConfirmModal({ state, title, body, actionLabel, variant, onClose, onConfirm }) { return <Modal show={Boolean(state)} onHide={onClose} centered><Modal.Header closeButton><Modal.Title>{title}</Modal.Title></Modal.Header><Modal.Body>{body}</Modal.Body><Modal.Footer><Button variant="light" onClick={onClose}>Cancel</Button><Button variant={variant} onClick={onConfirm}>{actionLabel}</Button></Modal.Footer></Modal> }
function StatusBadge({ status }) { return <Badge bg={status === 'CANCELLED' ? 'secondary' : status === 'DELIVERED' ? 'success' : 'primary'}>{status}</Badge> }
function Empty({ text }) { return <div className="empty-state"><p>{text}</p></div> }
function LoadError({ message, retry }) { return <div className="catalog-state"><Alert variant="warning">{message}</Alert><Button onClick={retry}>Try again</Button></div> }
export default AdminDashboard
