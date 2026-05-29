import './ProcedureCards.css';

const procedures = [
  {
    id: 1,
    title: 'Certificados',
    description: 'Obtén certificados de residencia, nacimiento y más al instante.',
    icon: '📄',
    color: 'blue'
  },
  {
    id: 2,
    title: 'Salvocondutos',
    description: 'Accede aquí para iniciar tu trámite.',
    icon: '🚗',
    color: 'indigo'
  },
  {
    id: 3,
    title: 'Juntas de vecinos',
    description: 'Obtén información sobre las juntas de vecinos de tu comuna.',
    icon: '📄',
    color: 'green'
  }
];

const ProcedureCards = () => {
  return (
    <section id="tramites" className="procedures-section">
      <div className="container">
        <div className="section-header text-center">
          <h2>Trámites Disponibles</h2>
          <p className="text-muted mt-4 mb-8">Accede a los servicios más solicitados por los vecinos.</p>
        </div>

        <div className="procedures-grid">
          {procedures.map((proc) => (
            <div key={proc.id} className="procedure-card">
              <div className={`card-icon-wrapper color-${proc.color}`}>
                <span className="card-icon">{proc.icon}</span>
              </div>
              <h3 className="card-title">{proc.title}</h3>
              <p className="card-desc">{proc.description}</p>
              <a href="/tramites/nuevo" className="card-link">
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
