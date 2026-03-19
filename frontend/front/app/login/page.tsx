import { Suspense } from "react";
import { Login } from "../../src/screens/Login";

export default function LoginPage() {
  return (
    <Suspense fallback={<div className="min-h-screen flex items-center justify-center">Cargando...</div>}>
      <Login />
    </Suspense>
  );
}