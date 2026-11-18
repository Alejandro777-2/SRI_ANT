import { useState } from 'react';
import { validarPlaca } from '../validators.js';

export default function FormVehiculo({ cargando, onEnviar }) {
  const [placa, setPlaca] = useState('');
  const [error, setError] = useState(null);

  function enviar(e) {
    e.preventDefault();
    const err = validarPlaca(placa);
    setError(err);
    if (!err) onEnviar(placa);
  }

  return (
    <form onSubmit={enviar} noValidate>
      <label htmlFor="placa">Matrícula del vehículo (placa, CAMV o CPN)</label>
      <input
        id="placa"
        value={placa}
        onChange={(e) => setPlaca(e.target.value.toUpperCase())}
        aria-invalid={!!error}
        aria-describedby={error ? 'err-placa' : undefined}
        placeholder="PBX-1234"
      />
      {error && <p id="err-placa" className="campo-error">{error}</p>}
      <button type="submit" disabled={cargando}>
        {cargando ? 'Consultando…' : 'Consultar vehículo'}
      </button>
    </form>
  );
}
