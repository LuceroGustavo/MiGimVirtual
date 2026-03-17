# Ayuda memoria – Contenido unificado

**El contenido de este archivo se unificó con el plan de desarrollo.**

Para ver la lista de mejoras pendientes e implementadas (ítem por ítem), el checklist y los pendientes detallados, consultá:

**[PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md](PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md)** – Fases y subplan de eliminaciones para app 100 % virtual; pendientes.

---

## Para mañana (lista rápida) – ✅ Completado Feb 2026

1. ~~**Formulario crear alumno – Correo opcional:**~~ ✅ Implementado. Campo correo opcional; script `alter_usuario_correo_nullable.sql`.
2. ~~**Alumno → inactivo:**~~ ✅ Implementado. Al dar de baja, se inactivan todas las rutinas asignadas.
3. ~~**Detalle alumno – Rutinas:**~~ ✅ Implementado. Iconos, reseña con texto truncado, acciones centradas.
4. ~~**Volver al origen tras guardar rutina:**~~ ✅ Implementado. Parámetros `alumnoId` y `returnTab` en editar rutina.
5. ~~**Modal de progreso:**~~ ✅ Implementado. Checkbox asistencia eliminado; asistencia se gestiona en panel/calendario.
6. ~~**Formulario modificar rutina:**~~ ✅ Implementado. Nuevo layout: Series a seleccionar (izq, 2 por fila) | Series seleccionadas (der) | Detalles abajo.
7. ~~**Vista alumnos:**~~ ✅ Implementado. Botón "Crear alumno" en título de Mis Alumnos.
8. ~~**Lista rutinas asignadas:**~~ ✅ Implementado. Textos abreviados, iconos estado, acciones centradas.

---

## Pendientes – Ejercicios y vistas

