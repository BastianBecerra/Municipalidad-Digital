import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Contact.css';

// Componente Contact: Formulario para recibir consultas de los vecinos
const Contact = () => {
  // Estado para los datos del formulario y el estado de envío
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    message: ''
  });
  const [submitted, setSubmitted] = useState(false); // Bandera para mostrar mensaje de éxito
  const navigate = useNavigate();

  // Manejador de cambios en los inputs (Patrón de componentes controlados)
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prevState => ({ ...prevState, [name]: value }));
  };

  // Manejador de envío: Simula una petición a servidor
  const handleSubmit = (e) => {
    e.preventDefault();
    console.log("Mensaje enviado:", formData); // Aquí iría la llamada real a tu API
    setSubmitted(true);

    // Reseteamos el formulario después de 4 segundos
    setTimeout(() => {
      setSubmitted(false);
      setFormData({ name: '', email: '', message: '' });
    }, 4000);
  };

  return (
    <div className="contact-page gradient-bg">
      <div className="contact-container">
        <div className="contact-card">
          <div className="contact-header">
            <span className="contact-icon">✉️</span>
            <h2 className="contact-title">Contáctanos</h2>
            <p className="contact-subtitle">Déjanos tu mensaje y te responderemos lo antes posible.</p>
          </div>

          {/* Renderizado condicional: Mostramos el mensaje de éxito o el formulario */}
          {submitted ? (
            <div className="success-message">
              <div className="success-icon">✅</div>
              <h3>¡Mensaje enviado con éxito!</h3>
              <p>Gracias por contactarte con nosotros. Hemos recibido tu consulta.</p>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="contact-form">
              <div className="form-group">
                <label htmlFor="name">Nombre Completo</label>
                <input
                  type="text"
                  id="name"
                  name="name"
                  className="contact-input"
                  placeholder="Ej. Juan Pérez"
                  value={formData.name}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="email">Correo Electrónico</label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  className="contact-input"
                  placeholder="juan@correo.com"
                  value={formData.email}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="message">Mensaje</label>
                <textarea
                  id="message"
                  name="message"
                  className="contact-input contact-textarea"
                  placeholder="¿En qué te podemos ayudar?"
                  value={formData.message}
                  onChange={handleChange}
                  required
                  rows="5"
                ></textarea>
              </div>

              <button type="submit" className="contact-btn-submit">
                Enviar Mensaje
              </button>
            </form>
          )}
        </div>

        {/* Botón para regresar al Home */}
        <div className="contact-back-wrapper">
          <button onClick={() => navigate('/')} className="btn-back">
            ← Volver al Inicio
          </button>
        </div>
      </div>

      {/* Auras decorativas de fondo */}
      <div className="floating-shape shape-1"></div>
      <div className="floating-shape shape-2"></div>
    </div>
  );
};

export default Contact;