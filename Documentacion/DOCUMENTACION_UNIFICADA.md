# Documentación unificada – Referencias y resúmenes

Contenido importante reunido de los documentos del proyecto. Para contexto: [LEEME_PRIMERO.md](LEEME_PRIMERO.md), [AYUDA_MEMORIA.md](AYUDA_MEMORIA.md) y [PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md](PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md).

---

## 0. Evolución MiGimVirtual (realizada)

- **Proyecto:** Nombre MiGimVirtual; paquete `com.migimvirtual`; clase principal `MigimvirtualApplication`.
- **Base de datos:** `migimvirtual` (URL en `application*.properties`); se crea al arrancar si no existe; tablas con `ddl-auto=update`.
- **Credenciales de desarrollo:** `profesor@migimvirtual.com` / `profesor`; `developer@migimvirtual.com` / `Qbasic.1977.migimvirtual`.
- **Marca:** Navbar con logo `mgvirtual_logo1.png` y fondo `fondo-navbar.png`. Página pública: carrusel con videos `video_mgvirtual_inicio_escritorio.mp4` (escritorio) y `Video_mgvirtual_inicio_movil.mp4` (móvil).
- **Servidor:** Menú `./migimvirtual`; scripts `reset_db_migimvirtual.sql` / `reset_db_migimvirtual.sh`; variables `MIGIMVIRTUAL_DB_USER`, `MIGIMVIRTUAL_DB_PASSWORD`.

---

## 1. Resumen de lo implementado

- **Panel único:** Profesor (roles DEVELOPER, ADMIN, AYUDANTE). Sin panel alumno ni admin separado.
- **Ejercicios:** Predeterminados 1–60 desde `uploads/ejercicios/`; ABM; grupos musculares como entidad (`GrupoMuscular`); formularios y modal Ver alineados con series/rutinas.
- **Series y rutinas:** ABM; asignación rutina → alumno; enlace por token `/rutinas/hoja/{token}`; Copiar enlace y WhatsApp desde ficha alumno; orden de series; modificar rutina con tres bloques (Detalles, Series en rutina, Añadir más).
- **Alumnos:** Solo ficha (sin login). Estado ACTIVO/INACTIVO; filtros por nombre, estado, tipo, día/horario; columna Presente (ciclo Pendiente→Presente→Ausente). Al eliminar alumno se borran asistencias, mediciones, excepciones y rutinas asignadas.
- **Calendario:** Semanal; presente/ausente/pendiente por clic; excepciones por día; sincronizado con ficha y columna Presente en Mis Alumnos.
- **Progreso:** Modal en ficha (grupos trabajados, observaciones); sin checkbox presente; historial y resumen mensual con detalle por día.
- **Pizarra y sala TV:** Editor desde panel; vista TV en `/sala/{token}`; columnas editables; ejercicios con peso/rep.
- **Página pública:** Landing `/`, Planes `/planes`, consultas; administración en `/profesor/pagina-publica`.
- **Manual del usuario:** HTML en `/profesor/manual` (botón en panel); cubre acceso, panel, alumnos, ejercicios, series, rutinas, calendario, pizarra, usuarios, administración.
- **Backup (terminado Mar 2026):** Ver sección 2.
- **Depuración de datos (terminado Feb 2026):** Ver sección 2.1.

---

## 2. Backup y exportación

**Estado:** Terminado (marzo 2026). Acceso: Administración → Backup y resguardo.

| Funcionalidad | Descripción |
|---------------|-------------|
| **Ejercicios + grupos + rutinas + series** | Exportar/importar ZIP. Opciones por checkbox (Grupos, Ejercicios, Rutinas, Series). Modos Agregar o Suplantar. Imágenes con nombres originales. |
| **Alumnos – JSON** | Exportar backup (datos, mediciones, asistencias). Importar desde JSON (Agregar o Suplantar). |
| **Alumnos – Excel** | Exportar a Excel para reportes. Una fila por alumno; columna final "Último trabajo" (fecha + grupos y observaciones del último progreso). No se usa para importar. |

