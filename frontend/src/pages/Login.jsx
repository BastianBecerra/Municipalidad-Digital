import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Login.css';

const Login = () => {
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const isEmail = identifier.includes('@');

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const body = isEmail
      ? { email: identifier, password }
      : { rut: identifier, password };

    try {
      const response = await fetch('/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });

      if (!response.ok) {
        const msg = await response.text();
        throw new Error(msg || 'Credenciales incorrectas');
      }

      const data = await response.json();
      localStorage.setItem('token', data.token);
      navigate('/');
    } catch (err) {
      setError(
        isEmail
          ? 'Email o contraseña incorrectos.'
          : 'RUN o ClaveÚnica incorrectos.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page gradient-bg">
      <div className="clave-unica-header glass-header">
        <div className="cu-header-content">
          <div className="cu-logo">
            <span className="logo-icon">🏛️</span>
            <span style={{ marginLeft: '0.5rem', color: 'var(--text-color)' }}>Muni Digital</span>
          </div>
          <div className="cu-title">Iniciar Sesión</div>
        </div>
      </div>

      <div className="login-container">
        <div className="login-card">
          <h2 className="login-title">Bienvenido de vuelta</h2>
          <p className="login-hint">Ingresa tu RUN o correo electrónico para continuar.</p>

          {error && <div className="login-error">{error}</div>}

          <form onSubmit={handleLogin} className="login-form">
            <div className="form-group">
              <label htmlFor="identifier">RUN o Email</label>
              <input
                type="text"
                id="identifier"
                className="cu-input"
                placeholder="12345678-9 o correo@ejemplo.cl"
                value={identifier}
                onChange={(e) => setIdentifier(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="password">
                {isEmail ? 'Contraseña' : 'ClaveÚnica'}
              </label>
              <input
                type="password"
                id="password"
                className="cu-input"
                placeholder={isEmail ? 'Ingresa tu contraseña' : 'Ingresa tu ClaveÚnica'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <button type="submit" className="cu-btn-submit" disabled={loading}>
              {loading ? 'Autenticando...' : 'Autenticar'}
            </button>
          </form>

          <div className="login-footer">
            <a href="#" className="cu-link">¿Olvidaste tu ClaveÚnica?</a>
          </div>
        </div>

        <div className="login-back-wrapper">
          <button onClick={() => navigate('/')} className="btn-back">
            ← Volver a Muni Digital
          </button>
        </div>
      </div>

      {/* Auras de fondo (Floating shapes) */}
      <div className="floating-shape shape-1"></div>
      <div className="floating-shape shape-2"></div>
    </div>
  );
};

export default Login;
