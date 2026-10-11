import { Alert, Container } from 'react-bootstrap'

function Home({ currentUser }) {
  return (
    <Container className="page-shell">
      <section className="hero-panel">
        <div className="eyebrow">ELECTRONICS, SIMPLIFIED</div>
        <h1>Tech that fits the way you live.</h1>
        <p className="hero-copy">Atias Express brings thoughtful gadgets and everyday essentials together in one clear, friendly store.</p>
        {currentUser ? (
          <Alert variant="success" className="welcome-alert">You are signed in as <strong>{currentUser.username}</strong>.</Alert>
        ) : (
          <p className="hero-note">Create an account or sign in to get started.</p>
        )}
      </section>
      <section className="coming-soon-card">
        <span className="coming-soon-icon">▦</span>
        <div>
          <h2>The catalog is on its way</h2>
          <p>Product browsing arrives in the next stage. For now, your account is ready to go.</p>
        </div>
      </section>
    </Container>
  )
}

export default Home