**Excel alumnos – columnas:** Título "Exportación de alumnos fecha dd/MM/yyyy". Columnas: Nombre, Correo, Celular, Edad, Sexo, Estado, Fecha de alta, Fecha baja, Tipo de asistencia, Días y horarios, Objetivos personales, Restricciones médicas, Notas profesor, Cantidad de asignaciones, **Último trabajo** (fecha en una línea, grupos y observaciones en la siguiente; ej. "11/03/26" y "CARDIO - CORE - trabajo muy bien"). No se exportan: Peso, Detalle asistencia, Contacto emergencia.

**Servicios:** `ExerciseZipBackupService`, `AlumnoJsonBackupService`, `AlumnoExportService`. Rutas en `AdminPanelController`: `/profesor/backup`, exportar-zip, importar, exportar-alumnos-json, importar-alumnos, exportar-alumnos-excel.

---

## 2.1 Depuración de datos

**Estado:** Terminado (febrero 2026). Acceso: Administración → Depuración de datos (entre Sistema de backups y Manual de usuario).

Permite eliminar registros antiguos para mantener la base de datos ligera. Dos tarjetas independientes:

| Funcionalidad | Descripción |
|---------------|-------------|
| **Registro de asistencias e inasistencias** | Se elige una fecha límite. Se eliminan todos los registros con fecha **anterior** a la elegida (ej.: 12/12/2025 → se borra todo antes de esa fecha). Acción irreversible; se recomienda hacer backup antes. |
| **Rutinas asignadas a alumnos** | Se elige una fecha límite. Se eliminan todas las rutinas asignadas cuya fecha de creación es **anterior** a la elegida. Las rutinas plantilla (Mis Rutinas) no se tocan. Acción irreversible. |

**Servicios:** `DepuracionService`. Rutas en `AdminPanelController`: `GET /profesor/depuracion`, `POST /profesor/depuracion/asistencias`, `POST /profesor/depuracion/rutinas-asignadas`. Repositorios: `AsistenciaRepository` (countByFechaBefore, deleteByFechaBefore), `RutinaRepository` (findByEsPlantillaFalseAndFechaCreacionBefore).

### 2.2 Modales y avisos unificados (confirmaciones y alertas)

**Estado:** Completado (febrero 2026). En toda la app las confirmaciones y avisos usan modales con estilo MiGimVirtual (cabecera morada `.modal-confirmar-header`, pie `.modal-confirmar-footer` en `style.css`), reemplazando `alert()` y `confirm()` nativos del navegador.

**Vistas con modal de confirmación y/o alerta:**

| Vista | Confirmación | Alerta (éxito/error/info) |
|-------|--------------|---------------------------|
| Panel Administración (backup, depuracion, usuarios-sistema, pagina-publica-admin) | Sí | Sí |
| Dashboard profesor | Eliminar serie, rutina, rutina asignada | Enlace copiado, “Debe ser administrador” |
| Detalle alumno | Eliminar alumno, inactivar todas las rutinas | Enlace copiado, “Datos actualizados” (flash) |
| Series crear/editar | — | Validación, éxito con redirección, errores |
| Rutinas crear | — | Nombre y al menos una serie |
| Ejercicios lista (profesor) | Eliminar ejercicio | — |
| Grupos musculares | Eliminar grupo | — |
| Pizarra lista | Eliminar pizarra | Código 4 dígitos, errores, enlace copiado |
| Pizarra editor | Quitar columna, eliminar ejercicio, nuevo enlace TV | Todos los mensajes (nombre, errores, enlace copiado) |
| Listado ejercicios (ejercicios) | — | “Ejercicio(s) agregado(s) a rutina” |

