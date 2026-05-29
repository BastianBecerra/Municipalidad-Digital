import { useState, useEffect } from 'react';
import './AccessibilityMenu.css';

const AccessibilityMenu = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [textSize, setTextSize] = useState('normal'); // normal, large, xlarge
  const [highContrast, setHighContrast] = useState(false);

  // Cargar preferencias al iniciar
  useEffect(() => {
    const savedTextSize = localStorage.getItem('acc-text-size') || 'normal';
    const savedContrast = localStorage.getItem('acc-contrast') === 'true';

    setTextSize(savedTextSize);
    setHighContrast(savedContrast);
    
    applySettings(savedTextSize, savedContrast);
  }, []);

  const applySettings = (size, contrast) => {
    // Reset classes
    document.body.classList.remove('text-large', 'text-xlarge', 'high-contrast');

    // Apply text size
    if (size === 'large') document.body.classList.add('text-large');
    if (size === 'xlarge') document.body.classList.add('text-xlarge');

    // Apply contrast
    if (contrast) document.body.classList.add('high-contrast');
  };

  const handleTextSizeChange = (size) => {
    setTextSize(size);
    localStorage.setItem('acc-text-size', size);
    applySettings(size, highContrast);
  };

  const toggleContrast = () => {
    const newContrast = !highContrast;
    setHighContrast(newContrast);
    localStorage.setItem('acc-contrast', newContrast);
    applySettings(textSize, newContrast);
  };

  return (
    <div className={`accessibility-widget ${isOpen ? 'open' : ''}`}>
      <button 
        className="acc-toggle-btn" 
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Menú de Accesibilidad"
        title="Opciones de Accesibilidad"
      >
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
          <path d="M12 2c1.1 0 2 .9 2 2s-.9 2-2 2-2-.9-2-2 .9-2 2-2zm9 7h-6v13h-2v-6h-2v6H9V9H3V7h18v2z"/>
        </svg>
      </button>

      {isOpen && (
        <div className="acc-panel">
          <div className="acc-header">
            <h3>Opciones de Accesibilidad</h3>
            <button className="acc-close" onClick={() => setIsOpen(false)} aria-label="Cerrar">&times;</button>
          </div>
          
          <div className="acc-body">
            <div className="acc-section">
              <h4>Tamaño de Texto</h4>
              <div className="acc-buttons">
                <button 
                  className={`acc-btn ${textSize === 'normal' ? 'active' : ''}`}
                  onClick={() => handleTextSizeChange('normal')}
                >
                  A
                </button>
                <button 
                  className={`acc-btn ${textSize === 'large' ? 'active' : ''}`}
                  style={{fontSize: '1.2rem'}}
                  onClick={() => handleTextSizeChange('large')}
                >
                  A
                </button>
                <button 
                  className={`acc-btn ${textSize === 'xlarge' ? 'active' : ''}`}
                  style={{fontSize: '1.4rem'}}
                  onClick={() => handleTextSizeChange('xlarge')}
                >
                  A
                </button>
              </div>
            </div>

            <div className="acc-section">
              <h4>Contraste</h4>
              <button 
                className={`acc-btn acc-btn-block ${highContrast ? 'active' : ''}`}
                onClick={toggleContrast}
              >
                {highContrast ? 'Desactivar Alto Contraste' : 'Activar Alto Contraste'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AccessibilityMenu;
