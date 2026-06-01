import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Html5Qrcode } from 'html5-qrcode';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import './ValidarDocumento.css';

const ValidarDocumento = () => {
  const { hash: urlHash } = useParams(); // Obtenemos el hash desde la URL si existe
  const navigate = useNavigate();

  // Estados de control para la UI
  const [scanning, setScanning] = useState(false); // ¿Está la cámara activa?
  const [loading, setLoading] = useState(false);   // ¿Estamos consultando la API/Blockchain?
  const [result, setResult] = useState(null);      // Almacena la respuesta del validador
  const [error, setError] = useState(null);        // Manejo de errores
  const [cameras, setCameras] = useState([]);      // Lista de cámaras disponibles
  const [activeCameraId, setActiveCameraId] = useState(''); // Cámara activa actual
  
  const html5QrCodeRef = useRef(null); // Referencia persistente para la instancia del escáner

  // EFECTO: Si el usuario entra directamente con un hash en la URL, validar sin escanear
  useEffect(() => {
    const sha256Regex = /^[a-fA-F0-9]{64}$/; // Validación de formato de hash (64 caracteres hexadecimales)
    if (urlHash && sha256Regex.test(urlHash)) {
      performValidation(urlHash);
    }
  }, [urlHash]);

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
      // Llamada al endpoint de validación
      const response = await fetch(`/api/validacion/public/validar/${hashToValidate}`);
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
        // Preferir cámara trasera si está disponible
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
        // Extraer el hash del mensaje del QR
        let hash = qrCodeMessage.includes('/validar/') ? qrCodeMessage.substring(qrCodeMessage.lastIndexOf('/') + 1) : qrCodeMessage;
        hash = hash.trim().toLowerCase();
        
        // Si el QR es válido (SHA-256), navegamos a la ruta para validar
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

  // Alternativa: Subir archivo de imagen para decodificar QR
  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setLoading(true);
    const html5QrCode = new Html5Qrcode("qr-reader-view-hidden");
    html5QrCode.scanFile(file, true)
      .then((qrCodeMessage) => {
        let hash = qrCodeMessage.substring(qrCodeMessage.lastIndexOf('/') + 1).trim().toLowerCase();
        if (/^[a-fA-F0-9]{64}$/.test(hash)) {
          navigate(`/validar/${hash}`);
        } else {
          setError('La imagen no contiene un código QR municipal válido.');
          setLoading(false);
        }
      })
      .catch(() => { setError('No se pudo decodificar la imagen.'); setLoading(false); });
  };

  const resetAll = () => { navigate('/validar'); setResult(null); setError(null); };

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
              <h3>Consultando en Blockchain...</h3>
            </div>
          )}

          {/* ESTADO 2: Error */}
          {!loading && error && (
            <div className="validation-alert alert-danger">
              <p>{error}</p>
              <button onClick={resetAll}>Volver a intentar</button>
            </div>
          )}

          {/* ESTADO 3: Cámara Activa */}
          {!loading && !error && scanning && (
            <div className="scanner-section">
              <div id="qr-reader-view"></div>
              <button onClick={stopScanning}>🛑 Cancelar</button>
            </div>
          )}

          {/* ESTADO 4: Menú inicial (cámara o archivo) */}
          {!loading && !error && !scanning && !result && (
            <div className="init-menu text-center">
              <h3>Selecciona el método de validación</h3>
              <div className="menu-actions">
                <button onClick={startScanning}>📷 Escanear con Cámara</button>
                <label>
                  📁 Cargar Archivo
                  <input type="file" accept="image/*" onChange={handleFileUpload} style={{ display: 'none' }} />
                </label>
              </div>
            </div>
          )}

          {/* ESTADO 5: Resultado (Si es válido o inválido) */}
          {!loading && !error && result && (
            <div className={`result-section ${result.valido ? 'result-valid' : 'result-invalid'}`}>
              <h3>{result.valido ? 'Documento Auténtico' : 'Documento No Válido'}</h3>
              
              {/* Despliegue de datos del documento */}
              {result.documento && (
                <div className="result-card">
                  <h4>📋 Datos del Documento</h4>
                  <p>Título: {result.documento.titulo}</p>
                </div>
              )}

              {/* Despliegue de Blockchain */}
              {result.blockchain && (
                <div className="result-card">
                  <h4>⛓️ Registro Criptográfico</h4>
                  <p>Hash: {result.blockchain.hash.substring(0, 20)}...</p>
                </div>
              )}

              <button onClick={resetAll}>🔄 Validar Otro</button>
            </div>
          )}
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default ValidarDocumento;