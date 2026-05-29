import './HeroSection.css';

const HeroSection = () => {
  return (
    <section id="inicio" className="hero-section gradient-bg">
      <div className="container hero-container">
        <div className="hero-content">
          <span className="badge">Plataforma Ciudadana 100% Digital</span>
          <h1 className="hero-title">
            Tus trámites municipales y vecinales, <span className="text-gradient">más simples que nunca</span>
          </h1>
          <p className="hero-subtitle">
            Solicita certificados, realiza pagos y sin hacer fila!. Todo desde la comodidad de tu hogar.
          </p>
          <div className="hero-buttons">
            <a href="/tramites/nuevo" className="btn btn-primary btn-lg">Iniciar Trámite</a>
            <button className="btn btn-secondary btn-lg">Ver Guía de Uso</button>
          </div>

          <div className="stats-container">
            <div className="stat-item">
              <h3>24/7</h3>
              <p>Atención Digital</p>
            </div>
            <div className="stat-item">
              <h3>100%</h3>
              <p>Confiable</p>
            </div>
            <div className="stat-item">
              <h3>100%</h3>
              <p>Seguro</p>
            </div>
          </div>
        </div>

        <div className="hero-image-wrapper">
          <div className="glass-card mockup-card">
            <div className="mockup-header">
              <span className="dot red"></span>
              <span className="dot yellow"></span>
              <span className="dot green"></span>
            </div>
            <div className="mockup-body">
              <div className="skeleton-line title"></div>
              <div className="skeleton-line"></div>
              <div className="skeleton-line short"></div>
              <div className="mockup-grid">
                <div className="skeleton-box"></div>
                <div className="skeleton-box"></div>
                <div className="skeleton-box"></div>
                <div className="skeleton-box"></div>
              </div>
            </div>
          </div>

          {/* Elementos Decorativos */}
          <div className="floating-shape shape-1"></div>
          <div className="floating-shape shape-2"></div>
        </div>
      </div>
    </section>
  );
};

export default HeroSection;
