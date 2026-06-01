import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Profile.css';

// Componente: Muestra la información del usuario autenticado
const Profile = () => {
  const [user, setUser] = useState(null); // Almacena los datos del usuario
  const [loading, setLoading] = useState(true); // Estado de carga para mostrar el spinner
  const [error, setError] = useState(''); // Estado para mensajes de error
  const navigate = useNavigate();

  // Efecto: Ejecuta al montar el componente para obtener los datos del usuario
  useEffect(() => {
    const token = localStorage.getItem('token');
    
    // Si no hay token, redirigimos al login inmediatamente (Protección de ruta)
    if (!token) {
      navigate('/login');
      return;
    }

    const fetchProfile = async () => {
      try {
        // Solicitud al endpoint protegido de usuario
        const response = await fetch('/usuarios/me', {
          headers: {
            'Authorization': `Bearer ${token}`, // Envío del JWT para autenticación
            'Content-Type': 'application/json',
          },
        });

        // Manejo de errores de sesión (Token expirado o inválido)
        if (response.status === 401 || response.status === 403) {
          localStorage.removeItem('token');
          navigate('/login');
          return;
        }

        if (!response.ok) {
          throw new Error('Error al obtener el perfil');
        }

        const data = await response.json();
        setUser(data);
      } catch (err) {
        setError('No se pudo cargar tu perfil. Intenta iniciar sesión nuevamente.');
      } finally {
        setLoading(false); // Terminamos el estado de carga
      }
    };

    fetchProfile();
  }, [navigate]);

  // Manejador para cerrar sesión
  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/');
  };

  // --- RENDERIZADO CONDICIONAL ---

  // Estado de carga
  if (loading) {
    return (
      <div className="profile-page gradient-bg">
        <div className="profile-container">
          <div className="profile-card">
            <div className="profile-loading">
              <div className="spinner"></div>
              <p>Cargando tu perfil...</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Estado de error
  if (error) {
    return (
      <div className="profile-page gradient-bg">
        <div className="profile-container">
          <div className="profile-card">
            <div className="profile-error">{error}</div>
            <button onClick={() => navigate('/login')} className="cu-btn-submit">
              Ir a Iniciar Sesión
            </button>
          </div>
        </div>
      </div>
    );
  }

  // Vista de perfil completa
  return (
    <div className="profile-page gradient-bg">
      <div className="profile-container">
        <div className="profile-card">
          {/* Cabecera: Avatar y Nombre */}
          <div className="profile-header">
            <div className="avatar">
              {user.nombres?.charAt(0)}{user.apellidoPaterno?.charAt(0)}
            </div>
            <h2 className="profile-name">{user.nombres} {user.apellidoPaterno} {user.apellidoMaterno}</h2>
            <span className="profile-role">{user.rol}</span>
          </div>

          {/* Secciones de información detallada */}
          <div className="profile-info">
            <div className="info-section">
              <h3 className="section-title">Datos Personales</h3>
              <div className="info-grid">
                <div className="info-item">
                  <span className="info-label">RUT</span>
                  <span className="info-value">{user.rut}</span>
                </div>
                <div className="info-item">
                  <span className="info-label">Fecha de Nacimiento</span>
                  <span className="info-value">{user.fechaNacimiento || '—'}</span>
                </div>
                <div className="info-item">
                  <span className="info-label">Género</span>
                  <span className="info-value">{user.genero || '—'}</span>
                </div>
              </div>
            </div>

            <div className="info-section">
              <h3 className="section-title">Contacto</h3>
              <div className="info-grid">
                <div className="info-item">
                  <span className="info-label">Email</span>
                  <span className="info-value">{user.email}</span>
                </div>
                <div className="info-item">
                  <span className="info-label">Teléfono</span>
                  <span className="info-value">{user.telefono || '—'}</span>
                </div>
              </div>
            </div>

            <div className="info-section">
              <h3 className="section-title">Dirección</h3>
              <div className="info-grid">
                <div className="info-item">
                  <span className="info-label">Dirección</span>
                  <span className="info-value">{user.direccion || '—'}</span>
                </div>
                <div className="info-item">
                  <span className="info-label">Comuna</span>
                  <span className="info-value">{user.comuna || '—'}</span>
                </div>
              </div>
            </div>

            {/* Renderizado condicional si el usuario pertenece a un territorio (Junta de vecinos) */}
            {user.territorio && (
              <div className="info-section">
                <h3 className="section-title">Junta de Vecinos</h3>
                <div className="info-grid">
                  <div className="info-item">
                    <span className="info-label">Territorio</span>
                    <span className="info-value">{user.territorio.nombre || '—'}</span>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Acciones del perfil */}
          <div className="profile-actions">
            <button onClick={handleLogout} className="btn-logout">
              Cerrar Sesión
            </button>
          </div>
        </div>

        <div className="login-back-wrapper">
          <button onClick={() => navigate('/')} className="btn-back">
            ← Volver al Inicio
          </button>
        </div>
      </div>
    </div>
  );
};

export default Profile;