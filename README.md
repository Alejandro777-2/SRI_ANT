# Consulta SRI / ANT

Sistema web: el usuario ingresa **correo + RUC**; se verifica que sea contribuyente y persona natural (SRI), se muestran sus datos, se consultan los **puntos de la licencia** (ANT, de baja disponibilidad) y, con la **matrícula**, los datos del vehículo (SRI).

- **Frontend:** React + Vite (`frontend/`)
- **Backend:** Java 21 + Spring Boot 3.5 (`backend/`)
- **Caché en la nube:** Redis (Upstash o Redis Cloud) vía `REDIS_URL`
- **Arquitectura:** `docs/C4-IcePanel.md` · **Prueba de usabilidad:** `docs/prueba-usabilidad.md`

## Ejecutar en local

```bash
# Backend (sin Redis, caché en memoria)
cd backend
CACHE_PROVIDER=memory mvn spring-boot:run

# Backend con Redis en la nube
REDIS_URL='rediss://default:<password>@<host>:<port>' mvn spring-boot:run

# Frontend (otra terminal)
cd frontend
npm install
npm run dev          # http://localhost:5173 (proxy de /api a :8080)
```

Pruebas: `cd backend && mvn test` · `cd frontend && npm test`

## Contrato de la API (`/api/v1`)

| Método y ruta | Descripción | Éxito | Errores |
|---|---|---|---|
| `POST /contribuyentes/verificaciones` body `{"correo","ruc"}` | Valida entradas, verifica contribuyente y persona natural, devuelve datos | 200 | 400, 404 `NO_ES_CONTRIBUYENTE`, 422 `NO_ES_PERSONA_NATURAL`, 503 |
| `GET /vehiculos/{placa}` | Datos del vehículo (placa, CAMV o CPN) | 200 | 400, 404 `VEHICULO_NO_ENCONTRADO`, 503 |
| `GET /licencias/{cedula}/puntos` | Puntos de la licencia (ANT) | 200 | 400, 404 `LICENCIA_NO_ENCONTRADA`, 503 |
| `GET /actuator/health` | Salud del servicio | 200 | — |

`POST` en la verificación porque el correo viaja en el cuerpo (no en URL ni logs); no crea un recurso, por eso responde 200 y no 201.

**Respuesta exitosa**

```json
{
  "datos": { "cedula": "1710034065", "puntos": 24, "puntosMaximo": 30 },
  "meta": {
    "origen": "CACHE_OBSOLETA",
    "obtenidoEn": "2026-11-17T21:10:00Z",
    "edadSegundos": 5400,
    "obsoleto": true,
    "aviso": "El servicio de ANT no está disponible. Se muestra el último dato correcto, obtenido hace 90 minutos; puede no estar actualizado."
  }
}
```

**Error estructurado** (siempre con `codigo` y `mensaje`)

```json
{
  "estado": 400,
  "codigo": "VALIDACION_FALLIDA",
  "mensaje": "Los datos enviados no son válidos.",
  "detalles": [{ "campo": "ruc", "codigo": "RUC_INVALIDO", "mensaje": "El RUC debe tener 13 dígitos numéricos." }],
  "ruta": "/api/v1/contribuyentes/verificaciones",
  "timestamp": "2026-11-17T22:00:00Z"
}
```

Códigos: `VALIDACION_FALLIDA` (400, con `detalles` por campo), `CUERPO_INVALIDO` (400), `NO_ES_CONTRIBUYENTE` / `VEHICULO_NO_ENCONTRADO` / `LICENCIA_NO_ENCONTRADA` / `RUTA_NO_ENCONTRADA` (404), `METODO_NO_PERMITIDO` (405), `TIPO_CONTENIDO_NO_SOPORTADO` (415), `NO_ES_PERSONA_NATURAL` (422), `SERVICIO_EXTERNO_NO_DISPONIBLE` (503, sin dato guardado), `ERROR_INTERNO` (500).

Validaciones: correo (formato), RUC (13 dígitos, establecimiento ≠ 000, primeros 10 = cédula con provincia y dígito verificador módulo 10), matrícula (5–17 alfanuméricos, se normaliza a mayúsculas sin guiones), cédula.

## Patrón de caché: «último dato bueno»

`ResilientCache` (`backend/.../cache/ResilientCache.java`):

1. Dato guardado **más joven que `fresca`** → se responde desde caché sin llamar al externo (`CACHE_FRESCA`). Protege a la ANT de carga innecesaria.
2. Si no, se consulta el externo; si responde bien, se **guarda** con TTL = `retencion` (`ORIGEN`).
3. Si el externo **falla** (timeout, 5xx, HTML irreconocible) y hay dato guardado → se devuelve ese dato con `obsoleto: true` y un `aviso` con la antigüedad (`CACHE_OBSOLETA`). La interfaz lo muestra en un cuadro ámbar.
4. Si falla y no hay dato guardado → 503 `SERVICIO_EXTERNO_NO_DISPONIBLE`.
5. Respuestas de negocio (no es contribuyente, sociedad, no encontrado) **no se cachean** ni activan el respaldo.
6. Si Redis falla, el servicio sigue funcionando sin caché (se registra la advertencia).

Tiempos de vida (`backend/src/main/resources/application.yml`) y su justificación:

| Dato | `fresca` | `retencion` | Por qué |
|---|---|---|---|
| Puntos de licencia (ANT) | 15 min | 7 días | Cambian solo cuando se procesan citaciones o cursos, no minuto a minuto; 15 min evita golpear a una web frágil y 7 días cubre caídas largas sin mostrar datos de hace meses |
| Contribuyente (SRI) | 1 h | 7 días | El catastro cambia rara vez (estado, actividad) |
| Vehículo (SRI) | 6 h | 30 días | Datos de matrícula casi estáticos |

Ajústelos si su docente pide otros valores; la justificación debe acompañar al número.

## Despliegue (resumen)

1. **Redis:** cree una base gratuita en Upstash (o Redis Cloud) y copie la URL TLS `rediss://default:<pwd>@<host>:<puerto>` → variable `REDIS_URL`.
2. **Backend:** Render/Railway/Fly con `backend/Dockerfile`. Variables: `REDIS_URL`, `CORS_ORIGINS=https://<su-frontend>`. Health check: `/actuator/health`.
3. **Frontend:** Vercel/Netlify, directorio `frontend`, build `npm run build`, salida `dist`, variable `VITE_API_URL=https://<su-backend>`.
4. **IcePanel:** siga `docs/C4-IcePanel.md` y enlace los directorios de este repositorio.

## Limitaciones conocidas (leer antes de entregar)

- Las APIs del SRI y la ANT **no se pudieron consultar** durante el desarrollo (el entorno bloquea esos dominios). Los nombres de campos del SRI (`personaSociedad`, `razonSocial`, `estadoContribuyenteRuc`, etc.) y el HTML de la ANT se implementaron de forma tolerante, pero **deben verificarse contra respuestas reales**: el SRI en `SriClient`/`ContribuyenteService`/`VehiculoService`, y la ANT solo en `AntHtmlParser`. Las muestras de `AntHtmlParserTest` son simuladas.
- El SRI/ANT pueden bloquear IPs fuera de Ecuador o limitar peticiones; si pasa en producción, despliegue en una región cercana o use otro proveedor.
- Se implementó **un único microservicio modular** (contribuyente, vehículo, licencia). Separar la ANT en su propio servicio es posible, pero añade un contenedor sin beneficio para este alcance.
