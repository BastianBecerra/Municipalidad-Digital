import { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import './MapSection.css';

// --- CONFIGURACIÓN DE LEAFLET ---
// Solución al problema común de React-Leaflet donde los íconos por defecto de los marcadores no cargan.
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

// Función para crear íconos personalizados con estilo premium y micro-animaciones
const getCustomIcon = (tipo, nombre) => {
  let emoji = '📍';
  let color = '#3b82f6';
  
  const nameLower = (nombre || '').toLowerCase();
  const tipoLower = (tipo || '').toLowerCase();
  
  if (nameLower.includes('municipalidad')) {
    emoji = '🏛️';
    color = '#2563eb'; // Azul premium
  } else if (nameLower.includes('comisaría') || nameLower.includes('carabineros') || nameLower.includes('pdi') || nameLower.includes('investigación criminal')) {
    emoji = '🚓';
    color = '#dc2626'; // Rojo seguridad
  } else if (tipoLower === 'junta_vecinos' || nameLower.includes('junta de vecinos') || tipoLower === 'junta-vecinal') {
    emoji = '🏡';
    color = '#10b981'; // Verde esmeralda
  }
  
  return L.divIcon({
    html: `<div style="background-color: ${color}; width: 42px; height: 42px; display: flex; align-items: center; justify-content: center; border-radius: 50%; border: 2.5px solid white; box-shadow: 0 4px 10px rgba(0,0,0,0.35); font-size: 20px; transition: transform 0.2s;" class="map-marker-bounce">${emoji}</div>`,
    className: 'custom-div-icon',
    iconSize: [42, 42],
    iconAnchor: [21, 21],
    popupAnchor: [0, -21]
  });
};

// Coordenadas centrales de Providencia por defecto
const DEFAULT_CENTER = [-33.4326, -70.6133];

const MapSection = () => {
  const [places, setPlaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeFilter, setActiveFilter] = useState('todos');

  useEffect(() => {
    const fetchPlaces = async () => {
      try {
        const token = localStorage.getItem('token');
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
        const response = await fetch('/territorios', { headers });
        if (response.ok) {
          const data = await response.json();
          // Filtrar los que tienen coordenadas válidas
          const validPlaces = data.filter(p => p.latitud && p.longitud);
          if (validPlaces.length > 0) {
            setPlaces(validPlaces);
            setLoading(false);
            return;
          }
        }
      } catch (err) {
        console.error("Error fetching territories, using seed fallback:", err);
      }
      
      // Fallback a los datos del seed (si el usuario es un visitante no registrado)
      const seedFallback = [
        {
          id: 1,
          nombre: 'Municipalidad de Providencia',
          tipo: 'SECTOR',
          numeroUnidadVecinal: 'MUN-PRO',
          comuna: 'Providencia',
          direccionSede: 'Av. Providencia 1234, Providencia',
          latitud: -33.4289,
          longitud: -70.6093,
          email: 'contacto@providencia.cl',
          telefono: '+56227241700',
          presidente: 'Alcalde de Providencia',
          descripcion: 'Edificio principal de la Municipalidad de Providencia'
        },
        {
          id: 2,
          nombre: '2ª Comisaría Carabineros de Providencia',
          tipo: 'SECTOR',
          numeroUnidadVecinal: 'CAR-PRO-02',
          comuna: 'Providencia',
          direccionSede: 'Av. Italia 1000, Providencia',
          latitud: -33.4391,
          longitud: -70.6142,
          email: 'comisaria2.providencia@carabineros.cl',
          telefono: '+56222226020',
          presidente: 'Capitán a/c 2ª Comisaría',
          descripcion: 'Cuartel de la 2ª Comisaría de Carabineros de Providencia'
        },
        {
          id: 3,
          nombre: 'Brigada de Investigación Criminal PDI – Providencia',
          tipo: 'SECTOR',
          numeroUnidadVecinal: 'PDI-BIC-PRO',
          comuna: 'Providencia',
          direccionSede: 'Av. Suecia 286, Providencia',
          latitud: -33.4248,
          longitud: -70.6050,
          email: 'bic.providencia@pdi.cl',
          telefono: '+56222071800',
          presidente: 'Jefe BIC Providencia',
          descripcion: 'Brigada de Investigación Criminal de la PDI en la comuna de Providencia'
        },
        {
          id: 4,
          nombre: 'Junta de Vecinos Villa Los Aromos',
          tipo: 'JUNTA_VECINOS',
          numeroUnidadVecinal: 'UV-01',
          comuna: 'Providencia',
          direccionSede: 'Av. Los Aromos 1250, Providencia',
          latitud: -33.4311,
          longitud: -70.6093,
          email: 'jjvv.losapomos@providencia.cl',
          telefono: '+56222341100',
          presidente: 'Carmen Gloria Muñoz',
          descripcion: 'Junta de vecinos del sector norte de Providencia'
        },
        {
          id: 5,
          nombre: 'Junta de Vecinos Los Quillayes',
          tipo: 'JUNTA_VECINOS',
          numeroUnidadVecinal: 'UV-02',
          comuna: 'Providencia',
          direccionSede: 'Calle Los Quillayes 340, Providencia',
          latitud: -33.4405,
          longitud: -70.6180,
          email: 'jjvv.losquillayes@providencia.cl',
          telefono: '+56222341200',
          presidente: 'Roberto Fuentes Díaz',
          descripcion: 'Junta de vecinos del sector sur de Providencia'
        }
      ];
      setPlaces(seedFallback);
      setLoading(false);
    };

    fetchPlaces();
  }, []);

  const filteredPlaces = places.filter(place => {
    const nameLower = (place.nombre || '').toLowerCase();
    const tipoLower = (place.tipo || '').toLowerCase();
    
    if (activeFilter === 'municipalidad') {
      return nameLower.includes('municipalidad');
    }
    if (activeFilter === 'juntas') {
      return tipoLower === 'junta_vecinos' || nameLower.includes('junta de vecinos') || tipoLower === 'junta-vecinal';
    }
    if (activeFilter === 'seguridad') {
      return nameLower.includes('comisaría') || nameLower.includes('carabineros') || nameLower.includes('pdi') || nameLower.includes('investigación');
    }
    return true; // 'todos'
  });

  return (
    <section className="map-section" id="encuentra-notarias">
      <div className="container">
        {/* Cabecera de la sección */}
        <div className="map-header text-center animate-in">
          <h2>Mapa de Servicios Municipales y Territorios</h2>
          <p className="text-muted mt-2 mb-8">
            Ubica en tiempo real la Municipalidad, las Juntas de Vecinos activas y los centros de seguridad de Providencia.
          </p>
        </div>

        <div className="map-container-glass">
          {/* Botones de Filtrado de Categorías */}
          <div className="filter-buttons-wrapper">
            <button 
              className={`filter-btn ${activeFilter === 'todos' ? 'active' : ''}`}
              onClick={() => setActiveFilter('todos')}
            >
              📍 Todos
            </button>
            <button 
              className={`filter-btn ${activeFilter === 'municipalidad' ? 'active' : ''}`}
              onClick={() => setActiveFilter('municipalidad')}
            >
              🏛️ Municipalidad
            </button>
            <button 
              className={`filter-btn ${activeFilter === 'juntas' ? 'active' : ''}`}
              onClick={() => setActiveFilter('juntas')}
            >
              🏡 Juntas de Vecinos
            </button>
            <button 
              className={`filter-btn ${activeFilter === 'seguridad' ? 'active' : ''}`}
              onClick={() => setActiveFilter('seguridad')}
            >
              🚓 Seguridad / Comisarías
            </button>
          </div>

          {/* Contenedor del Mapa Leaflet */}
          <div className="map-wrapper">
            {loading ? (
              <div className="loading-map" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', gap: '15px' }}>
                <div className="spinner"></div>
                <p style={{ color: 'var(--text-color)', fontSize: '1.1rem' }}>Cargando ubicaciones del mapa...</p>
              </div>
            ) : (
              <MapContainer
                center={DEFAULT_CENTER}
                zoom={14}
                scrollWheelZoom={false}
                className="leaflet-map"
              >
                {/* Capa base del mapa */}
                <TileLayer
                  attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />
                
                {/* Marcadores */}
                {filteredPlaces.map((place) => (
                  <Marker 
                    key={place.id} 
                    position={[place.latitud, place.longitud]}
                    icon={getCustomIcon(place.tipo, place.nombre)}
                  >
                    <Popup>
                      <div className="map-popup-card">
                        <h4>{place.nombre}</h4>
                        <p className="popup-desc">{place.descripcion || 'Sin descripción registrada.'}</p>
                        <hr className="popup-divider" />
                        <div className="popup-info">
                          {place.direccionSede && (
                            <div>📍 <strong>Sede:</strong> {place.direccionSede}</div>
                          )}
                          {place.presidente && (
                            <div>👤 <strong>Encargado:</strong> {place.presidente}</div>
                          )}
                          {place.telefono && (
                            <div>📞 <strong>Teléfono:</strong> {place.telefono}</div>
                          )}
                          {place.email && (
                            <div>✉️ <strong>Contacto:</strong> {place.email}</div>
                          )}
                        </div>
                      </div>
                    </Popup>
                  </Marker>
                ))}
              </MapContainer>
            )}
          </div>
        </div>
      </div>
    </section>
  );
};

export default MapSection;