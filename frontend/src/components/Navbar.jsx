import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
  
  // Estados para controlar la interfaz de usuario
  const [scrolled, setScrolled] = useState(false); // Determina si la página ha sido desplazada
  const [menuOpen, setMenuOpen] = useState(false); // Alterna el menú en móviles
  const [isLoggedIn, setIsLoggedIn] = useState(false); // Controla si el usuario tiene sesión activa
  const [userRole, setUserRole] = useState(null); // Rol del usuario actual
  const navigate = useNavigate();

  useEffect(() => {
    // Función para manejar el estilo del Navbar al hacer scroll
    const handleScroll = () => {
      setScrolled(window.scrollY > 50);
    };
    window.addEventListener('scroll', handleScroll);

    // Verificación inicial de sesión al cargar el componente
    // Comprueba si existe un token en el almacenamiento local
    const token = localStorage.getItem('token');
    setIsLoggedIn(!!token);
    
    if (token) {
      try {
        const parts = token.split('.');
        if (parts.length === 3) {
          const base64Url = parts[1];
          const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
          const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
          }).join(''));
          const decoded = JSON.parse(jsonPayload);
          setUserRole(decoded.rol);
        } else {
          setUserRole(null);
        }
      } catch (e) {
        console.error('Error al decodificar el token de sesión', e);
        setUserRole(null);
      }
    } else {
      setUserRole(null);
    }

    // Limpieza del event listener al desmontar el componente
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    // Se aplica la clase 'scrolled' dinámicamente según el estado de scroll
    <nav className={`navbar ${scrolled ? 'scrolled' : ''}`}>

      <div className="container navbar-container">

        {/* Logo de la aplicación */}
        <a href="#" className="logo">
          <span className="logo-icon">🏛️</span>
          <span className="logo-text">Muni Digital</span>
        </a>

        {/* Links de navegación: la clase 'active' controla la visibilidad en móviles */}
        <div className={`nav-links ${menuOpen ? 'active' : ''}`}>

          <a href="/" onClick={() => setMenuOpen(false)}>Inicio</a>

          <a href="/tramites/nuevo" onClick={() => setMenuOpen(false)}>Trámites</a>

          <a href="/junta-vecinos" onClick={() => setMenuOpen(false)}>Juntas de vecinos</a>

          <a href="/contacto" onClick={(e) => { e.preventDefault(); setMenuOpen(false); navigate('/contacto'); }}>Contacto</a>
          
          {isLoggedIn && (userRole === 'ADMIN' || userRole === 'FUNCIONARIO') && (
            <a href="/validar" onClick={(e) => { e.preventDefault(); setMenuOpen(false); navigate('/validar'); }}>Validar</a>
          )}
          
          {/* Renderizado condicional: cambia el botón dependiendo del estado de sesión */}
          {isLoggedIn ? (
            <button onClick={() => navigate('/perfil')} className="btn btn-primary nav-btn">Mi Perfil</button>
          ) : (
            <button onClick={() => navigate('/login')} className="btn btn-primary nav-btn">Ingresar</button>
          )}

        </div>

        {/* Botón para abrir/cerrar menú hamburguesa en dispositivos móviles */}
        <button className="mobile-menu-btn" onClick={() => setMenuOpen(!menuOpen)}>
          <span className={`hamburger ${menuOpen ? 'open' : ''}`}></span>
        </button>

      </div>

    </nav>
  );
};

export default Navbar;