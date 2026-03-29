'use server'

export async function reservarEspacio(prevState: any, data: FormData) {
    const espaciosIds = data.getAll('espaciosIds[]') as string[];

    const reservadaPorId = data.get('reservadaPorId') as string;
    const tipoUso = data.get('tipoUso') as string;
    const fecha = data.get('fecha') as string;
    const horaInicio = data.get('horaInicio') as string;
    const detallesAdicionales = data.get('detallesAdicionales') as string;

    const numeroAsistentes = Number(data.get('numeroAsistentes')) || 0;
    const duracionMinutos = Number(data.get('duracionMinutos')) || 0;

    const response = await fetch('http://172.31.245.33:8081/api/reservas', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            espaciosIds,
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
        const errorText = await response.text();
        console.error("Error del backend:", errorText);
        throw new Error('Error al realizar la reserva');
    }

    const result = await response.json();
    return result;
}
