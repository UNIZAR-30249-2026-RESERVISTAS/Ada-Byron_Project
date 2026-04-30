'use server'

const API_URL = process.env.NEXT_PUBLIC_BACKEND_URL;

export async function reservarEspacio(data: any) {
    //console.log('Datos recibidos en reservarEspacio:', data);

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


    // URL Sustituida por variable de entorno
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
        return { success: false, message: 'Error al realizar la reserva. Por favor, inténtalo de nuevo.' };

    }

    const result = await response.json();
    return { 
        success: true, 
        data: result 
    };
}

export async function reservarPorCriterios(data: any) {
  const {
    reservadaPorId,
    numEspacios,
    capacidadTotal,
    fecha,
    horaInicio,
    duracionMinutos,
  } = data;

  const res = await fetch(`${API_URL}/api/reservas/criterios`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
    body: JSON.stringify({
      reservadaPorId,
      numEspacios: Number(numEspacios),
      capacidadTotal: Number(capacidadTotal),
      fecha,
      horaInicio,
      duracionMinutos: Number(duracionMinutos),
    }),
  });

  if (!res.ok) {
    const text = await res.text();
    console.error("ERROR BACKEND:", text);
    throw new Error(text);
  }

  return await res.json();
}
