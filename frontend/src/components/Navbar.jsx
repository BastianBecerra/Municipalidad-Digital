import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 50);
    };
    window.addEventListener('scroll', handleScroll);

    // Check if user is logged in
    setIsLoggedIn(!!localStorage.getItem('token'));

    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <nav className={`navbar ${scrolled ? 'scrolled' : ''}`}>
      <div className="container navbar-container">
        <a href="#" className="logo">
          <span className="logo-icon">🏛️</span>
          <span className="logo-text">Muni Digital</span>
        </a>

        <div className={`nav-links ${menuOpen ? 'active' : ''}`}>
          <a href="#inicio" onClick={() => setMenuOpen(false)}>Inicio</a>
          <a href="#tramites" onClick={() => setMenuOpen(false)}>Trámites</a>
          <a href="#juntas" onClick={() => setMenuOpen(false)}>Juntas de vecinos</a>
          <a href="#" onClick={(e) => { e.preventDefault(); setMenuOpen(false); navigate('/contacto'); }}>Contacto</a>
          {isLoggedIn ? (
            <button onClick={() => navigate('/perfil')} className="btn btn-primary nav-btn">Mi Perfil</button>
          ) : (
            <button onClick={() => navigate('/login')} className="btn btn-primary nav-btn">Ingresar</button>
          )}
        </div>

        <button className="mobile-menu-btn" onClick={() => setMenuOpen(!menuOpen)}>
          <span className={`hamburger ${menuOpen ? 'open' : ''}`}></span>
        </button>
      </div>
    </nav>
  );
};

export default Navbar;

