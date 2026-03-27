"use client";

import { useState, useEffect, useMemo } from 'react';
import {
    Search,
    Edit,
    Trash2,
    ChevronLeft,
    ChevronRight,
    BookMarked
} from 'lucide-react';

// Interfaz adaptada a la estructura de PersonaDTO del backend
interface Reservation {
    id: string;
    espacioIds: string[];
    reservadaPorId: string;
    numeroAsistentes: number;
    fechaInicio: string;
    fechaFin: string;
    estado: string;
}

// Mapeo de roles a etiquetas legibles
const estadoLabels: Record<string, string> = {
    CONFIRMADA: 'Confirmada',
    POTENCIALMENTE_INVALIDA: 'Potencialmente Inválida'
};

// Mapeo de colores por rol para la UI
const estadoColors: Record<string, string> = {
    SOLICITADA: '#3B6FD4',
    CONFIRMADA: '#2A9B6F',
    RECHAZADA: '#C07A2A',
    POTENCIALMENTE_INVALIDA: '#8A8F9E',
    CANCELADA: '#C0392B'
};

const ITEMS_PER_PAGE = 10;

{/*
function UserAvatar({ name, role }: { name: string; role: string }) {
    const initials = name
        .split(' ')
        .slice(0, 2)
        .map(n => n[0])
        .join('');

    // Usamos el primer rol para determinar el color del avatar
    const displayRole = role.split(',')[0].trim();

    return (
        <div
            className="size-8 rounded-full flex items-center justify-center text-white flex-shrink-0"
            style={{ backgroundColor: roleColors[displayRole] || '#8A8F9E', fontSize: '11px', fontWeight: 600, fontFamily: "'DM Sans', sans-serif" }}
        >
            {initials}
        </div>
    );
}
*/}

