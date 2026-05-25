import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Html5Qrcode } from 'html5-qrcode';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import './ValidarDocumento.css';

const ValidarDocumento = () => {
  const { hash: urlHash } = useParams();
  const navigate = useNavigate();

  const [scanning, setScanning] = useState(false);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [cameras, setCameras] = useState([]);
  const [activeCameraId, setActiveCameraId] = useState('');
  const [cameraPermission, setCameraPermission] = useState('prompt'); // prompt, granted, denied

  const qrReaderRef = useRef(null);
  const html5QrCodeRef = useRef(null);

  // Si viene un hash en la URL, validar directamente
  useEffect(() => {
    const sha256Regex = /^[a-fA-F0-9]{64}$/;
    if (urlHash && sha256Regex.test(urlHash)) {
      performValidation(urlHash);
    }
  }, [urlHash]);

  // Manejar el ciclo de vida del lector de QR
  useEffect(() => {
    return () => {
      stopScanning();
    };
  }, []);

  const performValidation = async (hashToValidate) => {
    setLoading(true);
    setError(null);
    setResult(null);
    stopScanning();

    try {
      const response = await fetch(`/api/validacion/public/validar/${hashToValidate}`);
      const data = await response.json();
      setResult(data);
    } catch (err) {
      setError('Error al conectar con el servicio de validación. Por favor intenta más tarde.');
    } finally {
      setLoading(false);
    }
  };

  const startScanning = async () => {
    setResult(null);
    setError(null);
    setScanning(true);

    try {
      // Solicitar permisos de cámara y obtener lista de dispositivos
      const devices = await Html5Qrcode.getCameras();
      setCameras(devices);
      setCameraPermission('granted');

      if (devices.length > 0) {
        const defaultCam = devices.find(device => device.label.toLowerCase().includes('back')) || devices[0];
        setActiveCameraId(defaultCam.id);
        initCamera(defaultCam.id);
      } else {
        setError('No se encontraron cámaras en este dispositivo.');
        setScanning(false);
      }
    } catch (err) {
      console.error("Error al iniciar cámara: ", err);
      setCameraPermission('denied');
      setError('No se pudo acceder a la cámara. Por favor otorga permisos o sube una imagen.');
      setScanning(false);
    }
  };

  const initCamera = (cameraId) => {
    if (html5QrCodeRef.current) {
      html5QrCodeRef.current.stop().then(() => {
        startQrInstance(cameraId);
      }).catch(err => {
        console.error("Error al detener cámara previa: ", err);
        startQrInstance(cameraId);
      });
    } else {
      startQrInstance(cameraId);
    }
  };

  const startQrInstance = (cameraId) => {
    const html5QrCode = new Html5Qrcode("qr-reader-view");
    html5QrCodeRef.current = html5QrCode;

    html5QrCode.start(
      cameraId,
      {
        fps: 15,
        qrbox: (width, height) => {
          const size = Math.min(width, height) * 0.7;
          return { width: size, height: size };
        }
      },
      (qrCodeMessage) => {
        // Encontrar hash SHA-256 de 64 caracteres en el mensaje escaneado
        let hash = qrCodeMessage;
        if (qrCodeMessage.includes('/validar/')) {
          hash = qrCodeMessage.substring(qrCodeMessage.lastIndexOf('/') + 1);
        }

        // Limpiar el hash de caracteres no deseados
        hash = hash.trim().toLowerCase();

        // Validar formato SHA-256
        const sha256Regex = /^[a-fA-F0-9]{64}$/;
        if (sha256Regex.test(hash)) {
          navigate(`/validar/${hash}`);
        } else {
          setError('El código QR escaneado no contiene un formato de validación válido municipal.');
          stopScanning();
        }
      },
      (errorMessage) => {
        // Ignorar errores repetitivos de escaneo para mantener rendimiento
      }
    ).catch((err) => {
      setError('Error al iniciar el flujo de video de la cámara.');
      setScanning(false);
    });
  };

  const stopScanning = () => {
    if (html5QrCodeRef.current && html5QrCodeRef.current.isScanning) {
      html5QrCodeRef.current.stop().then(() => {
        setScanning(false);
      }).catch(err => {
        console.error("Error al detener scanner: ", err);
        setScanning(false);
      });
    } else {
      setScanning(false);
    }
  };

  const handleCameraChange = (e) => {
    const newId = e.target.value;
    setActiveCameraId(newId);
    initCamera(newId);
  };

  // Cargar una foto del QR si no se posee cámara
  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const html5QrCode = new Html5Qrcode("qr-reader-view-hidden");
      html5QrCode.scanFile(file, true)
        .then((qrCodeMessage) => {
          let hash = qrCodeMessage;
          if (qrCodeMessage.includes('/validar/')) {
            hash = qrCodeMessage.substring(qrCodeMessage.lastIndexOf('/') + 1);
          }
          hash = hash.trim().toLowerCase();
          const sha256Regex = /^[a-fA-F0-9]{64}$/;
          if (sha256Regex.test(hash)) {
            navigate(`/validar/${hash}`);
          } else {
            setError('La imagen cargada no contiene un hash de documento municipal válido.');
            setLoading(false);
          }
        })
        .catch((err) => {
          setError('No se pudo decodificar un código QR en esta imagen. Asegúrate de que la iluminación sea buena.');
          setLoading(false);
        });
    } catch (err) {
      console.error("Error initializing file scanner:", err);
      setError('Error al inicializar el escáner de imágenes. Intenta con la cámara o recarga la página.');
      setLoading(false);
    }
  };

  const resetAll = () => {
    setResult(null);
    setError(null);
    setScanning(false);
    navigate('/validar');
  };

  return (
    <div className="validar-page">
      <Navbar />
      <main className="container validar-container animate-fade-in">
        
        {/* Cabecera Principal */}
        <div className="validar-header text-center">
          <div className="badge-digital">Sello Digital Municipal</div>
          <h1>Validador Oficial de Documentos</h1>
          <p className="subtitle">
            Escanea el código QR de un certificado municipal impreso o carga una imagen para verificar su integridad y registro en la red Blockchain.
          </p>
        </div>

        {/* CONTENEDOR CENTRAL */}
        <div className="validar-body glass-card">
          <div id="qr-reader-view-hidden" style={{ position: 'absolute', opacity: 0, width: '1px', height: '1px', pointerEvents: 'none', zIndex: -9999 }}></div>
          
          {/* 1. MODO DE CARGA/ESPERA (SPINNER) */}
          {loading && (
            <div className="validation-loader">
              <div className="spinner"></div>
              <h3>Consultando en Blockchain...</h3>
              <p>Analizando firma digital y registros criptográficos del documento en tiempo real.</p>
            </div>
          )}

          {/* 2. ERROR GENERAL */}
          {!loading && error && (
            <div className="validation-alert alert-danger animate-slide-up">
              <span className="icon">⚠️</span>
              <div>
                <h4>Error de Verificación</h4>
                <p>{error}</p>
                <button className="btn btn-outline" onClick={resetAll}>Volver a intentar</button>
              </div>
            </div>
          )}

          {/* 3. MODO ESCÁNER ACTIVO */}
          {!loading && !error && scanning && (
            <div className="scanner-section animate-fade-in">
              <div className="scanner-container">
                <div id="qr-reader-view"></div>
                <div className="scanner-overlay">
                  <div className="scanner-laser"></div>
                  <div className="scanner-corners"></div>
                </div>
              </div>

              <div className="scanner-controls">
                {cameras.length > 1 && (
                  <div className="camera-selector">
                    <label htmlFor="cam-select">Cambiar Cámara:</label>
                    <select id="cam-select" value={activeCameraId} onChange={handleCameraChange}>
                      {cameras.map(cam => (
                        <option key={cam.id} value={cam.id}>{cam.label || `Cámara ${cameras.indexOf(cam) + 1}`}</option>
                      ))}
                    </select>
                  </div>
                )}
                <button className="btn btn-danger" onClick={stopScanning}>
                  🛑 Cancelar Escaneo
                </button>
              </div>
            </div>
          )}

          {/* 4. MENÚ DE INICIO (SIN ACCIÓN) */}
          {!loading && !error && !scanning && !result && (
            <div className="init-menu text-center animate-fade-in">
              <div className="shield-animation">
                <div className="shield-glow"></div>
                <span className="shield-icon">🛡️</span>
              </div>
              <h3>Selecciona el método de validación</h3>
              <p>Elige usar la cámara de tu dispositivo o cargar una fotografía del código QR del certificado.</p>
              
              <div className="menu-actions">
                <button className="btn btn-primary btn-lg" onClick={startScanning}>
                  📷 Escanear con Cámara
                </button>
                <label className="btn btn-secondary btn-lg btn-upload">
                  📁 Cargar Archivo/Foto QR
                  <input type="file" accept="image/*" onChange={handleFileUpload} style={{ display: 'none' }} />
                </label>
            </div>
            </div>
          )}

          {/* 5. VISTA DE RESULTADO DE VALIDACIÓN */}
          {!loading && !error && result && (
            <div className={`result-section animate-slide-up ${result.valido ? 'result-valid' : 'result-invalid'}`}>
              
              {/* Encabezado del Resultado */}
              <div className="result-header">
                <div className="result-badge">
                  <span className="badge-icon">{result.valido ? '✓' : '✗'}</span>
                  <h3>{result.valido ? 'Documento Auténtico' : 'Documento No Válido'}</h3>
                </div>
                <p className="result-motivo">{result.motivo}</p>
              </div>

              {/* Contenido en Rejilla */}
              <div className="result-grid">
                
                {/* Metadatos Municipales */}
                {result.documento && (
                  <div className="result-card data-card glass">
                    <h4>📋 Datos del Documento</h4>
                    <hr />
                    <div className="data-row">
                      <span className="label">Título:</span>
                      <span className="value bold">{result.documento.titulo}</span>
                    </div>
                    <div className="data-row">
                      <span className="label">Tipo:</span>
                      <span className="value font-accent">
                        {result.documento.usuarioComuna ? 'Certificado de Residencia' : 'Documento Municipal'}
                      </span>
                    </div>
                    {result.documento.usuarioNombreCompleto && (
                      <div className="data-row">
                        <span className="label">Titular:</span>
                        <span className="value uppercase">{result.documento.usuarioNombreCompleto}</span>
                      </div>
                    )}
                    {result.documento.usuarioRut && (
                      <div className="data-row">
                        <span className="label">RUT Titular:</span>
                        <span className="value">{result.documento.usuarioRut}</span>
                      </div>
                    )}
                    {result.documento.usuarioDireccion && (
                      <div className="data-row">
                        <span className="label">Domicilio:</span>
                        <span className="value">{result.documento.usuarioDireccion}, {result.documento.usuarioComuna}</span>
                      </div>
                    )}
                    <div className="data-row">
                      <span className="label">Firma Digital:</span>
                      <span className="value code-text">{result.documento.firmaDigital?.substring(0, 24)}...</span>
                    </div>
                    <div className="data-row">
                      <span className="label">Firmado Por:</span>
                      <span className="value">{result.documento.firmadoPor}</span>
                    </div>
                  </div>
                )}

                {/* Sello Blockchain */}
                {result.blockchain && (
                  <div className="result-card blockchain-card glass">
                    <h4>⛓️ Registro Criptográfico (Blockchain)</h4>
                    <hr />
                    <div className="blockchain-badge-seal">
                      <div className="chain-pulse"></div>
                      <span>SELLADO EN RED ETHEREUM</span>
                    </div>
                    
                    <div className="data-row">
                      <span className="label">Hash Documento:</span>
                      <span className="value code-text font-small-code" title={result.blockchain.hash}>
                        {result.blockchain.hash}
                      </span>
                    </div>
                    
                    {result.blockchain.timestamp && (
                      <div className="data-row">
                        <span className="label">Bloque Sellado:</span>
                        <span className="value">
                          {new Date(Number(result.blockchain.timestamp) * 1000).toLocaleString()}
                        </span>
                      </div>
                    )}
                    
                    {result.blockchain.registeredBy && (
                      <div className="data-row">
                        <span className="label">Autoridad Registradora:</span>
                        <span className="value code-text font-small-code" title={result.blockchain.registeredBy}>
                          {result.blockchain.registeredBy}
                        </span>
                      </div>
                    )}
                    
                    {result.documento?.blockchainTxHash && (
                      <div className="data-row">
                        <span className="label">TX Hash:</span>
                        <span className="value code-text font-small-code" title={result.documento.blockchainTxHash}>
                          {result.documento.blockchainTxHash}
                        </span>
                      </div>
                    )}
                  </div>
                )}
              </div>

              {/* Botón para reiniciar */}
              <div className="result-actions text-center">
                <button className="btn btn-primary btn-lg" onClick={resetAll}>
                  🔄 Validar Otro Documento
                </button>
              </div>

            </div>
          )}

        </div>
      </main>
      <Footer />
    </div>
  );
};

export default ValidarDocumento;
