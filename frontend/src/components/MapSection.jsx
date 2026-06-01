import { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import './MapSection.css';

// --- CONFIGURACIÓN DE LEAFLET ---
// Solución al problema común de React-Leaflet donde los íconos por defecto de los marcadores no cargan.
// Se eliminan las URLs rotas por defecto y se reemplazan por los enlaces directos a un CDN seguro.
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

// --- DATOS MOCK (Simulación de Base de Datos) ---
// Estructura de datos que simula la respuesta de una API con las comunas y sus notarías asociadas.
const comunasData = [
  {
    id: 'santiago',
    name: 'Santiago Centro',
    coords: [-33.4489, -70.6693], // Coordenadas centrales de la comuna
    notarias: [
      { id: 1, name: 'Notaría Patricio Raby', address: 'Moneda 920, Santiago', coords: [-33.4418, -70.6508] },
      { id: 2, name: 'Notaría Elena Leyton', address: 'Huérfanos 1160, Santiago', coords: [-33.4395, -70.6531] },
      { id: 3, name: 'Notaría Iván Torrealba', address: 'Agustinas 1070, Santiago', coords: [-33.4411, -70.6515] },
    ]
  },
  {
    id: 'providencia',
    name: 'Providencia',
    coords: [-33.4326, -70.6133],
    notarias: [
      { id: 4, name: 'Notaría Francisco Rosas', address: 'Av. Nueva Providencia 2353, Providencia', coords: [-33.4215, -70.6052] },
      { id: 5, name: 'Notaría Antonieta Mendoza', address: 'San Pío X 2460, Providencia', coords: [-33.4228, -70.6031] },
    ]
  },
  {
    id: 'las_condes',
    name: 'Las Condes',
    coords: [-33.4115, -70.5707],
    notarias: [
      { id: 6, name: 'Notaría René Benavente', address: 'Huérfanos 979, Las Condes', coords: [-33.4098, -70.5812] },
      { id: 7, name: 'Notaría Patricio Zaldívar', address: 'Apoquindo 3076, Las Condes', coords: [-33.4150, -70.5980] },
    ]
  }
];

// --- COMPONENTE AUXILIAR: MapRecenter ---
// Leaflet no actualiza el centro del mapa automáticamente cuando cambian las coordenadas en el estado.
// Este hook personalizado "escucha" los cambios en 'coords' y fuerza a la instancia del mapa a moverse a la nueva ubicación.
const MapRecenter = ({ coords }) => {
  const map = useMap();
  useEffect(() => {
    map.setView(coords, 14); // 14 es el nivel de zoom
  }, [coords, map]);
  return null; // No renderiza nada visualmente, solo ejecuta la lógica
};

// --- COMPONENTE PRINCIPAL ---
const MapSection = () => {
  // Estado para controlar qué comuna está seleccionada actualmente en el dropdown
  const [selectedComunaId, setSelectedComunaId] = useState(comunasData[0].id);

  // Derivamos los datos completos de la comuna seleccionada a partir de su ID
  const selectedComuna = comunasData.find(c => c.id === selectedComunaId);

  return (
    <section className="map-section" id='encuentra-notarias'>
      <div className="container">
        {/* Cabecera de la sección */}
        <div className="map-header text-center">
          <h2>Encuentra Notarías cerca de ti</h2>
          <p className="text-muted mt-2 mb-8">Selecciona tu comuna para ver las notarías disponibles cerca de ti.</p>
        </div>

        <div className="map-container-glass">
          {/* Selector de Comunas */}
          <div className="comuna-selector-wrapper">
            <label htmlFor="comuna-select" className="comuna-label">Comuna:</label>
            <select
              id="comuna-select"
              className="comuna-select"
              value={selectedComunaId}
              onChange={(e) => setSelectedComunaId(e.target.value)} // Actualiza el estado al cambiar la opción
            >
              {/* Mapeo dinámico de las opciones disponibles en comunasData */}
              {comunasData.map((comuna) => (
                <option key={comuna.id} value={comuna.id}>
                  {comuna.name}
                </option>
              ))}
            </select>
          </div>

          {/* Contenedor del Mapa Leaflet */}
          <div className="map-wrapper">
            <MapContainer
              center={selectedComuna.coords}
              zoom={14}
              scrollWheelZoom={false} // Desactivado para evitar que el usuario haga zoom por accidente al scrollear la página
              className="leaflet-map"
            >
              {/* Capa base del mapa (Diseño visual de OpenStreetMap) */}
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              
              {/* Componente que fuerza el reposicionamiento al cambiar de comuna */}
              <MapRecenter coords={selectedComuna.coords} />

              {/* Renderizado dinámico de los pines (Marcadores) para las notarías de la comuna seleccionada */}
              {selectedComuna.notarias.map((notaria) => (
                <Marker key={notaria.id} position={notaria.coords}>
                  <Popup>
                    <strong>{notaria.name}</strong><br />
                    {notaria.address}
                  </Popup>
                </Marker>
              ))}
            </MapContainer>
          </div>
        </div>
      </div>
    </section>
  );
};

export default MapSection;