export function ReservationDashboard() {
    const [reservations, setReservations] = useState<Reservation[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [estadoFilter, setEstadoFilter] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [nombres, setNombres] = useState<Record<string, string>>({});

    // Función para cargar los usuarios
    const fetchUsers = async () => {
        try {
            setLoading(true);
            const response = await fetch('http://localhost:8081/api/reservas');
            if (!response.ok) {
                throw new Error('Error al cargar los usuarios desde la API');
            }
            const data: Reservation[] = await response.json();
            setReservations(data);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Un error desconocido ha ocurrido');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    // Función para eliminar una reserva
    const handleDeleteReservation = async (id: string) => {
        if (window.confirm('¿Estás seguro de que quieres eliminar esta reserva?')) {
            try {
                const response = await fetch(`http://localhost:8081/api/reservas/eliminar/${id}`, {
                    method: 'DELETE',
                });

                if (!response.ok) {
                    // Si el status no es 2xx, lanzamos un error para que lo capture el catch
                    const errorData = await response.text();
                    throw new Error(errorData || 'Error al eliminar la reserva.');
                }

                // Si se elimina correctamente, actualizamos el estado para reflejar el cambio en la UI
                setReservations(currentReservations => currentReservations.filter(reservations => reservations.id !== id));

            } catch (err) {
                alert(err instanceof Error ? err.message : 'Un error desconocido ha ocurrido al eliminar.');
            }
        }
    };

    // Función para editar un usuario (por ahora solamente muestra un alert)
    const handleEditReservation = (reservationId: string) => {
        alert(`Funcionalidad de edición de la reserva ${reservationId} aún no implementada.`);
    };

    const buscarNombreUsuario = async (personaId: string) => {
        try {
            const response = await fetch(`http://localhost:8081/api/personas/${personaId}`);
            if (!response.ok) {
                throw new Error('Error al cargar el número de reservas');
            }
            const data = await response.json();
            setNombres(prev => ({ ...prev, [personaId]: data.nombre }));
            console.log(`Nombre de usuario para ID ${personaId}: ${data.nombre}`);
        } catch (err) {
        }
    };

    useEffect(() => {
            // Recorremos la lista de usuarios que acabamos de descargar
            reservations.forEach(r => {
                // Para evitar llamadas infinitas o repetidas, comprobamos si ya lo hemos buscado
                if (nombres[r.id] === undefined) {
                    buscarNombreUsuario(r.reservadaPorId);
                }
            });
        }, [reservations]);

    const filteredReservations = useMemo(() => {
        return reservations
            .filter(r => {
                if (searchTerm) {
                    const s = searchTerm.toLowerCase();
                    if (
                        !r.reservadaPorId.toLowerCase().includes(s)
                    ) return false;
                }
                // Filtro por rol, comprobando si alguno de los roles del usuario coincide
                if (estadoFilter && !r.estado.includes(estadoFilter)) return false;
                return true;
            })
            .sort((a, b) => new Date(b.fechaInicio).getTime() - new Date(a.fechaInicio).getTime());
    }, [searchTerm, estadoFilter, reservations]);


    const totalPages = Math.ceil(filteredReservations.length / ITEMS_PER_PAGE);
    const paginatedReservations = filteredReservations.slice(
        (currentPage - 1) * ITEMS_PER_PAGE,
        currentPage * ITEMS_PER_PAGE
    );

    const stats = {
        total: reservations.length,
    };

    const roleDistribution = useMemo(() => {
        const dist: Record<string, number> = {};
        reservations.forEach(r => {
            const estadoActual = r.estado;
            dist[estadoActual] = (dist[estadoActual] || 0) + 1;
        });
        return dist;
    }, [reservations]);

    if (loading) {
        return <div className="flex justify-center items-center h-screen">Cargando reservas...</div>;
    }

    if (error) {
        return <div className="flex justify-center items-center h-screen text-red-500">Error: {error}</div>;
    }

    return (
        <div className="h-full overflow-auto custom-scrollbar" style={{ backgroundColor: '#F5F4F0' }}>
            <div className="max-w-7xl mx-auto px-8 py-6">
                {/* Título */}
                <h2 className="font-serif-display mb-6" style={{ fontSize: '24px', color: '#1B2A4A' }}>
                    Dashboard de Reservas
                </h2>

                <div className="grid grid-cols-4 gap-4 mb-6">
                    {/*  Usuarios Totales */}
                    <div className="rounded-xl px-5 py-4" style={{ backgroundColor: '#1B2A4A' }}>
                        <div className="flex items-start justify-between">
                            <div>
                                <p style={{ fontSize: '12px', color: 'rgba(255,255,255,0.6)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
                                    Reservas Totales
                                </p>
                                <p className="font-serif-display mt-1" style={{ fontSize: '36px', color: '#fff', lineHeight: 1.1 }}>
                                    {stats.total}
                                </p>
                            </div>
                            <BookMarked className="size-5" style={{ color: 'rgba(255,255,255,0.4)' }} />
                        </div>
                        {/* Estado mini-bars */}
                        <div className="flex items-end gap-[3px] mt-2" style={{ height: '20px' }}>
                            {Object.entries(roleDistribution).map(([role, count]) => (
                                <div
                                    key={role}
                                    className="rounded-sm"
                                    style={{
                                        width: '12px',
                                        height: `${Math.max((count / Math.max(...Object.values(roleDistribution), 1)) * 20, 2)}px`,
                                        backgroundColor: estadoColors[role] || '#8A8F9E',
                                        opacity: 0.7,
                                    }}
                                />
                            ))}
                        </div>
                    </div>
                </div>

                {/* Búsqueda y filtrado */}
                <div
                    className="rounded-xl mb-5 flex items-center"
                    style={{ backgroundColor: '#fff', border: '1px solid #C8C3BB' }}
                >
                    <div className="flex-1 flex items-center px-4 py-2.5">
                        <Search className="size-4 mr-2.5 flex-shrink-0" style={{ color: '#8A8F9E' }} />
                        <input
                            type="text"
                            placeholder="Buscar por asistentes, usuario"
                            value={searchTerm}
                            onChange={e => { setSearchTerm(e.target.value); setCurrentPage(1); }}
                            className="w-full bg-transparent outline-none"
                            style={{ fontSize: '13px', fontFamily: "'DM Sans', sans-serif", color: '#1B2A4A' }}
                        />
                    </div>

                    <div className="px-3 py-2.5" style={{ borderLeft: '1px solid #E2DDD6' }}>
                        <select
                            value={estadoFilter}
                            onChange={e => { setEstadoFilter(e.target.value); setCurrentPage(1); }}
                            className="bg-transparent outline-none cursor-pointer"
                            style={{ fontSize: '13px', fontFamily: "'DM Sans', sans-serif", color: '#1B2A4A', minWidth: '160px' }}
                        >
                            <option value="">Todos los estados</option>
                            {Object.keys(estadoLabels).map(roleKey => (
                                <option key={roleKey} value={roleKey}>{estadoLabels[roleKey]}</option>
                            ))}
                        </select>
                    </div>
                </div>

                {/* Tabla Usuarios */}
                <div
                    className="rounded-xl overflow-hidden"
                    style={{ backgroundColor: '#fff', border: '1px solid #E2DDD6' }}
                >
                    <div className="overflow-x-auto custom-scrollbar">
                        <table className="w-full">
                            <thead>
                                <tr style={{ backgroundColor: '#F0EDE6' }}>
                                    {['Usuario', 'Espacios', 'Asistentes', 'Estado', 'Periodo', 'Acciones'].map(header => (
                                        <th
                                            key={header}
                                            className="px-5 py-3 text-left"
                                            style={{
                                                fontSize: '11px',
                                                color: '#6B6560',
                                                textTransform: 'uppercase',
                                                letterSpacing: '0.08em',
                                                fontFamily: "'DM Sans', sans-serif",
                                                fontWeight: 500,
                                            }}
                                        >
                                            {header}
                                        </th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody>
                                {paginatedReservations.map(reservation => (
                                    <tr
                                        key={reservation.id}
                                        className="transition-colors"
                                        style={{ borderBottom: '1px solid #EDE9E3' }}
                                        onMouseEnter={e => (e.currentTarget.style.backgroundColor = '#F7F4EF')}
                                        onMouseLeave={e => (e.currentTarget.style.backgroundColor = 'transparent')}
                                    >
                                        {/* ID Usuario */}
                                        <td className="px-5 py-3.5">
                                            <div className="flex items-center gap-1.5">
                                                <div>
                                                    <span style={{ fontSize: '14px', color: '#6B6560', fontWeight: 500, display: 'block' }}>
                                                        {nombres[reservation.reservadaPorId] || reservation.reservadaPorId}
                                                    </span>
                                                </div>
                                            </div>
                                        </td>

                                        {/* Espacios */}
                                        <td className="px-5 py-3.5">
                                            <div className="flex items-center gap-1.5">
                                                <span style={{ fontSize: '13px', color: '#6B6560' }}>
                                                    {reservation.espacioIds?.join(', ') || 'Sin espacios asignados'}
                                                </span>
                                            </div>
                                        </td>

                                        {/* Rol */}
                                        <td className="px-5 py-3.5">
                                            <span style={{ fontSize: '12px', color: '#6B6560' }} className="truncate">
                                                {reservation.numeroAsistentes}
                                            </span>
                                        </td>

                                        {/* Estado */}
                                        <td className="px-5 py-3.5">
                                            <span style={{ fontSize: '12px', color: '#6B6560' }} className="truncate">
                                                {reservation.estado}
                                            </span>
                                        </td>

                                        {/* Fechas */}
                                        <td className="px-5 py-3.5">
                                            <span style={{ fontSize: '12px', color: '#6B6560' }} className="truncate">
                                                {reservation.fechaInicio.split(' ')[0].split('T')[0]} - {reservation.fechaFin.split(' ')[0].split('T')[0]}
                                            </span>
                                        </td>

                                        {/* Reservas */}
                                        {/*
                                        <td className="px-5 py-3.5">
                                            <div className="flex items-center gap-1.5">
                                                <BookOpen className="size-3" style={{ color: '#C8C3BB' }} />
                                                <span
                                                    className="font-mono"
                                                    style={{ fontSize: '13px', color: '#1B2A4A', fontFamily: "'JetBrains Mono', monospace" }}
                                                >
                                                    0
                                                </span>
                                            </div>
                                        </td>
                                        */}

                                        {/* Acciones sobre el usuario (por el momento sin funcionalidad) */}
                                        <td className="px-5 py-3.5">
                                            <div className="flex items-center gap-1">
                                                <button
                                                    onClick={() => handleEditReservation(reservation.id)}
                                                    className="p-2 rounded-lg transition-all"
                                                    title="Editar usuario"
                                                    onMouseEnter={e => {
                                                        e.currentTarget.style.backgroundColor = 'rgba(59,111,212,0.1)';
                                                    }}
                                                    onMouseLeave={e => {
                                                        e.currentTarget.style.backgroundColor = 'transparent';
                                                    }}
                                                >
                                                    <Edit className="size-4" style={{ color: '#8A8F9E' }}
                                                        onMouseEnter={e => (e.currentTarget.style.color = '#3B6FD4')}
                                                        onMouseLeave={e => (e.currentTarget.style.color = '#8A8F9E')}
                                                    />
                                                </button>
                                                <button
                                                    onClick={() => handleDeleteReservation(reservation.id)}
                                                    className="p-2 rounded-lg transition-all"
                                                    title="Eliminar usuario"
                                                    onMouseEnter={e => {
                                                        e.currentTarget.style.backgroundColor = 'rgba(212,59,59,0.1)';
                                                    }}
                                                    onMouseLeave={e => {
                                                        e.currentTarget.style.backgroundColor = 'transparent';
                                                    }}
                                                >
                                                    <Trash2 className="size-4" style={{ color: '#8A8F9E' }}
                                                        onMouseEnter={e => (e.currentTarget.style.color = '#D43B3B')}
                                                        onMouseLeave={e => (e.currentTarget.style.color = '#8A8F9E')}
                                                    />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    {/* Empty state */}
                    {filteredReservations.length === 0 && (
                        <div className="text-center py-16">
                            <BookMarked className="size-10 mx-auto mb-3" style={{ color: '#C8C3BB' }} />
                            <p style={{ fontSize: '14px', color: '#8A8F9E' }}>
                                No se encontraron reservas
                            </p>
                        </div>
                    )}

                    {/* Paginación */}
                    {filteredReservations.length > 0 && (
                        <div
                            className="flex items-center justify-between px-5 py-3"
                            style={{ borderTop: '1px solid #EDE9E3' }}
                        >
                            <span style={{ fontSize: '12px', color: '#8A8F9E' }}>
                                Mostrando {(currentPage - 1) * ITEMS_PER_PAGE + 1}–
                                {Math.min(currentPage * ITEMS_PER_PAGE, filteredReservations.length)} de{' '}
                                {filteredReservations.length} usuarios
                            </span>
                            <div className="flex items-center gap-1">
                                <button
                                    onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                                    disabled={currentPage === 1}
                                    className="p-1.5 rounded-md transition-colors disabled:opacity-30"
                                    style={{ color: '#6B6560' }}
                                    onMouseEnter={e => { if (!e.currentTarget.disabled) e.currentTarget.style.backgroundColor = '#E2DDD6'; }}
                                    onMouseLeave={e => { if (!e.currentTarget.disabled) e.currentTarget.style.backgroundColor = 'transparent'; }}
                                >
                                    <ChevronLeft className="size-4" />
                                </button>
                                <span style={{ fontSize: '12px', color: '#1B2A4A', fontFamily: "'DM Sans', sans-serif" }}>
                                    Página {currentPage} de {totalPages}
                                </span>
                                <button
                                    onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                                    disabled={currentPage === totalPages}
                                    className="p-1.5 rounded-md transition-colors disabled:opacity-30"
                                    style={{ color: '#6B6560' }}
                                    onMouseEnter={e => { if (!e.currentTarget.disabled) e.currentTarget.style.backgroundColor = '#E2DDD6'; }}
                                    onMouseLeave={e => { if (!e.currentTarget.disabled) e.currentTarget.style.backgroundColor = 'transparent'; }}
                                >
                                    <ChevronRight className="size-4" />
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}