import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import Modal from '../components/Modal';
import './GenerarDocumento.css';

// Algoritmo para validar el RUT chileno (cálculo del dígito verificador)
const validarRut = (rut) => {
  const cleaned = rut.trim().replace(/\./g, '').replace(/-/g, '');
  if (cleaned.length < 2) return false;
  const body = cleaned.slice(0, -1);
  const dv = cleaned.slice(-1).toUpperCase();
  if (!/^\d+$/.test(body)) return false;
  
  let sum = 0;
  let mul = 2;
  for (let i = body.length - 1; i >= 0; i--) {
    sum += parseInt(body[i]) * mul;
    mul = mul === 7 ? 2 : mul + 1;
  }
  const expectedDv = 11 - (sum % 11);
  const dvChar = expectedDv === 11 ? '0' : expectedDv === 10 ? 'K' : String(expectedDv);
  return dv === dvChar;
};

const GenerarDocumento = () => {
  const [searchParams] = useSearchParams();
  const queryTipo = searchParams.get('tipo');
  const [tipoTramite, setTipoTramite] = useState(queryTipo || 'residencia');
  const [profile, setProfile] = useState(null);
  const [loadingProfile, setLoadingProfile] = useState(true);
  const [profileError, setProfileError] = useState(false);
  const [formData, setFormData] = useState({
    rut: '',
    nombreCompleto: '',
    direccion: '',
    comuna: '',
    motivo: '',
    tipoActa: 'ACTA_ASAMBLEA',
    rutMinistroDeFe: '',
    nombreJuntaVecinal: '',
    juntaVecinosId: '',
  });
  
  // Estados para manejo de errores y UX
  const [fieldErrors, setFieldErrors] = useState({});
  const [touched, setTouched] = useState({}); 
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Estados para el Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalTitle, setModalTitle] = useState('');
  const [modalType, setModalType] = useState('info');
  const [modalContent, setModalContent] = useState(null);
  const [createdDocId, setCreatedDocId] = useState(null);
  const [createdDocStatus, setCreatedDocStatus] = useState('');
  const [createdDocBlockchainStatus, setCreatedDocBlockchainStatus] = useState('');
  const [downloadStatus, setDownloadStatus] = useState('idle'); // 'idle' | 'downloading' | 'completed' | 'error'

  const navigate = useNavigate();

  // EFECTO: Obtener perfil del usuario autenticado
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }

    const fetchProfile = async () => {
      try {
        const response = await fetch('/usuarios/me', {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        });

        if (response.status === 401 || response.status === 403) {
          localStorage.removeItem('token');
          navigate('/login');
          return;
        }

        if (!response.ok) {
          throw new Error('Error al obtener perfil');
        }

        const data = await response.json();
        console.log('User profile loaded:', data);
        setProfile(data);
        setProfileError(false);
      } catch (err) {
        console.error('Failed to load profile:', err);
        setProfileError(true);
        // No bloquear la UI completa, solo mostrar advertencia
      } finally {
        setLoadingProfile(false);
      }
    };

    fetchProfile();
  }, [navigate]);

  // EFECTO: Rellenar inputs dinámicamente si cambia el tipo de trámite o el perfil
  useEffect(() => {
    if (profile) {
      setFormData((prev) => ({
        ...prev,
        rut: profile.rut || '',
        nombreCompleto: `${profile.nombres || ''} ${profile.apellidoPaterno || ''} ${profile.apellidoMaterno || ''}`.trim(),
        direccion: tipoTramite === 'residencia' ? (profile.direccion || '') : '',
        comuna: profile.comuna || '',
        nombreJuntaVecinal: profile.territorio?.nombre || 'No asociado a una Junta de Vecinos',
        juntaVecinosId: profile.territorio?.id || '',
      }));
    }
  }, [tipoTramite, profile]);

  // Función de validación por campo
  const validateField = (name, value) => {
    switch (name) {
      case 'rut':
        if (!value.trim()) return 'El RUT es obligatorio.';
        if (!validarRut(value)) return 'El RUT ingresado no es válido.';
        return '';
      case 'nombreCompleto':
        if (!value.trim()) return 'El nombre es obligatorio.';
        return '';
      case 'direccion':
        if (!value.trim()) return 'La dirección es obligatoria.';
        return '';
      case 'comuna':
        if (tipoTramite === 'residencia' && !value.trim()) return 'La comuna es obligatoria.';
        return '';
      case 'rutMinistroDeFe':
        if (tipoTramite === 'junta-vecinal') {
          if (!value.trim()) return 'El RUT del Ministro de Fe es obligatorio.';
          if (!validarRut(value)) return 'El RUT ingresado no es válido.';
        }
        return '';
      case 'motivo':
        if (!value.trim()) return 'El motivo es obligatorio.';
        return '';
      default:
        return '';
    }
  };

  const validateAll = () => {
    // Solo validar los campos que el usuario debe rellenar manualmente.
    // Los campos readonly (rut, nombreCompleto, direccion, comuna) vienen del
    // perfil autenticado y son datos de confianza — no se validan en el frontend.
    const fields = ['motivo'];
    if (tipoTramite === 'salvoconducto') {
      fields.push('direccion'); // el usuario ingresa la dirección destino
    } else if (tipoTramite === 'junta-vecinal') {
      fields.push('rutMinistroDeFe');
    }

    const errors = {};
    let isValid = true;
    fields.forEach((field) => {
      const err = validateField(field, formData[field]);
      if (err) {
        errors[field] = err;
        isValid = false;
      }
    });
    setFieldErrors(errors);
    
    const allTouched = {};
    fields.forEach((f) => (allTouched[f] = true));
    setTouched(allTouched);
    return isValid;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (touched[name]) {
      setFieldErrors((prev) => ({ ...prev, [name]: validateField(name, value) }));
    }
  };

  const handleBlur = (e) => {
    const { name, value } = e.target;
    setTouched((prev) => ({ ...prev, [name]: true }));
    setFieldErrors((prev) => ({ ...prev, [name]: validateField(name, value) }));
  };

  const handleDownloadPdf = async (docId) => {
    try {
      setDownloadStatus('downloading');
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

      const docType = tipoTramite === 'residencia' ? 'certificado_residencia' : tipoTramite === 'salvoconducto' ? 'salvoconducto' : 'acta_junta_vecinal';
      const citizen = cleanString(formData.nombreCompleto);
      const filename = `${docType}_${citizen}_${docId}.pdf`;

      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      setDownloadStatus('completed');
    } catch (err) {
      console.error(err);
      setDownloadStatus('error');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateAll()) {
      setError('Por favor completa todos los campos requeridos antes de continuar.');
      return;
    }
    setError(null);

    // Si el perfil no cargó, re-intentar cargar primero
    if (!profile) {
      setError('No se pudo cargar tu perfil. Por favor, recarga la página o inicia sesión nuevamente.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      let payload;
      let endpoint;

      if (tipoTramite === 'residencia') {
        payload = {
          titulo: 'Certificado de Residencia',
          descripcion: formData.motivo,
          usuarioRut: formData.rut,
          usuarioNombreCompleto: formData.nombreCompleto,
          usuarioId: profile?.id || 1,
          usuarioDireccion: formData.direccion,
          usuarioComuna: formData.comuna
        };
        endpoint = '/documentos/residencia';
      } else if (tipoTramite === 'salvoconducto') {
        payload = {
          titulo: 'Salvoconducto',
          descripcion: formData.motivo,
          usuarioRut: formData.rut,
          usuarioNombreCompleto: formData.nombreCompleto,
          usuarioId: profile?.id || 1,
          direccionDestino: formData.direccion,
          motivo: formData.motivo
        };
        endpoint = '/documentos/salvoconducto';
      } else if (tipoTramite === 'junta-vecinal') {
        payload = {
          titulo: 'Acta Junta Vecinal',
          descripcion: formData.motivo,
          juntaVecinosId: formData.juntaVecinosId ? parseInt(formData.juntaVecinosId) : null,
          nombreJuntaVecinal: formData.nombreJuntaVecinal,
          tipoActa: formData.tipoActa,
          rutMinistroDeFe: formData.rutMinistroDeFe
        };
        endpoint = '/documentos/junta-vecinal';
      }

      const token = localStorage.getItem('token');
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...(token && { 'Authorization': `Bearer ${token}` }) },
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        const data = await response.json();
        setFormData((prev) => ({ ...prev, motivo: '', rutMinistroDeFe: '' }));
        setTouched({});
        setFieldErrors({});
        
        setCreatedDocId(data.id);
        setCreatedDocStatus(data.estado);
        setCreatedDocBlockchainStatus(data.estadoBlockchain);
        
        // Trigger automatic download if enabled in settings
        const autoDownloadPref = localStorage.getItem('autoDownload');
        if (autoDownloadPref !== 'false') {
          handleDownloadPdf(data.id);
        } else {
          setDownloadStatus('idle');
        }
        
        setModalTitle('¡Solicitud Creada con Éxito!');
        setModalType('success');
        setIsModalOpen(true);
      } else {
        setModalTitle('Error');
        setModalType('error');
        setModalContent(<p>Error al generar el documento. Por favor verifica los datos.</p>);
        setIsModalOpen(true);
      }
    } catch (err) {
      setModalTitle('Error de Conexión');
      setModalType('error');
      setModalContent(<p>Ocurrió un error al intentar conectar con el servicio.</p>);
      setIsModalOpen(true);
    } finally {
      setLoading(false);
    }
  };

  const inputClass = (name) => `cu-input ${touched[name] && fieldErrors[name] ? 'input-error' : ''}`;

  return (
    <div className="generar-doc-page">
      <Navbar />
      <main className="container generar-doc-container">
        {loadingProfile ? (
          <div className="loading-profile-box glass-card animate-in" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '40px', minHeight: '300px', width: '100%', maxWidth: '700px' }}>
            <div className="spinner" style={{ border: '4px solid rgba(255,255,255,0.1)', width: '50px', height: '50px', borderRadius: '50%', borderLeftColor: '#3b82f6', animation: 'spin 1s linear infinite', marginBottom: '16px' }}></div>
            <p style={{ color: '#fff', fontSize: '18px' }}>Cargando datos del perfil...</p>
            <style>{`
              @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
              }
            `}</style>
          </div>
        ) : (
          <div className="generar-card glass-card">

            {/* Formulario de solicitud */}
            <form className="generar-form" onSubmit={handleSubmit} noValidate>
              <div className="form-group">
                <label htmlFor="tipoTramite">Tipo de Trámite</label>
                <select id="tipoTramite" value={tipoTramite} onChange={(e) => setTipoTramite(e.target.value)} className="cu-input">
                  <option value="residencia">Certificado de Residencia</option>
                  <option value="salvoconducto">Salvoconducto</option>
                  <option value="junta-vecinal">Acta de Junta de Vecinos</option>
                </select>
              </div>

              {/* Campos del formulario */}
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="rut">RUT del Vecino</label>
                  <input type="text" id="rut" name="rut" value={formData.rut} readOnly className="cu-input input-readonly" />
                </div>
                <div className="form-group">
                  <label htmlFor="nombreCompleto">Nombre Completo</label>
                  <input type="text" id="nombreCompleto" name="nombreCompleto" value={formData.nombreCompleto} readOnly className="cu-input input-readonly" />
                </div>
              </div>

              {tipoTramite === 'junta-vecinal' ? (
                <>
                  <div className="form-row">
                    <div className="form-group">
                      <label htmlFor="nombreJuntaVecinal">Junta de Vecinos</label>
                      <input type="text" id="nombreJuntaVecinal" name="nombreJuntaVecinal" value={formData.nombreJuntaVecinal} readOnly className="cu-input input-readonly" />
                    </div>
                    <div className="form-group">
                      <label htmlFor="juntaVecinosId">ID Registro</label>
                      <input type="text" id="juntaVecinosId" name="juntaVecinosId" value={formData.juntaVecinosId} readOnly className="cu-input input-readonly" />
                    </div>
                  </div>
                  <div className="form-row">
                    <div className="form-group">
                      <label htmlFor="tipoActa">Tipo de Acta</label>
                      <select id="tipoActa" name="tipoActa" value={formData.tipoActa} onChange={handleChange} className="cu-input">
                        <option value="ACTA_ASAMBLEA">Acta de Asamblea</option>
                        <option value="ACTA_CONSTITUCION">Acta de Constitución</option>
                        <option value="ESTATUTOS">Estatutos</option>
                        <option value="ACTA_ELECCION">Acta de Elección</option>
                        <option value="NOMINA_SOCIOS">Nómina de Socios</option>
                        <option value="CERTIFICADO_ANTECEDENTES">Certificado de Antecedentes</option>
                        <option value="OTRO">Otro</option>
                      </select>
                    </div>
                    <div className="form-group">
                      <label htmlFor="rutMinistroDeFe">RUT Ministro de Fe</label>
                      <input 
                        type="text" 
                        id="rutMinistroDeFe" 
                        name="rutMinistroDeFe" 
                        value={formData.rutMinistroDeFe} 
                        onChange={handleChange} 
                        onBlur={handleBlur} 
                        placeholder="Ej: 12.345.678-9"
                        className={inputClass('rutMinistroDeFe')} 
                      />
                      {touched.rutMinistroDeFe && fieldErrors.rutMinistroDeFe && (
                        <span className="field-error">{fieldErrors.rutMinistroDeFe}</span>
                      )}
                    </div>
                  </div>
                </>
              ) : (
                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="direccion">
                      {tipoTramite === 'residencia' ? 'Dirección de Residencia' : 'Dirección Destino'}
                    </label>
                    <input 
                      type="text" 
                      id="direccion" 
                      name="direccion" 
                      value={formData.direccion} 
                      onChange={handleChange} 
                      onBlur={handleBlur} 
                      readOnly={tipoTramite === 'residencia'} 
                      className={tipoTramite === 'residencia' ? 'cu-input input-readonly' : inputClass('direccion')} 
                    />
                    {tipoTramite !== 'residencia' && touched.direccion && fieldErrors.direccion && (
                      <span className="field-error">{fieldErrors.direccion}</span>
                    )}
                  </div>
                  {tipoTramite === 'residencia' && (
                    <div className="form-group">
                      <label htmlFor="comuna">Comuna</label>
                      <input type="text" id="comuna" name="comuna" value={formData.comuna} readOnly className="cu-input input-readonly" />
                    </div>
                  )}
                </div>
              )}

              <div className="form-group">
                <label htmlFor="motivo">Motivo del Trámite</label>
                <textarea id="motivo" name="motivo" value={formData.motivo} onChange={handleChange} onBlur={handleBlur} className={inputClass('motivo')} rows="4" placeholder="Describa el motivo por el cual solicita este documento..." />
                {touched.motivo && fieldErrors.motivo && <span className="field-error">{fieldErrors.motivo}</span>}
              </div>
              
              {/* Error de envío */}
              {error && (
                <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', color: '#f87171', padding: '0.75rem 1rem', borderRadius: '0.5rem', fontSize: '0.9rem', marginBottom: '1rem' }}>
                  {error}
                </div>
              )}

              {/* Botón de envío */}
              <button type="submit" className="btn btn-primary btn-submit" disabled={loading}>
                {loading ? 'Enviando...' : 'Solicitar Documento'}
              </button>
            </form>
          </div>
        )}
      </main>

      {/* Modal para feedback visual */}
      <Modal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        title={modalTitle} 
        type={modalType}
      >
        {modalType === 'success' ? (
          <div className="modal-success-content">
            <p>El documento ha sido generado exitosamente. <strong>La descarga del archivo PDF comenzará automáticamente.</strong></p>
            
            {/* Indicador visual de estado de descarga */}
            <div className={`download-status-indicator status-${downloadStatus}`}>
              {downloadStatus === 'downloading' && (
                <>
                  <span className="spinner-mini"></span>
                  <span>Descargando archivo PDF...</span>
                </>
              )}
              {downloadStatus === 'completed' && (
                <span>Descarga completada con exito.</span>
              )}
              {downloadStatus === 'error' && (
                <span>Error al descargar. Usa el boton de abajo.</span>
              )}
              {downloadStatus === 'idle' && (
                <span>Descarga automatica desactivada.</span>
              )}
            </div>

            <div style={{ marginTop: '16px', background: '#f3f4f6', padding: '16px', borderRadius: '8px', textAlign: 'left', color: '#1f2937' }}>
              <h4 style={{ margin: '0 0 12px 0', borderBottom: '1px solid #e5e7eb', paddingBottom: '6px', fontSize: '15px', fontWeight: 'bold' }}>
                Resumen del Documento
              </h4>
              <div style={{ display: 'grid', gridTemplateColumns: '140px 1fr', gap: '6px', fontSize: '13px', lineHeight: '1.4' }}>
                <div><strong>Trámite:</strong></div>
                <div>{tipoTramite === 'residencia' ? 'Certificado de Residencia' : tipoTramite === 'salvoconducto' ? 'Salvoconducto' : 'Acta de Junta de Vecinos'}</div>
                
                <div><strong>Solicitante:</strong></div>
                <div>{formData.nombreCompleto}</div>
                
                <div><strong>RUT:</strong></div>
                <div>{formData.rut}</div>

                {tipoTramite === 'residencia' && (
                  <>
                    <div><strong>Dirección:</strong></div>
                    <div>{formData.direccion}</div>
                    <div><strong>Comuna:</strong></div>
                    <div>{formData.comuna}</div>
                  </>
                )}

                {tipoTramite === 'salvoconducto' && (
                  <>
                    <div><strong>Destino:</strong></div>
                    <div>{formData.direccion}</div>
                  </>
                )}

                {tipoTramite === 'junta-vecinal' && (
                  <>
                    <div><strong>Junta de Vecinos:</strong></div>
                    <div>{formData.nombreJuntaVecinal} (ID: {formData.juntaVecinosId})</div>
                    <div><strong>Tipo de Acta:</strong></div>
                    <div>{formData.tipoActa}</div>
                    <div><strong>Ministro de Fe:</strong></div>
                    <div>{formData.rutMinistroDeFe}</div>
                  </>
                )}

                <div style={{ gridColumn: '1 / span 2', borderTop: '1px solid #e5e7eb', marginTop: '6px', paddingTop: '6px' }}></div>

                <div><strong>ID Documento:</strong></div>
                <div>{createdDocId}</div>
                
                <div><strong>Estado:</strong></div>
                <div>{createdDocStatus}</div>
                
                <div><strong>Blockchain:</strong></div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <span style={{
                    display: 'inline-block',
                    padding: '2px 8px',
                    borderRadius: '4px',
                    fontSize: '12px',
                    fontWeight: '600',
                    background: createdDocBlockchainStatus === 'CONFIRMADO' ? 'rgba(16,185,129,0.15)' : 'rgba(245,158,11,0.15)',
                    color: createdDocBlockchainStatus === 'CONFIRMADO' ? '#34d399' : '#fcd34d',
                    border: createdDocBlockchainStatus === 'CONFIRMADO' ? '1px solid rgba(16,185,129,0.3)' : '1px solid rgba(245,158,11,0.3)'
                  }}>
                    {createdDocBlockchainStatus || 'PROCESANDO'}
                  </span>
                  {createdDocBlockchainStatus !== 'CONFIRMADO' && (
                    <span style={{ fontSize: '11px', color: '#9ca3af' }}>
                      (verificar en Mis Documentos)
                    </span>
                  )}
                </div>
              </div>
            </div>
            <div style={{ marginTop: '20px', display: 'flex', gap: '10px' }}>
              <button 
                onClick={() => handleDownloadPdf(createdDocId)} 
                className="btn btn-secondary" 
                style={{ flex: 1, padding: '10px', fontSize: '14px', cursor: 'pointer' }}
                disabled={downloadStatus === 'downloading'}
              >
                {downloadStatus === 'downloading' ? 'Descargando...' : 'Reintentar Descarga'}
              </button>
              <button 
                onClick={() => navigate('/perfil')} 
                className="btn btn-primary" 
                style={{ flex: 1, padding: '10px', fontSize: '14px', cursor: 'pointer' }}
              >
                Ir a Mis Documentos
              </button>
            </div>
          </div>
        ) : (
          modalContent
        )}
      </Modal>

      <Footer />
    </div>
  );
};

export default GenerarDocumento;