import { useState } from 'react'
import { Button, Container, Form, Nav, Navbar as BootstrapNavbar } from 'react-bootstrap'
import { Link, NavLink, useNavigate } from 'react-router-dom'

function Navbar({ currentUser, onLogout }) {
  const [search, setSearch] = useState('')
  const navigate = useNavigate()

  function submitSearch(event) {
    event.preventDefault()
    setSearch('')
    navigate('/')
  }

  return (
    <BootstrapNavbar expand="lg" className="store-navbar" variant="light">
      <Container>
        <BootstrapNavbar.Brand as={Link} to="/" className="brand-mark">
          <span className="brand-icon">AE</span><span>Atias Express</span>
        </BootstrapNavbar.Brand>
        <BootstrapNavbar.Toggle aria-controls="main-navigation" />
        <BootstrapNavbar.Collapse id="main-navigation">
          <Nav className="me-auto">
            <Nav.Link as={NavLink} to="/" end>Home</Nav.Link>
          </Nav>
          <Form className="d-flex navbar-search" onSubmit={submitSearch} role="search">
            <Form.Control value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search the store" aria-label="Search the store" />
            <Button type="submit" variant="outline-primary">Search</Button>
          </Form>
          <Nav className="ms-lg-3 align-items-lg-center account-nav">
            {currentUser ? (
              <>
                <span className="welcome-copy">Hi, {currentUser.username}</span>
                <Button variant="light" size="sm" onClick={onLogout}>Log out</Button>
              </>
            ) : (
              <>
                <Nav.Link as={NavLink} to="/sign-up">Sign up</Nav.Link>
                <Nav.Link as={NavLink} to="/sign-in" className="sign-in-link">Sign in</Nav.Link>
              </>
            )}
          </Nav>
        </BootstrapNavbar.Collapse>
      </Container>
    </BootstrapNavbar>
  )
}

export default Navbar
