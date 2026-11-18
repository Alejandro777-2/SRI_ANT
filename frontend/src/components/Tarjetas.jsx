import Aviso from './Aviso.jsx';

function Fila({ k, v }) {
  if (v == null || v === '') return null;
  return (
    <div className="fila">
      <dt>{k}</dt>
      <dd>{v}</dd>
    </div>
  );
}

export function TarjetaContribuyente({ resultado }) {
  const { correo, contribuyente: c } = resultado.datos;
  return (
    <section className="tarjeta" aria-labelledby="t-contrib">
      <h2 id="t-contrib">Contribuyente verificado</h2>
      <Aviso meta={resultado.meta} />
      <dl>
        <Fila k="Nombre / razón social" v={c.razonSocial} />
        <Fila k="RUC" v={c.ruc} />
        <Fila k="Estado" v={c.estado} />
        <Fila k="Tipo" v={c.tipoContribuyente} />
        <Fila k="Actividad principal" v={c.actividadEconomica} />
        <Fila k="Régimen" v={c.regimen} />
        <Fila k="Obligado a llevar contabilidad" v={c.obligadoContabilidad} />
        <Fila k="Inicio de actividades" v={c.fechaInicioActividades} />
        <Fila k="Correo registrado" v={correo} />
      </dl>
    </section>
  );
}

export function TarjetaLicencia({ estado }) {
  return (
    <section className="tarjeta" aria-labelledby="t-lic">
      <h2 id="t-lic">Puntos de la licencia</h2>
      {estado.cargando && <p>Consultando la ANT…</p>}
      {estado.error && (
        <div className="alerta alerta-error" role="alert">
          <strong>{estado.error.message}</strong>
          <span className="codigo">Código: {estado.error.codigo}</span>
        </div>
      )}
      {estado.resultado && (
        <>
          <Aviso meta={estado.resultado.meta} />
          <p className="puntos">
            <span className="numero">{estado.resultado.datos.puntos}</span>
            <span> de {estado.resultado.datos.puntosMaximo} puntos</span>
          </p>
        </>
      )}
    </section>
  );
}

export function TarjetaVehiculo({ resultado }) {
  const v = resultado.datos;
  return (
    <section className="tarjeta" aria-labelledby="t-veh">
      <h2 id="t-veh">Vehículo</h2>
      <Aviso meta={resultado.meta} />
      <dl>
        <Fila k="Matrícula" v={v.placa} />
        <Fila k="Marca" v={v.marca} />
        <Fila k="Modelo" v={v.modelo} />
        <Fila k="Año" v={v.anio} />
        <Fila k="Clase" v={v.clase} />
        <Fila k="Color" v={v.color} />
        <Fila k="Servicio" v={v.servicio} />
      </dl>
    </section>
  );
}
