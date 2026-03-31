'use client';
import { useState, useEffect, useActionState, use } from 'react';
import { useRouter } from 'next/navigation';
import dynamic from 'next/dynamic';
import { LogOut, User, Users, BookMarked } from 'lucide-react';
import { getCurrentUser, logoutUser, checkSession } from '../src/services/auth';
import { reservarEspacio } from './actions';


interface ReservaData {
  espacioIds: string[];
  tipoUso: string;
  numeroAsistentes: number;
  fecha: string;
  horaInicio: string;
  duracionMinutos: number;
  detallesAdicionales: string;
}

// Importamos el mapa indicando que NO se renderice en el servidor (ssr: false)
const MapWithNoSSR = dynamic(() => import('./MapaProxy'), {
  ssr: false,
  loading: () => <div className="h-full w-full bg-gray-100 flex items-center justify-center">Cargando mapa...</div>
});

export default function PaginaPrincipal() {
  const router = useRouter();
  const [selectedFloor, setSelectedFloor] = useState('planta0');
  const [geoData, setGeoData] = useState(null);
  const [filterCategory, setFilterCategory] = useState('');
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [filterId, setFilterId] = useState('');
  const [filterOcupantes, setFilterOcupantes] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalContent, setModalContent] = useState<ReservaData>({
    espacioIds: [],
    tipoUso: '',
    numeroAsistentes: 0,
    fecha: '',
    horaInicio: '',
    duracionMinutos: 0,
    detallesAdicionales: ''
  });
  const [state, formAction, isPending] = useActionState(reservarEspacio, null);
  const [user, setUser] = useState<any>(null);

  useEffect(() => {
    setUser(getCurrentUser());
  }, []);
    


  const handleLogout = async () => {
    setIsLoggingOut(true);
    await logoutUser();
    router.push('/login');
  }

  const handleGoToUsersDashboard = () => {
    router.push('/usuarios');
  };

  const handleGoToReservationsDashboard = () => {
    router.push('/reservas');
  }

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

  const toggleModal = () => {
    setIsModalOpen(!isModalOpen);
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;

    if (name === 'espacioIds') {
      // Aquí le decimos que 'id' es un string
      const idsArray = value.split(',').map((id: string) => id.trim()).filter((id: string) => id !== "");
      setModalContent(prev => ({ ...prev, [name]: idsArray }));
    } else {
      // Para los números (asistentes y duración), es buena idea convertirlos
      if (name === 'numeroAsistentes' || name === 'duracionMinutos') {
        setModalContent(prev => ({ ...prev, [name]: Number(value) }));
      } else {
        setModalContent(prev => ({ ...prev, [name]: value }));
      }
    }
  };


  useEffect(() => {
    const fetchData = async () => {
      setGeoData(null);
      try {
        const timestamp = new Date().getTime();
        let url = `http://localhost:5000/collections/${selectedFloor}/items?limit=100&_=${timestamp}`;
        if (filterCategory) {
          const categoryUpper = filterCategory.toUpperCase();
          url += `&properties=USO,espacio_id&additionalProp1=%7B%7D&skipGeometry=false&offset=0&USO=${categoryUpper}`;
        }
        else if (filterId) {
          url += `&properties=USO,espacio_id&additionalProp1=%7B%7D&skipGeometry=false&offset=0&espacio_id=${filterId}`;
        }
        else if (filterOcupantes) {
          //url += `&properties=USO,espacio_id&additionalProp1=%7B%7D&skipGeometry=false&offset=0&ocupantes_min=${filterOcupantes}`;
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
  }, [selectedFloor, filterCategory, filterId, filterOcupantes]);

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
        <div
          className="flex items-center justify-between px-4 py-3"
          style={{ borderBottom: '1px solid #D4CFC6' }}
        >
          <div className="flex items-center gap-2 overflow-hidden">
            <div
              className="size-7 rounded-full flex items-center justify-center flex-shrink-0"
              style={{ backgroundColor: '#1B2A4A' }}
            >
              <User className="size-3.5" style={{ color: 'rgba(255,255,255,0.85)' }} />
            </div>
            <div className="overflow-hidden">
              <p
                className="truncate"
                style={{ fontSize: '12px', color: '#1B2A4A', fontFamily: "'DM Sans', sans-serif", fontWeight: 500 }}
              >
                {user?.nombre ?? 'Usuario'}
              </p>
              <p
                className="truncate"
                style={{ fontSize: '10px', color: '#8A8F9E', fontFamily: "'DM Sans', sans-serif" }}
              >
                {user?.email ?? ''}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-1 flex-shrink-0">

            {/* Botón Dashboard Usuarios — solo visible para GERENTE */}
            {user?.roles?.includes('GERENTE') && (
              <button
                onClick={handleGoToUsersDashboard}
                title="Dashboard de usuarios"
                className="p-1.5 rounded-lg transition-colors"
                style={{ color: '#8A8F9E' }}
                onMouseEnter={e => (e.currentTarget.style.color = '#3B6FD4')}
                onMouseLeave={e => (e.currentTarget.style.color = '#8A8F9E')}
              >
                <Users className="size-4" />
              </button>
            )}

            {/* Botón Dashboard Reservas — solo visible para GERENTE */}
            {user?.roles?.includes('GERENTE') && (
              <button
                onClick={handleGoToReservationsDashboard}
                title="Dashboard de reservas"
                className="p-1.5 rounded-lg transition-colors"
                style={{ color: '#8A8F9E' }}
                onMouseEnter={e => (e.currentTarget.style.color = '#3B6FD4')}
                onMouseLeave={e => (e.currentTarget.style.color = '#8A8F9E')}
              >
                <BookMarked className="size-4" />
              </button>
            )}

            {/* Botón de cerrar sesión */}
            <button
              onClick={handleLogout}
              disabled={isLoggingOut}
              title="Cerrar sesión"
              className="flex-shrink-0 p-1.5 rounded-lg transition-colors"
              style={{ color: '#8A8F9E' }}
              onMouseEnter={e => (e.currentTarget.style.color = '#C0392B')}
              onMouseLeave={e => (e.currentTarget.style.color = '#8A8F9E')}
            >
              <LogOut className="size-4" />
            </button>
          </div>
        </div>

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
        <div className="pl-2 mt-4">
          <label
            className="block mb-1.5"
            style={{
              fontSize: '11px',
              color: '#6B6560',
              textTransform: 'uppercase',
              letterSpacing: '0.06em'
            }}
          >
            Identificador
          </label>

          <div className="relative w-7/8">
            <input
              type="text"
              id="identificador"
              placeholder="Buscar por ID"
              value={filterId}
              onChange={(e) => setFilterId(e.target.value)}
              className="w-full py-2 pr-3 pl-9 bg-white rounded-md placeholder-gray-400"
              style={{
                border: '1px solid #C8C3BB',
                fontSize: '13px',
                color: '#1B2A4A',
                outline: 'none',
                fontFamily: "'DM Sans', sans-serif",
              }}
            />
          </div>
        </div>
        <div className="pl-2 mt-3">
          <label
            className="block mb-1.5"
            style={{ fontSize: '11px', color: '#6B6560', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            Categoría
          </label>
          <select
            id="categoria"
            value={filterCategory}
            onChange={(e) => setFilterCategory(e.target.value)}
            className="w-7/8 px-3 py-2 bg-white rounded-md appearance-none cursor-pointer"
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
        <div className="pl-2 mt-4">
          <label
            className="block mb-1.5"
            style={{
              fontSize: '11px',
              color: '#6B6560',
              textTransform: 'uppercase',
              letterSpacing: '0.06em'
            }}
          >
            Ocupantes mínimos
          </label>
          <div className="relative w-7/8">
            <input
              type="text"
              id="identificador"
              placeholder="Mín. ocupantes"
              value={filterOcupantes}
              onChange={(e) => setFilterOcupantes(e.target.value)}
              className="w-full py-2 pr-3 pl-2 bg-white rounded-md placeholder-gray-400"
              style={{
                border: '1px solid #C8C3BB',
                fontSize: '13px',
                color: '#1B2A4A',
                outline: 'none',
                fontFamily: "'DM Sans', sans-serif",
              }}
            />
          </div>
          {user && (
            <div className="pl-2 mt-4">
              <button
                onClick={toggleModal}
                className='w-1/2 text-white py-2 rounded-lg font-mediu transtion-colors' style={{ backgroundColor: '#1B2A4A' }}>
                Hacer Reserva
              </button>
            </div>
          )}
        </div>
      </aside>

      {isModalOpen && user && (
        <div className="fixed inset-0 z-[10000] flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto custom-scrollbar" style={{ backgroundColor: '#EEEBE4' }}>
            <div className="flex justify-between items-center mb-5 border-b pb-3">
              <h2 className="text-xl font-bold text-[#1B2A4A]">Datos Reserva</h2>
              <button onClick={() => setIsModalOpen(false)} className="text-gray-400 hover:text-gray-600">✕</button>
            </div>

            <form className="space-y-4" action={formAction}>

              <input type="hidden" name="reservadaPorId" value={user?.id || ''} />

              {/* Espacios IDs (Vector) */}
              <div>
                <label className="block text-[11px] text-[#6B6560] uppercase mb-1">IDs de Espacios (separados por coma)</label>
                <input
                  type="text"
                  name="espacioIds"
                  placeholder="101, 102..."
                  value={modalContent.espacioIds.join(', ')}
                  onChange={handleChange}
                  className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md focus:ring-2 focus:ring-[#3B6FD4] outline-none text-[13px] placeholder-gray-400 text-[#1B2A4A]"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                {/* Tipo de Uso */}
                <div>
                  <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Tipo de Uso</label>
                  <select
                    name="tipoUso"
                    value={modalContent.tipoUso}
                    onChange={handleChange}
                    className="w-full px-3 py-2 bg-white border border-[#C8C3BB] rounded-md text-[13px] text-[#1B2A4A]"
                  >
                    <option value="Docencia">Docencia</option>
                    <option value="Investigación">Investigación</option>
                    <option value="Gestión">Gestión</option>
                    <option value="Otro">Otro</option>
                  </select>
                </div>
                {/* Número Asistentes */}
                <div>
                  <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Asistentes</label>
                  <input
                    type="text"
                    name="numeroAsistentes"
                    value={modalContent.numeroAsistentes}
                    onChange={handleChange}
                    className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] placeholder-gray-400 text-[#1B2A4A]"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                {/* Fecha */}
                <div>
                  <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Fecha</label>
                  <input
                    type="date"
                    name="fecha"
                    value={modalContent.fecha}
                    onChange={handleChange}
                    className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] placeholder-gray-400 text-[#1B2A4A]"
                  />
                </div>
                {/* Hora Inicio */}
                <div>
                  <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Hora Inicio</label>
                  <input
                    type="time"
                    name="horaInicio"
                    value={modalContent.horaInicio}
                    onChange={handleChange}
                    className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] placeholder-gray-400 text-[#1B2A4A]"
                  />
                </div>
              </div>

              {/* Duración */}
              <div>
                <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Duración (minutos)</label>
                <input
                  type="text"
                  name="duracionMinutos"
                  value={modalContent.duracionMinutos}
                  onChange={handleChange}
                  className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] placeholder-gray-400 text-[#1B2A4A]"
                />
              </div>

              {/* Detalles Adicionales */}
              <div>
                <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Mensaje Opcional</label>
                <textarea
                  name="detallesAdicionales"
                  value={modalContent.detallesAdicionales}
                  onChange={handleChange}
                  className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] resize-none placeholder-gray-400 text-[#1B2A4A]"
                  placeholder="Indique detalles adicionales"
                ></textarea>
              </div>

              <div className="pt-4 flex gap-3">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="flex-1 bg-white py-2 text-[13px] text-gray-500 hover:bg-gray-100 rounded-lg transition-colors"
                >
                  Descartar
                </button>
                <button
                  type="submit"
                  className="flex-1 py-2 text-[13px] text-white bg-[#1B2A4A] rounded-lg hover:bg-[#3B6FD4] transition-all"
                >
                  Confirmar Reserva
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

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