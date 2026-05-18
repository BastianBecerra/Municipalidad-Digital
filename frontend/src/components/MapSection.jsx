import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import './MapSection.css';

// Fix for default marker icons in react-leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

// Mock data
const comunasData = [
  {
    id: 'santiago',
    name: 'Santiago Centro',
    coords: [-33.4489, -70.6693],
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

const MapRecenter = ({ coords }) => {
  const map = useMap();
  useEffect(() => {
    map.setView(coords, 14);
  }, [coords, map]);
  return null;
};

const MapSection = () => {
  const [selectedComunaId, setSelectedComunaId] = useState(comunasData[0].id);

  const selectedComuna = comunasData.find(c => c.id === selectedComunaId);

  return (
    <section className="map-section">
      <div className="container">
        <div className="map-header text-center">
          <h2>Encuentra Notarías cerca de ti</h2>
          <p className="text-muted mt-2 mb-8">Selecciona tu comuna para ver las notarías disponibles cerca de ti.</p>
        </div>

        <div className="map-container-glass">
          <div className="comuna-selector-wrapper">
            <label htmlFor="comuna-select" className="comuna-label">Comuna:</label>
            <select
              id="comuna-select"
              className="comuna-select"
              value={selectedComunaId}
              onChange={(e) => setSelectedComunaId(e.target.value)}
            >
              {comunasData.map((comuna) => (
                <option key={comuna.id} value={comuna.id}>
                  {comuna.name}
                </option>
              ))}
            </select>
          </div>

          <div className="map-wrapper">
            <MapContainer
              center={selectedComuna.coords}
              zoom={14}
              scrollWheelZoom={false}
              className="leaflet-map"
            >
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              <MapRecenter coords={selectedComuna.coords} />

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
