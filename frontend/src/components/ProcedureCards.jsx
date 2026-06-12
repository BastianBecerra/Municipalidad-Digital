import './ProcedureCards.css';

// Lista de trámites disponibles: Esta estructura permite agregar o modificar trámites fácilmente
const procedures = [
  {
    id: 1,
    title: 'Certificados',
    description: 'Obtén certificados de residencia, nacimiento y más al instante.',
    icon: '📄',
    color: 'blue',
    path: '/tramites/nuevo?tipo=residencia'
  },
  {
    id: 2,
    title: 'Salvocondutos',
    description: 'Accede aquí para iniciar tu trámite.',
    icon: '🚗',
    color: 'indigo',
    path: '/tramites/nuevo?tipo=salvoconducto'
  },
  {
    id: 3,
    title: 'Juntas de vecinos',
    description: 'Obtén información sobre las juntas de vecinos de tu comuna.',
    icon: '📄',
    color: 'green',
    path: '/junta-vecinos'
  }
];

// Componente principal: Renderiza la sección de tarjetas con los trámites disponibles
const ProcedureCards = () => {
  return (
    // ID 'tramites' definido para el anclaje (scroll automático) desde el menú/footer
    <section id="tramites" className="procedures-section">
      <div className="container">
        <div className="section-header text-center">
          <h2>Trámites Disponibles</h2>
          <p className="text-muted mt-4 mb-8">Accede a los servicios más solicitados por los vecinos.</p>
        </div>

        <div className="procedures-grid">
          {/* Mapeamos el arreglo de trámites para renderizar dinámicamente cada tarjeta */}
          {procedures.map((proc) => (
            <div key={proc.id} className="procedure-card">
              {/* Contenedor del icono: utiliza una clase dinámica para aplicar el color específico */}
              <div className={`card-icon-wrapper color-${proc.color}`}>
                <span className="card-icon">{proc.icon}</span>
              </div>
              <h3 className="card-title">{proc.title}</h3>
              <p className="card-desc">{proc.description}</p>
              <a href={proc.path} className="card-link">
                Iniciar trámite <span>→</span>
              </a>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default ProcedureCards;