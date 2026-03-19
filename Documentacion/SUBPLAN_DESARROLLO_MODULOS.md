# Subplan de desarrollo por módulos – MiGimVirtual

**Objetivo:** Ir módulo por módulo (en el orden del panel del profesor) adaptando la app a **uso 100 % online** para personal trainers que no tienen gimnasio físico y envían rutinas a alumnos virtuales. Hacer **todo el panel responsive** (hoy solo la parte pública lo es).

**Referencia:** [PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md](PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md), [LEEME_PRIMERO.md](LEEME_PRIMERO.md).

---

## Visión del producto

- **Quién usa la app:** Personal trainers que trabajan **online** (sin gimnasio físico), enviando rutinas de ejercicios a sus alumnos.
- **Qué debe permitir:**
  - Crear y administrar **alumnos virtuales**.
  - Crear **ejercicios propios**, **series** y **rutinas**.
  - **Enviar rutinas** a alumnos (p. ej. por WhatsApp con enlace a la hoja por token).
  - **Control y estadísticas** de envíos; **alertas** si hace falta.
  - **Página pública** sencilla: promoción de servicios y **formulario de contacto**.
- **Estado actual:** Aproximadamente el 90 % ya está implementado; falta adaptar a 100 % virtual, quitar lo presencial (calendario/asistencia, pizarra/sala) cuando corresponda y hacer **todo responsive** (panel del profesor hoy no lo es).

---

## Orden de trabajo (según el panel del profesor)

Seguimos el orden de las tarjetas del dashboard:

| # | Módulo | Descripción breve | Estado |
|---|--------|-------------------|--------|
| 1 | **Alumnos** | Entidad, métodos, vistas; alumnos virtuales (sin día/horario presencial, sin “Presente”). | 🔲 En curso |
| 2 | **Series** | Crear/editar series, listado; responsive. | Pendiente |
| 3 | **Rutinas** | Crear/editar rutinas, asignación; responsive. | Pendiente |
| 4 | **Asignaciones** | Rutinas asignadas a alumnos, envío por WhatsApp, hoja por token; estadísticas/envíos si aplica. | Pendiente |
| 5 | **Calendario** | Decidir: eliminar (app 100 % virtual) o reorientar (ej. recordatorios/envíos). | Pendiente |
| 6 | **Ejercicios** | ABM ejercicios propios, grupos musculares; responsive. | Pendiente |
| 7 | **Pizarra** | Decidir: eliminar (sala TV presencial) o reemplazar por algo virtual. | Pendiente |
| 8 | **Administrar sistema** | Backup, depuración, usuarios, página pública; responsive. | Pendiente |

Cada módulo se cierra cuando: entidad/métodos están adaptados, vistas responsive y documentación de avances actualizada.

---

## 1. Alumnos

**Objetivo:** Alumnos como **virtuales** (sin asistencia presencial, sin día/horario de clase física). Mantener ficha, rutinas asignadas y envío por WhatsApp.

### Checklist

- [ ] **Entidad `Usuario` (alumno):** Revisar campos; quitar o reorientar día/horario presencial, tipo asistencia presencial, “Presente”. Dejar lo necesario para contacto (nombre, correo, celular, etc.) y estado ACTIVO/INACTIVO.
- [ ] **Servicio `UsuarioService`:** Métodos de alta/baja/edición/listado; quitar lógica de presente/ausente y excepciones de calendario si no aplican.
- [ ] **Controlador / rutas:** ProfesorController (alumnos); asegurar que no queden referencias rotas a calendario o pizarra.
- [ ] **Vistas:** Lista de alumnos, nuevo alumno, detalle alumno (ficha). Quitar columna “Presente” y bloques de día/horario presencial. Hacer **responsive** (móvil/tablet/escritorio).
- [ ] **Detalle alumno:** Mantener rutinas asignadas, “Copiar enlace”, “WhatsApp”; progreso si se mantiene.
- [ ] **Documentación:** Anotar aquí los cambios realizados (archivos tocados, criterios).

### Avances (se irán completando)

| Fecha | Cambio | Archivos / notas |
|-------|--------|-------------------|
| Mar 2026 | Formulario crear/editar alumno: quitados correo, tipo asistencias, selector horarios/calendario, contacto emergencia, historial físico (mediciones + IMC). Entidad: correo explícito nullable. | `nuevoalumno.html`, `Usuario.java`. AYUDA_MEMORIA: nota calendario historial rutinas enviadas (más adelante). |

---

## 2. Series

- [ ] Entidad y repositorio; métodos de creación/edición/listado.
- [ ] Vistas crear/editar/ver serie: **responsive**.
- [ ] Integración con rutinas (series como bloques de una rutina).
- [ ] Avances: _por anotar_.

---

## 3. Rutinas

- [ ] Entidad y servicio; crear/editar rutina (series, orden).
- [ ] Vistas: **responsive**.
- [ ] Asignación rutina → alumno (enlace por token).
- [ ] Avances: _por anotar_.

---

## 4. Asignaciones

- [ ] Listado de rutinas asignadas; envío por WhatsApp (enlace hoja).
- [ ] Hoja pública `/rutinas/hoja/{token}` (ya existe; revisar responsive).
- [ ] Control/estadísticas de envíos y alertas (si se implementan).
- [ ] Avances: _por anotar_.

---

## 5. Calendario

- [ ] Decisión: **eliminar** (app 100 % virtual) o **reorientar** (ej. recordatorios, fechas de envío).
- [ ] Si se elimina: según subplan en PLAN_DE_DESARROLLO_MIGIMVIRTUAL (controlador, servicio, entidades, templates).
- [ ] Avances: _por anotar_.

---

## 6. Ejercicios

- [x] **Vista Mis Ejercicios:** responsive completada (Mar 2026). Tarjetas con + arriba derecha, mismo tamaño, cantidad grupos musculares; móvil cuadradas; filtros; modal al tocar fila; barra inferior. Ver GUIA_RESPONSIVE.md §5.6.
- [ ] **Crear ejercicio:** responsive pendiente.
- [ ] **Modificar ejercicio:** responsive pendiente.
- [ ] **Crear grupos musculares:** responsive pendiente.
- [ ] **Modificar grupos musculares:** responsive pendiente.
- [ ] Avances: Vista lista terminada Mar 2026. Pendiente: formularios crear/modificar ejercicio y grupos musculares.

---

## 7. Pizarra

- [ ] Decisión: **eliminar** (sala TV presencial) o reemplazar por funcionalidad virtual.
- [ ] Si se elimina: según subplan en PLAN_DE_DESARROLLO_MIGIMVIRTUAL.
- [ ] Avances: _por anotar_.

---

## 8. Administrar sistema

- [ ] Backup, depuración, usuarios del sistema, configuración de página pública.
- [ ] Vistas de administración: **responsive**.
- [ ] Página pública: landing, planes, formulario de contacto (ya existe; revisar responsive y contenidos).
- [ ] Avances: _por anotar_.

---

## Criterios generales

- **Responsive:** Todas las vistas del panel del profesor deben verse y usarse bien en móvil, tablet y escritorio (móvil primero si se define así).
- **Virtual:** Sin dependencias de “asistencia presencial”, “día/horario de clase”, “Presente” en sala; enfoque en envío de rutinas y seguimiento online.
- **Avances:** Ir anotando en cada sección del subplan (fecha, cambio, archivos) para seguir el progreso.

---

*Creado: Marzo 2026. Primer módulo a trabajar: **Alumnos** (entidad y métodos).*
