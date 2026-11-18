const BASE = import.meta.env.VITE_API_URL ?? '';

export class ApiError extends Error {
  constructor(codigo, mensaje, detalles = [], estado = 0) {
    super(mensaje);
    this.codigo = codigo;
    this.detalles = detalles;
    this.estado = estado;
  }
}

async function solicitar(ruta, opciones = {}) {
  let res;
  try {
    res = await fetch(BASE + ruta, {
      ...opciones,
      headers: { Accept: 'application/json', ...(opciones.headers ?? {}) },
    });
  } catch {
    throw new ApiError('SIN_CONEXION', 'No se pudo contactar el servidor. Revise su conexión e intente de nuevo.');
  }
  let cuerpo = null;
  try {
    cuerpo = await res.json();
  } catch {
    /* cuerpo vacío o no JSON */
  }
  if (!res.ok) {
    throw new ApiError(
      cuerpo?.codigo ?? 'ERROR_DESCONOCIDO',
      cuerpo?.mensaje ?? 'Ocurrió un error inesperado.',
      cuerpo?.detalles ?? [],
      res.status,
    );
  }
  return cuerpo;
}

export const verificarContribuyente = (correo, ruc) =>
  solicitar('/api/v1/contribuyentes/verificaciones', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ correo, ruc }),
  });

export const consultarVehiculo = (placa) =>
  solicitar(`/api/v1/vehiculos/${encodeURIComponent(placa)}`);

export const consultarPuntos = (cedula) =>
  solicitar(`/api/v1/licencias/${encodeURIComponent(cedula)}/puntos`);
