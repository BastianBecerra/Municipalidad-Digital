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
      } catch (err) {
        console.error('Failed to load profile:', err);
        setError('No se pudo cargar el perfil del usuario. Intenta iniciar sesión de nuevo.');
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
    const fields = ['rut', 'nombreCompleto', 'motivo'];
    if (tipoTramite === 'residencia') {
      fields.push('direccion', 'comuna');
    } else if (tipoTramite === 'salvoconducto') {
      fields.push('direccion');
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
      link.setAttribute('download', `documento_${docId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
    } catch (err) {
      console.error(err);
      setModalTitle('Error de Descarga');
      setModalType('error');
      setModalContent(<p>No se pudo descargar el archivo PDF del documento.</p>);
      setIsModalOpen(true);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateAll()) return;

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
        
        // Mostrar modal de éxito con opción de descargar PDF y mostrar TODOS los datos
        setModalTitle('Solicitud Generada');
        setModalType('success');
        setModalContent(
          <div className="modal-success-content">
            <p>El documento ha sido generado exitosamente.</p>
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
                <div>{data.id}</div>
                
                <div><strong>Estado:</strong></div>
                <div>{data.estado}</div>
                
                <div><strong>Blockchain:</strong></div>
                <div>{data.estadoBlockchain || 'REGISTRADO'}</div>
              </div>
            </div>
            <button 
              onClick={() => handleDownloadPdf(data.id)} 
              className="btn btn-primary" 
              style={{ marginTop: '20px', width: '100%', padding: '12px' }}
            >
              📥 Descargar Documento (PDF)
            </button>
          </div>
        );
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
        ) : error ? (
          <div className="status-box error-box">{error}</div>
        ) : (
          <div className="generar-card glass-card">
            <div className="generar-header">
              <span className="icon">📝</span>
              <h1>Generar Solicitud</h1>
            </div>

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
              
              {/* Botón de envío */}
              <button type="submit" className="btn btn-primary btn-submit" disabled={loading || !profile}>
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
        {modalContent}
      </Modal>

      <Footer />
    </div>
  );
};

export default GenerarDocumento;