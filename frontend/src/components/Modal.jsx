import './Modal.css';

const Modal = ({ isOpen, onClose, title, type = 'info', children }) => {
  if (!isOpen) return null;

  // Seleccionar icono según el tipo
  const getIcon = () => {
    switch (type) {
      case 'success':
        return '✅';
      case 'error':
        return '❌';
      case 'warning':
        return '⚠️';
      case 'info':
      default:
        return 'ℹ️';
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className={`modal-card modal-${type}`} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <span className="modal-icon">{getIcon()}</span>
          <h3>{title}</h3>
          <button className="modal-close-btn" onClick={onClose}>×</button>
        </div>
        <div className="modal-body">
          {children}
        </div>
        <div className="modal-footer">
          <button className={`btn btn-modal-close btn-${type}`} onClick={onClose}>
            Entendido
          </button>
        </div>
      </div>
    </div>
  );
};

export default Modal;
