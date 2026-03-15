# Eliminación de calendario, asistencias y pizarra online (Mar 2026)

Documentación de la eliminación completa de los módulos **calendario**, **sistema de asistencias/fichadas** y **pizarra online** (incluida la sala de transmisión TV). La aplicación queda orientada al uso 100 % virtual: sin control de presente/ausente ni pizarra en sala.

---

## 1. Calendario – eliminado

### Archivos eliminados
- **Controlador:** `CalendarioController.java`
- **Servicios:** `CalendarioService.java`, `CalendarioExcepcionService.java`
- **Repositorio:** `CalendarioExcepcionRepository.java`
- **Entidad:** `CalendarioExcepcion.java`
- **DTO:** `CalendarioSemanalDTO.java`
- **Plantillas:** `calendario/semanal.html`, `calendario/semanal-profesor.html`

### Referencias quitadas
- **SecurityConfig:** Eliminado `requestMatchers("/calendario/**")` de las rutas autorizadas.
- **Dashboard:** Eliminada la tarjeta/enlace "Calendario" en el grid del profesor.
- **UsuarioService:** En `eliminarUsuario` ya no se usa `CalendarioExcepcionRepository` (excepciones por día).

---

## 2. Sistema de asistencias (fichadas) – eliminado

### Archivos eliminados
- **Entidades:** `Asistencia.java`, `DiaHorarioAsistencia.java`
- **Enum:** `TipoAsistencia.java`
- **Repositorio:** `AsistenciaRepository.java`
- **Servicio:** `AsistenciaService.java`
- **DTO:** `AsistenciaVistaDTO.java`

### Referencias quitadas
- **UsuarioService:** Eliminadas inyección y uso de `AsistenciaRepository` y `CalendarioExcepcionRepository`. Al eliminar un alumno solo se borran mediciones físicas y rutinas asignadas.
- **ProfesorController:**  
  - Eliminada inyección de `AsistenciaService`.  
  - Dashboard: sin construcción de `estadoAsistenciaHoy`; sin columna "Presente" ni botones ciclo Presente/Ausente/Pendiente.  
  - Detalle alumno (`verAlumno`): sin `historialAsistencia`, `asistenciaHoy` ni `gruposMusculares`.  
  - Eliminados endpoints: `GET /alumnos/{id}/asistencias`, `POST .../asistencias/{asistenciaId}/registrado-por`, `POST /alumnos/{id}/asistencia`, `POST /alumnos/{id}/asistencia/deshacer`, `POST /alumnos/{id}/progreso`.
- **Dashboard:** Sin columna "Presente", sin botón "Progreso" por fila, sin JavaScript de ciclo de estados ni llamada a API de marcar asistencia.
- **alumno-detalle.html:** Eliminado el modal "Registrar progreso" y el formulario que enviaba a `/progreso`. La tarjeta "Progreso del alumno" es solo informativa: texto "Sin registros de progreso en esta aplicación." (sin abrir modal ni enviar datos).
- **DepuracionService:** Eliminados `AsistenciaRepository`, `contarAsistenciasAntesDe` y `depurarAsistenciasAntesDe`. Depuración solo de rutinas asignadas.
- **AdminPanelController:** Eliminado `POST /profesor/depuracion/asistencias`.
- **depuracion.html:** Eliminada la tarjeta "Registro de asistencias e inasistencias".
- **administracion.html:** Mensaje de depuración solo para tipo "rutinas".
- **AlumnoJsonBackupService:** Sin exportar/importar asistencias; sin `AsistenciaRepository` ni `mapToAsistencia`.
- **AlumnoExportService:** Eliminada la columna "Último trabajo" (dependía de asistencias); sin `AsistenciaRepository` ni `formatearUltimoTrabajo`.

### Base de datos
Las tablas `asistencia`, `calendario_excepcion` (y relacionadas) pueden quedar en la BD si ya existían. Se pueden borrar a mano o regenerar el esquema (por ejemplo borrando la BD y dejando que Hibernate la cree de nuevo).

---

## 3. Pizarra online y sala de transmisión – eliminado

### Archivos eliminados
- **Controladores:** `PizarraController.java`, `SalaController.java`
- **Servicio:** `PizarraService.java`
- **Entidades:** `Pizarra.java`, `PizarraColumna.java`, `PizarraItem.java`, `PizarraTrabajo.java`, `SalaTransmision.java`
- **Repositorios:** `PizarraRepository.java`, `PizarraColumnaRepository.java`, `PizarraItemRepository.java`, `PizarraTrabajoRepository.java`, `SalaTransmisionRepository.java`
- **DTO:** `PizarraEstadoDTO.java`
- **Plantillas:**  
  - `profesor/pizarra-editor.html`, `profesor/pizarra-lista.html`, `profesor/pizarra-nueva.html`  
  - `sala/sala.html`, `sala/sala-pin.html`, `sala/sala-error.html`

