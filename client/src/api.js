const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:9000').replace(/\/$/, '')

export class ApiError extends Error {
  constructor(message, status = 0) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

async function request(path, { requiresAuth = false, ...options } = {}) {
  const headers = { ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...(options.headers || {}) }
  const token = localStorage.getItem('token')
  if (token) headers.Authorization = `Bearer ${token}`

  let response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers })
  } catch {
    throw new ApiError('The server is unreachable. Please check that the backend is running and try again.')
  }

  const contentType = response.headers.get('content-type') || ''
  const payload = contentType.includes('application/json') ? await response.json() : await response.text()
  if (!response.ok) {
    if (requiresAuth && (response.status === 401 || response.status === 403)) {
      localStorage.removeItem('token')
      window.dispatchEvent(new Event('auth-expired'))
    }
    const message = typeof payload === 'string' && payload ? payload : payload?.message
    throw new ApiError(message || `Request failed (${response.status}).`, response.status)
  }
  return payload
}

export function registerUser(user) {
  return request('/users/register', { method: 'POST', body: JSON.stringify(user) })
}

export function authenticate(credentials) {
  return request('/authenticate', { method: 'POST', body: JSON.stringify(credentials) })
}

export function getCurrentUser() {
  return request('/users', { requiresAuth: true })
}

export function updateCurrentUser(user) {
  return request('/users', { method: 'PUT', body: JSON.stringify(user), requiresAuth: true })
}

export function deleteCurrentUser() {
  return request('/users', { method: 'DELETE', requiresAuth: true })
}

export function getCategories() {
  return request('/categories')
}

export function getProducts({ categoryId, search } = {}) {
  const params = new URLSearchParams()
  if (categoryId) params.set('categoryId', categoryId)
  if (search) params.set('search', search)
  const query = params.toString()
  return request(`/products${query ? `?${query}` : ''}`)
}

export function getProduct(id) {
  return request(`/products/${id}`)
}

export function getCart() {
  return request('/cart', { requiresAuth: true })
}

export function addToCart(productId, quantity) {
  return request('/cart', { method: 'POST', body: JSON.stringify({ productId, quantity }), requiresAuth: true })
}

export function updateCartItem(productId, quantity) {
  return request(`/cart/${productId}`, { method: 'PUT', body: JSON.stringify({ quantity }), requiresAuth: true })
}

export function removeCartItem(productId) {
  return request(`/cart/${productId}`, { method: 'DELETE', requiresAuth: true })
}

export function clearCart() {
  return request('/cart', { method: 'DELETE', requiresAuth: true })
}

export function getFavorites() {
  return request('/favorites', { requiresAuth: true })
}

export function addFavorite(productId) {
  return request(`/favorites/${productId}`, { method: 'POST', requiresAuth: true })
}

export function removeFavorite(productId) {
  return request(`/favorites/${productId}`, { method: 'DELETE', requiresAuth: true })
}

export function createOrder(shippingAddress) {
  return request('/orders', { method: 'POST', body: JSON.stringify({ shippingAddress }), requiresAuth: true })
}

export function getOrder(id) {
  return request(`/orders/${id}`, { requiresAuth: true })
}

export function getOrders() {
  return request('/orders', { requiresAuth: true })
}

export function getAdminProducts() { return request('/admin/products', { requiresAuth: true }) }
export function createAdminProduct(product) { return request('/admin/products', { method: 'POST', body: JSON.stringify(product), requiresAuth: true }) }
export function updateAdminProduct(id, product) { return request(`/admin/products/${id}`, { method: 'PUT', body: JSON.stringify(product), requiresAuth: true }) }
export function deactivateAdminProduct(id) { return request(`/admin/products/${id}`, { method: 'DELETE', requiresAuth: true }) }
export function restoreAdminProduct(id) { return request(`/admin/products/${id}/restore`, { method: 'PUT', requiresAuth: true }) }
export function getAdminOrders() { return request('/admin/orders', { requiresAuth: true }) }
export function getAdminOrder(id) { return request(`/admin/orders/${id}`, { requiresAuth: true }) }
export function updateAdminOrderStatus(id, status) { return request(`/admin/orders/${id}/status`, { method: 'PUT', body: JSON.stringify({ status }), requiresAuth: true }) }
export function getAdminUsers() { return request('/admin/all-users', { requiresAuth: true }) }
export function updateAdminUserRole(username, role) { return request(`/admin/users/${username}/role`, { method: 'PUT', body: JSON.stringify({ role }), requiresAuth: true }) }
export function deleteAdminUser(username) { return request(`/admin/delete-user/${username}`, { method: 'DELETE', requiresAuth: true }) }
