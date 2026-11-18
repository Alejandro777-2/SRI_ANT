// Validación de apoyo en el cliente (feedback inmediato). El backend es la fuente de verdad.

export function validarCorreo(v) {
  if (!v.trim()) return 'Ingrese su correo electrónico.';
  if (!/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(v.trim())) return 'El correo no tiene un formato válido.';
  return null;
}

export function validarRuc(v) {
  if (!v) return 'Ingrese su RUC.';
  if (!/^\d{13}$/.test(v)) return 'El RUC debe tener 13 dígitos numéricos.';
  return null;
}

export function validarPlaca(v) {
  const p = normalizarPlaca(v);
  if (!p) return 'Ingrese la matrícula de su vehículo.';
  if (!/^[A-Z0-9]{5,17}$/.test(p)) return 'La matrícula debe tener entre 5 y 17 caracteres (ej. PBX1234).';
  return null;
}

export const normalizarPlaca = (v) => v.trim().toUpperCase().replace(/[-\s]/g, '');

export const cedulaDeRuc = (ruc) => ruc.slice(0, 10);
