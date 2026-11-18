import { useState } from 'react';
import { consultarPuntos, consultarVehiculo, verificarContribuyente } from './api.js';
import { cedulaDeRuc, normalizarPlaca } from './validators.js';
import ErrorApi from './components/ErrorApi.jsx';
import FormIdentidad from './components/FormIdentidad.jsx';
import FormVehiculo from './components/FormVehiculo.jsx';
import { TarjetaContribuyente, TarjetaLicencia, TarjetaVehiculo } from './components/Tarjetas.jsx';

export default function App() {
  const [verificando, setVerificando] = useState(false);
  const [errorIdentidad, setErrorIdentidad] = useState(null);
  const [contribuyente, setContribuyente] = useState(null);
  const [licencia, setLicencia] = useState({ cargando: false, resultado: null, error: null });

  const [consultandoVehiculo, setConsultandoVehiculo] = useState(false);
  const [errorVehiculo, setErrorVehiculo] = useState(null);
  const [vehiculo, setVehiculo] = useState(null);

  async function alVerificar(correo, ruc) {
    setVerificando(true);
    setErrorIdentidad(null);
    setContribuyente(null);
    setVehiculo(null);
    setErrorVehiculo(null);
    setLicencia({ cargando: false, resultado: null, error: null });
    try {
      const resultado = await verificarContribuyente(correo, ruc);
      setContribuyente(resultado);
      cargarPuntos(cedulaDeRuc(ruc));
    } catch (e) {
      setErrorIdentidad(e);
    } finally {
      setVerificando(false);
    }
  }

  // Se lanza sin bloquear el resto del flujo: la ANT es lenta o puede estar caída.
  async function cargarPuntos(cedula) {
    setLicencia({ cargando: true, resultado: null, error: null });
    try {
      setLicencia({ cargando: false, resultado: await consultarPuntos(cedula), error: null });
    } catch (e) {
      setLicencia({ cargando: false, resultado: null, error: e });
    }
  }

  async function alConsultarVehiculo(placa) {
    setConsultandoVehiculo(true);
    setErrorVehiculo(null);
    setVehiculo(null);
    try {
      setVehiculo(await consultarVehiculo(normalizarPlaca(placa)));
    } catch (e) {
      setErrorVehiculo(e);
    } finally {
      setConsultandoVehiculo(false);
    }
  }

  return (
    <main>
      <header>
        <h1>Consulta de contribuyente, vehículo y licencia</h1>
        <p className="subtitulo">Datos del SRI y de la ANT.</p>
      </header>

      <section className="tarjeta" aria-labelledby="paso1">
        <h2 id="paso1">1. Sus datos</h2>
        <FormIdentidad cargando={verificando} onEnviar={alVerificar} />
        <ErrorApi error={errorIdentidad} />
      </section>

      {contribuyente && (
        <>
          <TarjetaContribuyente resultado={contribuyente} />
          <TarjetaLicencia estado={licencia} />

          <section className="tarjeta" aria-labelledby="paso2">
            <h2 id="paso2">2. Su vehículo</h2>
            <FormVehiculo cargando={consultandoVehiculo} onEnviar={alConsultarVehiculo} />
            <ErrorApi error={errorVehiculo} />
          </section>

          {vehiculo && <TarjetaVehiculo resultado={vehiculo} />}
        </>
      )}
    </main>
  );
}
