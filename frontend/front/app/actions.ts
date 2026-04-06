'use server'

const API_URL = process.env.NEXT_PUBLIC_BACKEND_URL;

export async function reservarEspacio(prevState: any, data: FormData) {
    //console.log('Datos recibidos en reservarEspacio:', data);

    const rawEspacios = data.get('espacioIds') as string || '';
    const espacioIds = rawEspacios.split(',').map(id => id.trim()).filter(id => id !== '');

    const reservadaPorId = data.get('reservadaPorId') as string;
    let tipoUso = data.get('tipoUso') as string;
    if (tipoUso === 'Docencia') tipoUso = 'DOCENCIA';
    if (tipoUso === 'Investigación') tipoUso = 'INVESTIGACION';
    if (tipoUso === 'Gestión') tipoUso = 'GESTION';
    if (tipoUso === 'Otro') tipoUso = 'OTRO';

    const fecha = data.get('fecha') as string;
    const horaInicio = data.get('horaInicio') as string;
    const detallesAdicionales = data.get('detallesAdicionales') as string;

    const numeroAsistentes = Number(data.get('numeroAsistentes')) || 0;
    const duracionMinutos = Number(data.get('duracionMinutos')) || 0;



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
