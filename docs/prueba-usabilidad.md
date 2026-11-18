# Prueba de usabilidad (plantilla — completar con la sesión real)

> La rúbrica exige una prueba **con una persona real**, con tareas definidas, registro de éxito, tiempo y errores, y mejoras concretas. Este documento deja todo preparado; **los resultados deben salir de su sesión**: no los invente.

## Preparación
- Participante: una persona que no haya visto el sistema (idealmente no técnica). Anote perfil (edad, familiaridad con trámites SRI/ANT). No use su nombre completo si no lo autoriza.
- Datos: use un RUC y una matrícula reales **del propio participante o de prueba autorizados**.
- Entorno: sistema desplegado, navegador del participante, cronómetro, grabación de pantalla opcional (con permiso).
- Guion al participante: «Estamos probando el sistema, no a usted. Piense en voz alta. No le ayudaré salvo que se bloquee más de 2 minutos».

## Tareas

| # | Tarea (se lee en voz alta) | Éxito si… |
|---|---|---|
| T1 | «Verifique que usted es contribuyente con su correo y RUC.» | Ve la tarjeta «Contribuyente verificado» |
| T2 | «Diga cuántos puntos tiene su licencia.» | Lee correctamente el número de puntos |
| T3 | «Consulte los datos de su vehículo con su placa.» | Ve la tarjeta «Vehículo» |
| T4 | «Ingrese un RUC con un dígito de menos y explique qué pasó.» | Entiende el mensaje y lo corrige |
| T5 | (Si la ANT está caída o se simula) «¿Qué le dice la pantalla sobre los puntos?» | Identifica que el dato puede no ser reciente |

## Registro (una fila por tarea)

| Tarea | ¿Éxito? (sí / con ayuda / no) | Tiempo (mm:ss) | Errores (n) | Observaciones (qué dijo/hizo) |
|---|---|---|---|---|
| T1 | | | | |
| T2 | | | | |
| T3 | | | | |
| T4 | | | | |
| T5 | | | | |

Resumen: tasa de éxito = ___ / 5 · tiempo total = ___ · errores totales = ___ · satisfacción (1–5) = ___

## Hallazgos y mejoras concretas

Redacte cada mejora ligada a una observación (ejemplos de formato, **reemplace por los suyos**):

| Observación | Problema | Mejora propuesta | Archivo a cambiar |
|---|---|---|---|
| «No supe qué poner en RUC» (T1) | Ayuda insuficiente | Añadir ejemplo visible en el campo | `frontend/src/components/FormIdentidad.jsx` |
| | | | |

## Cómo simular la caída de la ANT para T5
Con el backend local: apague la red del backend hacia la ANT (p. ej. `app.upstream.ant-base-url=http://localhost:9` en `application.yml`) **después** de haber consultado una vez los puntos de esa cédula; la segunda consulta debe mostrar el aviso «Dato no reciente». Para forzar que no se use la ventana «fresca», ponga `app.politicas.licencia.fresca: 0s`.
