# Cambios: panel alumno, filtros, módulo asistencias y detalle (Mar 2026)

Documentación de los cambios realizados en la vista de alumnos del panel, en el detalle del alumno y en la eliminación del módulo de asistencias de la UI. **Realizado después:** eliminación completa de calendario, asistencias (backend y UI) y pizarra online; ver [ELIMINACION_CALENDARIO_Y_PIZARRA_MAR2026.md](ELIMINACION_CALENDARIO_Y_PIZARRA_MAR2026.md).

---

## 1. Entidad Usuario y campos eliminados

- **Eliminados de la entidad `Usuario`:**  
  `tipoAsistencia`, `diasHorariosAsistencia`, `contactoEmergenciaNombre`, `contactoEmergenciaTelefono`, `historialAsistencia`, `detalleAsistencia`, `medicionesFisicas` (relación OneToMany).  
- **Mantenido:** `correo` (nullable), usado para login de profesores (`UserDetails.getUsername()`).
- **Repositorio:** Eliminados `findByTipoAsistencia` y `findByIdWithMediciones`.  
- **Servicios/controladores:** Actualizados para no usar esos campos (CalendarioService, ProfesorController, UsuarioService, AsistenciaService, AlumnoExportService, AlumnoJsonBackupService, CalendarioController).  
- La entidad `MedicionFisica` se mantiene; al eliminar un alumno se siguen borrando sus mediciones vía `MedicionFisicaRepository.deleteByUsuario_Id`.

---

## 2. Panel del profesor – Vista “Mis Alumnos”

- **Filtros:** Solo quedan **Buscar por nombre** y **Estado** (Todos / Activos / Inactivos).  
- **Eliminados:** Filtros Tipo, Día y Horario.  
- **Tabla:** Eliminada la columna “Tipo”.  
- **JavaScript:** Guardado/restauración en `localStorage` y lógica de filtrado solo por nombre y estado.  
- **Manual de usuario:** Texto de filtros actualizado en `manual-usuario.html`.

**Archivo:** `profesor/dashboard.html`.

---

## 3. Detalle del alumno – Tarjetas superiores

- **Orden y contenido de las 4 tarjetas:**
  1. **Datos personales:** Nombre, edad, celular, fecha de alta (sin correo; sin fecha de inicio).  
  2. **Restricciones médicas:** Texto o “Ninguna”.  
  3. **Progreso del alumno:** Último registro de progreso (fecha, grupos, observaciones) o “Sin registros. Clic para cargar.”  
  4. **Rutinas asignadas:** Número y “Clic para asignar” (o mensaje si inactivo).

- **Modal “Registrar progreso”:**  
  - Se abre **al hacer clic en la tarjeta “Progreso del alumno”** (no hay botón “Progreso” en el header).  
  - Formulario: fecha, grupos musculares trabajados, observaciones; envía a `POST /profesor/alumnos/{id}/progreso`.  
  - El controlador vuelve a enviar `historialAsistencia`, `asistenciaHoy` y `gruposMusculares` para la tarjeta y el modal. *(Desde Mar 2026: modal y progreso eliminados; tarjeta solo informativa. Ver ELIMINACION_CALENDARIO_Y_PIZARRA_MAR2026.md.)*

- **Editar alumno:**  
  - **Icono de editar** en la esquina superior derecha de la tarjeta “Datos personales” (estilo circular como el “+” de crear alumno en el panel).  
  - **Eliminado** el botón “Editar” del header; solo quedan “Volver al Dashboard” y “Eliminar”.

- **Bloque ampliado** debajo de las tarjetas: Fecha de alta, Sexo, Historial de Estado, Objetivos (sin duplicar datos ya mostrados en las tarjetas).

**Archivos:** `profesor/alumno-detalle.html`, `ProfesorController.verAlumno`.

---

## 4. Módulo de asistencias – Eliminado de la UI (detalle alumno)

- **Eliminado en la vista detalle del alumno:**  
  - Bloque “Últimos 5 asistencias/ausencias” (tabla y botón “Consultar asistencias”).  
  - Modal “Consultar asistencias” (resumen mensual).  
  - Modal “Registrar progreso” se mantiene (abierto desde la tarjeta Progreso); el resto del flujo de asistencias en esta pantalla se quitó.

- **Mensaje al eliminar alumno:** Ahora solo menciona “mediciones y rutinas asignadas” (sin “asistencias”).  
- **JavaScript:** Eliminado el código del modal de asistencias y del selector “registrado por”.  
- **Alerta:** Eliminada “Progreso guardado correctamente” cuando no aplicaba; se mantiene cuando se guarda desde el modal de progreso (`?progreso=ok`).

Los endpoints de asistencias y progreso en el backend se mantienen; solo se quitaron los enlaces y la UI de asistencias en el detalle. La funcionalidad “Registrar progreso” (guardar fecha, grupos, observaciones) sigue disponible desde la tarjeta.

---

## 5. Vista alumno-detalle – Otras referencias

- **Correo:** No se muestra en la tarjeta “Datos personales” (solo nombre, edad, celular, fecha de alta). El campo `correo` sigue en la entidad para login de profesores.  
- **Ficha alumno (vista detalle):** Uso de “Fecha de alta” en lugar de “Fecha de inicio” donde correspondía.

---

## 6. Calendario – Pendiente

- **Próximo paso acordado:** Eliminación del **calendario** (y, si se desea, limpieza completa del sistema de asistencias en backend).  
- Este documento no incluye esos cambios; se documentarán en una siguiente sesión.

---

## Archivos tocados (resumen)

- **Entidad:** `Usuario.java`  
- **Repositorios:** `UsuarioRepository.java`  
- **Servicios:** `UsuarioService.java`, `CalendarioService.java`, `AsistenciaService.java`, `AlumnoExportService.java`, `AlumnoJsonBackupService.java`  
- **Controladores:** `ProfesorController.java`, `CalendarioController.java`  
- **Vistas:** `profesor/dashboard.html`, `profesor/alumno-detalle.html`, `profesor/alumno-detalle.html` (manual), `profesor/manual-usuario.html` (filtros)

---

*Documento creado Mar 2026. Siguiente: eliminación de calendario.*
