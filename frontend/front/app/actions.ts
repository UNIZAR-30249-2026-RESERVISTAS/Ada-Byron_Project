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
    numeroEspacios,
    numeroAsistentes,
    fecha,
    horaInicio,
    duracionMinutos,
    categoria,
    detallesAdicionales,
    tipoUso
  } = data;
  
  var categoriaId = 0;

  //if(categoria === "Aula") {
  //  categoriaId = 1;
  //} else if(categoria === "Seminario") {
  //  categoriaId = 2;
  //} else if(categoria === "Laboratorio") {
  //  categoriaId = 3;
  //} else if(categoria === "Despacho") {
  //  categoriaId = 4;
  //} else if(categoria === "Sala Común") {
  //  categoriaId = 5;
  //}

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
    throw new Error(text);
  }

  return await res.json();
}
