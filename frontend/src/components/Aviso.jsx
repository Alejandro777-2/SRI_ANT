const ETIQUETAS = {
  ORIGEN: { texto: 'Consulta en vivo', clase: 'vivo' },
  CACHE_FRESCA: { texto: 'Guardado recientemente', clase: 'cache' },
  CACHE_OBSOLETA: { texto: 'Dato no reciente', clase: 'obsoleto' },
};

function formatearEdad(seg) {
  if (seg < 60) return 'hace menos de un minuto';
  const min = Math.floor(seg / 60);
  if (min < 60) return `hace ${min} min`;
  const h = Math.floor(min / 60);
  if (h < 48) return `hace ${h} h`;
  return `hace ${Math.floor(h / 24)} días`;
}

/** Muestra la procedencia del dato y, si no es reciente, un aviso visible. */
export default function Aviso({ meta }) {
  if (!meta) return null;
  const et = ETIQUETAS[meta.origen] ?? ETIQUETAS.ORIGEN;
  return (
    <>
      <p className="procedencia">
        <span className={`etiqueta ${et.clase}`}>{et.texto}</span>
        {meta.origen !== 'ORIGEN' && <span> · obtenido {formatearEdad(meta.edadSegundos)}</span>}
      </p>
      {meta.obsoleto && (
        <div className="alerta alerta-aviso" role="alert">
          <strong>Este dato puede no estar actualizado.</strong> {meta.aviso}
        </div>
      )}
    </>
  );
}
