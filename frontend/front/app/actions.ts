'use server'

const API_URL = process.env.NEXT_PUBLIC_BACKEND_URL;

export async function reservarEspacio(data: any) {
  const {
    reservadaPorId,
    espacioIds,
    tipoUso,
    numeroAsistentes,
    fecha,
    horaInicio,
    duracionMinutos,
    detallesAdicionales,
  } = data;

  try {
    const response = await fetch(`${API_URL}/api/reservas`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        espacioIds,
        reservadaPorId,
        tipoUso,
        numeroAsistentes,
        fecha,
        horaInicio,
        duracionMinutos,
        detallesAdicionales
      }),
    });

    if (!response.ok) {
      return { success: false, error: 'Error al realizar la reserva. Por favor, inténtalo de nuevo.' };
    }

    const result = await response.json();
    return { success: true, data: result };

  } catch (error) {
    console.error("Error en servidor:", error);
    return { success: false, error: 'Error de conexión con el servidor.' };
  }
}

export async function reservarPorCriterios(data: any) {
  const {
    reservadaPorId,
    numeroEspacios,
    numeroAsistentes,
    fecha,
    horaInicio,
    duracionMinutos,
    categoria,
    detallesAdicionales,
    tipoUso
  } = data;

  try {
    const res = await fetch(`${API_URL}/api/reservas/criterios`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify({
        reservadaPorId,
        numEspacios: Number(numeroEspacios),
        numeroAsistentes: Number(numeroAsistentes),
        fecha,
        horaInicio,
        duracionMinutos: Number(duracionMinutos),
        categoria,
        detallesAdicionales,
        tipoUso: String(tipoUso || '').toUpperCase()
      }),
    });

    if (!res.ok) {
      const text = await res.text();
      console.error("ERROR BACKEND:", text);
      // Devolvemos el error en lugar de hacer un throw para no romper el cliente
      return { success: false, error: text };
    }

    const result = await res.json();
    // Normalizamos la respuesta para que el cliente siempre lea 'success: true'
    return { success: true, data: result };

  } catch (error: any) {
    console.error("Error de red/servidor:", error);
    return { success: false, error: error.message || 'Error desconocido' };
  }
}