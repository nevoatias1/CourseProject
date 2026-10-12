import { useCallback, useEffect, useState } from 'react'
import { Alert, Button, Container, Spinner } from 'react-bootstrap'
import { Link, useParams } from 'react-router-dom'
import { getOrder } from '../api.js'
import ProductVisual from '../components/ProductVisual.jsx'
import { formatCurrency } from '../utils/formatters.js'

const currency = { format: formatCurrency }

function OrderConfirmationPage() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)
  const [error, setError] = useState('')

  const loadOrder = useCallback(() => {
    setError('')
    getOrder(id).then(setOrder).catch((requestError) => setError(requestError.message))
  }, [id])

  useEffect(() => { loadOrder() }, [loadOrder])

  if (error) return <Container className="cart-page"><Alert variant="danger">{error}</Alert><Button onClick={loadOrder}>Try again</Button></Container>
  if (!order) return <Container className="cart-page"><div className="confirmation-loading"><Spinner /> Loading your confirmation...</div></Container>

  return <Container className="confirmation-page"><section className="confirmation-header"><div className="confirmation-icon"><i className="bi bi-check-lg" /></div><div className="eyebrow">ORDER CONFIRMED</div><h1>Thanks for your order.</h1><p>Your demo payment was accepted. Order <strong>#{order.id}</strong> is marked {order.status.toLowerCase()}.</p></section><section className="confirmation-card"><div className="confirmation-card-heading"><h2>Order #{order.id}</h2><strong>{currency.format(order.total)}</strong></div>{order.items.map((item) => <div className="confirmation-item" key={item.productId}><ProductVisual productName={item.productName} className="product-visual-confirmation" /><div><strong>{item.productName}</strong><span>{item.quantity} × {currency.format(item.unitPrice)}</span></div><strong>{currency.format(item.lineTotal)}</strong></div>)}<div className="shipping-confirmation"><span>Shipping to</span><strong>{order.shippingAddress}</strong></div></section><Button as={Link} to="/" variant="primary">Continue shopping</Button></Container>
}

export default OrderConfirmationPage
