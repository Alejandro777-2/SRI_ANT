import { describe, expect, it } from 'vitest';
import { cedulaDeRuc, normalizarPlaca, validarCorreo, validarPlaca, validarRuc } from './validators.js';

describe('validadores del cliente', () => {
  it('valida correo', () => {
    expect(validarCorreo('a@b.com')).toBeNull();
    expect(validarCorreo('a@b')).not.toBeNull();
    expect(validarCorreo('  ')).not.toBeNull();
  });

  it('valida RUC de 13 dígitos', () => {
    expect(validarRuc('1710034065001')).toBeNull();
    expect(validarRuc('171003406500')).not.toBeNull();
    expect(validarRuc('17100340650ab')).not.toBeNull();
  });

  it('normaliza y valida matrícula', () => {
    expect(normalizarPlaca(' pbx-1234 ')).toBe('PBX1234');
    expect(validarPlaca('pbx-1234')).toBeNull();
    expect(validarPlaca('A1')).not.toBeNull();
  });

  it('obtiene la cédula desde el RUC', () => {
    expect(cedulaDeRuc('1710034065001')).toBe('1710034065');
  });
});
