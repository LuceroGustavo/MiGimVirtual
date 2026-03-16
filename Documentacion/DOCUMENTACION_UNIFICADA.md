# Documentación unificada – Referencias y resúmenes

Contenido importante reunido de los documentos del proyecto. Para contexto: [LEEME_PRIMERO.md](LEEME_PRIMERO.md), [AYUDA_MEMORIA.md](AYUDA_MEMORIA.md) y [PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md](PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md).

---

## 0. Evolución MiGimVirtual (realizada)

- **Proyecto:** Nombre MiGimVirtual; paquete `com.migimvirtual`; clase principal `MigimvirtualApplication`.
- **Base de datos:** `migimvirtual` (URL en `application*.properties`); se crea al arrancar si no existe; tablas con `ddl-auto=update`.
- **Credenciales de desarrollo:** `profesor@migymvirtual.com` / `profesor`; `lucerogustavosi@gmail.com` / `Qbasic.1977`.
- **Marca:** Navbar con logo `mgvirtual_logo1.png` y fondo `fondo-navbar.png`. Página pública: carrusel con videos `video_mgvirtual_inicio_escritorio.mp4` (escritorio) y `Video_mgvirtual_inicio_movil.mp4` (móvil).
- **Servidor:** Menú `./migimvirtual`; scripts `reset_db_migimvirtual.sql` / `reset_db_migimvirtual.sh`; variables `MIGIMVIRTUAL_DB_USER`, `MIGIMVIRTUAL_DB_PASSWORD`.

---

## 1. Resumen de lo implementado

- **Panel único:** Profesor (roles DEVELOPER, ADMIN, AYUDANTE). Sin panel alumno ni admin separado.
- **Ejercicios:** Predeterminados 1–60 desde `uploads/ejercicios/`; ABM; grupos musculares como entidad (`GrupoMuscular`); formularios y modal Ver alineados con series/rutinas.
- **Series y rutinas:** ABM; asignación rutina → alumno; enlace por token `/rutinas/hoja/{token}`; Copiar enlace y WhatsApp desde ficha alumno; orden de series; modificar rutina con tres bloques (Detalles, Series en rutina, Añadir más).
- **Alumnos:** Solo ficha (sin login). Estado ACTIVO/INACTIVO; filtros por nombre y estado. Al eliminar alumno se borran mediciones y rutinas asignadas. Tarjeta "Progreso del alumno" con historial de registros (crear, editar, eliminar). **Vista del alumno terminada (Mar 2026):** responsive móvil, modal progreso al tocar registro, modal confirmar eliminar progreso, botón Guardar notas, Eliminar usuario debajo de todo, barra inferior móvil, formato fecha dd/MM/yy. Pendiente: scroll vertical en progresos móvil (>5 registros).
- **Página pública:** Landing `/`, Planes `/planes`, consultas; administración en `/profesor/pagina-publica`.
- **Manual del usuario:** HTML en `/profesor/manual` (botón en panel); cubre acceso, panel, alumnos, ejercicios, series, rutinas, usuarios, administración. (Calendario y pizarra eliminados en Mar 2026.)
- **Backup (terminado Mar 2026):** Ver sección 2.
- **Depuración de datos:** Módulo eliminado en Mar 2026 (ya no existe en Administración).

---

## 2. Backup y exportación

**Estado:** Terminado (marzo 2026). Acceso: Administración → Backup y resguardo.

| Funcionalidad | Descripción |
|---------------|-------------|
| **Ejercicios + grupos + rutinas + series** | Exportar/importar ZIP. Opciones por checkbox (Grupos, Ejercicios, Rutinas, Series). Modos Agregar o Suplantar. Imágenes con nombres originales. |
| **Alumnos – JSON** | Exportar backup (datos, mediciones). Importar desde JSON (Agregar o Suplantar). Sin asistencias desde Mar 2026. |
| **Alumnos – Excel** | Exportar a Excel para reportes. Una fila por alumno. Columnas: Nombre, Correo, Celular, Edad, Sexo, Estado, Fecha de alta, Fecha baja, Tipo de asistencia, Días y horarios, Objetivos personales, Restricciones médicas, Notas profesor, Cantidad de asignaciones. (Columna "Último trabajo" eliminada en Mar 2026.) |

**Excel alumnos – columnas:** Título "Exportación de alumnos fecha dd/MM/yyyy". Sin columna Último trabajo desde Mar 2026.

**Servicios:** `ExerciseZipBackupService`, `AlumnoJsonBackupService`, `AlumnoExportService`. Rutas en `AdminPanelController`: `/profesor/backup`, exportar-zip, importar, exportar-alumnos-json, importar-alumnos, exportar-alumnos-excel.

---

## 2.1 Depuración de datos (eliminado Mar 2026)

El módulo "Depuración de datos" fue eliminado por completo en marzo 2026. No existe en el menú de Administración ni en la aplicación (servicio, controlador, plantilla y referencias eliminados).

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
| Listado ejercicios (ejercicios) | — | “Ejercicio(s) agregado(s) a rutina” |

*(Pizarra lista y editor eliminados en Mar 2026.)*

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
3. Alumnos (lista, crear, editar, ficha, filtros, rutinas asignadas)
4. Ejercicios (lista, crear, editar, grupos musculares)
5. Series (crear, editar, ver)
6. Rutinas (crear, modificar, asignar, enlace, WhatsApp)
7. Usuarios del sistema (admin/ayudante, perfiles)
8. Administración (backup, página pública, usuarios del sistema, etc.)
9. Resumen rápido (tabla "Quiero… / Dónde")

*(Secciones Calendario, Presentismo, Progreso con modal y Pizarra eliminadas en Mar 2026; ver ELIMINACION_CALENDARIO_Y_PIZARRA_MAR2026.md.)*

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

*Última actualización: Marzo 2026. Depuración de datos eliminada (§2.1). Para pendientes ver PENDIENTES_FINALES.md.*
