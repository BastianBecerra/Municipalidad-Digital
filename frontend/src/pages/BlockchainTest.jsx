import { useState } from 'react';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import './BlockchainTest.css';

const BlockchainTest = () => {
  const [docId, setDocId] = useState('CERT-' + Math.floor(Math.random() * 10000));
  const [content, setContent] = useState('Este es un certificado de residencia para prueba de blockchain.');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

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
        setResult({ type: 'register', txHash: data.transactionHash });
      } else {
        setError(data.message);
      }
    } catch (err) {
      setError('Error al conectar con el servidor de blockchain. Asegúrate de que el backend esté corriendo en el puerto 8087.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async () => {
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const response = await fetch(`/api/blockchain/consultar/${docId}`);
      const data = await response.json();
      if (data.status !== 'error') {
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
              <button
                className="btn btn-primary"
                onClick={handleRegister}
                disabled={loading}
              >
                {loading ? 'Procesando...' : 'Registrar en Blockchain'}
              </button>
              <button
                className="btn btn-secondary"
                onClick={handleVerify}
                disabled={loading}
              >
                Verificar Integridad
              </button>
            </div>
          </div>

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
              <span className="status-icon"></span>
              <div>
                <h3>Datos Recuperados</h3>
                <div className="data-grid">
                  <div className="data-item">
                    <span>Hash SHA-256</span>
                    <code>{result.data.hash.substring(0, 30)}...</code>
                  </div>
                  <div className="data-item">
                    <span>Fecha Registro</span>
                    <p>{new Date(Number(result.data.timestamp) * 1000).toLocaleString()}</p>
                  </div>
                  <div className="data-item">
                    <span>Billetera Notario</span>
                    <code>{result.data.registeredBy.substring(0, 20)}...</code>
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
