import { useCallback, useEffect, useState } from 'react'
import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { getCart, getCurrentUser, getFavorites } from './api.js'
import AccountPage from './pages/AccountPage.jsx'
import AdminDashboard from './pages/AdminDashboard.jsx'
import CartPage from './pages/CartPage.jsx'
import CheckoutPage from './pages/CheckoutPage.jsx'
import OrderConfirmationPage from './pages/OrderConfirmationPage.jsx'
import OrderDetailsPage from './pages/OrderDetailsPage.jsx'
import Navbar from './components/Navbar.jsx'
import Home from './pages/Home.jsx'
import ProductPage from './pages/ProductPage.jsx'
import SignIn from './pages/SignIn.jsx'
import SignUp from './pages/SignUp.jsx'

function App() {
  const [currentUser, setCurrentUser] = useState(null)
  const [loadingUser, setLoadingUser] = useState(true)
  const [cartCount, setCartCount] = useState(0)
  const [favoriteIds, setFavoriteIds] = useState(new Set())
  const navigate = useNavigate()
  const location = useLocation()

  const refreshCart = useCallback(async (cartResponse) => {
    if (!cartResponse && !localStorage.getItem('token')) {
      setCartCount(0)
      return null
    }
    const cart = cartResponse || await getCart()
    setCartCount(cart.items.reduce((total, item) => total + item.quantity, 0))
    return cart
  }, [])

  const refreshFavorites = useCallback(async () => {
    if (!localStorage.getItem('token')) {
      setFavoriteIds(new Set())
      return []
    }
    const favorites = await getFavorites()
    setFavoriteIds(new Set(favorites.map((product) => product.id)))
    return favorites
  }, [])

  const handleFavoriteChange = useCallback((productId, isFavorite) => {
    setFavoriteIds((currentIds) => {
      const nextIds = new Set(currentIds)
      if (isFavorite) nextIds.add(productId)
      else nextIds.delete(productId)
      return nextIds
    })
  }, [])

  useEffect(() => {
    if (!localStorage.getItem('token')) {
      setLoadingUser(false)
      return
    }

    getCurrentUser()
      .then(setCurrentUser)
      .catch(() => {
        localStorage.removeItem('token')
        setCurrentUser(null)
      })
      .finally(() => setLoadingUser(false))
  }, [])

  useEffect(() => {
    if (currentUser) {
      refreshCart().catch(() => {})
      refreshFavorites().catch(() => {})
    } else {
      setCartCount(0)
      setFavoriteIds(new Set())
    }
  }, [currentUser, refreshCart, refreshFavorites])

  useEffect(() => {
    function handleAuthExpired() {
      setCurrentUser(null)
      setCartCount(0)
      setFavoriteIds(new Set())
      navigate('/sign-in', { replace: true, state: { from: location } })
    }
    window.addEventListener('auth-expired', handleAuthExpired)
    return () => window.removeEventListener('auth-expired', handleAuthExpired)
  }, [location, navigate])

  function handleLogout() {
    localStorage.removeItem('token')
    setCurrentUser(null)
    setCartCount(0)
    setFavoriteIds(new Set())
  }

  if (loadingUser) {
    return <div className="app-loading">Loading your store...</div>
  }

  return (
    <>
      <Navbar currentUser={currentUser} cartCount={cartCount} onLogout={handleLogout} />
      <main>
        <Routes>
          <Route path="/" element={<Home currentUser={currentUser} favoriteIds={favoriteIds} onFavoriteChange={handleFavoriteChange} />} />
          <Route path="/products/:id" element={<ProductPage currentUser={currentUser} onCartChange={refreshCart} favoriteIds={favoriteIds} onFavoriteChange={handleFavoriteChange} />} />
          <Route path="/cart" element={<ProtectedRoute currentUser={currentUser}><CartPage onCartChange={refreshCart} /></ProtectedRoute>} />
          <Route path="/checkout" element={<ProtectedRoute currentUser={currentUser}><CheckoutPage currentUser={currentUser} onCartChange={refreshCart} /></ProtectedRoute>} />
          <Route path="/order-confirmation/:id" element={<ProtectedRoute currentUser={currentUser}><OrderConfirmationPage /></ProtectedRoute>} />
          <Route path="/orders/:id" element={<ProtectedRoute currentUser={currentUser}><OrderDetailsPage /></ProtectedRoute>} />
          <Route path="/account" element={<ProtectedRoute currentUser={currentUser}><AccountPage currentUser={currentUser} favoriteIds={favoriteIds} onFavoriteChange={handleFavoriteChange} onFavoritesRefresh={refreshFavorites} onUserUpdate={setCurrentUser} onLogout={handleLogout} /></ProtectedRoute>} />
          <Route path="/admin" element={<AdminRoute currentUser={currentUser}><AdminDashboard currentUser={currentUser} /></AdminRoute>} />
          <Route path="/sign-in" element={<SignIn currentUser={currentUser} setCurrentUser={setCurrentUser} />} />
          <Route path="/sign-up" element={<SignUp currentUser={currentUser} />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </>
  )
}

function ProtectedRoute({ currentUser, children }) {
  const location = useLocation()
  if (!currentUser) return <Navigate to="/sign-in" replace state={{ from: location }} />
  return children
}

function AdminRoute({ currentUser, children }) {
  if (!currentUser || currentUser.role !== 'ADMIN') return <Navigate to="/" replace />
  return children
}

export default App
