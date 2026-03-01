'use client';
import { MapContainer, TileLayer, GeoJSON } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

// Este componente recibe los datos y la planta para pintar
export default function MapaProxy({ data, floorId }: { data: any, floorId: string }) {
  return (
    <MapContainer center={[41.6836, -0.8887]} zoom={18} className="h-full w-full">
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
      {data && (
        <GeoJSON 
          key={floorId} 
          data={data} 
          onEachFeature={(feature, layer) => {
            layer.bindPopup(`Objeto: ${feature.id}`);
          }}
        />
      )}
    </MapContainer>
  );
}