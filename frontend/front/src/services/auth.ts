

const API_URL = process.env.NEXT_PUBLIC_BACKEND_URL; // Sustituida por variable de entorno

// Roles disponibles 
export type Rol =
  | 'ESTUDIANTE'
  | 'DOCENTE_INVESTIGADOR'
  | 'INVESTIGADOR_CONTRATADO'
  | 'CONSERJE'
  | 'TECNICO_LABORATORIO'
  | 'GERENTE';

// Interfaz que coincide con PersonaDTO del backend
export interface User {
  id: string;
  nombre: string;
  email: string;
  roles: Rol[];
  departamentoId: number | null;
  departamentoNombre: string | null;
}

// Clave para almacenar el usuario en sessionStorage (más seguro que localStorage)
const USER_STORAGE_KEY = 'adabyron_user';

/**
 * Login: Autentica al usuario con email y contraseña.
 */
export async function loginUser(email: string, password: string): Promise<User> {
  const res = await fetch(`${API_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include', // permite enviar/recibir cookies
    body: JSON.stringify({ email, password }),
  });

  if (!res.ok) {
    if (res.status === 401) {
      throw new Error('Credenciales inválidas. Verifica tu correo y contraseña.');
    }
    throw new Error('Error al iniciar sesión. Inténtalo de nuevo.');
  }

  const user: User = await res.json();

  // Guardamos datos del usuario en sessionStorage (no la sesión, que está en cookie HttpOnly)
  if (typeof window !== 'undefined') {
    sessionStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
  }

  return user;
}

/**
 * Obtenemos el usuario actual desde sessionStorage.
 */
export function getCurrentUser(): User | null {
  if (typeof window === 'undefined') return null;

  const stored = sessionStorage.getItem(USER_STORAGE_KEY);
  if (!stored) return null;

  try {
    return JSON.parse(stored) as User;
  } catch {
    return null;
  }
}

/**
 * Verifica si la sesión actual es válida consultando al servidor.
 * Útil para comprobar si el usuario sigue autenticado.
 */
export async function checkSession(): Promise<boolean> {
  try {
    const res = await fetch(`${API_URL}/api/auth/me`, {
      credentials: 'include',
    });
    return res.ok;
  } catch {
    return false;
  }
}

/**
 * Para cerrar la sesión del usuario.
 */
export async function logoutUser(): Promise<void> {
  try {
    await fetch(`${API_URL}/api/auth/logout`, {
      method: 'POST',
      credentials: 'include',
    });
  } catch {
    // Ignoramos errores de red en logout
  }

  if (typeof window !== 'undefined') {
    sessionStorage.removeItem(USER_STORAGE_KEY);
  }
}

/**
 * Verifica si el usuario tiene el rol de GERENTE
 */
export function isGerente(user: User): boolean {
  return user.roles.includes('GERENTE');
}
