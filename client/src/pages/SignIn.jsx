import { useState } from 'react'
import { Alert, Button, Card, Container, Form, Spinner } from 'react-bootstrap'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ApiError, authenticate, getCurrentUser } from '../api.js'

function SignIn({ currentUser, setCurrentUser }) {
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (currentUser) return <Navigate to="/" replace />

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    if (!form.username.trim() || !form.password) {
      setError('Please enter your username and password.')
      return
    }
    setSubmitting(true)
    try {
      const response = await authenticate(form)
      localStorage.setItem('token', response.jwt)
      const user = await getCurrentUser()
      setCurrentUser(user)
      const destination = location.state?.from
      navigate(destination ? `${destination.pathname}${destination.search || ''}` : '/')
    } catch (requestError) {
      localStorage.removeItem('token')
      setError(requestError instanceof ApiError && requestError.status === 403
        ? 'Those credentials were not recognized. Please try again.'
        : requestError.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Container className="auth-shell">
      <Card className="auth-card">
        <Card.Body>
          <div className="eyebrow">WELCOME BACK</div>
          <h1>Sign in</h1>
          <p className="muted-copy">Pick up where you left off.</p>
          {error && <Alert variant="danger">{error}</Alert>}
          <Form onSubmit={submit} noValidate>
            <Form.Group className="mb-3" controlId="signInUsername">
              <Form.Label>Username</Form.Label>
              <Form.Control name="username" value={form.username} onChange={updateField} autoComplete="username" />
            </Form.Group>
            <Form.Group className="mb-4" controlId="signInPassword">
              <Form.Label>Password</Form.Label>
              <Form.Control type="password" name="password" value={form.password} onChange={updateField} autoComplete="current-password" />
            </Form.Group>
            <Button className="w-100 primary-button" type="submit" disabled={submitting}>
              {submitting ? <><Spinner animation="border" size="sm" /> Signing in...</> : 'Sign in'}
            </Button>
          </Form>
          <p className="auth-footer">New to Atias Express? <Link to="/sign-up">Create an account</Link></p>
        </Card.Body>
      </Card>
    </Container>
  )
}

export default SignIn
