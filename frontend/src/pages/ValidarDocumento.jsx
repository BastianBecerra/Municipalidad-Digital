import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Html5Qrcode } from 'html5-qrcode';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import './ValidarDocumento.css';

// Función para renderizar la primera página de un PDF a un Canvas usando PDF.js
const renderPdfPageToCanvas = async (file) => {
  const arrayBuffer = await file.arrayBuffer();
  const pdfjsLib = window.pdfjsLib;
  if (!pdfjsLib) {
    throw new Error('La biblioteca PDF.js no se cargó correctamente.');
  }
  // Configurar el worker de PDF.js
  pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.4.120/pdf.worker.min.js';
  const pdf = await pdfjsLib.getDocument({ data: arrayBuffer }).promise;
  const page = await pdf.getPage(1);
  
  const viewport = page.getViewport({ scale: 2.0 }); // Escala 2.0 para mejorar la nitidez del QR
  const canvas = document.createElement('canvas');
  const context = canvas.getContext('2d');
  canvas.height = viewport.height;
  canvas.width = viewport.width;
  
  await page.render({ canvasContext: context, viewport: viewport }).promise;
  return canvas;
};

const ValidarDocumento = () => {
  const { hash: urlHash } = useParams(); // Obtenemos el hash desde la URL si existe
  const navigate = useNavigate();

  // Estados de control para la UI
  const [authorized, setAuthorized] = useState(null); // null = cargando, false = denegado, true = autorizado
  const [scanning, setScanning] = useState(false); // ¿Está la cámara activa?
  const [loading, setLoading] = useState(false);   // ¿Estamos consultando la API/Blockchain?
  const [result, setResult] = useState(null);      // Almacena la respuesta del validador
  const [error, setError] = useState(null);        // Manejo de errores
  const [cameras, setCameras] = useState([]);      // Lista de cámaras disponibles
  const [activeCameraId, setActiveCameraId] = useState(''); // Cámara activa actual
  
  const html5QrCodeRef = useRef(null); // Referencia persistente para la instancia del escáner

  // EFECTO: Verificar permisos del usuario al cargar
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }

    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      const decoded = JSON.parse(jsonPayload);
      
      if (decoded.rol === 'ADMIN' || decoded.rol === 'FUNCIONARIO') {
        setAuthorized(true);
      } else {
        setAuthorized(false);
      }
    } catch (e) {
      console.error(e);
      setAuthorized(false);
    }
  }, [navigate]);

  // EFECTO: Si el usuario entra directamente con un hash en la URL, validar sin escanear
  useEffect(() => {
    const sha256Regex = /^[a-fA-F0-9]{64}$/; // Validación de formato de hash
    if (authorized && urlHash && sha256Regex.test(urlHash)) {
      performValidation(urlHash);
    }
  }, [urlHash, authorized]);

  // EFECTO: Limpiar escáner al desmontar el componente (seguridad)
  useEffect(() => {
    return () => { stopScanning(); };
  }, []);

  // Lógica principal de consulta al Backend/Blockchain
  const performValidation = async (hashToValidate) => {
    setLoading(true);
    setError(null);
    setResult(null);
    stopScanning();

    try {
      const token = localStorage.getItem('token');
      // Llamada al endpoint de validación
      const response = await fetch(`/api/validacion/public/validar/${hashToValidate}`, {
        headers: {
          ...(token && { 'Authorization': `Bearer ${token}` })
        }
      });
      const data = await response.json();
      setResult(data);
    } catch (err) {
      setError('Error al conectar con el servicio de validación. Por favor intenta más tarde.');
    } finally {
      setLoading(false);
    }
  };

  // Inicialización de la cámara
  const startScanning = async () => {
    setResult(null);
    setError(null);
    setScanning(true);

    try {
      const devices = await Html5Qrcode.getCameras();
      setCameras(devices);

      if (devices.length > 0) {
        const defaultCam = devices.find(device => device.label.toLowerCase().includes('back')) || devices[0];
        setActiveCameraId(defaultCam.id);
        initCamera(defaultCam.id);
      } else {
        setError('No se encontraron cámaras en este dispositivo.');
        setScanning(false);
      }
    } catch (err) {
      setError('No se pudo acceder a la cámara. Por favor otorga permisos.');
      setScanning(false);
    }
  };

  const initCamera = (cameraId) => {
    if (html5QrCodeRef.current) {
      html5QrCodeRef.current.stop().then(() => startQrInstance(cameraId));
    } else {
      startQrInstance(cameraId);
    }
  };

  // Configuración de la librería de escaneo QR
  const startQrInstance = (cameraId) => {
    const html5QrCode = new Html5Qrcode("qr-reader-view");
    html5QrCodeRef.current = html5QrCode;

    html5QrCode.start(
      cameraId,
      { fps: 15, qrbox: { width: 250, height: 250 } },
      (qrCodeMessage) => {
        let hash = qrCodeMessage.includes('/validar/') ? qrCodeMessage.substring(qrCodeMessage.lastIndexOf('/') + 1) : qrCodeMessage;
        hash = hash.trim().toLowerCase();
        
        if (/^[a-fA-F0-9]{64}$/.test(hash)) {
          navigate(`/validar/${hash}`);
        } else {
          setError('El código QR no contiene un formato de validación válido.');
          stopScanning();
        }
      },
      () => { /* Ignorar errores de lectura continua */ }
    );
  };

  const stopScanning = () => {
    if (html5QrCodeRef.current && html5QrCodeRef.current.isScanning) {
      html5QrCodeRef.current.stop().then(() => setScanning(false));
    } else {
      setScanning(false);
    }
  };

  // Alternativa: Subir archivo de imagen o PDF para decodificar QR
  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      let scanSource = file;

      // Si es un archivo PDF, procesar con PDF.js
      if (file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf')) {
        const canvas = await renderPdfPageToCanvas(file);
        scanSource = await new Promise((resolve) => {
          canvas.toBlob((blob) => {
            resolve(new File([blob], 'document_page.png', { type: 'image/png' }));
          }, 'image/png');
        });
      }

      const html5QrCode = new Html5Qrcode("qr-reader-view-hidden");
      const qrCodeMessage = await html5QrCode.scanFile(scanSource, true);
      
      let hash = qrCodeMessage.includes('/validar/') ? qrCodeMessage.substring(qrCodeMessage.lastIndexOf('/') + 1) : qrCodeMessage;
      hash = hash.trim().toLowerCase();

      if (/^[a-fA-F0-9]{64}$/.test(hash)) {
        navigate(`/validar/${hash}`);
      } else {
        setError('El código QR decodificado no contiene un formato de hash válido.');
        setLoading(false);
      }
    } catch (err) {
      console.error(err);
      setError('No se pudo decodificar el archivo. Asegúrate de que el documento o PDF contenga un código QR nítido.');
      setLoading(false);
    }
  };

  const resetAll = () => { navigate('/validar'); setResult(null); setError(null); };

  // --- RENDERIZADOS SEGÚN ESTADO DE AUTORIZACIÓN ---

  if (authorized === null) {
    return (
      <div className="validar-page">
        <Navbar />
        <main className="container validar-container text-center" style={{ paddingTop: '150px' }}>
          <div className="spinner"></div>
          <p>Verificando credenciales de acceso...</p>
        </main>
        <Footer />
      </div>
    );
  }

  if (authorized === false) {
    return (
      <div className="validar-page">
        <Navbar />
        <main className="container validar-container" style={{ paddingTop: '100px' }}>
          <div className="validar-header text-center">
            <div className="badge-digital" style={{ backgroundColor: '#ef4444' }}>Acceso Restringido</div>
            <h1 style={{ color: '#ef4444' }}>Acceso Denegado</h1>
          </div>
          <div className="validar-body glass-card text-center" style={{ padding: '40px' }}>
            <span style={{ fontSize: '64px', display: 'block', marginBottom: '20px' }}>🚫</span>
            <h3>No tienes permisos para validar documentos</h3>
            <p style={{ color: '#666', marginTop: '10px', marginBottom: '30px' }}>
              La validación oficial de certificados municipales y sellado en Blockchain está restringida para Administradores y Funcionarios de la plataforma.
            </p>
            <button onClick={() => navigate('/')} className="btn btn-primary" style={{ padding: '12px 24px' }}>
              Volver al Inicio
            </button>
          </div>
        </main>
        <Footer />
      </div>
    );
  }

  return (
    <div className="validar-page">
      <Navbar />
      <main className="container validar-container animate-fade-in">
        
        <div className="validar-header text-center">
          <div className="badge-digital">Sello Digital Municipal</div>
          <h1>Validador Oficial de Documentos</h1>
        </div>

        <div className="validar-body glass-card">
          {/* Contenedor oculto para la decodificación de archivos */}
          <div id="qr-reader-view-hidden" style={{ display: 'none' }}></div>
          
          {/* ESTADO 1: Cargando */}
          {loading && (
            <div className="validation-loader">
              <div className="spinner"></div>
              <h3>Procesando y Consultando en Blockchain...</h3>
            </div>
          )}

          {/* ESTADO 2: Error */}
          {!loading && error && (
            <div className="validation-alert alert-danger" style={{ padding: '24px', textAlign: 'center' }}>
              <p style={{ fontSize: '16px', marginBottom: '16px' }}>{error}</p>
              <button onClick={resetAll} className="btn btn-primary">Volver a intentar</button>
            </div>
          )}

          {/* ESTADO 3: Cámara Activa */}
          {!loading && !error && scanning && (
            <div className="scanner-section" style={{ textAlign: 'center' }}>
              <div id="qr-reader-view" style={{ maxWidth: '400px', margin: '0 auto 20px auto' }}></div>
              <button onClick={stopScanning} className="btn btn-secondary">🛑 Detener Cámara</button>
            </div>
          )}

          {/* ESTADO 4: Menú inicial (cámara o archivo) */}
          {!loading && !error && !scanning && !result && (
            <div className="init-menu text-center">
              <h3>Selecciona el método de validación</h3>
              <div className="menu-actions" style={{ display: 'flex', gap: '20px', justifyContent: 'center', marginTop: '20px' }}>
                <button onClick={startScanning} className="btn btn-primary" style={{ padding: '14px 28px' }}>
                  📷 Escanear con Cámara
                </button>
                <label className="btn btn-secondary" style={{ padding: '14px 28px', cursor: 'pointer', display: 'inline-block' }}>
                  📁 Cargar Documento (PDF o Imagen)
                  <input type="file" accept="image/*,application/pdf" onChange={handleFileUpload} style={{ display: 'none' }} />
                </label>
              </div>
            </div>
          )}

          {/* ESTADO 5: Resultado (Si es válido o inválido) */}
          {!loading && !error && result && (
            <div className="result-section animate-fade-in">

              {/* Badge de estado centrado */}
              <div className="result-status-center">
                <div className={`result-status-badge ${result.valido ? 'badge-valid' : 'badge-invalid'}`}>
                  <span className="badge-icon">{result.valido ? '✅' : '❌'}</span>
                  <span>{result.valido ? 'DOCUMENTO AUTÉNTICO' : 'DOCUMENTO NO VÁLIDO'}</span>
                </div>
                <p className="result-status-msg">{result.motivo}</p>
              </div>

              {/* Grid de tarjetas de datos */}
              <div className={`result-data-grid ${!result.blockchain ? 'single-col' : ''}`}>

                {/* Tarjeta: Datos del Documento */}
                {result.documento && (
                  <div className="result-data-card glass-card">
                    <div className="rdc-header">
                      <span className="rdc-icon">📋</span>
                      <h3>Datos del Documento</h3>
                    </div>
                    <div className="rdc-divider" />
                    <div className="rdc-rows">
                      <div className="rdc-row">
                        <span className="rdc-label">Título:</span>
                        <span className="rdc-value">{result.documento.titulo}</span>
                      </div>
                      <div className="rdc-row">
                        <span className="rdc-label">Tipo:</span>
                        <span className="rdc-value rdc-accent">{result.documento.tipo || 'Documento Municipal'}</span>
                      </div>
                      <div className="rdc-row">
                        <span className="rdc-label">Titular:</span>
                        <span className="rdc-value">{result.documento.usuarioNombreCompleto}</span>
                      </div>
                      <div className="rdc-row">
                        <span className="rdc-label">RUT Titular:</span>
                        <span className="rdc-value">{result.documento.usuarioRut}</span>
                      </div>
                      {result.documento.usuarioDireccion && (
                        <div className="rdc-row">
                          <span className="rdc-label">Domicilio:</span>
                          <span className="rdc-value">{result.documento.usuarioDireccion}</span>
                        </div>
                      )}
                      {result.documento.firmaDigital && (
                        <div className="rdc-row">
                          <span className="rdc-label">Firma Digital:</span>
                          <span className="rdc-value rdc-mono">{result.documento.firmaDigital.substring(0, 22)}...</span>
                        </div>
                      )}
                      <div className="rdc-row">
                        <span className="rdc-label">Firmado Por:</span>
                        <span className="rdc-value rdc-mono">{result.documento.firmadoPor || 'SISTEMA_MUNICIPAL_AUTOMATICO'}</span>
                      </div>
                    </div>
                  </div>
                )}

                {/* Tarjeta: Registro Blockchain */}
                {result.blockchain && (
                  <div className="result-data-card glass-card">
                    <div className="rdc-header">
                      <span className="rdc-icon">⛓️</span>
                      <h3>Registro Criptográfico (Blockchain)</h3>
                    </div>
                    <div className="rdc-divider" />

                    <div className="blockchain-seal-badge">
                      <span>🔵 SELLADO EN RED ETHEREUM</span>
                    </div>

                    <div className="rdc-rows">
                      {result.blockchain.hash && (
                        <div className="rdc-row rdc-row-col">
                          <span className="rdc-label">Hash Documento:</span>
                          <span className="rdc-value rdc-mono rdc-hash">{result.blockchain.hash}</span>
                        </div>
                      )}
                      {result.blockchain.timestamp && (
                        <div className="rdc-row">
                          <span className="rdc-label">Bloque Sellado:</span>
                          <span className="rdc-value">{new Date(parseInt(result.blockchain.timestamp) * 1000).toLocaleString('es-CL')}</span>
                        </div>
                      )}
                      {result.blockchain.registradoPor && (
                        <div className="rdc-row rdc-row-col">
                          <span className="rdc-label">Autoridad Registradora:</span>
                          <span className="rdc-value rdc-mono rdc-hash">{result.blockchain.registradoPor}</span>
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>

              {/* Botón de reinicio */}
              <button onClick={resetAll} className="btn btn-primary result-reset-btn">
                🔄 Validar Otro Documento
              </button>
            </div>
          )}
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default ValidarDocumento;