1. **Mejorar HTML crear y modificar ejercicios:**
   - ~~Formularios acordes al resto de creaciones (estructura, estilos).~~ ✅ **Hecho (Mar 2026):** título compacto, ancho completo, cabecera gradiente "Datos del ejercicio", mismo criterio que crear serie. Colores por módulo ejercicios (violeta #764ba2/#667eea) aplicados.
   - **Pendiente:** En la **creación de ejercicios**, incluir un **acceso directo** a la creación de grupos musculares (enlace o botón a `/profesor/mis-grupos-musculares/nuevo` o similar).
2. **Mejorar modal que muestra el ejercicio:** ~~En la lista de Mis Ejercicios.~~ ✅ **Hecho (Mar 2026):** modal con cabecera gradiente, botón cerrar, badge lavanda/violeta, imagen en contenedor redondeado, alineado con series y rutinas. En la hoja de rutina (alumno): botón "Ver video" solo si hay URL. Pendiente si hay otro modal de ejercicio en otras vistas por revisar.

---

## Verificar / reparar

### Scroll vertical en vista de progresos (móvil)
- **Pendiente:** Verificar que el scroll **vertical** se active al superar los 5 registros en la vista de progresos del alumno, solo en móvil. Actualmente no funciona correctamente (se activa scroll horizontal en lugar de vertical).

### Eliminar usuario y rutinas asignadas

- **Problema:** Al eliminar todos los usuarios y luego ir a "Rutinas asignadas", al abrir una rutina aparecía que no se podía ver porque solo se pueden ver rutinas asignadas a usuarios (al no existir el usuario, la rutina quedaba huérfana).
- **Lógica a seguir:** Al **eliminar un usuario**, deben **eliminarse también todas sus rutinas asignadas** (activas e inactivas). Así, si se eliminan todos los usuarios, no debe haber rutinas asignadas.
- **Implementado:** En `UsuarioService.eliminarUsuario` se eliminan las rutinas del alumno con `rutinaService.eliminarRutina(id)` en lugar de solo desasignarlas (antes se hacía `setUsuario(null)`).
- **Backup de alumnos:** El sistema de backup está **terminado** (marzo 2026). Si se quiere **mantener el historial del usuario** antes de eliminarlo, usar **Backup y resguardo** → "Exportar backup" (JSON de alumnos) o "Exportar a Excel" (reportes). Ver [DOCUMENTACION_UNIFICADA.md](DOCUMENTACION_UNIFICADA.md) § Backup y exportación.

---

## Paginación / límite de registros en panel del profesor

- **Pendiente:** Definir una **cantidad máxima de registros** en las vistas del panel del profesor (Mis Alumnos, Mis Series, Mis Rutinas, Asignaciones, etc.). Si hay muchos registros (ej. 50 o más), la lista no termina y es poco óptimo, especialmente en celular.
- **Para probar:** Crear un **script con datos de prueba** (alumnos, series, rutinas, asignaciones) que genere suficiente volumen para validar la paginación y el rendimiento. Verlo después.
- **Opciones a evaluar:** paginación, scroll infinito, o límite por defecto (ej. últimas 20) con "Ver más" o búsqueda.

---

## Calendario / historial de rutinas enviadas (más adelante)

- **Pendiente:** Crear un calendario (o módulo similar) para **historial de rutinas enviadas** a alumnos (fechas de envío, qué rutina, a quién). No es el calendario actual de asistencia presencial; es nuevo y orientado a uso 100 % virtual.

---

## Sistema de backup — terminado (Mar 2026)

- **Ejercicios + grupos + rutinas + series:** Exportar/importar ZIP desde Administración → Backup y resguardo. Opciones por checkbox (Grupos, Ejercicios, Rutinas, Series). Agregar o Suplantar.
- **Alumnos:** Exportar backup (JSON) o Exportar a Excel (reportes con columna Último trabajo). Importar desde JSON (Agregar o Suplantar).
- **Documentación:** [DOCUMENTACION_UNIFICADA.md](DOCUMENTACION_UNIFICADA.md) (Backup y exportación, Excel alumnos).

---

## Módulo de Series – vista responsive (Mar 2026) – ✅ Completado

Vista responsive del módulo de series terminada:

- **Vista detalle de serie** (`verSerie.html`): responsive (grid 1/2/3 cols), flecha "Volver al panel" solo móvil, footer; badge "vueltas" en verde (#7ee787), badge "ejercicios" en naranja.
- **Dashboard pestaña Series**: modal al tocar fila (móvil); botón Ver cierra modal y navega en misma pestaña.
- **Formulario crear/modificar serie** (`crearSerie.html`): título más pequeño, filtros en 2 filas en móvil, panel serie con estilo violeta, botón Limpiar, reorden de paneles (ejercicios → tabla serie → nombre/guardar), table-responsive, flechas subir/bajar en tabla, footer móvil.

Opcional más adelante: revisar vista editar serie si requiere ajustes específicos.

Ver GUIA_RESPONSIVE.md §5.2.

---

## Siguiente sesión – pendiente

- **Continuar con creación de rutinas** (módulo de rutinas: crear, modificar, asignar, etc. desde el panel del profesor).

---

## Próximos pasos (orden sugerido)

1. ~~**Mejorar front con responsividad** (panel del profesor).~~ ✅ **Hecho (Mar 2026):** login, navbar compacto, dashboard 6 tarjetas en móvil, tabla alumnos (acción, celular icono, ver solo ojo, asignar desde detalle), footer una fila. Ver GUIA_RESPONSIVE.md §5 y CHANGELOG [2026-03-15].
2. ~~**Vista de ficha del alumno – mejoras para móvil.**~~ ✅ **Hecho (Mar 2026):** contenedor, título y subtítulo adaptados; header (Volver/Eliminar) a ancho completo; tarjetas en 1 columna; bloque alumno-info apilado; botones de tabla con área táctil mínima 38px; breakpoints 991px y 575px. **Vista del alumno terminada:** modal progreso al tocar registro (móvil), modal confirmar eliminar progreso (estilo borrar alumno), botón Guardar notas, Eliminar usuario debajo de todo, barra inferior móvil igual al panel, formato fecha dd/MM/yy. Pendiente: scroll vertical en progresos (móvil, >5 registros).
3. ~~**Módulo de series (vista responsive).**~~ ✅ **Hecho (Mar 2026):** ver sección "Módulo de Series" más arriba. Opcional: revisar editar serie y flujos restantes.
4. **Módulo de rutinas (creación de rutinas):** continuar con crear, modificar, asignar, etc. desde el panel del profesor (ver "Siguiente sesión" más arriba).
5. **Luego:** Revisar/cambiar el **sistema de backup** — todavía no está definido si va a quedar como está o se modificará.
6. **Luego:** **Modificar el manual del usuario** (actualizar contenido y estructura según los cambios de la app).

---

*Última actualización: Marzo 2026. AYUDA_MEMORIA y PLAN_DE_DESARROLLO se unificaron en un solo documento.*
