const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:9000').replace(/\/$/, '')

export class ApiError extends Error {
  constructor(message, status = 0) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

async function request(path, options = {}) {
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
  return request('/users')
}
