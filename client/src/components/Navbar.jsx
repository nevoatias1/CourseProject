import { useState } from 'react'
import { Button, Container, Form, Nav, NavDropdown, Navbar as BootstrapNavbar } from 'react-bootstrap'
import { Link, NavLink, useNavigate, useSearchParams } from 'react-router-dom'

function Navbar({ currentUser, cartCount, onLogout }) {
  const [search, setSearch] = useState('')
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()

  function submitSearch(event) {
    event.preventDefault()
    const nextParams = new URLSearchParams(searchParams)
    if (search.trim()) nextParams.set('search', search.trim())
    else nextParams.delete('search')
    navigate(`/${nextParams.toString() ? `?${nextParams.toString()}` : ''}`)
  }

  return (
    <BootstrapNavbar expand="lg" className="store-navbar" variant="light">
      <Container>
        <BootstrapNavbar.Brand as={Link} to="/" className="brand-mark">
          <span className="brand-icon">AE</span><span>Atias Express</span>
        </BootstrapNavbar.Brand>
        <BootstrapNavbar.Toggle aria-controls="main-navigation" aria-label="Toggle navigation" />
        <BootstrapNavbar.Collapse id="main-navigation">
          <Nav className="me-auto">
            <Nav.Link as={NavLink} to="/" end>Home</Nav.Link>
          </Nav>
          <Form className="d-flex navbar-search" onSubmit={submitSearch} role="search">
            <Form.Control value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search the store" aria-label="Search the store" />
            <Button type="submit" variant="outline-primary">Search</Button>
          </Form>
          <Nav className="ms-lg-3 align-items-lg-center account-nav">
            <Nav.Link as={NavLink} to="/cart" className="cart-link" aria-label={`Cart with ${cartCount} items`}>
              <i className="bi bi-cart3" aria-hidden="true" />
              {cartCount > 0 && <span className="cart-count">{cartCount}</span>}
            </Nav.Link>
            {currentUser ? (
              <>
                <NavDropdown title={`Hi, ${currentUser.username}`} id="account-navigation" align="end">
                  {currentUser.role === 'ADMIN' && <NavDropdown.Item as={Link} to="/admin"><i className="bi bi-shield-lock me-2" />Admin</NavDropdown.Item>}
                  <NavDropdown.Item as={Link} to="/account?tab=profile"><i className="bi bi-person me-2" />My account</NavDropdown.Item>
                  <NavDropdown.Item as={Link} to="/account?tab=orders"><i className="bi bi-box-seam me-2" />My orders</NavDropdown.Item>
                  <NavDropdown.Item as={Link} to="/account?tab=favorites"><i className="bi bi-heart me-2" />Favorites</NavDropdown.Item>
                  <NavDropdown.Divider />
                  <NavDropdown.Item as="button" onClick={onLogout}>Log out</NavDropdown.Item>
                </NavDropdown>
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
