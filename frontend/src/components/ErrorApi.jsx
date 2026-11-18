export default function ErrorApi({ error }) {
  if (!error) return null;
  return (
    <div className="alerta alerta-error" role="alert">
      <strong>{error.message}</strong>
      {error.detalles?.length > 0 && (
        <ul>
          {error.detalles.map((d, i) => (
            <li key={i}>{d.mensaje}</li>
          ))}
        </ul>
      )}
      <span className="codigo">Código: {error.codigo}</span>
    </div>
  );
}
