# Plan de desarrollo unificado – Mattfuncional

**Para contexto del proyecto (sobre todo desde otra PC):** [LEEME_PRIMERO.md](LEEME_PRIMERO.md).

**Última actualización:** Marzo 2026  
**Origen:** Fusión de los antiguos PLAN_DE_DESARROLLO, PLAN_MODIFICACIONES y AYUDA_MEMORIA (ya eliminados).  
**Uso:** Un solo documento con la visión del proyecto, fases, checklist y pendientes detallados (ítem por ítem). Resumen de lo implementado en [DOCUMENTACION_UNIFICADA.md](DOCUMENTACION_UNIFICADA.md).

---

## 1. Visión general

| Aspecto | Definición |
|--------|------------|
| **Nombre** | Mattfuncional (evolución de MiGym) |
| **Referencia** | Presupuesto - Mat Funcional.pdf + indicaciones del cliente |
| **Roles con acceso** | ADMIN y AYUDANTE (gestionados por el admin). Sin panel admin separado |
| **Alumnos** | Solo ficha física + online, **sin usuario ni contraseña**; no hay login de alumno |
| **Acceso a rutinas** | Por enlace (ej. WhatsApp); el alumno abre el link y ve la rutina en HTML |
| **Objetivo** | Un único panel de profesor, sin panel alumno ni admin, con pantalla de sala y página pública |

---

## 2. Lo que QUEDA (reutilizar/adaptar)

- **Ejercicios:** ABM en panel profesor; predeterminados auto-asegurados desde `uploads/ejercicios/` (1–60). Estructura: `Exercise`, `ExerciseService`, `ExerciseCargaDefaultOptimizado`, `ExerciseRepository`.
- **Series:** ABM de series (agrupaciones de ejercicios). `Serie`, `SerieEjercicio`, `SerieService`, `SerieController`, `SerieRepository`.
- **Rutinas:** Creación en base a series, ABM, asignación rutina → alumno, enlace único (token), hoja `/rutinas/hoja/{token}`, Copiar enlace, WhatsApp desde ficha alumno.
- **Usuarios (alumnos):** Ficha (datos físicos y online). Sin usuario/contraseña; alumnos nunca autentican (Fase 6 completada).
- **Calendario semanal:** Mantener. Uso: alumnos que asisten, vista por día/horario, presentismo. Asistencia (presente/ausente/pendiente) ya implementada en calendario y vista Mis Alumnos. Estructura: `CalendarioController`, `CalendarioService`, `Asistencia`, `DiaHorarioAsistencia`, `SlotConfig`, `semanal-profesor.html`.
- **Grupos musculares:** Entidad `GrupoMuscular` (ya implementado). ABM en `/profesor/mis-grupos-musculares`. Ver DOCUMENTACION_UNIFICADA.md § Referencias técnicas.

---

## 3. Lo que NO QUEDA (eliminar o desactivar)

| Elemento | Acción recomendada |
|----------|---------------------|
| **Panel del usuario/alumno** | Eliminado: rutas, controladores y vistas del panel alumno (`/usuario/*`, dashboard alumno con login). |
| **Chat profesor–alumno** | Eliminado: módulo de mensajería (`MensajeController`, `MensajeService`, entidad `Mensaje`, repositorio, vistas de chat y referencias en navbar). |
| **Panel de administrador** | Eliminado: `AdministradorController` y vistas bajo `admin/`. |
| **Creación de profesores** | Eliminado: alta/edición de profesores; se gestiona por usuarios del sistema. |
| **Login de alumno** | Eliminado: flujo de login para rol USER/alumno. |
| **WebSocket / chat en tiempo real** | Eliminado: `WebSocketController`, `WebSocketConfig` y dependencias de WebSocket. |
| **Gestión de múltiples profesores** | Quitar lógica de “elegir profesor”, “profesores disponibles”, etc. |

### Archivos/carpetas ya eliminados o desactivados

- `AdministradorController.java`, templates `admin/*`, `usuario/dashboard.html`, `profesor/chat-alumno.html`.
- `MensajeController`, `MensajeService`, `WebSocketController`, `WebSocketConfig`.
- Entidades/repos: `Mensaje`, `MensajeRepository`.
- Rutas y permisos en `SecurityConfig` y navbar relacionados con admin, alumno y chat.

---

## 4. Fases de implementación

