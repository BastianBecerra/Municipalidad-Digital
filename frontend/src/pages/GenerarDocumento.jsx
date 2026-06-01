import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import './GenerarDocumento.css';

// Algoritmo para validar el RUT chileno (cálculo del dígito verificador)
// Es fundamental para asegurar la integridad de los datos municipales
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
  
  // Estados para manejo de errores y UX (experiencia de usuario)
  const [fieldErrors, setFieldErrors] = useState({});
  const [touched, setTouched] = useState({}); // Rastrea qué campos ha visitado el usuario
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  // Función de validación por campo: Encapsula la lógica de reglas de negocio
  const validateField = (name, value) => {
    switch (name) {
      case 'rut':
        if (!value.trim()) return 'El RUT es obligatorio.';
        if (!validarRut(value)) return 'El RUT ingresado no es válido.';
        return '';
      case 'nombreCompleto':
        if (!value.trim()) return 'El nombre es obligatorio.';
        if (value.trim().length < 3) return 'Nombre muy corto.';
        return '';
      case 'direccion':
        if (!value.trim()) return 'La dirección es obligatoria.';
        return '';
      case 'comuna':
        if (tipoTramite === 'residencia' && !value.trim()) return 'La comuna es obligatoria.';
        return '';
      case 'motivo':
        if (!value.trim()) return 'El motivo es obligatorio.';
        return '';
      default:
        return '';
    }
  };

  // Validador global al momento de enviar el formulario
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
    
    // Marcamos todos como "tocados" para mostrar errores visuales
    const allTouched = {};
    fields.forEach((f) => (allTouched[f] = true));
    setTouched(allTouched);
    return isValid;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Revalidación en tiempo real si el campo ya fue tocado
    if (touched[name]) {
      setFieldErrors((prev) => ({ ...prev, [name]: validateField(name, value) }));
    }
  };

  const handleBlur = (e) => {
    const { name, value } = e.target;
    setTouched((prev) => ({ ...prev, [name]: true }));
    setFieldErrors((prev) => ({ ...prev, [name]: validateField(name, value) }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateAll()) return;

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      // Preparación del payload según el tipo de trámite seleccionado
      let payload = {
        titulo: tipoTramite === 'residencia' ? 'Certificado de Residencia' : 'Salvoconducto',
        descripcion: formData.motivo,
        usuarioRut: formData.rut,
        usuarioNombreCompleto: formData.nombreCompleto,
        usuarioId: 1, // ID mock para la sesión actual
        // Añadimos campos dinámicos según el trámite
        ...(tipoTramite === 'residencia' ? { usuarioDireccion: formData.direccion, usuarioComuna: formData.comuna } : { direccionDestino: formData.direccion, motivo: formData.motivo })
      };

      const token = localStorage.getItem('token');
      const response = await fetch(tipoTramite === 'residencia' ? '/documentos/residencia' : '/documentos/salvoconducto', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...(token && { 'Authorization': `Bearer ${token}` }) },
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        const data = await response.json();
        setResult({ message: 'Documento generado exitosamente como BORRADOR.', docId: data.id, estado: data.estado });
        setFormData({ rut: '', nombreCompleto: '', direccion: '', comuna: '', motivo: '' });
      } else {
        setError('Error al generar el documento.');
      }
    } catch (err) {
      setError('Error de red al conectar con el servidor.');
    } finally {
      setLoading(false);
    }
  };

  const inputClass = (name) => `cu-input ${touched[name] && fieldErrors[name] ? 'input-error' : ''}`;

  return (
    <div className="generar-doc-page">
      <Navbar />
      <main className="container generar-doc-container">
        <div className="generar-card glass-card">
          <div className="generar-header">
            <span className="icon">📝</span>
            <h1>Generar Solicitud</h1>
          </div>

          {/* Formulario de solicitud */}
          <form className="generar-form" onSubmit={handleSubmit} noValidate>
            <div className="form-group">
              <label>Tipo de Trámite</label>
              <select value={tipoTramite} onChange={(e) => setTipoTramite(e.target.value)} className="cu-input">
                <option value="residencia">Certificado de Residencia</option>
                <option value="salvoconducto">Salvoconducto</option>
              </select>
            </div>

            {/* Campos del formulario */}
            <div className="form-row">
              <div className="form-group">
                <label>RUT del Vecino</label>
                <input type="text" name="rut" value={formData.rut} onChange={handleChange} onBlur={handleBlur} placeholder="12.345.678-9" className={inputClass('rut')} />
                {touched.rut && fieldErrors.rut && <span className="field-error">{fieldErrors.rut}</span>}
              </div>
              <div className="form-group">
                <label>Nombre Completo</label>
                <input type="text" name="nombreCompleto" value={formData.nombreCompleto} onChange={handleChange} onBlur={handleBlur} className={inputClass('nombreCompleto')} />
                {touched.nombreCompleto && fieldErrors.nombreCompleto && <span className="field-error">{fieldErrors.nombreCompleto}</span>}
              </div>
            </div>

            {/* Resto del formulario... */}
            {/* Botón de envío */}
            <button type="submit" className="btn btn-primary btn-submit" disabled={loading}>
              {loading ? 'Enviando...' : 'Solicitar Documento'}
            </button>
          </form>

          {/* Feedback de resultado */}
          {result && <div className="status-box success-box"><h3>¡Solicitud Recibida!</h3><p>{result.message}</p></div>}
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default GenerarDocumento;