"use client";

import { useState, useEffect, useMemo } from 'react';
import {
  Search,
  Edit,
  Trash2,
  Users,
  ChevronLeft,
  ChevronRight,
  Mail,
  BookOpen,
} from 'lucide-react';

// Interfaz adaptada a la estructura de PersonaDTO del backend
interface SystemUser {
  id: string;
  nombre: string;
  email: string;
  roles: string[]; 
  departamentoNombre?: string; // Opcional, por si no todos los usuarios tienen departamento
}

// Mapeo de roles a etiquetas legibles
const roleLabels: Record<string, string> = {
  ESTUDIANTE: 'Estudiante',
  DOCENTE_INVESTIGADOR: 'Docente-Investigador',
  INVESTIGADOR_CONTRATADO: 'Investigador Contratado',
  CONSERJE: 'Conserje',
  TECNICO_LABORATORIO: 'Técnico de Laboratorio',
  GERENTE: 'Gerente',
};

// Mapeo de colores por rol para la UI
const roleColors: Record<string, string> = {
  ESTUDIANTE: '#3B6FD4',
  DOCENTE_INVESTIGADOR: '#2A9B6F',
  INVESTIGADOR_CONTRATADO: '#C07A2A',
  CONSERJE: '#8A8F9E',
  TECNICO_LABORATORIO: '#7B52A8',
  GERENTE: '#C0392B',
};

const ITEMS_PER_PAGE = 10;

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

