import { useCallback, useEffect, useState } from 'react'
import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { getCart, getCurrentUser } from './api.js'
import CartPage from './pages/CartPage.jsx'
import CheckoutPage from './pages/CheckoutPage.jsx'
import OrderConfirmationPage from './pages/OrderConfirmationPage.jsx'
import Navbar from './components/Navbar.jsx'
import Home from './pages/Home.jsx'
import ProductPage from './pages/ProductPage.jsx'
import SignIn from './pages/SignIn.jsx'
import SignUp from './pages/SignUp.jsx'

function App() {
  const [currentUser, setCurrentUser] = useState(null)
  const [loadingUser, setLoadingUser] = useState(true)
  const [cartCount, setCartCount] = useState(0)
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
    if (currentUser) refreshCart().catch(() => {})
    else setCartCount(0)
  }, [currentUser, refreshCart])

  useEffect(() => {
    function handleAuthExpired() {
      setCurrentUser(null)
      setCartCount(0)
      navigate('/sign-in', { replace: true, state: { from: location } })
    }
    window.addEventListener('auth-expired', handleAuthExpired)
    return () => window.removeEventListener('auth-expired', handleAuthExpired)
  }, [location, navigate])

  function handleLogout() {
    localStorage.removeItem('token')
    setCurrentUser(null)
    setCartCount(0)
  }

  if (loadingUser) {
    return <div className="app-loading">Loading your store...</div>
  }

  return (
    <>
      <Navbar currentUser={currentUser} cartCount={cartCount} onLogout={handleLogout} />
      <main>
        <Routes>
          <Route path="/" element={<Home currentUser={currentUser} />} />
          <Route path="/products/:id" element={<ProductPage currentUser={currentUser} onCartChange={refreshCart} />} />
          <Route path="/cart" element={<ProtectedRoute currentUser={currentUser}><CartPage onCartChange={refreshCart} /></ProtectedRoute>} />
          <Route path="/checkout" element={<ProtectedRoute currentUser={currentUser}><CheckoutPage currentUser={currentUser} onCartChange={refreshCart} /></ProtectedRoute>} />
          <Route path="/order-confirmation/:id" element={<ProtectedRoute currentUser={currentUser}><OrderConfirmationPage /></ProtectedRoute>} />
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

export default App
