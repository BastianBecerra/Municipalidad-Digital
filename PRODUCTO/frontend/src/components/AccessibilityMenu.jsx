import { useState, useEffect } from 'react';
import './AccessibilityMenu.css';

// Componente: Menú flotante de accesibilidad para personalizar la interfaz
const AccessibilityMenu = () => {
  const [isOpen, setIsOpen] = useState(false); // Estado para abrir/cerrar el panel
  const [textSize, setTextSize] = useState('normal'); // Tamaño del texto actual
  const [highContrast, setHighContrast] = useState(false); // Estado del alto contraste

  // Efecto: Cargar preferencias guardadas al iniciar la aplicación
  useEffect(() => {
    const savedTextSize = localStorage.getItem('acc-text-size') || 'normal';
    const savedContrast = localStorage.getItem('acc-contrast') === 'true';

    setTextSize(savedTextSize);
    setHighContrast(savedContrast);
    
    // Aplicamos los ajustes al cargar la página
    applySettings(savedTextSize, savedContrast);
  }, []);

  // Función principal: Aplica las clases de accesibilidad al body del documento
  const applySettings = (size, contrast) => {
    // Primero limpiamos las clases antiguas para evitar conflictos
    document.body.classList.remove('text-large', 'text-xlarge', 'high-contrast');

    // Aplicamos el tamaño de fuente elegido
    if (size === 'large') document.body.classList.add('text-large');
    if (size === 'xlarge') document.body.classList.add('text-xlarge');

    // Aplicamos el contraste si está activado
    if (contrast) document.body.classList.add('high-contrast');
  };

  // Manejador para cambiar el tamaño de fuente y persistirlo
  const handleTextSizeChange = (size) => {
    setTextSize(size);
    localStorage.setItem('acc-text-size', size);
    applySettings(size, highContrast);
  };

  // Manejador para activar/desactivar alto contraste y persistirlo
  const toggleContrast = () => {
    const newContrast = !highContrast;
    setHighContrast(newContrast);
    localStorage.setItem('acc-contrast', newContrast);
    applySettings(textSize, newContrast);
  };

  return (
    <div className={`accessibility-widget ${isOpen ? 'open' : ''}`}>
      {/* Botón flotante para abrir el menú */}
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

      {/* Panel de configuración, solo visible si isOpen es true */}
      {isOpen && (
        <div className="acc-panel">
          <div className="acc-header">
            <h3>Opciones de Accesibilidad</h3>
            <button className="acc-close" onClick={() => setIsOpen(false)} aria-label="Cerrar">&times;</button>
          </div>
          
          <div className="acc-body">
            {/* Sección de tamaño de fuente */}
            <div className="acc-section">
              <h4>Tamaño de Texto</h4>
              <div className="acc-buttons">
                <button 
                  className={`acc-btn ${textSize === 'normal' ? 'active' : ''}`}
                  onClick={() => handleTextSizeChange('normal')}
                >A</button>
                <button 
                  className={`acc-btn ${textSize === 'large' ? 'active' : ''}`}
                  style={{fontSize: '1.2rem'}}
                  onClick={() => handleTextSizeChange('large')}
                >A</button>
                <button 
                  className={`acc-btn ${textSize === 'xlarge' ? 'active' : ''}`}
                  style={{fontSize: '1.4rem'}}
                  onClick={() => handleTextSizeChange('xlarge')}
                >A</button>
              </div>
            </div>

            {/* Sección de contraste */}
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