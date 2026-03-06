'use client';
import { MapContainer, TileLayer, GeoJSON } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

export default function MapaProxy({ data, floorId }: { data: any, floorId: string }) {
  const getStyle = (feature: any) => {
    const tipo = feature.properties?.USO || 'default';
    if (tipo === 'LABORATORIO'){
      return { color: 'orange', weight: 2, fillColor: '#FFCC80', fillOpacity: 0.6 };
    } else if (tipo === 'DESPACHO') {
      return { color: 'purple', weight: 2, fillColor: '#E1BEE7', fillOpacity: 0.6 };
    } else if (tipo === 'AULA'){
      return { color: 'blue', weight: 2, fillColor: 'lightblue', fillOpacity: 0.6 };
    } else if (tipo === 'SEMINARIO') {
      return { color: 'green', weight: 2, fillColor: 'lightgreen', fillOpacity: 0.6 };
    } else if (tipo === 'SALA COMÚN'){
      return { color: 'red', weight: 1, fillColor: '#ef5757', fillOpacity: 0.6 };
    } else {
      return { color: '#CCCCCC', weight: 1, fillColor: '#FFFFFF', fillOpacity: 0.3 }
    }
};

  return (
    <MapContainer center={[41.6836, -0.8887]} zoom={18} className="h-full w-full">
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
      {data && (
        <GeoJSON 
          key={floorId} 
          data={data} 
          style={getStyle}
          onEachFeature={(feature, layer) => {
            layer.bindPopup(`Objeto: ${feature.id}`);
          }}
        />
      )}
    </MapContainer>
  );
}