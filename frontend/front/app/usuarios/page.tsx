import { UsersDashboard } from '../../src/screens/UserDashboard';
import { Suspense } from 'react';

export default function UsuariosPage() {
  return (
    <Suspense fallback={<div className="min-h-screen flex items-center justify-center">Cargando...</div>}>
      <UsersDashboard />
    </Suspense>
  );
}