'use client';
import { MapContainer, TileLayer, GeoJSON } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

const API_URL = process.env.NEXT_PUBLIC_BACKEND_URL;

export default function MapaProxy({ data, floorId }: { data: any, floorId: string }) {
  const getStyle = (feature: any) => {
    const tipo = feature.properties?.USO || 'default';
    if (tipo === 'LABORATORIO') {
      return { color: 'orange', weight: 2, fillColor: '#FFCC80', fillOpacity: 0.6 };
    } else if (tipo === 'DESPACHO') {
      return { color: 'purple', weight: 2, fillColor: '#E1BEE7', fillOpacity: 0.6 };
    } else if (tipo === 'AULA') {
      return { color: 'blue', weight: 2, fillColor: 'lightblue', fillOpacity: 0.6 };
    } else if (tipo === 'SEMINARIO') {
      return { color: 'green', weight: 2, fillColor: 'lightgreen', fillOpacity: 0.6 };
    } else if (tipo === 'SALA COMÚN') {
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

            layer.bindPopup('<div style="padding: 10px;">Cargando información... ⏳</div>');
            //layer.bindPopup(`Objeto: ${feature.id}`);

            layer.on('click', async () => {

              const idDominio = feature.properties.espacio_id;

              if (!idDominio) {
                layer.setPopupContent('<div style="padding: 5px;">Espacio sin ID asignado</div>');
                //layer.bindPopup(`Objeto: ${feature.id}`);
                return;
              }

              try {// URL Sustituida por variable de entorno
                const response = await fetch(`${API_URL}/api/espacios/${idDominio}`);

                if (!response.ok) {
                  throw new Error('No se encontraron los datos en el servidor');
                }

                const datos = await response.json();

                const contenidoHTML = `
                <div style="font-family: 'DM Sans', sans-serif; min-width: 160px; padding: 5px;">
                  <h3 style="margin: 0 0 8px 0; color: #1B2A4A; font-size: 15px; font-weight: 600; border-bottom: 1px solid #eee; padding-bottom: 4px;">
                    ${datos.categoria} ${datos.id}
                  </h3>
                  <p style="margin: 5px 0; font-size: 13px; color: #4A5568;">
                    <b>👥 Capacidad:</b> ${datos.ocupantes} personas
                  </p>
                  <p style="margin: 5px 0; font-size: 13px; color: #4A5568;">
                    <b>📅 Reservable:</b> ${datos.reservable ? 'Sí ✅' : 'No ❌'}
                  </p>
                  <p style="margin: 5px 0; font-size: 13px; color: #4A5568;">
                    <b>📏 Tamaño:</b> ${datos.area ? datos.area + ' m²' : '--'}
                  </p>
                </div>
                `;

                layer.setPopupContent(contenidoHTML);

              } catch (error) {
                console.error("Error al obtener los datos del espacio:", error);
                layer.setPopupContent(`
                  <div style="color: red; padding: 5px;">
                    Datos de negocio no disponibles para ${idDominio}.
                  </div>
                `);
              }
            });
          }}
        />
      )}
    </MapContainer>
  );
}