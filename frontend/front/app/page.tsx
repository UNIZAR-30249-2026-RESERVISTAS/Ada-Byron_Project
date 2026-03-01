'use client';
import { useState, useEffect } from 'react';
import dynamic from 'next/dynamic';

// Importamos el mapa indicando que NO se renderice en el servidor (ssr: false)
const MapWithNoSSR = dynamic(() => import('./MapaProxy'), { 
  ssr: false,
  loading: () => <div className="h-full w-full bg-gray-100 flex items-center justify-center">Cargando mapa...</div>
});

export default function PaginaPrincipal() {
  const [selectedFloor, setSelectedFloor] = useState('planta0');
  const [geoData, setGeoData] = useState(null);

  const floors = [
    { id: 'sotano1', name: 'Sótano 1' },
    { id: 'planta0', name: 'Planta Baja' },
    { id: 'planta1', name: 'Planta 1' },
    { id: 'planta2', name: 'Planta 2' },
    { id: 'planta3', name: 'Planta 3' },
    { id: 'planta4', name: 'Planta 4' },
  ];

  useEffect(() => {
    const fetchData = async () => {
      try {
        const timestamp = new Date().getTime();
        const response = await fetch(`http://localhost:5000/collections/${selectedFloor}/items?limit=100&_=${timestamp}`, {
          cache: 'no-store',
          headers: {
            'Pragma': 'no-cache',
            'Cache-Control': 'no-cache',
          },
        });
        const data = await response.json();
        setGeoData(data);
      } catch (error) {
        console.error("Error cargando la planta:", error);
      }
    };
    fetchData();
  }, [selectedFloor]);

  return (
    <div className="flex flex-col h-screen w-screen overflow-hidden">
      {/* Selector superior */}
      <div className="p-4 bg-white shadow-md z-[1000] flex gap-4 items-center">
        <select 
          value={selectedFloor} 
          onChange={(e) => setSelectedFloor(e.target.value)}
          className="p-2 border rounded"
        >
          {floors.map(f => <option key={f.id} value={f.id}>{f.name}</option>)}
        </select>
      </div>

      {/* Contenedor del Mapa */}
      <div className="flex-1 relative">
        <MapWithNoSSR data={geoData} floorId={selectedFloor} />
      </div>
    </div>
  );
}