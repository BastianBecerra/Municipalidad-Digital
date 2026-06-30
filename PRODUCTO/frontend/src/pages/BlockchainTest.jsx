import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import './BlockchainTest.css';

// Componente: Interfaz de pruebas para la integración con Blockchain
const BlockchainTest = () => {
  // Estados locales para manejar los datos del documento y la respuesta del servidor
  const [docId, setDocId] = useState('CERT-' + Math.floor(Math.random() * 10000));
  const [content, setContent] = useState('Este es un certificado de residencia para prueba de blockchain.');
  const [loading, setLoading] = useState(false); // Indica si hay una petición en curso
  const [result, setResult] = useState(null);    // Guarda el resultado (éxito o verificación)
  const [error, setError] = useState(null);      // Guarda mensajes de error

  // Función: Registra el hash del documento en el ledger (Blockchain)
  const handleRegister = async () => {
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const response = await fetch('/api/blockchain/registrar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ documentId: docId, content: content })
      });
      const data = await response.json();
      
      if (data.status === 'success') {
        // Almacenamos el hash de la transacción para mostrarlo en la interfaz
        setResult({ type: 'register', txHash: data.transactionHash });
      } else {
        setError(data.message);
      }
    } catch (err) {
      setError('Error al conectar con el servidor de blockchain.');
    } finally {
      setLoading(false);
    }
  };

  // Función: Consulta la Blockchain para validar la existencia e integridad del documento
  const handleVerify = async () => {
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const response = await fetch(`/api/blockchain/consultar/${docId}`);
      const data = await response.json();
      
      if (data.status !== 'error') {
        // Mostramos los datos recuperados de la red (timestamp, hash, origen)
        setResult({ type: 'verify', data: data });
      } else {
        setError(data.message);
      }
    } catch (err) {
      setError('Documento no encontrado o error en la consulta.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="blockchain-page">
      <Navbar />
      <main className="container blockchain-container">
        <div className="blockchain-card glass">
          <div className="blockchain-header">
            <span className="icon">⛓️</span>
            <h1>Verificador Blockchain</h1>
            <p>Registra y verifica la autenticidad de documentos municipales en tiempo real.</p>
          </div>

          {/* Formulario de registro/consulta */}
          <div className="form-section">
            <div className="input-group">
              <label>ID del Documento</label>
              <input
                type="text"
                value={docId}
                onChange={(e) => setDocId(e.target.value)}
                placeholder="Ej: CERT-123"
              />
            </div>
            <div className="input-group">
              <label>Contenido del Documento</label>
              <textarea
                rows="4"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="Escribe el contenido que quieres proteger..."
              ></textarea>
            </div>

            <div className="actions">
              <button className="btn btn-primary" onClick={handleRegister} disabled={loading}>
                {loading ? 'Procesando...' : 'Registrar en Blockchain'}
              </button>
              <button className="btn btn-secondary" onClick={handleVerify} disabled={loading}>
                Verificar Integridad
              </button>
            </div>
          </div>

          {/* Gestión de estados: Error, Registro Exitoso o Verificación */}
          {error && (
            <div className="status-box error-box animate-in">
              <span className="status-icon">⚠️</span>
              <p>{error}</p>
            </div>
          )}

          {result && result.type === 'register' && (
            <div className="status-box success-box animate-in">
              <span className="status-icon">✅</span>
              <div>
                <h3>¡Registrado con Éxito!</h3>
                <p>El documento ha sido sellado en la blockchain.</p>
                <code className="tx-hash">Tx: {result.txHash}</code>
              </div>
            </div>
          )}

          {result && result.type === 'verify' && (
            <div className="status-box verify-box animate-in">
              <div>
                <h3>Datos Recuperados</h3>
                <div className="data-grid">
                  <div className="data-item">
                    <span>Hash SHA-256</span>
                    <code>{result.data.hash.substring(0, 30)}...</code>
                  </div>
                  <div className="data-item">
                    <span>Fecha Registro</span>
                    {/* Convertimos el timestamp de Unix a formato legible */}
                    <p>{new Date(Number(result.data.timestamp) * 1000).toLocaleString()}</p>
                  </div>
                </div>
                <p className="verified-badge">✓ Documento Auténtico</p>
              </div>
            </div>
          )}
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default BlockchainTest;