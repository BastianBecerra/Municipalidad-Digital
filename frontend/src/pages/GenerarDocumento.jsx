import { useState } from 'react';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import './GenerarDocumento.css';

// Validador de RUT chileno (formato XX.XXX.XXX-X o XXXXXXXX-X)
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
  const [tipoTramite, setTipoTramite] = useState('residencia');
  const [formData, setFormData] = useState({
    rut: '',
    nombreCompleto: '',
    direccion: '',
    comuna: '',
    motivo: '',
  });
  const [fieldErrors, setFieldErrors] = useState({});
  const [touched, setTouched] = useState({});
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  // Validar un campo individual
  const validateField = (name, value) => {
    switch (name) {
      case 'rut':
        if (!value.trim()) return 'El RUT es obligatorio.';
        if (!validarRut(value)) return 'El RUT ingresado no es válido. Usa el formato 12.345.678-9';
        return '';
      case 'nombreCompleto':
        if (!value.trim()) return 'El nombre es obligatorio.';
        if (value.trim().length < 3) return 'El nombre debe tener al menos 3 caracteres.';
        return '';
      case 'direccion':
        if (!value.trim()) return 'La dirección es obligatoria.';
        if (value.trim().length < 5) return 'La dirección debe tener al menos 5 caracteres.';
        return '';
      case 'comuna':
        if (tipoTramite === 'residencia') {
          if (!value.trim()) return 'La comuna es obligatoria.';
          if (value.trim().length < 3) return 'La comuna debe tener al menos 3 caracteres.';
        }
        return '';
      case 'motivo':
        if (!value.trim()) return 'El motivo es obligatorio.';
        if (value.trim().length < 10) return 'Describe con más detalle el motivo (mínimo 10 caracteres).';
        return '';
      default:
        return '';
    }
  };

  // Validar todos los campos y devolver si el formulario es válido
  const validateAll = () => {
    const fields = ['rut', 'nombreCompleto', 'direccion', 'motivo'];
    if (tipoTramite === 'residencia') fields.push('comuna');

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
    // Marcar todos como tocados para mostrar los errores
    const allTouched = {};
    fields.forEach((f) => (allTouched[f] = true));
    setTouched(allTouched);
    return isValid;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));

    // Si el campo ya fue tocado, revalidar en tiempo real
    if (touched[name]) {
      const err = validateField(name, value);
      setFieldErrors((prev) => ({ ...prev, [name]: err }));
    }
  };

  const handleBlur = (e) => {
    const { name, value } = e.target;
    setTouched((prev) => ({ ...prev, [name]: true }));
    const err = validateField(name, value);
    setFieldErrors((prev) => ({ ...prev, [name]: err }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validar antes de enviar
    if (!validateAll()) return;

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      let payload = {};
      let endpoint = '';

      if (tipoTramite === 'residencia') {
        endpoint = '/documentos/residencia?isSimple=true';
        payload = {
          titulo: 'Certificado de Residencia',
          descripcion: formData.motivo,
          usuarioRut: formData.rut,
          usuarioNombreCompleto: formData.nombreCompleto,
          usuarioDireccion: formData.direccion,
          usuarioComuna: formData.comuna,
          usuarioId: 1,
        };
      } else if (tipoTramite === 'salvoconducto') {
        endpoint = '/documentos/salvoconducto?isSimple=true';
        payload = {
          titulo: 'Salvoconducto',
          descripcion: formData.motivo,
          motivo: formData.motivo,
          usuarioRut: formData.rut,
          usuarioNombreCompleto: formData.nombreCompleto,
          direccionDestino: formData.direccion,
          usuarioId: 1,
        };
      }

      const token = localStorage.getItem('token');
      const headers = { 'Content-Type': 'application/json' };
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      const response = await fetch(endpoint, {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        const data = await response.json();
        setResult({
          message: 'Documento generado exitosamente como BORRADOR.',
          docId: data.id,
          estado: data.estado
        });
        setFormData({ rut: '', nombreCompleto: '', direccion: '', comuna: '', motivo: '' });
        setFieldErrors({});
        setTouched({});
      } else {
        const errorData = await response.json().catch(() => ({}));
        setError(errorData.message || 'Error al generar el documento. Verifica tus permisos o datos.');
      }
    } catch (err) {
      setError('Error de red. No se pudo contactar al servidor.');
    } finally {
      setLoading(false);
    }
  };

  // Helper para clases de input con error
  const inputClass = (name) =>
    `cu-input ${touched[name] && fieldErrors[name] ? 'input-error' : ''}`;

  return (
    <div className="generar-doc-page">
      <Navbar />
      <main className="container generar-doc-container">
        <div className="generar-card glass-card">
          <div className="generar-header">
            <span className="icon">📝</span>
            <h1>Generar Solicitud</h1>
            <p>Ingresa tus datos personales y describe el motivo por el cual necesitas este documento.</p>
          </div>

          <form className="generar-form" onSubmit={handleSubmit} noValidate>
            <div className="form-group">
              <label>Tipo de Trámite</label>
              <select
                value={tipoTramite}
                onChange={(e) => setTipoTramite(e.target.value)}
                className="cu-input"
              >
                <option value="residencia">Certificado de Residencia</option>
                <option value="salvoconducto">Salvoconducto</option>
              </select>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>RUT del Vecino</label>
                <input
                  type="text"
                  name="rut"
                  value={formData.rut}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  placeholder="Ej: 12.345.678-9"
                  className={inputClass('rut')}
                />
                {touched.rut && fieldErrors.rut && (
                  <span className="field-error">{fieldErrors.rut}</span>
                )}
              </div>
              <div className="form-group">
                <label>Nombre Completo</label>
                <input
                  type="text"
                  name="nombreCompleto"
                  value={formData.nombreCompleto}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  placeholder="Ej: Juan Pérez"
                  className={inputClass('nombreCompleto')}
                />
                {touched.nombreCompleto && fieldErrors.nombreCompleto && (
                  <span className="field-error">{fieldErrors.nombreCompleto}</span>
                )}
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>{tipoTramite === 'residencia' ? 'Dirección de Residencia' : 'Dirección de Destino'}</label>
                <input
                  type="text"
                  name="direccion"
                  value={formData.direccion}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  placeholder="Ej: Calle Las Palmas 123"
                  className={inputClass('direccion')}
                />
                {touched.direccion && fieldErrors.direccion && (
                  <span className="field-error">{fieldErrors.direccion}</span>
                )}
              </div>
              {tipoTramite === 'residencia' && (
                <div className="form-group">
                  <label>Comuna</label>
                  <input
                    type="text"
                    name="comuna"
                    value={formData.comuna}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Ej: Providencia"
                    className={inputClass('comuna')}
                  />
                  {touched.comuna && fieldErrors.comuna && (
                    <span className="field-error">{fieldErrors.comuna}</span>
                  )}
                </div>
              )}
            </div>

            <div className="form-group">
              <label>Descripción / Motivo</label>
              <textarea
                name="motivo"
                rows="4"
                value={formData.motivo}
                onChange={handleChange}
                onBlur={handleBlur}
                placeholder="Por favor explica detalladamente para qué necesitas este documento..."
                className={inputClass('motivo')}
              ></textarea>
              {touched.motivo && fieldErrors.motivo ? (
                <span className="field-error">{fieldErrors.motivo}</span>
              ) : (
                <small className="help-text">Esta información será revisada por un funcionario municipal.</small>
              )}
            </div>

            <button type="submit" className="btn btn-primary btn-submit" disabled={loading}>
              {loading ? 'Enviando solicitud...' : 'Solicitar Documento'}
            </button>
          </form>

          {error && (
            <div className="status-box error-box animate-in mt-4">
              <span className="status-icon">⚠️</span>
              <p>{error}</p>
            </div>
          )}

          {result && (
            <div className="status-box success-box animate-in mt-4">
              <span className="status-icon">✅</span>
              <div>
                <h3>¡Solicitud Recibida!</h3>
                <p>{result.message}</p>
                <p><strong>ID Documento:</strong> {result.docId} | <strong>Estado:</strong> {result.estado}</p>
              </div>
            </div>
          )}
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default GenerarDocumento;
