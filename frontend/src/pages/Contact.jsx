import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Contact.css';

const Contact = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    message: ''
  });
  const [submitted, setSubmitted] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prevState => ({ ...prevState, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log("Mensaje enviado:", formData);
    setSubmitted(true);

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

        <div className="contact-back-wrapper">
          <button onClick={() => navigate('/')} className="btn-back">
            ← Volver al Inicio
          </button>
        </div>
      </div>

      {/* Auras de fondo (Floating shapes) idénticas al login/hero */}
      <div className="floating-shape shape-1"></div>
      <div className="floating-shape shape-2"></div>
    </div>
  );
};

export default Contact;
