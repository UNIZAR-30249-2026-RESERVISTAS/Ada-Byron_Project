import { ReservationDashboard } from '../../src/screens/ReservationDashboard';
import { Suspense } from 'react';

export default function ReservasPage() {
  return (
    <Suspense fallback={<div className="min-h-screen flex items-center justify-center">Cargando...</div>}>
      <ReservationDashboard />
    </Suspense>
  );
}