### Referencias quitadas
- **CustomAuthenticationSuccessHandler:** Eliminados `PizarraService` y la llamada a `rotarTokenSala(profesor.getId())` en el login.
- **SecurityConfig:** Eliminado `requestMatchers("/sala/**").permitAll()`.
- **Dashboard:** Eliminada la tarjeta "Pizarra" (enlace a `/profesor/pizarra`) y estilos `.btn-pizarra`, `.card-pizarra`. La columna 3 del grid queda solo con "Rutinas".
- **ExerciseZipBackupService:** Eliminados `PizarraItemRepository` y la eliminación de items de pizarra (`deleteAllItems`) en el borrado previo a "Suplantar" al importar backup ZIP.

### Base de datos
Pueden quedar tablas `pizarra`, `pizarra_columna`, `pizarra_item`, `pizarra_trabajo`, `sala_transmision`. Se pueden eliminar manualmente o regenerar el esquema.

---

## 4. Resumen de mejoras (app 100 % virtual)

| Área              | Antes                                      | Después                                                                 |
|-------------------|--------------------------------------------|--------------------------------------------------------------------------|
| Calendario        | Semanal, excepciones, vista profesor      | Eliminado                                                               |
| Asistencias       | Registro presente/ausente, columna Presente, modal progreso | Eliminado; tarjeta Progreso solo informativa                            |
| Pizarra / Sala TV | Editor, lista, transmisión por token       | Eliminado                                                               |
| Dashboard         | Tarjetas Calendario y Pizarra, columna Presente, botón Progreso | Sin esas tarjetas/columna/botón                                        |
| Depuración        | Asistencias + rutinas asignadas            | Solo rutinas asignadas                                                  |
| Backup alumnos    | JSON con asistencias, Excel con "Último trabajo" | JSON sin asistencias; Excel sin columna Último trabajo                  |
| Login             | Rotación de token de sala al ingresar      | Redirección directa al panel                                            |

---

## 5. Revisión posterior: estado ACTIVO/INACTIVO del alumno

Tras la eliminación de calendario y asistencias se revisó que la lógica de **ACTIVO/INACTIVO** en creación y modificación de alumno no dependa de entidades o servicios eliminados.

- **UsuarioService.crearAlumno:** Asigna estado por defecto ACTIVO, fechaAlta/fechaBaja y `appendHistorialEstado(usuario, "ALTA")`. Solo usa `Usuario` (estadoAlumno, fechaAlta, fechaBaja, historialEstado) y `usuarioRepository`. Sin referencias a Asistencia, Calendario ni Pizarra.
- **UsuarioService.actualizarUsuario:** Al cambiar a INACTIVO pone fechaBaja, `appendHistorialEstado(..., "BAJA")` y `rutinaService.inactivarTodasRutinasDelAlumno(id)`; al pasar a ACTIVO pone fechaBaja null y `appendHistorialEstado(..., "REACTIVADO")`. Solo usa Usuario, UsuarioRepository y RutinaService (RutinaRepository/Rutina). Sin referencias a módulos eliminados.
- **appendHistorialEstado:** Solo usa `Usuario.getHistorialEstado()` / `setHistorialEstado()` (campo que se mantiene en la entidad).

**Ajuste realizado:** En `UsuarioService.getUsuarioByIdParaFicha` se actualizó el comentario del método: se eliminó la mención a "horarios de asistencia" y se dejó "(profesor, rutinas)".

---

## 6. Archivos de documentación actualizados

- **CHANGELOG.md:** Nueva entrada para eliminación calendario, asistencias y pizarra (Mar 2026); entrada para revisión ACTIVO/INACTIVO y comentario.
- **DOCUMENTACION_UNIFICADA.md:** Actualizado resumen de lo implementado; eliminadas referencias a calendario, asistencias, pizarra y sala; depuración solo rutinas; backup alumnos sin asistencias/Último trabajo; manual sin secciones calendario/pizarra.
- **LEEME_PRIMERO.md:** Actualizado "Qué hace", "Dónde está cada cosa" y "Resumen de estado"; eliminaciones de Fase 3 marcadas como realizadas.
- **CAMBIOS_PANEL_ALUMNO_Y_ASISTENCIAS_MAR2026.md:** Se puede anotar que la eliminación de calendario y asistencias ya está hecha (este documento la describe).
- **ELIMINACION_CALENDARIO_Y_PIZARRA_MAR2026.md:** Añadida sección 5 (revisión ACTIVO/INACTIVO y ajuste de comentario en getUsuarioByIdParaFicha).

---

*Última actualización: Marzo 2026.*
