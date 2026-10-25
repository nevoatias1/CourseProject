import { useCallback, useEffect, useState } from 'react'
import { Alert, Button, Col, Container, Form, Row } from 'react-bootstrap'
import { Link, useNavigate } from 'react-router-dom'
import { createOrder, getCart } from '../api.js'
import ProductVisual from '../components/ProductVisual.jsx'
import { formatCurrency } from '../utils/formatters.js'

const initialPayment = { cardHolder: '', cardNumber: '', expiry: '', cvv: '' }
const currency = { format: formatCurrency }

function CheckoutPage({ currentUser, onCartChange }) {
  const [cart, setCart] = useState(null)
  const [shippingOption, setShippingOption] = useState('saved')
  const [differentAddress, setDifferentAddress] = useState('')
  const [payment, setPayment] = useState(initialPayment)
  const [error, setError] = useState('')
  const [paying, setPaying] = useState(false)
  const navigate = useNavigate()

  const loadCart = useCallback(async () => {
    try {
      const result = await getCart()
      setCart(result)
      await onCartChange(result)
    } catch (requestError) {
      setError(requestError.message)
    }
  }, [onCartChange])

  useEffect(() => { loadCart() }, [loadCart])

  function updatePayment(event) {
    let value = event.target.value
    if (event.target.name === 'cardNumber') value = value.replace(/\D/g, '').slice(0, 16).replace(/(\d{4})(?=\d)/g, '$1 ')
    if (event.target.name === 'expiry') value = value.replace(/\D/g, '').slice(0, 4).replace(/(\d{2})(\d)/, '$1/$2')
    if (event.target.name === 'cvv') value = value.replace(/\D/g, '').slice(0, 3)
    setPayment({ ...payment, [event.target.name]: value })
  }

  function selectedShippingAddress() {
    return shippingOption === 'saved' ? currentUser.address || '' : differentAddress
  }

  function paymentError() {
    if (!selectedShippingAddress().trim()) return shippingOption === 'saved' ? 'Your saved address is required. Choose a different address or update your profile.' : 'Shipping address is required.'
    if (!payment.cardHolder.trim()) return 'Card holder name is required.'
    if (payment.cardNumber.replace(/\s/g, '').length !== 16) return 'Enter a 16-digit card number.'
    if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(payment.expiry)) return 'Enter expiry as MM/YY.'
    const [month, year] = payment.expiry.split('/').map(Number)
    const expiryDate = new Date(2000 + year, month, 1)
    if (expiryDate <= new Date(new Date().getFullYear(), new Date().getMonth(), 1)) return 'Card expiry must be in the future.'
    if (!/^\d{3}$/.test(payment.cvv)) return 'Enter a 3-digit CVV.'
    return ''
  }

  async function submitPayment(event) {
    event.preventDefault()
    setError('')
    const validationError = paymentError()
    if (validationError) {
      setError(validationError)
      return
    }
    setPaying(true)
    try {
      const order = await createOrder(selectedShippingAddress().trim())
      await onCartChange({ items: [], total: 0 })
      navigate(`/order-confirmation/${order.id}`)
    } catch (requestError) {
      const message = requestError.message.replace(/^Insufficient stock for product:\s*/i, 'Not enough stock for ')
      await loadCart()
      setError(message)
    } finally {
      setPaying(false)
    }
  }

  if (!cart && error) return <Container className="cart-page"><div className="catalog-state"><Alert variant="warning"><strong>We couldn't load checkout.</strong><br />{error}</Alert><Button variant="primary" onClick={loadCart}>Try again</Button></div></Container>
  if (!cart) return <Container className="cart-page"><div className="skeleton cart-skeleton" /></Container>
  if (cart.items.length === 0) return <Container className="cart-page"><div className="empty-state"><div className="empty-state-icon"><i className="bi bi-cart3" /></div><h2>Your cart is empty</h2><p>Add products before checking out.</p><Button as={Link} to="/" variant="primary">Go to the store</Button></div></Container>

  return (
    <Container className="checkout-page">
      <div className="eyebrow">CHECKOUT</div><h1>Complete your order</h1>
      <Alert variant="info" className="demo-note"><i className="bi bi-info-circle" /> Demo store – no real payment. Card details stay in this browser and are never sent to the server.</Alert>
      {error && <Alert variant="danger">{error}</Alert>}
      <Row className="g-4 align-items-start">
        <Col lg={7}>
          <Form className="checkout-form" onSubmit={submitPayment}>
            <section>
              <h2>Shipping address</h2>
              <Form.Check type="radio" id="savedAddress" name="shippingOption" label="Use my saved address" checked={shippingOption === 'saved'} onChange={() => setShippingOption('saved')} />
              <div className="saved-address">{currentUser.address || 'No saved address is available.'}</div>
              <Form.Check className="mt-3" type="radio" id="differentAddress" name="shippingOption" label="Ship to a different address" checked={shippingOption === 'different'} onChange={() => setShippingOption('different')} />
              {shippingOption === 'different' && <Form.Group className="mt-3" controlId="shippingAddress"><Form.Label>Shipping address</Form.Label><Form.Control as="textarea" rows={3} value={differentAddress} onChange={(event) => setDifferentAddress(event.target.value)} required /></Form.Group>}
            </section>
            <section>
              <h2>Demo payment</h2>
              <Form.Group className="mb-3" controlId="cardHolder"><Form.Label>Card holder</Form.Label><Form.Control name="cardHolder" value={payment.cardHolder} onChange={updatePayment} autoComplete="cc-name" /></Form.Group>
              <Form.Group className="mb-3" controlId="cardNumber"><Form.Label>Card number</Form.Label><Form.Control name="cardNumber" inputMode="numeric" value={payment.cardNumber} onChange={updatePayment} placeholder="1234 5678 9012 3456" autoComplete="cc-number" /></Form.Group>
              <Row><Col sm={6}><Form.Group className="mb-3" controlId="expiry"><Form.Label>Expiry</Form.Label><Form.Control name="expiry" inputMode="numeric" value={payment.expiry} onChange={updatePayment} placeholder="MM/YY" autoComplete="cc-exp" /></Form.Group></Col><Col sm={6}><Form.Group className="mb-3" controlId="cvv"><Form.Label>CVV</Form.Label><Form.Control name="cvv" inputMode="numeric" value={payment.cvv} onChange={updatePayment} placeholder="123" autoComplete="cc-csc" /></Form.Group></Col></Row>
            </section>
            <Button type="submit" variant="primary" size="lg" className="w-100" disabled={paying}>{paying ? 'Processing demo payment...' : `Pay ${currency.format(cart.total)}`}</Button>
          </Form>
        </Col>
        <Col lg={5}><aside className="checkout-summary"><h2>Your items</h2>{cart.items.map((item) => <div className="checkout-item" key={item.productId}><ProductVisual productName={item.productName} imageUrl={item.imageUrl} className="product-visual-checkout" /><div><strong>{item.productName}</strong><span>{item.quantity} × {currency.format(item.price)}</span></div><strong>{currency.format(item.lineTotal)}</strong></div>)}<div className="checkout-total"><span>Total</span><strong>{currency.format(cart.total)}</strong></div></aside></Col>
      </Row>
    </Container>
  )
}

export default CheckoutPage
