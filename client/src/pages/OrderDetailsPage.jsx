import { useCallback, useEffect, useState } from 'react'
import { Alert, Button, Container, Spinner } from 'react-bootstrap'
import { Link, useParams } from 'react-router-dom'
import { getOrder } from '../api.js'
import ProductVisual from '../components/ProductVisual.jsx'
import { formatCurrency, formatDateTime as formatDate } from '../utils/formatters.js'

const currency = { format: formatCurrency }

function OrderDetailsPage() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)
  const [error, setError] = useState('')
  const loadOrder = useCallback(() => { setError(''); getOrder(id).then(setOrder).catch((requestError) => setError(requestError.message)) }, [id])
  useEffect(() => { loadOrder() }, [loadOrder])
  if (error) return <Container className="order-details-page"><Alert variant="danger">{error}</Alert><Button onClick={loadOrder}>Try again</Button></Container>
  if (!order) return <Container className="order-details-page"><div className="confirmation-loading"><Spinner /> Loading your order...</div></Container>
  return <Container className="order-details-page"><Link to="/account?tab=orders" className="back-link">← Back to orders</Link><div className="eyebrow">ORDER DETAILS</div><div className="order-details-heading"><div><h1>Order #{order.id}</h1><span>{formatDate(order.createdAt)} · {order.status}</span></div><strong>{currency.format(order.total)}</strong></div><section className="confirmation-card">{order.items.map((item) => <div className="confirmation-item" key={item.productId}><ProductVisual productName={item.productName} imageUrl={item.imageUrl} className="product-visual-confirmation" /><div><strong>{item.productName}</strong><span>{item.quantity} × {currency.format(item.unitPrice)}</span></div><strong>{currency.format(item.lineTotal)}</strong></div>)}<div className="shipping-confirmation"><span>Shipping to</span><strong>{order.shippingAddress}</strong></div></section></Container>
}

export default OrderDetailsPage
