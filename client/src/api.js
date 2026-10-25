const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:9000').replace(/\/$/, '')
const RETRY_DELAYS = [1000, 3000, 7000, 15000, 30000]
const SERVER_UNAVAILABLE_MESSAGE = 'The server is not responding right now. Please try again in a minute.'

export class ApiError extends Error {
  constructor(message, status = 0) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

async function request(path, { requiresAuth = false, verifyTokenOnForbidden = true, ...options } = {}) {
  const headers = { ...(options.body && !(options.body instanceof FormData) ? { 'Content-Type': 'application/json' } : {}), ...(options.headers || {}) }
  const token = localStorage.getItem('token')
  if (token) headers.Authorization = `Bearer ${token}`

  const response = await fetchWithRetry(`${API_BASE_URL}${path}`, { ...options, headers })

  const contentType = response.headers.get('content-type') || ''
  const responseBody = await response.text()
  let payload = responseBody
  if (contentType.includes('application/json') && responseBody) {
    try {
      payload = JSON.parse(responseBody)
    } catch {
      payload = responseBody
    }
  }
  if (!response.ok) {
    if (requiresAuth && response.status === 401) {
      localStorage.removeItem('token')
      window.dispatchEvent(new Event('auth-expired'))
    }
    if (requiresAuth && response.status === 403) {
      let tokenIsValid = false
      if (verifyTokenOnForbidden && path !== '/users') {
        try {
          await request('/users', { requiresAuth: true, verifyTokenOnForbidden: false })
          tokenIsValid = true
        } catch {
          tokenIsValid = false
        }
      }
      if (!tokenIsValid) {
        localStorage.removeItem('token')
        window.dispatchEvent(new Event('auth-expired'))
      }
    }
    const message = getErrorMessage(payload, response.status)
    throw new ApiError(message || `Request failed (${response.status}).`, response.status)
  }
  return payload
}

async function fetchWithRetry(url, options) {
  let retrying = false
  for (let attempt = 0; attempt <= RETRY_DELAYS.length; attempt += 1) {
    try {
      const response = await fetch(url, options)
      if (retrying) emitServerWaking(false)
      return response
    } catch {
      const delay = RETRY_DELAYS[attempt]
      if (delay === undefined) {
        emitServerWaking(false)
        throw new ApiError(SERVER_UNAVAILABLE_MESSAGE)
      }
      if (!retrying) {
        retrying = true
        emitServerWaking(true)
      }
      await new Promise((resolve) => setTimeout(resolve, delay))
    }
  }
  throw new ApiError(SERVER_UNAVAILABLE_MESSAGE)
}

function emitServerWaking(waking) {
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent('server-waking', { detail: { waking } }))
  }
}

function getErrorMessage(payload, status) {
  if (typeof payload === 'string') return payload.trim()
  if (payload && typeof payload === 'object') {
    return String(payload.message || payload.error || '').trim()
  }
  return `Request failed (${status}).`
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

export function getApiImageUrl(imageUrl) {
  return imageUrl ? `${API_BASE_URL}${imageUrl}` : null
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
export function uploadProductImage(id, blob) {
  const formData = new FormData()
  formData.append('file', blob, blob.type === 'image/jpeg' ? 'product.jpg' : 'product.webp')
  return request(`/admin/products/${id}/image`, { method: 'PUT', body: formData, requiresAuth: true })
}
export function deleteProductImage(id) { return request(`/admin/products/${id}/image`, { method: 'DELETE', requiresAuth: true }) }
export function deactivateAdminProduct(id) { return request(`/admin/products/${id}`, { method: 'DELETE', requiresAuth: true }) }
export function restoreAdminProduct(id) { return request(`/admin/products/${id}/restore`, { method: 'PUT', requiresAuth: true }) }
export function getAdminOrders() { return request('/admin/orders', { requiresAuth: true }) }
export function getAdminOrder(id) { return request(`/admin/orders/${id}`, { requiresAuth: true }) }
export function updateAdminOrderStatus(id, status) { return request(`/admin/orders/${id}/status`, { method: 'PUT', body: JSON.stringify({ status }), requiresAuth: true }) }
export function getAdminUsers() { return request('/admin/all-users', { requiresAuth: true }) }
export function updateAdminUserRole(username, role) { return request(`/admin/users/${username}/role`, { method: 'PUT', body: JSON.stringify({ role }), requiresAuth: true }) }
export function deleteAdminUser(username) { return request(`/admin/delete-user/${username}`, { method: 'DELETE', requiresAuth: true }) }
