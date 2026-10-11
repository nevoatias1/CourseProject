import { useEffect, useState } from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { getCurrentUser } from './api.js'
import Navbar from './components/Navbar.jsx'
import Home from './pages/Home.jsx'
import SignIn from './pages/SignIn.jsx'
import SignUp from './pages/SignUp.jsx'

function App() {
  const [currentUser, setCurrentUser] = useState(null)
  const [loadingUser, setLoadingUser] = useState(true)

  useEffect(() => {
    if (!localStorage.getItem('token')) {
      setLoadingUser(false)
      return
    }

    getCurrentUser()
      .then(setCurrentUser)
      .catch(() => {
        localStorage.removeItem('token')
        setCurrentUser(null)
      })
      .finally(() => setLoadingUser(false))
  }, [])

  function handleLogout() {
    localStorage.removeItem('token')
    setCurrentUser(null)
  }

  if (loadingUser) {
    return <div className="app-loading">Loading your store...</div>
  }

  return (
    <BrowserRouter>
      <Navbar currentUser={currentUser} onLogout={handleLogout} />
      <main>
        <Routes>
          <Route path="/" element={<Home currentUser={currentUser} />} />
          <Route path="/sign-in" element={<SignIn currentUser={currentUser} setCurrentUser={setCurrentUser} />} />
          <Route path="/sign-up" element={<SignUp currentUser={currentUser} />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </BrowserRouter>
  )
}

export default App