| Fase | Contenido | Estado |
|------|-----------|--------|
| **Fase 1 – Limpieza** | Renombrar proyecto a Mattfuncional. Eliminar panel admin, chat, WebSocket, ABM de profesores. Ajustar SecurityConfig y navbar. | Completado |
| **Fase 2 – Un solo panel** | Redirigir login al panel del profesor. Quitar referencias a panel admin/alumno y lista de profesores. | Completado |
| **Fase 3 – Ejercicios y series** | Ejercicios con asegurar predeterminados. ABM de ejercicios y ABM de series en panel profesor. | Completado |
| **Fase 4 – Rutinas y asignación** | ABM rutinas basadas en series, asignación rutina → alumno, token, hoja `/rutinas/hoja/{token}`, Copiar enlace, WhatsApp, modificar rutina con series, orden de series, tabs dashboard, logo. | Completado |
| **Fase 5 – Vista rutina por enlace** | Página pública (sin login) con token que muestre la rutina en HTML. Hoja en `/rutinas/hoja/{token}` con permitAll. | **Completado** |
| **Fase 6 – Alumnos sin login** | Alumno solo como ficha (física + online), sin usuario/contraseña. Calendario semanal y presentismo. UserDetailsService excluye ALUMNO del login. | **Completado** |
| **Fase 7 – Pantalla de sala** | Modo sala para TV: ruta de solo lectura, control desde panel profesor, pizarra digital, columnas, vista TV con polling. | **Completado** |
| **Fase 8 – Página pública** | Sitio institucional: landing en `/`, página Planes, formulario de consulta, administración desde panel. | **Completado** |

---

## 5. Nuevos módulos a desarrollar

### 5.1 Pantalla de entrenamiento en sala (Fase 7)

- Mostrar rutinas/series/ejercicios en pantalla grande. Control solo desde el panel del profesor. Pantalla en sala sin interacción (solo visualización). Ruta con token; actualización por polling o similar.

### 5.2 Página pública del gimnasio (Fase 8)

- Presentación, servicios, horarios, formulario de contacto, promociones, productos. Contenido editable desde el panel (o fijo en una primera etapa).
- **Plan detallado:** [PLAN_PAGINA_PUBLICA_GIMNASIO.md](PLAN_PAGINA_PUBLICA_GIMNASIO.md) – referencia RedFit, recopilación de datos, puntos a relatar (entrenamiento personalizado, presencial/virtual, etc.), hero con carrusel de imágenes, index como entrada y ícono de login por ahora.

---

## 6. Ajustes de modelo y seguridad

- **Roles:** ADMIN y AYUDANTE. No hay panel admin separado; la gestión de usuarios del sistema se hace desde `/profesor/usuarios-sistema`.
- **Alumnos:** Solo registros de ficha; sin rol de login. Se mantiene `Usuario` con `rol=ALUMNO`; `findByCorreoParaLogin` excluye ALUMNO para que nunca puedan autenticarse.
- **Usuarios sistema:** Se crean por el admin (y developer por sistema). No hay pantallas de crear/listar profesores tradicionales.
- **Seguridad:** Rutas públicas para `/`, `/public/**`, `/rutinas/hoja/**`, `/sala/**`. Ruta privada: panel del profesor (ej. `/profesor/**`). Eliminar reglas para `/admin/**` y login alumno.

---

## 7. Checklist único

- [x] Renombrar app a **Mattfuncional** (pom, títulos, documentación).
- [x] Un único **panel: profesor** (no admin, no alumno).
- [x] **Ejercicios:** Predeterminados desde `uploads/ejercicios/` (1–60); ABM en panel profesor.
- [x] **Series y rutinas:** ABM y rutinas basadas en series (crear, editar, orden de series).
- [x] **Alumnos:** solo ficha (física + online), sin usuario/contraseña. UserDetailsService excluye ALUMNO.
- [x] **Asignación de rutinas** + **enlace para WhatsApp** + **vista HTML de rutina** (hoja en `/rutinas/hoja/{token}`).
- [x] **Calendario semanal y presentismo** (dar presente/ausente, clic en alumno → ficha, día por excepción / recuperar clase).
- [x] **Pantalla de entrenamiento en sala** (pizarra digital, modo TV, control desde panel, polling).
- [x] **Página pública:** landing, Planes, formulario de consulta, administración desde panel.
- [x] **Manual del usuario:** Implementado. Página HTML en `/profesor/manual` accesible desde el panel (botón "Manual del usuario"); cubre acceso, panel, alumnos, ejercicios, grupos musculares, series, rutinas, calendario, presentismo, progreso, pizarra, usuarios del sistema, administración y resumen. MANUAL-USUARIO.md sigue como referencia.
- [x] **Gestión de backup:** Terminado (Mar 2026). En Administración → Backup y resguardo: exportar/importar ZIP (ejercicios, grupos, rutinas, series con opciones por checkbox); exportar/importar alumnos (JSON); exportar alumnos a Excel (reportes con Último trabajo). Ver [PLAN_BACKUP_Y_EXPORTACION.md](PLAN_BACKUP_Y_EXPORTACION.md).
- [ ] **Depuración anual de datos:** Método o proceso para archivar/eliminar datos antiguos (ej. registros de asistencia/presente de años anteriores). Conservar al menos 12 meses. Ver [PENDIENTES_FINALES.md](PENDIENTES_FINALES.md) §3.2.
- [x] **Eliminar:** panel alumno, chat, panel admin, creación de profesores, WebSocket, login alumno.

