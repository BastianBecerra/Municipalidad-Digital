import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Profile.css';

const Profile = () => {
  const [user, setUser] = useState(null); // Almacena los datos del usuario
  const [loading, setLoading] = useState(true); // Estado de carga para mostrar el spinner
  const [error, setError] = useState(''); // Estado para mensajes de error
  const [documents, setDocuments] = useState([]); // Historial de documentos
  const [loadingDocs, setLoadingDocs] = useState(true); // Estado de carga de documentos
  const [activeTab, setActiveTab] = useState('info'); // Pestaña activa: 'info' o 'docs'
  const [downloadStates, setDownloadStates] = useState({}); // Estado individual de descargas: docId -> 'idle' | 'downloading' | 'completed' | 'error'
  const [autoDownload, setAutoDownload] = useState(() => {
    const saved = localStorage.getItem('autoDownload');
    return saved !== 'false'; // Por defecto true si no está guardado
  });
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

  // Efecto: Cargar documentos una vez que el usuario está disponible
  useEffect(() => {
    if (!user) return;
    
    const fetchDocuments = async () => {
      try {
        const token = localStorage.getItem('token');
        // Si el rol es administrativo (ADMIN/FUNCIONARIO), cargamos todos los documentos. Si es VECINO, cargamos los suyos.
        const endpoint = (user.rol === 'ADMIN' || user.rol === 'FUNCIONARIO') ? '/documentos' : '/documentos/me';
        
        const response = await fetch(endpoint, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        });

        if (response.ok) {
          const data = await response.json();
          setDocuments(data);
        }
      } catch (err) {
        console.error('Error fetching documents:', err);
      } finally {
        setLoadingDocs(false);
      }
    };

    fetchDocuments();
  }, [user]);

  // Manejador para descargar PDF
  const handleDownloadPdf = async (doc) => {
    const docId = doc.id;
    try {
      setDownloadStates(prev => ({ ...prev, [docId]: 'downloading' }));
      const token = localStorage.getItem('token');
      const response = await fetch(`/documentos/${docId}/pdf`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      if (!response.ok) throw new Error('Error al descargar PDF');
      
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      
      // Limpiar y normalizar strings para usarlos en el nombre del archivo
      const cleanString = (str) => {
        return str
          ? str.toLowerCase()
               .normalize("NFD").replace(/[\u0300-\u036f]/g, "") // remover acentos
               .replace(/[^a-z0-9]/g, "_")                       // caracteres no alfanuméricos por guiones bajos
               .replace(/_+/g, "_")                              // encoger guiones múltiples
               .replace(/(^_|_$)/g, "")                          // quitar extremos
          : "";
      };

      const typeClean = cleanString(doc.titulo);
      const userClean = cleanString(doc.usuarioNombreCompleto || (user ? `${user.nombres} ${user.apellidoPaterno} ${user.apellidoMaterno}` : ""));
      const filename = `${typeClean}_${userClean}_${docId}.pdf`;

      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);

      // Feedback de éxito
      setDownloadStates(prev => ({ ...prev, [docId]: 'completed' }));
      
      // Regresa a estado normal tras 3 segundos
      setTimeout(() => {
        setDownloadStates(prev => ({ ...prev, [docId]: 'idle' }));
      }, 3000);

    } catch (err) {
      console.error(err);
      setDownloadStates(prev => ({ ...prev, [docId]: 'error' }));
      // Regresa a estado normal tras 3 segundos en caso de error
      setTimeout(() => {
        setDownloadStates(prev => ({ ...prev, [docId]: 'idle' }));
      }, 3000);
    }
  };

  // Manejador para activar/desactivar la descarga automática
  const handleToggleAutoDownload = () => {
    setAutoDownload(prev => {
      const newVal = !prev;
      localStorage.setItem('autoDownload', newVal ? 'true' : 'false');
      return newVal;
    });
  };

  // Manejador para cerrar sesión
  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/');
  };

  // Estado de carga
  if (loading) {
    return (
      <div className="profile-page gradient-bg">
        <div className="floating-shape shape-1"></div>
        <div className="floating-shape shape-2"></div>
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
        <div className="floating-shape shape-1"></div>
        <div className="floating-shape shape-2"></div>
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
      <div className="floating-shape shape-1"></div>
      <div className="floating-shape shape-2"></div>
      
      <div className="profile-container">
        {/* Cabecera común a ambas pestañas */}
        <div className={`profile-card profile-main-card ${activeTab === 'docs' ? 'wide-card' : ''}`}>
          <div className="profile-header">
            <div className="avatar">
              {user.nombres?.charAt(0)}{user.apellidoPaterno?.charAt(0)}
            </div>
            <h2 className="profile-name">
              {user.nombres} {user.apellidoPaterno} {user.apellidoMaterno}
            </h2>
            <span className="profile-role">{user.rol}</span>
          </div>

          {/* Selector de pestañas */}
          <div className="profile-tabs">
            <button 
              className={`tab-btn ${activeTab === 'info' ? 'active' : ''}`}
              onClick={() => setActiveTab('info')}
            >
              Mi Información
            </button>
            <button 
              className={`tab-btn ${activeTab === 'docs' ? 'active' : ''}`}
              onClick={() => setActiveTab('docs')}
            >
              Mis Documentos y Descargas
            </button>
          </div>

          {/* Contenido pestaña: MI INFORMACIÓN */}
          {activeTab === 'info' && (
            <div className="profile-info animate-fade-in">
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
          )}

          {/* Contenido pestaña: DOCUMENTOS Y DESCARGAS */}
          {activeTab === 'docs' && (
            <div className="profile-docs animate-fade-in">
              {/* Sección Opciones de Descarga */}
              <div className="settings-panel">
                <div className="settings-text">
                  <h4>Descarga Automática</h4>
                  <p>Inicia la descarga de archivos PDF en tu equipo automáticamente al generar una nueva solicitud.</p>
                </div>
                <label className="toggle-switch">
                  <input 
                    type="checkbox" 
                    checked={autoDownload} 
                    onChange={handleToggleAutoDownload} 
                  />
                  <span className="slider round"></span>
                </label>
              </div>

              {/* Lista de Documentos */}
              <div className="info-section">
                <h3 className="section-title">Historial de Trámites y Certificados</h3>
                
                {loadingDocs ? (
                  <div className="loading-docs">
                    <div className="spinner-mini"></div>
                    <p>Buscando tus documentos...</p>
                  </div>
                ) : documents.length === 0 ? (
                  <div className="empty-docs">
                    <p>No se encontraron trámites o documentos asociados a tu cuenta.</p>
                    <button 
                      onClick={() => navigate('/generar-documento')} 
                      className="cu-btn-submit"
                      style={{ marginTop: '1rem', width: 'auto', padding: '0.6rem 1.5rem' }}
                    >
                      Generar Nuevo Trámite
                    </button>
                  </div>
                ) : (
                  <div className="table-responsive">
                    <table className="docs-table">
                      <thead>
                        <tr>
                          <th>ID</th>
                          <th>Detalle de Trámite</th>
                          <th>Fecha</th>
                          <th>Estado</th>
                          <th>Blockchain</th>
                          <th>Descarga</th>
                        </tr>
                      </thead>
                      <tbody>
                        {documents.map((doc) => {
                          const status = downloadStates[doc.id] || 'idle';
                          
                          return (
                            <tr key={doc.id}>
                              <td className="doc-col-id">#{doc.id}</td>
                              <td className="doc-col-detail">
                                <div className="doc-title-cell">{doc.titulo}</div>
                                {doc.descripcion && <div className="doc-desc-cell">{doc.descripcion}</div>}
                              </td>
                              <td className="doc-col-date">
                                {new Date(doc.fechaCreacion).toLocaleDateString('es-CL', {
                                  day: '2-digit',
                                  month: '2-digit',
                                  year: 'numeric'
                                })}
                              </td>
                              <td className="doc-col-status">
                                <span className={`status-badge badge-${doc.estado?.toLowerCase()}`}>
                                  {doc.estado}
                                </span>
                              </td>
                              <td className="doc-col-blockchain">
                                <span className={`blockchain-badge bc-${doc.estadoBlockchain?.toLowerCase() || 'pendiente'}`} title={doc.blockchainTxHash || ''}>
                                  {doc.estadoBlockchain || 'PENDIENTE'}
                                </span>
                              </td>
                              <td className="doc-col-action">
                                <button 
                                  onClick={() => handleDownloadPdf(doc)} 
                                  disabled={status === 'downloading'}
                                  className={`btn-download-action state-${status}`}
                                  aria-label={`Descargar PDF del documento ${doc.id}`}
                                >
                                  {status === 'downloading' && (
                                    <>
                                      <span className="spinner-mini"></span>
                                      <span className="btn-text">Descargando...</span>
                                    </>
                                  )}
                                  {status === 'completed' && (
                                    <>
                                      <span className="btn-icon">✓</span>
                                      <span className="btn-text">Completado</span>
                                    </>
                                  )}
                                  {status === 'error' && (
                                    <>
                                      <span className="btn-icon">!</span>
                                      <span className="btn-text">Error</span>
                                    </>
                                  )}
                                  {status === 'idle' && (
                                    <>
                                      <span className="btn-icon">&#8595;</span>
                                      <span className="btn-text">Descargar PDF</span>
                                    </>
                                  )}
                                </button>
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Acciones de pie de página */}
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