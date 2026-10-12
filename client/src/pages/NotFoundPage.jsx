import { Button, Container } from 'react-bootstrap'
import { Link } from 'react-router-dom'

function NotFoundPage() {
  return (
    <Container className="not-found-page">
      <div className="empty-state">
        <div className="empty-state-icon"><i className="bi bi-compass" /></div>
        <div className="eyebrow">404</div>
        <h1>Page not found</h1>
        <p>The page you requested does not exist or may have moved.</p>
        <Button as={Link} to="/" variant="primary">Back to the store</Button>
      </div>
    </Container>
  )
}

export default NotFoundPage
