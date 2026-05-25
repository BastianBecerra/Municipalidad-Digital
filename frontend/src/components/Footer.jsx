import './Footer.css';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="container footer-container">
        <div className="footer-grid">
          <div className="footer-brand">
            <h3 className="footer-logo">🏛️ Muni Digital</h3>
            <p className="footer-desc">
              Acercando los servicios municipales y vecinales a tu hogar.
              Más rápido, más simple, 100% digital.
            </p>
            <div className="social-links">
              <a href="#" className="social-link">FB</a>
              <a href="#" className="social-link">TW</a>
              <a href="#" className="social-link">IG</a>
            </div>
          </div>

          <div className="footer-links">
            <h4>Enlaces Rápidos</h4>
            <ul>
              <li><a href="#inicio">Inicio</a></li>
              <li><a href="#tramites">Trámites Destacados</a></li>
              <li><a href="#">Juntas de Vecinos</a></li>
            </ul>
          </div>

          <div className="footer-links">
            <h4>Servicios</h4>
            <ul>
              <li><a href="#">Certificados</a></li>
              <li><a href="#">Pagos en Línea</a></li>
              <li><a href="#">Permisos</a></li>
              <li><a href="#">Atención Virtual</a></li>
            </ul>
          </div>

          <div className="footer-contact">
            <h4>Contacto</h4>
            <p>✉️ contacto@gmail.com</p>
          </div>
        </div>

        <div className="footer-bottom">
          <p>&copy; {new Date().getFullYear()} Muni Digital. Todos los derechos reservados.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