---

## 8. Pendientes (lo que realmente falta)

La mayoría del desarrollo está completada (calendario, pizarra, página pública, ficha alumno, backup, etc.). Queda por implementar o completar:

- **Manual del usuario:** **Completado.** Página HTML en `/profesor/manual` (botón en panel); cubre todas las funcionalidades. MANUAL-USUARIO.md se mantiene como referencia en documentación.
- **Gestión de backup:** **Completado (Mar 2026).** En Administración → Backup y resguardo: exportar/importar ZIP (ejercicios, grupos, rutinas, series); exportar/importar alumnos (JSON); exportar alumnos a Excel. Ver [DOCUMENTACION_UNIFICADA.md](DOCUMENTACION_UNIFICADA.md) § Backup y exportación.
- **Depuración / eliminación de datos antiguos:** Método o proceso (manual o programado) para archivar o eliminar datos viejos: por ejemplo registros de asistencia/presente de años anteriores. Conservar al menos 12 meses para consultas; evita que la BD crezca sin control. Ver [PENDIENTES_FINALES.md](PENDIENTES_FINALES.md) §3.2.

Opcional / operativo:

- **Script en base del servidor:** Ejecutar `scripts/servidor/alter_consulta_email_nullable.sql` en la BD del servidor (Donweb) para que el formulario de consulta en `/planes` funcione con solo teléfono en producción.
- **Script borrar base entera:** Ya existe opción en menú; mantener documentado para desarrollo o reset total.

---

## 9. Pendientes detallados (ítem por ítem)

Lista de mejoras para implementar o ya implementadas. Se van agregando aquí para no olvidarlas.

### Creación de rutina – Orden de las series

- **Qué falta:** En la creación/edición de rutina, poder **cambiar el orden** de las series (reordenar).
- **Estado:** **Implementado.** En crear rutina: lista "Series seleccionadas" con botones Subir/Bajar. En editar rutina: botones Subir/Bajar en cada serie. El campo `orden` en `Serie` se persiste.

### Calendario semanal – Dar presente / falta desde el calendario

- **Qué falta:** Poder **dar el presente** (o marcar falta) al usuario directamente desde el calendario semanal.
- **Estado:** **Implementado.** Puntos verde/rojo/gris por alumno y slot; clic alterna presente/ausente vía API. Vista Mis Alumnos unificada. Ver `CAMBIOS-ASISTENCIA-CALENDARIO-Y-VISTA-ALUMNOS.md`.

### Calendario semanal – Acceso al detalle del usuario desde el botón del usuario

- **Qué falta:** Desde el **botón del usuario** en cada celda del calendario, poder **ingresar al detalle del usuario** (ficha del alumno).
- **Estado:** **Implementado.** Clic en alumno en la celda abre `/profesor/alumnos/{id}`.

### Calendario semanal – Día por excepción para un alumno (recuperar clase)

- **Qué falta:** Que el profesor pueda **agregar a un alumno un día por excepción** (ej. recuperar clase) en un día/horario que no es su horario habitual.
- **Estado:** **Implementado.** Frontend y backend completados; se ve en el calendario y se puede gestionar desde la interfaz.

### Ficha del alumno – Mejoras de detalle y usabilidad

- **Qué falta:** Mejorar vista de detalle del alumno (organización, legibilidad, accesos rápidos, historial y progreso más claros).
- **Estado:** **Implementado.**

### Asistencia – Marcar ausente automáticamente si pasó el horario sin presente

- **Qué falta:** Si pasó el día y horario sin que el alumno tenga presente, registrar como **ausente** de forma automática.
- **Estado:** **Implementado (backend, on-demand).** Opcional a futuro: cron nocturno.

### Depuración anual de datos

- **Qué falta:** Crear un **método o proceso programado** de **depuración anual de datos**: archivar o eliminar datos antiguos (ej. asistencia con más de 1 año).
- **Plazo anual:** Conservar al menos 12 meses; archivar a export antes de purgar si hace falta.
- **Estado:** Por implementar. Ver [PENDIENTES_FINALES.md](PENDIENTES_FINALES.md) §3.2.

### Manual del usuario

