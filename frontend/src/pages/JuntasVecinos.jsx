import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import './JuntasVecinos.css';

const JuntasVecinos = () => {
  const [profile, setProfile] = useState(null);
  const [territorios, setTerritorios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('token');
    
    const fetchData = async () => {
      try {
        // 1. Fetch Juntas de Vecinos directory (accessible to everyone)
        const terrHeaders = token ? { 'Authorization': `Bearer ${token}` } : {};
        const terrResponse = await fetch('/usuarios/me', { headers: terrHeaders }).then(res => res.ok ? res.json() : null).catch(() => null);
        
        if (terrResponse) {
          setProfile(terrResponse);
        }

        // Fetch all territories
        const listResponse = await fetch('/territorios', { headers: terrHeaders });
        if (listResponse.ok) {
          const listData = await listResponse.json();
          // Filter to only show JUNTA_VECINOS type or show all
          setTerritorios(listData);
        } else {
          throw new Error('Error al cargar el directorio de juntas de vecinos');
        }
      } catch (err) {
        console.error(err);
        setError('No se pudo cargar la información. Intenta recargar la página.');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleRequestDocument = () => {
    navigate('/tramites/nuevo?tipo=junta-vecinal');
  };

  const filteredTerritorios = territorios.filter(t => 
    t.nombre.toLowerCase().includes(searchQuery.toLowerCase()) ||
    t.comuna.toLowerCase().includes(searchQuery.toLowerCase()) ||
    (t.presidente && t.presidente.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  return (
    <div className="jjvv-page">
      <Navbar />
      
      <main className="container jjvv-container">
        <header className="jjvv-header text-center">
          <span className="jjvv-icon">🏡</span>
          <h1>Juntas de Vecinos</h1>
          <p className="text-muted">
            Conoce tu junta de vecinos activa, accede a información de contacto y solicita trámites oficiales.
          </p>
        </header>

        {loading ? (
          <div className="loading-box glass-card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '40px', minHeight: '300px' }}>
            <div className="spinner"></div>
            <p style={{ color: '#fff', fontSize: '18px', marginTop: '16px' }}>Cargando información municipal...</p>
          </div>
        ) : error ? (
          <div className="status-box error-box">{error}</div>
        ) : (
          <div className="jjvv-content animate-in">
            {/* 1. SECCIÓN: Mi Junta de Vecinos */}
            {profile && profile.territorio ? (
              <section className="my-jjvv-section glass-card">
                <div className="section-badge">Tu Junta de Vecinos Activa</div>
                <div className="my-jjvv-header-block">
                  <h2>{profile.territorio.nombre}</h2>
                  <span className="uv-number">Unidad Vecinal: {profile.territorio.numeroUnidadVecinal || 'UV-N/A'}</span>
                </div>

                <div className="jjvv-details-grid">
                  <div className="detail-card">
                    <span className="card-detail-icon">👤</span>
                    <div className="detail-info">
                      <span className="detail-label">Presidente(a)</span>
                      <span className="detail-value">{profile.territorio.presidente || 'No especificado'}</span>
                    </div>
                  </div>

                  <div className="detail-card">
                    <span className="card-detail-icon">📍</span>
                    <div className="detail-info">
                      <span className="detail-label">Sede Social</span>
                      <span className="detail-value">{profile.territorio.direccionSede || 'Sin dirección registrada'}</span>
                    </div>
                  </div>

                  <div className="detail-card">
                    <span className="card-detail-icon">📞</span>
                    <div className="detail-info">
                      <span className="detail-label">Teléfono</span>
                      <span className="detail-value">{profile.territorio.telefono || 'Sin teléfono'}</span>
                    </div>
                  </div>

                  <div className="detail-card">
                    <span className="card-detail-icon">✉️</span>
                    <div className="detail-info">
                      <span className="detail-label">Correo Electrónico</span>
                      <span className="detail-value">{profile.territorio.email || 'Sin correo electrónico'}</span>
                    </div>
                  </div>
                </div>

                {profile.territorio.descripcion && (
                  <div className="jjvv-desc-block">
                    <h4>Descripción de la Junta</h4>
                    <p>{profile.territorio.descripcion}</p>
                  </div>
                )}

                {/* Límites Territoriales */}
                <div className="jjvv-limits-block">
                  <h4>Límites Territoriales</h4>
                  <div className="limits-grid">
                    <div><strong>Norte:</strong> {profile.territorio.limiteNorte || '—'}</div>
                    <div><strong>Sur:</strong> {profile.territorio.limiteSur || '—'}</div>
                    <div><strong>Este:</strong> {profile.territorio.limiteEste || '—'}</div>
                    <div><strong>Oeste:</strong> {profile.territorio.limiteOeste || '—'}</div>
                  </div>
                </div>

                <div className="my-jjvv-actions">
                  <button onClick={handleRequestDocument} className="btn btn-primary btn-jjvv-action">
                    📝 Solicitar Acta o Certificado de Junta
                  </button>
                </div>
              </section>
            ) : profile ? (
              <section className="my-jjvv-section glass-card info-warning-card">
                <div className="warning-content text-center" style={{ padding: '20px' }}>
                  <h3>⚠️ Sin Junta de Vecinos Asignada</h3>
                  <p className="text-muted mt-2">
                    Tu cuenta no está vinculada a ninguna Junta de Vecinos. Contacta con soporte municipal o el administrador para registrar tu territorio.
                  </p>
                </div>
              </section>
            ) : (
              <section className="my-jjvv-section glass-card info-warning-card text-center" style={{ padding: '30px' }}>
                <h3>🔑 Inicia sesión para ver tu Junta de Vecinos</h3>
                <p className="text-muted mt-2 mb-4">
                  Si inicias sesión, podrás ver la información de tu Junta de Vecinos activa y realizar solicitudes en línea.
                </p>
                <button onClick={() => navigate('/login')} className="btn btn-primary" style={{ maxWidth: '200px', margin: '0 auto' }}>
                  Ingresar
                </button>
              </section>
            )}

            {/* 2. SECCIÓN: Directorio de Juntas de Vecinos */}
            <section className="jjvv-directory-section">
              <div className="directory-header">
                <h2>Directorio Municipal de Organizaciones</h2>
                <div className="search-bar-wrapper">
                  <input 
                    type="text" 
                    placeholder="Buscar por nombre, comuna o presidente..." 
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="cu-input search-input"
                  />
                  <span className="search-icon">🔍</span>
                </div>
              </div>

              {filteredTerritorios.length === 0 ? (
                <div className="no-results glass-card text-center" style={{ padding: '40px' }}>
                  <p className="text-muted">No se encontraron juntas de vecinos que coincidan con la búsqueda.</p>
                </div>
              ) : (
                <div className="jjvv-grid">
                  {filteredTerritorios.map((t) => (
                    <div key={t.id} className="jjvv-item-card glass-card">
                      <div className="jjvv-card-header">
                        <h3>{t.nombre}</h3>
                        <span className="badge-type">{t.tipo || 'JUNTA_VECINOS'}</span>
                      </div>
                      <div className="jjvv-card-body">
                        <div className="card-info-row">
                          <strong>Presidente:</strong> <span>{t.presidente || '—'}</span>
                        </div>
                        <div className="card-info-row">
                          <strong>Sede:</strong> <span>{t.direccionSede || '—'}</span>
                        </div>
                        <div className="card-info-row">
                          <strong>Comuna:</strong> <span>{t.comuna || '—'}</span>
                        </div>
                        {t.email && (
                          <div className="card-info-row">
                            <strong>Contacto:</strong> <span>{t.email}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </section>
          </div>
        )}
      </main>

      <Footer />
    </div>
  );
};

export default JuntasVecinos;
