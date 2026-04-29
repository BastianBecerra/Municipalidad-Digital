import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Login.css';

const Login = () => {
  const [rut, setRut] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleLogin = (e) => {
    e.preventDefault();
    alert(`Intentando iniciar sesión con RUT: ${rut}`);
    navigate('/');
  };

  return (
    <div className="login-page gradient-bg">
      <div className="clave-unica-header glass-header">
        <div className="cu-header-content">
          <div className="cu-logo">
            <span className="logo-icon">🏛️</span>
            <span style={{ marginLeft: '0.5rem', color: 'var(--text-color)' }}>Muni Digital</span>
          </div>
          <div className="cu-title">ClaveÚnica</div>
        </div>
      </div>

      <div className="login-container">
        <div className="login-card">
          <h2 className="login-title">Inicia sesión con tu ClaveÚnica</h2>
          
          <form onSubmit={handleLogin} className="login-form">
            <div className="form-group">
              <label htmlFor="rut">RUN</label>
              <input
                type="text"
                id="rut"
                className="cu-input"
                placeholder="12345678-9"
                value={rut}
                onChange={(e) => setRut(e.target.value)}
                required
              />
            </div>
            
            <div className="form-group">
              <label htmlFor="password">ClaveÚnica</label>
              <input
                type="password"
                id="password"
                className="cu-input"
                placeholder="Ingresa tu clave"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            
            <button type="submit" className="cu-btn-submit">
              Autenticar
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
