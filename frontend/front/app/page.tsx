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
  const [filterCategory, setFilterCategory] = useState('');

  const currentFloor = selectedFloor.startsWith('planta')
    ? parseInt(selectedFloor.replace('planta', ''))
    : (selectedFloor === 'sotano1' ? -1 : 0);

  const changeFloor = (floorNum: any) => {
    if (floorNum === -1) setSelectedFloor('sotano1');
    else if (floorNum === 0) setSelectedFloor('planta0');
    else setSelectedFloor(`planta${floorNum}`);
  };

  const categories = [
    { key: 'aula', label: 'Aula', color: { color: 'blue', weight: 2, fillColor: 'lightblue', fillOpacity: 0.8 } },
    { key: 'seminario', label: 'Seminario', color: { color: 'green', weight: 2, fillColor: 'lightgreen', fillOpacity: 0.8 } },
    { key: 'laboratorio', label: 'Laboratorio', color: { color: 'orange', weight: 2, fillColor: '#FFCC80', fillOpacity: 0.8 } },
    { key: 'despacho', label: 'Despacho', color: { color: 'purple', weight: 2, fillColor: '#E1BEE7', fillOpacity: 0.8 } },
    { key: 'sala común', label: 'Sala Común', color: { color: 'red', weight: 1, fillColor: '#ef5757', fillOpacity: 0.8 } },
  ];



  useEffect(() => {
    const fetchData = async () => {
      setGeoData(null);
      try {
        const timestamp = new Date().getTime();
        let url = `http://localhost:5000/collections/${selectedFloor}/items?limit=100&_=${timestamp}`;
        if (filterCategory) {
          const categoryUpper = filterCategory.toUpperCase();
          url += `&properties=USO&additionalProp1=%7B%7D&skipGeometry=false&offset=0&USO=${categoryUpper}`;
        }
        const response = await fetch(url, {
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
  }, [selectedFloor, filterCategory]);

  return (
    <div className="flex flex-row h-screen w-screen overflow-hidden">
      <aside
        className="h-full overflow-y-auto custom-scrollbar"
        style={{
          backgroundColor: '#EEEBE4',
          borderRight: '1px solid #D4CFC6',
          width: '260px',
        }}
      >
        <div className="pt-4 pl-2" style={{ borderTop: '1px solid #D4CFC6' }}>
          <span
            className="block mb-2.5"
            style={{ fontSize: '11px', color: '#6B6560', textTransform: 'uppercase', letterSpacing: '0.06em' }}
          >
            Leyenda
          </span>
          <div className="space-y-1.5 mb-4">
            {categories.map(cat => (
              <div key={cat.key} className="flex items-center gap-2.5">
                <div
                  className="rounded-sm flex-shrink-0"
                  style={{
                    width: '14px',
                    height: '8px',
                    backgroundColor: cat.color.fillColor,
                    borderColor: cat.color.color,
                    fillOpacity: cat.color.fillOpacity,
                    borderRadius: '2px',
                  }}
                />
                <span style={{ fontSize: '12px', color: '#1B2A4A' }}>{cat.label}</span>
              </div>
            ))}
          </div>
        </div>
        <div className="pl-2">
          <label
            className="block mb-1.5"
            style={{ fontSize: '11px', color: '#6B6560', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            Categoría
          </label>
          <select
            id="categoria"
            value={filterCategory}
            onChange={(e) => setFilterCategory(e.target.value)}
            className="w-2/3 px-3 py-2 bg-white rounded-md appearance-none cursor-pointer"
            style={{
              border: '1px solid #C8C3BB',
              fontSize: '13px',
              color: '#1B2A4A',
              outline: 'none',
              fontFamily: "'DM Sans', sans-serif",
              backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%238A8F9E' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E")`,
              backgroundRepeat: 'no-repeat',
              backgroundPosition: 'right 10px center',
            }}
          >
            <option value="" style={{ color: '#1B2A4A' }}>Todas las categorías</option>
            {categories.map(cat => (
              <option key={cat.key} value={cat.key}>{cat.label}</option>
            ))}
          </select>
        </div>
      </aside>

      <div
        className="flex flex-col py-4 px-2 flex-shrink-0"
        style={{ backgroundColor: '#EEEBE4', borderRight: '1px solid #D4CFC6', width: '64px' }}
      >
        <span
          className="text-center mb-3"
          style={{ fontSize: '9px', color: '#8A8F9E', textTransform: 'uppercase', letterSpacing: '0.08em' }}
        >
          Planta
        </span>
        {[-1, 0, 1, 2, 3, 4].map(floor => (
          <button
            key={floor}
            onClick={() => changeFloor(floor)}
            className="relative mb-2 py-3 rounded-md transition-all text-center"
            style={{
              backgroundColor: currentFloor === floor ? '#E8E3DC' : 'transparent',
              borderLeft: currentFloor === floor ? '3px solid #3B6FD4' : '3px solid transparent',
            }}
          >
            <span
              className="block"
              style={{
                fontSize: '15px',
                color: currentFloor === floor ? '#1B2A4A' : '#8A8F9E',
                fontFamily: "'DM Sans', sans-serif",
                fontWeight: currentFloor === floor ? 600 : 400,
              }}
            >
              P{floor}
            </span>
            <span
              className="block"
              style={{
                fontSize: '9px',
                color: currentFloor === floor ? '#2A9B6F' : '#8A8F9E',
                marginTop: '2px',
              }}
            >
            </span>
          </button>
        ))}
      </div>

      {/* Contenedor del Mapa */}
      <div className="flex-1 relative">
        <MapWithNoSSR data={geoData} floorId={selectedFloor} />
      </div>
    </div>
  );
}