- **Qué falta:** Manual de uso del panel para profesor/admin accesible desde la app.
- **Estado:** **Implementado.** Página HTML `profesor/manual-usuario.html` en ruta `/profesor/manual`, accesible desde el botón "Manual del usuario" en el panel. Incluye: acceso, panel, alumnos, ejercicios, grupos musculares, series, rutinas, calendario, presentismo, progreso del alumno, pizarra en sala, usuarios del sistema, administración y página pública, resumen rápido. [MANUAL-USUARIO.md](MANUAL-USUARIO.md) se mantiene como referencia en documentación.

### Gestión de backup

- **Implementado (Mar 2026).** En Administración → Backup y resguardo: exportar/importar **ZIP** (ejercicios, grupos musculares, rutinas, series; opciones por checkbox; Agregar o Suplantar); exportar/importar **alumnos** (JSON; Agregar o Suplantar); exportar **alumnos a Excel** (reportes con columna Último trabajo). No hay listado de archivos en servidor: el usuario descarga o sube el archivo desde su equipo.
- **Estado:** Terminado. Detalle en [DOCUMENTACION_UNIFICADA.md](DOCUMENTACION_UNIFICADA.md) § Backup y exportación.

### Depuración anual de datos (eliminar datos viejos)

- **Qué falta:** Método o proceso para **archivar o eliminar datos antiguos** (ej. asistencia/presente de años anteriores). Conservar al menos 12 meses.
- **Estado:** Por implementar. Ver [PENDIENTES_FINALES.md](PENDIENTES_FINALES.md) §3.2.

### Mejoras post-modales (detalle alumno, rutinas, formularios)

Pendientes agregados tras resolver los modales en la ficha del alumno. Ver también [AYUDA_MEMORIA.md](AYUDA_MEMORIA.md) – "Para mañana".

1. ~~**Formulario crear alumno – Correo opcional:**~~ **Implementado.** Campo correo opcional; script `alter_usuario_correo_nullable.sql`.
2. ~~**Alumno inactivo → inactivar rutinas:**~~ **Implementado.** UsuarioService llama a RutinaService.inactivarTodasRutinasDelAlumno al cambiar a INACTIVO.
3. ~~**Detalle alumno – Vista rutinas:**~~ **Implementado.** Iconos, reseña con texto truncado (50 chars), acciones centradas.
4. ~~**Volver al origen tras guardar rutina:**~~ **Implementado.** Parámetros `alumnoId` y `returnTab` en GET/POST editar rutina.
5. ~~**Modal de progreso:**~~ **Implementado.** Checkbox "Estuvo presente" eliminado; asistencia en panel/calendario.
6. ~~**Formulario modificar rutina:**~~ **Implementado.** Layout: Series a seleccionar (izq, 2 tarjetas/fila) | Series seleccionadas (der) | Detalles abajo.
7. ~~**Vista alumnos – Botón crear:**~~ **Implementado.** Botón "Crear alumno" en título de Mis Alumnos.
8. ~~**Lista rutinas asignadas:**~~ **Implementado.** Textos abreviados, estado con iconos, acciones centradas.

**Estado:** **Completado** (Feb 2026). Ver [CHANGELOG_UNIFICADO_FEB2026.md](CHANGELOG_UNIFICADO_FEB2026.md) §18.

### Después del backup (pendientes opcionales)

Con el backup ya implementado, pendientes operativos opcionales:

1. **Estilos de excepciones (avisos):** Estilos consistentes en pantallas de excepciones/avisos/errores. **En curso.** Ver [PENDIENTES_FINALES.md](PENDIENTES_FINALES.md) §3.1.
2. **Archivo para migración y despliegue:** Preparar un archivo (o documento) con todos los datos e instrucciones necesarios para la migración y el despliegue al servidor que compre el cliente (datos, scripts, pasos, variables, etc.).

**Estado:** Ítem 1 en trabajo; resto opcional.

---

## 10. Documentos de referencia

| Documento | Contenido |
|----------|------------|
| [DOCUMENTACION_UNIFICADA.md](DOCUMENTACION_UNIFICADA.md) | Resúmenes: lo implementado, backup y exportación, Excel alumnos, despliegue, manual, referencias técnicas. |
| [PENDIENTES_FINALES.md](PENDIENTES_FINALES.md) | Últimos pendientes: excepciones/estilos, depuración de base, referencia rota. |
| [servidor/DESPLIEGUE-SERVIDOR.md](servidor/DESPLIEGUE-SERVIDOR.md) | Despliegue en VPS (SSH, Nginx, menú). |

---

*Este plan unificado reemplaza el uso de los planes dispersos y de AYUDA_MEMORIA. Se puede ir actualizando en cada iteración.*
