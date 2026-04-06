import { UsersDashboard } from '../../src/screens/UserDashboard';
import { Suspense } from 'react';
import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';

async function requireGerente() {
  const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL;

  if (!backendUrl) {
    redirect('/login');
  }

  const cookieStore = await cookies();
  const cookieHeader = cookieStore
    .getAll()
    .map((c) => c.name + '=' + c.value)
    .join('; ');

  const res = await fetch(`${backendUrl}/api/auth/me`, {
    method: 'GET',
    headers: { cookie: cookieHeader },
    cache: 'no-store',
  });

  if (!res.ok) {
    redirect('/login');
  }

  const me = await res.json();
  const roles = Array.isArray(me.roles) ? me.roles.map((r: unknown) => String(r)) : [];

  if (!roles.includes('GERENTE')) {
    redirect('/');
  }
}

export default async function UsuariosPage() {
  await requireGerente();

  return (
    <Suspense fallback={<div className="min-h-screen flex items-center justify-center">Cargando...</div>}>
      <UsersDashboard />
    </Suspense>
  );
}