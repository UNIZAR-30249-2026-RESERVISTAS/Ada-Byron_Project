'use client';
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import dynamic from 'next/dynamic';
import { LogOut, User, Users, BookMarked } from 'lucide-react';
import { getCurrentUser, logoutUser } from '../src/services/auth';
import { reservarEspacio, reservarPorCriterios } from './actions';

interface ReservaData {
  espacioIds: string;
  tipoUso: string;
  numeroAsistentes: number;
  fecha: string;
  horaInicio: string;
  duracionMinutos: number;
  detallesAdicionales: string;
  numeroEspacios: number;
  categoria: string;
}

// Importamos el mapa indicando que NO se renderice en el servidor (ssr: false)
const MapWithNoSSR = dynamic(() => import('./MapaProxy'), {
  ssr: false,
  loading: () => <div className="h-full w-full bg-gray-100 flex items-center justify-center">Cargando mapa...</div>
});

const API_URL = process.env.NEXT_PUBLIC_BACKEND_URL;

export default function PaginaPrincipal() {
  const router = useRouter();
  const [selectedFloor, setSelectedFloor] = useState('planta0');
  const [geoData, setGeoData] = useState(null);
  const [filterCategory, setFilterCategory] = useState('');
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [filterId, setFilterId] = useState('');
  const [filterOcupantes, setFilterOcupantes] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modoReserva, setModoReserva] = useState<'ids' | 'criterios'>('ids');
  const [ocupacion, setOcupacion] = useState(0.0);

  const [modalContent, setModalContent] = useState<ReservaData>({
    espacioIds: '',
    tipoUso: '',
    numeroAsistentes: 0,
    fecha: '',
    horaInicio: '',
    duracionMinutos: 0,
    detallesAdicionales: '',
    numeroEspacios: 0,
    categoria: ''
  });

  useEffect(() => {
    const fetchOcupacion = async () => {
      try {
        const response = await fetch(`${API_URL}/api/edificio/ocupacion`, {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Accept': 'application/json'
          }
        });

        if (!response.ok) {
          throw new Error(`Error en la petición: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();

        setOcupacion(data.porcentajeOcupacionMaxima);

      } catch (error) {
        console.error("Error al cargar la ocupación:", error);
      }
    };

    // No olvides llamar a la función dentro del useEffect
    fetchOcupacion();
  }, []); // El array vacío asegura que se ejecute solo al montar el componente

  // Reemplazamos useActionState por estados simples
  const [isPending, setIsPending] = useState(false);
  const [estadoReserva, setEstadoReserva] = useState('');

  const [user, setUser] = useState<any>(null);
  const [mostrarPopUp, setMostrarPopUp] = useState(false);
  const [tipoUso, setTipoUso] = useState('');
  const [cargandoTipoUso, setCargandoTipoUso] = useState(false);

  useEffect(() => {
    const textoIds = modalContent.espacioIds || '';
    const primerId = textoIds.split(',')[0]?.trim();

    // Si no hay ID, limpiamos el campo
    // Si no hay ID o el ID está incompleto (ej. "0" o "00"), limpiamos y NO hacemos petición
    if (!primerId || primerId.length < 3) {
      setTipoUso('');
      return;
    }

    const obtenerTipoDeUso = async () => {
      setCargandoTipoUso(true);
      try {
        const response = await fetch(`${API_URL}/api/espacios/${primerId}`);
        const data = await response.json();

        if (data.categoria === 'Aula') {
          setTipoUso('Docencia');
        } else if (data.categoria === 'Despacho') {
          setTipoUso('Gestión');
        } else if (data.categoria === 'Laboratorio' || data.categoria === 'Seminario') {
          setTipoUso('Investigación');
        } else {
          setTipoUso('Otro');
        }
      } catch (error) {
        console.error("Error al obtener el espacio:", error);
        setTipoUso('Error al cargar');
      } finally {
        setCargandoTipoUso(false);
      }
    };

    const timeoutId = setTimeout(() => {
      obtenerTipoDeUso();
    }, 500);

    return () => clearTimeout(timeoutId);
  }, [modalContent.espacioIds]);

  useEffect(() => {
    setUser(getCurrentUser());
  }, []);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setIsPending(true);

    const form = e.currentTarget;
    const formData = new FormData(form);

    // Formateo de Tipo de Uso para el Enum de Java
    const rawTipoUso = String(formData.get('tipoUso') || '');
    let tipoUsoFormateado = rawTipoUso.toUpperCase();
    if (tipoUsoFormateado === 'INVESTIGACIÓN') tipoUsoFormateado = 'INVESTIGACION';
    if (tipoUsoFormateado === 'GESTIÓN') tipoUsoFormateado = 'GESTION';

    try {
      let result;

      if (modoReserva === 'ids') {
        const idsString = String(formData.get('espacioIds') || '');
        const idsArray = idsString.split(',').map(id => id.trim()).filter(id => id !== '');

        result = await reservarEspacio({
          reservadaPorId: formData.get('reservadaPorId'),
          espacioIds: idsArray,
          tipoUso: tipoUsoFormateado,
          numeroAsistentes: Number(formData.get('numeroAsistentes')),
          fecha: formData.get('fecha'),
          horaInicio: formData.get('horaInicio'),
          duracionMinutos: Number(formData.get('duracionMinutos')),
          detallesAdicionales: formData.get('detallesAdicionales'),
        });
      } else {
        result = await reservarPorCriterios({
          reservadaPorId: String(formData.get('reservadaPorId') || ''),
          numeroEspacios: Number(formData.get('numeroEspacios') || 0),
          numeroAsistentes: Number(formData.get('numeroAsistentes') || 0),
          fecha: String(formData.get('fecha') || ''),
          horaInicio: String(formData.get('horaInicio') || ''),
          duracionMinutos: Number(formData.get('duracionMinutos') || 0),
          categoria: String(formData.get('categoria') || ''),
          detallesAdicionales: formData.get('detallesAdicionales'),
          tipoUso: tipoUsoFormateado
        });
      }

      if (result?.success) {
        // 1. Extraemos los IDs del array 'espacioIds' que viene en 'data'
        const idsArray = result.data?.espacioIds || [];
        let idsTexto = "";
        let etiqueta = "";

        if (idsArray.length > 0) {
          idsTexto = idsArray.join(', '); // Unimos los IDs con coma: "301, 002"
          // Si hay más de uno ponemos "Aulas", si no "Aula"
          etiqueta = idsArray.length > 1 ? "Aulas " : "Aula ";
        }

        // 2. Obtenemos el estado (CONFIRMADA)
        const mensajeBase = result.data?.estado || 'Reserva completada';

        // 3. Montamos el mensaje final: "CONFIRMADA: Aulas 301, 002"
        const mensajeFinal = idsTexto
          ? `${mensajeBase}: ${etiqueta}${idsTexto}`
          : mensajeBase;

        setEstadoReserva(mensajeFinal);
        setMostrarPopUp(true);
        setIsModalOpen(false);

        // Dejamos el popup 4 segundos para que dé tiempo a leer los IDs
        setTimeout(() => setMostrarPopUp(false), 4000);
      } else {
        alert(result?.error || 'Ocurrió un error al intentar reservar.');
      }
    } catch (error) {
      alert('Error de conexión con el servidor.');
    } finally {
      setIsPending(false);
    }
  };

  const handleLogout = async () => {
    setIsLoggingOut(true);
    await logoutUser();
    router.push('/login');
  }

  const handleGoToUsersDashboard = () => {
    if (!user?.roles?.includes('GERENTE')) return;
    router.push('/usuarios');
  };
  const handleGoToReservationsDashboard = () => {
    if (!user?.roles?.includes('GERENTE')) return;
    router.push('/reservas');
  };

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
    if (name === 'numeroAsistentes' || name === 'duracionMinutos') {
      setModalContent(prev => ({ ...prev, [name]: Number(value) }));
    } else {
      setModalContent(prev => ({ ...prev, [name]: value }));
    }
  };


  useEffect(() => {
  const peticionTimeout = setTimeout(async () => {
    
    setGeoData(null);
    try {
      let idsValidos = null;

      if (filterOcupantes) {
        const resSpring = await fetch(`${API_URL}/api/espacios/filtrarPorAforo?personas=${filterOcupantes}`);
        if (resSpring.ok) {
          idsValidos = await resSpring.json();
        }
      }

      const timestamp = new Date().getTime();
      let url = `http://localhost:5000/collections/${selectedFloor}/items?limit=100&_=${timestamp}`;
      
      if (filterCategory) {
        const categoryUpper = filterCategory.toUpperCase();
        const categoryFiltro =filterCategory === 'sala común' ? 'SALA COMéN' : categoryUpper;
        url += `&properties=USO,espacio_id&additionalProp1=%7B%7D&skipGeometry=false&offset=0&USO=${encodeURIComponent(categoryFiltro)}`;
      } else if (filterId) {
        url += `&properties=USO,espacio_id&additionalProp1=%7B%7D&skipGeometry=false&offset=0&espacio_id=${filterId}`;
      }

      const response = await fetch(url, {
        cache: 'no-store',
        headers: {
          'Pragma': 'no-cache',
          'Cache-Control': 'no-cache',
        },
      });
      let geoJsonData = await response.json();
      if (idsValidos !== null) {
        geoJsonData.features = geoJsonData.features.filter((feature: any) => 
          idsValidos.includes(feature.properties.espacio_id)
        );
      }

      setGeoData(geoJsonData);

    } catch (error) {
      console.error("Error cargando la planta o cruzando datos:", error);
    }
    
  }, 400);
  return () => clearTimeout(peticionTimeout);

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
        {user?.roles?.includes('GERENTE') && (
          <div className='flex flex-row'>
            <div>
              <p style={{ fontSize: '12px', color: '#1B2A4A', fontFamily: "'DM Sans', sans-serif", fontWeight: 500 }}>
                Porcentaje de ocupacion actual: {ocupacion}%
              </p>
            </div>
          </div>
        )}

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
              id="identificador_ocupantes"
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
                className='w-1/2 text-white py-2 rounded-lg font-medium transition-colors' style={{ backgroundColor: '#1B2A4A' }}>
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

            <form className="space-y-4" onSubmit={handleSubmit}>
              <div className="flex gap-2 mb-4">
                <button
                  type="button"
                  onClick={() => setModoReserva('ids')}
                  className={`px-3 py-1 rounded transition-colors ${modoReserva === 'ids' ? 'bg-[#1B2A4A] text-white' : 'bg-white'}`}
                >
                  Por IDs
                </button>
                <button
                  type="button"
                  onClick={() => setModoReserva('criterios')}
                  className={`px-3 py-1 rounded transition-colors ${modoReserva === 'criterios' ? 'bg-[#1B2A4A] text-white' : 'bg-white'}`}
                >
                  Por criterios
                </button>
              </div>

              <input type="hidden" name="reservadaPorId" value={user?.id || ''} />

              {modoReserva === 'ids' && (
                <>
                  <div>
                    <label className="block text-[11px] text-[#6B6560] uppercase mb-1">
                      IDs de Espacios (separados por coma)
                    </label>
                    <input
                      type="text"
                      name="espacioIds"
                      placeholder="101, 102..."
                      value={modalContent.espacioIds}
                      onChange={handleChange}
                      className="w-full bg-white px-3 py-2 border border-[#C8C3BB] text-[13px] rounded-md placeholder-gray-400 text-[#1B2A4A] outline-none"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    {user.roles.includes('GERENTE') ? (
                      <div>
                        <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Tipo de Uso</label>
                        <select
                          name="tipoUso"
                          value={modalContent.tipoUso}
                          onChange={handleChange}
                          className="w-full px-3 py-2 bg-white border border-[#C8C3BB] rounded-md text-[13px] text-[#1B2A4A] outline-none"
                        >
                          <option value="Docencia">Docencia</option>
                          <option value="Investigación">Investigación</option>
                          <option value="Gestión">Gestión</option>
                          <option value="Otro">Otro</option>
                        </select>
                      </div>
                    ) : (
                      <div>
                        <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Tipo de Uso</label>
                        <input type="hidden" name="tipoUso" value={tipoUso} />
                        <input
                          type="text"
                          readOnly
                          value={cargandoTipoUso ? 'Buscando...' : tipoUso}
                          placeholder="Se autocompleta con el ID..."
                          className="w-full px-3 py-2 bg-[#E5E2DC] border border-[#C8C3BB] rounded-md text-[13px] text-[#6B6560] cursor-not-allowed outline-none font-medium"
                        />
                      </div>
                    )}

                    <div>
                      <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Asistentes</label>
                      <input
                        type="text"
                        name="numeroAsistentes"
                        value={modalContent.numeroAsistentes}
                        onChange={handleChange}
                        className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] placeholder-gray-400 text-[#1B2A4A] outline-none"
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Fecha</label>
                      <input
                        type="date"
                        name="fecha"
                        value={modalContent.fecha}
                        onChange={handleChange}
                        className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] placeholder-gray-400 text-[#1B2A4A] outline-none"
                      />
                    </div>
                    <div>
                      <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Hora Inicio</label>
                      <input
                        type="time"
                        name="horaInicio"
                        value={modalContent.horaInicio}
                        onChange={handleChange}
                        className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] placeholder-gray-400 text-[#1B2A4A] outline-none"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Duración (minutos)</label>
                    <input
                      type="text"
                      name="duracionMinutos"
                      value={modalContent.duracionMinutos}
                      onChange={handleChange}
                      className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] placeholder-gray-400 text-[#1B2A4A] outline-none"
                    />
                  </div>

                  <div>
                    <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Mensaje Opcional</label>
                    <textarea
                      name="detallesAdicionales"
                      value={modalContent.detallesAdicionales}
                      onChange={handleChange}
                      className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] resize-none placeholder-gray-400 text-[#1B2A4A] outline-none"
                      placeholder="Indique detalles adicionales"
                    ></textarea>
                  </div>
                </>
              )}

              {modoReserva === 'criterios' && (
                <div className="space-y-3">
                  <div>
                    <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Número de espacios</label>
                    <input
                      type="text"
                      name="numeroEspacios"
                      value={modalContent.numeroEspacios}
                      onChange={handleChange}
                      className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] placeholder-gray-400 text-[#1B2A4A] outline-none"
                    />
                  </div>

                  {user.roles.includes('GERENTE') ? (
                    <div>
                      <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Tipo de Uso</label>
                      <select
                        name="tipoUso"
                        value={modalContent.tipoUso}
                        onChange={handleChange}
                        className="w-full px-3 py-2 bg-white border border-[#C8C3BB] rounded-md text-[13px] text-[#1B2A4A] outline-none"
                      >
                        <option value="Docencia">Docencia</option>
                        <option value="Investigación">Investigación</option>
                        <option value="Gestión">Gestión</option>
                        <option value="Otro">Otro</option>
                      </select>
                    </div>
                  ) : (
                    <div>
                      <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Tipo de Uso</label>
                      <input type="hidden" name="tipoUso" value={tipoUso} />
                      <input
                        type="text"
                        readOnly
                        value={cargandoTipoUso ? 'Buscando...' : tipoUso}
                        placeholder="Se autocompleta con el ID..."
                        className="w-full px-3 py-2 bg-[#E5E2DC] border border-[#C8C3BB] rounded-md text-[13px] text-[#6B6560] cursor-not-allowed outline-none font-medium"
                      />
                    </div>
                  )}

                  <div>
                    <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Categoría</label>
                    <input
                      type="text"
                      name="categoria"
                      placeholder="Aula, Seminario, Laboratorio, Despacho o Sala Común"
                      value={modalContent.categoria}
                      onChange={handleChange}
                      className="w-full bg-white px-3 py-2 border border-[#C8C3BB] text-[13px] rounded-md placeholder-gray-400 text-[#1B2A4A] outline-none"
                    />
                  </div>

                  <div>
                    <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Asistentes</label>
                    <input
                      type="text"
                      name="numeroAsistentes"
                      value={modalContent.numeroAsistentes}
                      onChange={handleChange}
                      className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] placeholder-gray-400 text-[#1B2A4A] outline-none"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Fecha</label>
                      <input
                        type="date"
                        name="fecha"
                        value={modalContent.fecha}
                        onChange={handleChange}
                        className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] placeholder-gray-400 text-[#1B2A4A] outline-none"
                      />
                    </div>
                    <div>
                      <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Hora Inicio</label>
                      <input
                        type="time"
                        name="horaInicio"
                        value={modalContent.horaInicio}
                        onChange={handleChange}
                        className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] placeholder-gray-400 text-[#1B2A4A] outline-none"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Duración (minutos)</label>
                    <input
                      type="text"
                      name="duracionMinutos"
                      value={modalContent.duracionMinutos}
                      onChange={handleChange}
                      className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] placeholder-gray-400 text-[#1B2A4A] outline-none"
                    />
                  </div>

                  <div>
                    <label className="block text-[11px] text-[#6B6560] uppercase mb-1">Mensaje Opcional</label>
                    <textarea
                      name="detallesAdicionales"
                      value={modalContent.detallesAdicionales}
                      onChange={handleChange}
                      className="w-full bg-white px-3 py-2 border border-[#C8C3BB] rounded-md text-[13px] resize-none placeholder-gray-400 text-[#1B2A4A] outline-none"
                      placeholder="Indique detalles adicionales"
                    ></textarea>
                  </div>
                </div>
              )}

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
                  disabled={isPending}
                  className="flex-1 py-2 text-[13px] text-white bg-[#1B2A4A] rounded-lg hover:bg-[#3B6FD4] transition-all disabled:bg-gray-400 disabled:cursor-not-allowed"
                >
                  {isPending ? 'Procesando...' : 'Confirmar Reserva'}
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
          </button>
        ))}
      </div>

      {/* Contenedor del Mapa */}
      <div className="flex-1 relative">
        <MapWithNoSSR data={geoData} floorId={selectedFloor} />
      </div>

      {mostrarPopUp && (
        <div className="fixed bottom-10 left-1/2 transform -translate-x-1/2 z-[9999] animate-in fade-in slide-in-from-bottom-4 duration-300">
          <div className="bg-[#1B2A4A] text-white px-8 py-4 rounded-full shadow-2xl flex items-center space-x-3 border-2 border-[#1B2A4A] whitespace-nowrap">
            <span className="font-bold">{estadoReserva}</span>
          </div>
        </div>
      )}
    </div>
  );
}