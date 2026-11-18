import { useState } from 'react';
import { validarCorreo, validarRuc } from '../validators.js';

export default function FormIdentidad({ cargando, onEnviar }) {
  const [correo, setCorreo] = useState('');
  const [ruc, setRuc] = useState('');
  const [errores, setErrores] = useState({});

  function enviar(e) {
    e.preventDefault();
    const nuevos = { correo: validarCorreo(correo), ruc: validarRuc(ruc) };
    setErrores(nuevos);
    if (nuevos.correo || nuevos.ruc) return;
    onEnviar(correo.trim(), ruc);
  }

  return (
    <form onSubmit={enviar} noValidate>
      <label htmlFor="correo">Correo electrónico</label>
      <input
        id="correo"
        type="email"
        autoComplete="email"
        value={correo}
        onChange={(e) => setCorreo(e.target.value)}
        aria-invalid={!!errores.correo}
        aria-describedby={errores.correo ? 'err-correo' : undefined}
        placeholder="nombre@ejemplo.com"
      />
      {errores.correo && <p id="err-correo" className="campo-error">{errores.correo}</p>}

      <label htmlFor="ruc">RUC (persona natural)</label>
      <input
        id="ruc"
        inputMode="numeric"
        maxLength={13}
        value={ruc}
        onChange={(e) => setRuc(e.target.value.replace(/\D/g, ''))}
        aria-invalid={!!errores.ruc}
        aria-describedby={errores.ruc ? 'err-ruc' : 'ayuda-ruc'}
        placeholder="1710034065001"
      />
      {errores.ruc ? (
        <p id="err-ruc" className="campo-error">{errores.ruc}</p>
      ) : (
        <p id="ayuda-ruc" className="ayuda">13 dígitos: su cédula seguida de 001.</p>
      )}

      <button type="submit" disabled={cargando}>
        {cargando ? 'Verificando…' : 'Verificar contribuyente'}
      </button>
    </form>
  );
}
