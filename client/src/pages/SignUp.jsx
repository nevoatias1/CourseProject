import { useState } from 'react'
import { Alert, Button, Card, Col, Container, Form, Row, Spinner } from 'react-bootstrap'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { registerUser } from '../api.js'

const initialForm = {
  first_name: '', last_name: '', email: '', phone: '', address: '', username: '', password: '', confirmPassword: '',
}

function SignUp({ currentUser }) {
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (currentUser) return <Navigate to="/" replace />

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  function validate() {
    const requiredFields = ['first_name', 'last_name', 'email', 'phone', 'address', 'username', 'password', 'confirmPassword']
    if (requiredFields.some((field) => !form[field].trim())) return 'Please complete every field.'
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return 'Please enter a valid email address.'
    if (form.password !== form.confirmPassword) return 'Passwords do not match.'
    return ''
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    setSuccess('')
    const validationError = validate()
    if (validationError) {
      setError(validationError)
      return
    }
    setSubmitting(true)
    try {
      const { confirmPassword, ...user } = form
      await registerUser(user)
      setSuccess('Your account was created. Redirecting you to sign in...')
      setTimeout(() => navigate('/sign-in'), 700)
    } catch (requestError) {
      setError(requestError.message || 'Registration failed. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Container className="auth-shell sign-up-shell">
      <Card className="auth-card">
        <Card.Body>
          <div className="eyebrow">JOIN NORTHSTAR</div>
          <h1>Create your account</h1>
          <p className="muted-copy">Save your details now and shop with less friction later.</p>
          {error && <Alert variant="danger">{error}</Alert>}
          {success && <Alert variant="success">{success}</Alert>}
          <Form onSubmit={submit} noValidate>
            <Row>
              <Col md={6}><Form.Group className="mb-3" controlId="firstName"><Form.Label>First name</Form.Label><Form.Control name="first_name" value={form.first_name} onChange={updateField} autoComplete="given-name" /></Form.Group></Col>
              <Col md={6}><Form.Group className="mb-3" controlId="lastName"><Form.Label>Last name</Form.Label><Form.Control name="last_name" value={form.last_name} onChange={updateField} autoComplete="family-name" /></Form.Group></Col>
            </Row>
            <Row>
              <Col md={6}><Form.Group className="mb-3" controlId="email"><Form.Label>Email</Form.Label><Form.Control type="email" name="email" value={form.email} onChange={updateField} autoComplete="email" /></Form.Group></Col>
              <Col md={6}><Form.Group className="mb-3" controlId="phone"><Form.Label>Phone</Form.Label><Form.Control name="phone" value={form.phone} onChange={updateField} autoComplete="tel" /></Form.Group></Col>
            </Row>
            <Form.Group className="mb-3" controlId="address"><Form.Label>Address</Form.Label><Form.Control name="address" value={form.address} onChange={updateField} autoComplete="street-address" /></Form.Group>
            <Form.Group className="mb-3" controlId="username"><Form.Label>Username</Form.Label><Form.Control name="username" value={form.username} onChange={updateField} autoComplete="username" /></Form.Group>
            <Row>
              <Col md={6}><Form.Group className="mb-4" controlId="password"><Form.Label>Password</Form.Label><Form.Control type="password" name="password" value={form.password} onChange={updateField} autoComplete="new-password" /></Form.Group></Col>
              <Col md={6}><Form.Group className="mb-4" controlId="confirmPassword"><Form.Label>Confirm password</Form.Label><Form.Control type="password" name="confirmPassword" value={form.confirmPassword} onChange={updateField} autoComplete="new-password" /></Form.Group></Col>
            </Row>
            <Button className="w-100 primary-button" type="submit" disabled={submitting}>
              {submitting ? <><Spinner animation="border" size="sm" /> Creating account...</> : 'Create account'}
            </Button>
          </Form>
          <p className="auth-footer">Already have an account? <Link to="/sign-in">Sign in</Link></p>
        </Card.Body>
      </Card>
    </Container>
  )
}

export default SignUp