**Editar alumno:** Tras guardar, redirección al detalle del alumno (`/profesor/alumnos/{id}`) con mensaje flash “Datos del alumno actualizados correctamente.” (ya no redirige al dashboard).

**Vista Mis Ejercicios:** No se muestra la estrella azul ni el aviso “La estrellita azul indica ejercicios predeterminados del sistema.”; todas las filas tienen el mismo estilo (sin `table-info` en predeterminados).

**Referencia:** CHANGELOG entrada [2026-02-09] feat(ux): modales unificados y mejoras en flujos.

---

## 3. Despliegue y servidor

**Resumen:** App en VPS Donweb. Acceso SSH: `ssh -p 5638 root@149.50.144.53`. Aplicación en puerto 8080. Si PowerShell está bloqueado, usar Consola VNC de Donweb y menú `./migimvirtual` / `screen -r migimvirtual`. **Límite de subida (Nginx):** Para restaurar backups grandes, configurar `client_max_body_size` (ej. 50M) en la config de Nginx; ver archivo de ejemplo en `servidor/nginx-detodoya.conf`.

**Detalle completo:** [servidor/DESPLIEGUE-SERVIDOR.md](servidor/DESPLIEGUE-SERVIDOR.md) (acceso SSH, Consola VNC, menú, Nginx, reinicio, backups en servidor).

---

## 4. Manual del usuario – Índice de secciones

El manual en la app (`/profesor/manual`) incluye:

1. Acceso al sistema (URL, login, credenciales)
2. Panel del profesor (dashboard, botones, tabs)
3. Alumnos (lista, crear, editar, ficha, filtros, Presente, progreso, rutinas asignadas)
4. Ejercicios (lista, crear, editar, grupos musculares)
5. Series (crear, editar, ver)
6. Rutinas (crear, modificar, asignar, enlace, WhatsApp)
7. Calendario semanal (presente/ausente, excepciones)
8. Presentismo (columna Presente en Mis Alumnos)
9. Progreso del alumno (modal grupos + observaciones)
10. Pizarra en sala (editor, vista TV)
11. Usuarios del sistema (admin/ayudante, perfiles)
12. Administración (backup, depuración de datos, página pública, etc.)
13. Resumen rápido (tabla "Quiero… / Dónde")

---

## 5. Referencias técnicas (una línea)

| Tema | Resumen |
|------|--------|
| **Grupos musculares** | Entidad `GrupoMuscular`; sistema + por profesor; ABM en `/profesor/mis-grupos-musculares`; ejercicios con `@ManyToMany`. |
| **Asistencia en calendario** | `CalendarioController`, `AsistenciaService`; endpoint `POST /calendario/api/marcar-asistencia` (estado PENDIENTE/PRESENTE/AUSENTE); columna Presente en Mis Alumnos usa el mismo endpoint. |
| **Pizarra / sala TV** | Fase 7. Editor en panel; vista `/sala/{token}`; API estado y actualizaciones; columnas y ejercicios con peso/rep. |
| **Página pública** | Fase 8. Landing `/`, Planes `/planes`, consultas; hero con video/carrusel; administración en panel. |
| **Ejercicios predeterminados** | `ExerciseCargaDefaultOptimizado.asegurarEjerciciosPredeterminados()`; imágenes en `uploads/ejercicios/` (1.webp–60.webp). |
| **Restricción AYUDANTE** | No puede acceder a "Administrar sistema"; redirección y mensaje si intenta entrar a `/profesor/administracion`. |
| **Eliminar alumno** | `UsuarioService.eliminarUsuario`: borra asistencias, mediciones, excepciones, rutinas asignadas; luego el usuario. |
| **Depuración de datos** | `DepuracionService`; panel en `/profesor/depuracion`; elimina asistencias o rutinas asignadas anteriores a una fecha elegida. |

---

*Última actualización: Febrero 2026. Modales y avisos unificados (§2.2). Para pendientes ver PENDIENTES_FINALES.md.*
