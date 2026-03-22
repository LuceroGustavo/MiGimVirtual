# Paleta de colores por módulo – MiGymVirtual

Referencia de los colores asignados a cada módulo del panel del profesor. Se usan tonos **pastel** y sus derivaciones para mantener consistencia visual.

---

## Módulos y colores

| Módulo | Color principal | Uso |
|--------|-----------------|-----|
| **Alumnos** | Verde | Pestañas, tarjetas, botones, filtros. |
| **Series** | Violeta / rosa viejo | Pestañas, tarjetas, botones, filtros. |
| **Rutinas** | Amarillo | Pestañas, tarjetas, botones, filtros. |
| **Asignaciones** | Azul / celeste | Pestañas, tarjetas, botones, filtros. |
| **Ejercicios** | Naranja | Pestañas, tarjetas, botones, filtros. |
| **Administrar el sistema** | Gris | Menú, secciones de administración. |

---

## Sectores dentro de Administrar sistema (Mar 2026)

Dentro del panel **Administrar sistema** (y en la URL directa de cada sección), algunos bloques usan **marco y fondo suave** para orientar al usuario. Los colores **riman** con el módulo de backups: violeta = “catálogo / perfil”, azul = “alumnos en bloque / listado”.

| Bloque | Color de contorno (referencia) | Uso |
|--------|--------------------------------|-----|
| Backup **contenido** (ZIP) | `#7e57c2` (violeta, familia `#5e35b1`) | Ejercicios, rutinas plantilla, series, categorías, imágenes |
| Backup **alumnos** (JSON) | `#039be5` (azul / cian) | Datos de alumnos, mediciones, progresos |
| **Usuarios — Mi usuario / Mi perfil** | Mismo violeta que backup contenido | Edición de nombre, correo, acceso a cambiar contraseña |
| **Usuarios — listado** (tabla) | Mismo azul que backup alumnos | Vista principalmente **DEVELOPER**; gestión de cuentas |

**Implementación:** `static/style.css` — selectores `.admin-backup-fragment …` y `.admin-usuarios-sistema-root …` (necesario para fragmentos cargados sin `<head>` de la plantilla).

---

## Criterios

- **Tonos pastel:** Fondos, tarjetas y botones secundarios (ej. botón "Limpiar") usan el color pastel del módulo.
- **Derivaciones:** Botones principales (ej. "Crear alumno", "Nueva Serie") usan un tono más saturado del mismo color.
- **Consistencia:** Cada módulo mantiene su familia de colores en pestañas activas, cabeceras de tabla, badges y botones de acción.

---

## Referencia en código (dashboard)

Los colores se definen en `profesor/dashboard.html` (y plantillas relacionadas):

- **Alumnos:** `#c8e6c9` (pastel), `#81c784` (principal), `#1b5e20` (texto oscuro).
- **Series:** `#e1bee7` (pastel), `#ce93d8` (principal), `#4a148c` (texto oscuro).
- **Rutinas:** `#ffe082` / `#fff9c4` (pastel), `#e65100` (texto oscuro).
- **Asignaciones:** `#bbdefb` (pastel), `#90caf9` (principal), `#0d47a1` (texto oscuro).
- **Ejercicios:** `#ffccbc` / `#ffab91` (pastel), `#bf360c` (texto/icono oscuro).
- **Administrar:** tonos grises.

---

*Documento de referencia. Actualizado Marzo 2026 (sectores Administración).*
