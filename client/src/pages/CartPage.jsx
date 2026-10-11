import { useCallback, useEffect, useState } from 'react'
import { Alert, Button, Container, Spinner } from 'react-bootstrap'
import { Link, useNavigate } from 'react-router-dom'
import { clearCart, getCart, removeCartItem, updateCartItem } from '../api.js'
import ProductVisual from '../components/ProductVisual.jsx'

const currency = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' })

function CartPage({ onCartChange }) {
  const [cart, setCart] = useState(null)
  const [error, setError] = useState('')
  const [updatingId, setUpdatingId] = useState(null)
  const navigate = useNavigate()

  const loadCart = useCallback(async () => {
    setError('')
    try {
      const result = await getCart()
      setCart(result)
      await onCartChange(result)
    } catch (requestError) {
      setError(requestError.message)
    }
  }, [onCartChange])

  useEffect(() => { loadCart() }, [loadCart])

  async function changeQuantity(item, quantity) {
    setUpdatingId(item.productId)
    setError('')
    try {
      await updateCartItem(item.productId, quantity)
      await loadCart()
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setUpdatingId(null)
    }
  }

  async function removeItem(productId) {
    setUpdatingId(productId)
    setError('')
    try {
      await removeCartItem(productId)
      await loadCart()
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setUpdatingId(null)
    }
  }

  async function handleClearCart() {
    setUpdatingId('clear')
    setError('')
    try {
      await clearCart()
      const emptyCart = { items: [], total: 0 }
      setCart(emptyCart)
      await onCartChange(emptyCart)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setUpdatingId(null)
    }
  }

  if (!cart) return <Container className="cart-page"><div className="skeleton cart-skeleton" /></Container>

  if (cart.items.length === 0) {
    return <Container className="cart-page"><EmptyCart /></Container>
  }

  return (
    <Container className="cart-page">
      <div className="page-title-row"><div><div className="eyebrow">YOUR CART</div><h1>Ready when you are.</h1></div><Button variant="outline-primary" onClick={handleClearCart} disabled={updatingId === 'clear'}>{updatingId === 'clear' ? 'Clearing...' : 'Clear cart'}</Button></div>
      {error && <Alert variant="danger">{error}</Alert>}
      <div className="cart-layout">
        <section className="cart-items" aria-label="Cart items">
          {cart.items.map((item) => <article className="cart-item" key={item.productId}><ProductVisual productName={item.productName} className="product-visual-cart" /><div className="cart-item-info"><h2>{item.productName}</h2><p>{currency.format(item.price)} each</p><button type="button" className="text-action" onClick={() => removeItem(item.productId)} disabled={updatingId === item.productId}>Remove</button></div><div className="cart-quantity"><span>Quantity</span><div><Button variant="outline-primary" size="sm" onClick={() => changeQuantity(item, item.quantity - 1)} disabled={item.quantity <= 1 || updatingId === item.productId}>−</Button><strong>{updatingId === item.productId ? <Spinner size="sm" /> : item.quantity}</strong><Button variant="outline-primary" size="sm" onClick={() => changeQuantity(item, item.quantity + 1)} disabled={updatingId === item.productId}>+</Button></div></div><strong className="cart-line-total">{currency.format(item.lineTotal)}</strong></article>)}
        </section>
        <aside className="cart-summary"><h2>Order summary</h2><div><span>Cart total</span><strong>{currency.format(cart.total)}</strong></div><Button variant="primary" size="lg" className="w-100" onClick={() => navigate('/checkout')}>Proceed to checkout</Button><Link to="/" className="continue-shopping">Continue shopping</Link></aside>
      </div>
    </Container>
  )
}

function EmptyCart() {
  return <div className="empty-state"><div className="empty-state-icon"><i className="bi bi-cart3" /></div><h2>Your cart is empty</h2><p>Add an essential from the catalog when you are ready.</p><Button as={Link} to="/" variant="primary">Go to the store</Button></div>
}

export default CartPage