export function UsersDashboard() {
    const [users, setUsers] = useState<SystemUser[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [roleFilter, setRoleFilter] = useState('');
    const [currentPage, setCurrentPage] = useState(1);

    // Función para cargar los usuarios
    const fetchUsers = async () => {
        try {
        setLoading(true);
        const response = await fetch('http://localhost:8081/api/personas');
        if (!response.ok) {
            throw new Error('Error al cargar los usuarios desde la API');
        }
        const data: SystemUser[] = await response.json();
        setUsers(data);
        } catch (err) {
        setError(err instanceof Error ? err.message : 'Un error desconocido ha ocurrido');
        } finally {
        setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);
    
    // Función para eliminar un usuario
    const handleDeleteUser = async (userId: string) => {
        if (window.confirm('¿Estás seguro de que quieres eliminar a este usuario?')) {
        try {
            const response = await fetch(`http://localhost:8081/api/personas/${userId}`, {
            method: 'DELETE',
            });

            if (!response.ok) {
            // Si el status no es 2xx, lanzamos un error para que lo capture el catch
            const errorData = await response.text();
            throw new Error(errorData || 'Error al eliminar el usuario.');
            }

            // Si se elimina correctamente, actualizamos el estado para reflejar el cambio en la UI
            setUsers(currentUsers => currentUsers.filter(user => user.id !== userId));

        } catch (err) {
            alert(err instanceof Error ? err.message : 'Un error desconocido ha ocurrido al eliminar.');
        }
        }
    };

    // Función para editar un usuario (por ahora solamente muestra un alert)
    const handleEditUser = (userNombre: string) => {
        alert(`Funcionalidad de edición del usuario ${userNombre} aún no implementada.`);
    };

    const filteredUsers = useMemo(() => {
      return users
        .filter(u => {
          if (searchTerm) {
            const s = searchTerm.toLowerCase();
            if (
              !u.nombre.toLowerCase().includes(s) &&
              !u.email.toLowerCase().includes(s) &&
              !u.id.toLowerCase().includes(s) &&
              !u.departamentoNombre?.toLowerCase().includes(s)    
            ) return false;
          }
          // Filtro por rol, comprobando si alguno de los roles del usuario coincide
          if (roleFilter && !u.roles.includes(roleFilter)) return false;
          return true;
        })
        .sort((a, b) => a.nombre.localeCompare(b.nombre));
    }, [searchTerm, roleFilter, users]);

    const totalPages = Math.ceil(filteredUsers.length / ITEMS_PER_PAGE);
    const paginatedUsers = filteredUsers.slice(
      (currentPage - 1) * ITEMS_PER_PAGE,
      currentPage * ITEMS_PER_PAGE
    );

    const stats = {
      total: users.length,
    };

    const roleDistribution = useMemo(() => {
      const dist: Record<string, number> = {};
      users.forEach(u => {
        u.roles.forEach(role => {
          dist[role] = (dist[role] || 0) + 1;
        });
      });
      return dist;
    }, [users]);

    if (loading) {
      return <div className="flex justify-center items-center h-screen">Cargando usuarios...</div>;
    }

    if (error) {
      return <div className="flex justify-center items-center h-screen text-red-500">Error: {error}</div>;
    }

    return (
        <div className="h-full overflow-auto custom-scrollbar" style={{ backgroundColor: '#F5F4F0' }}>
        <div className="max-w-7xl mx-auto px-8 py-6">
            {/* Título */}
            <h2 className="font-serif-display mb-6" style={{ fontSize: '24px', color: '#1B2A4A' }}>
            Dashboard de Usuarios
            </h2>

            <div className="grid grid-cols-4 gap-4 mb-6">
            {/*  Usuarios Totales */}
            <div className="rounded-xl px-5 py-4" style={{ backgroundColor: '#1B2A4A' }}>
                <div className="flex items-start justify-between">
                <div>
                    <p style={{ fontSize: '12px', color: 'rgba(255,255,255,0.6)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
                    Usuarios Totales
                    </p>
                    <p className="font-serif-display mt-1" style={{ fontSize: '36px', color: '#fff', lineHeight: 1.1 }}>
                    {stats.total}
                    </p>
                </div>
                <Users className="size-5" style={{ color: 'rgba(255,255,255,0.4)' }} />
                </div>
                {/* Role mini-bars */}
                <div className="flex items-end gap-[3px] mt-2" style={{ height: '20px' }}>
                {Object.entries(roleDistribution).map(([role, count]) => (
                    <div
                    key={role}
                    className="rounded-sm"
                    style={{
                        width: '12px',
                        height: `${Math.max((count / Math.max(...Object.values(roleDistribution), 1)) * 20, 2)}px`,
                        backgroundColor: roleColors[role] || '#8A8F9E',
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
                placeholder="Buscar por nombre, email o ID..."
                value={searchTerm}
                onChange={e => { setSearchTerm(e.target.value); setCurrentPage(1); }}
                className="w-full bg-transparent outline-none"
                style={{ fontSize: '13px', fontFamily: "'DM Sans', sans-serif", color: '#1B2A4A' }}
                />
            </div>

            <div className="px-3 py-2.5" style={{ borderLeft: '1px solid #E2DDD6' }}>
                <select
                value={roleFilter}
                onChange={e => { setRoleFilter(e.target.value); setCurrentPage(1); }}
                className="bg-transparent outline-none cursor-pointer"
                style={{ fontSize: '13px', fontFamily: "'DM Sans', sans-serif", color: '#1B2A4A', minWidth: '160px' }}
                >
                <option value="">Todos los roles</option>
                {Object.keys(roleLabels).map(roleKey => (
                    <option key={roleKey} value={roleKey}>{roleLabels[roleKey]}</option>
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
                    {['Usuario', 'Email', 'Rol', 'Departamento', 'Reservas', 'Acciones'].map(header => (
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
                    {paginatedUsers.map(user => (
                    <tr
                        key={user.id}
                        className="transition-colors"
                        style={{ borderBottom: '1px solid #EDE9E3' }}
                        onMouseEnter={e => (e.currentTarget.style.backgroundColor = '#F7F4EF')}
                        onMouseLeave={e => (e.currentTarget.style.backgroundColor = 'transparent')}
                    >
                        {/* Usuario */}
                        <td className="px-5 py-3.5">
                        <div className="flex items-center gap-2.5">
                            <UserAvatar name={user.nombre} role={user.roles.join(', ')} />
                            <div>
                            <span style={{ fontSize: '14px', color: '#1B2A4A', fontWeight: 500, display: 'block' }}>
                                {user.nombre}
                            </span>
                            </div>
                        </div>
                        </td>

                        {/* Email */}
                        <td className="px-5 py-3.5">
                        <div className="flex items-center gap-1.5">
                            <Mail className="size-3 flex-shrink-0" style={{ color: '#C8C3BB' }} />
                            <span style={{ fontSize: '13px', color: '#1B2A4A' }}>
                            {user.email}
                            </span>
                        </div>
                        </td>

                        {/* Rol */}
                        <td className="px-5 py-3.5">
                        <div className="flex flex-wrap gap-1">
                            {user.roles.map(role => (
                            <span
                                key={role}
                                className="inline-flex items-center px-2.5 py-1 rounded-full"
                                style={{
                                backgroundColor: `${roleColors[role] || '#8A8F9E'}15`,
                                color: roleColors[role] || '#8A8F9E',
                                fontSize: '11px',
                                fontWeight: 500,
                                }}
                            >
                                <div
                                className="size-1.5 rounded-full mr-1.5 flex-shrink-0"
                                style={{ backgroundColor: roleColors[role] || '#8A8F9E' }}
                                />
                                {roleLabels[role] || role}
                            </span>
                            ))}
                        </div>
                        </td>

                        {/* Departmento */}
                        <td className="px-5 py-3.5">
                        <span style={{ fontSize: '12px', color: '#6B6560' }} className="truncate">
                            {user.departamentoNombre ?? 'N/A'}
                        </span>
                        </td>

                        {/* Reservas */}
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

                        {/* Acciones sobre el usuario (por el momento sin funcionalidad) */}
                        <td className="px-5 py-3.5">
                        <div className="flex items-center gap-1">
                            <button
                            onClick={() => handleEditUser(user.nombre)}
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
                            onClick={() => handleDeleteUser(user.id)}
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
            {filteredUsers.length === 0 && (
                <div className="text-center py-16">
                <Users className="size-10 mx-auto mb-3" style={{ color: '#C8C3BB' }} />
                <p style={{ fontSize: '14px', color: '#8A8F9E' }}>
                    No se encontraron usuarios
                </p>
                </div>
            )}

            {/* Paginación */}
            {filteredUsers.length > 0 && (
                <div
                className="flex items-center justify-between px-5 py-3"
                style={{ borderTop: '1px solid #EDE9E3' }}
                >
                <span style={{ fontSize: '12px', color: '#8A8F9E' }}>
                    Mostrando {(currentPage - 1) * ITEMS_PER_PAGE + 1}–
                    {Math.min(currentPage * ITEMS_PER_PAGE, filteredUsers.length)} de{' '}
                    {filteredUsers.length} usuarios
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