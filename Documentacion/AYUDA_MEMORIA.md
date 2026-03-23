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

## Página pública — virtual (Mar 2026) — ✅ Base implementada

- **Qué:** Landing y Planes usan la misma config BD; eslogan, email, redes (IG, TikTok, YouTube, Facebook, LinkedIn, X/Twitter), dirección y **URL de mapa** opcionales; textos orientados a **online**.
- **Dónde editar:** Administrar sistema → **Página pública** → Configuración de datos.
- **Detalle técnico:** [DOCUMENTACION_UNIFICADA.md](DOCUMENTACION_UNIFICADA.md) §1.1 bis; `CHANGELOG.md` entrada **feat(publica)**.
- **Pendiente opcional:** Reemplazar imágenes estáticas del carrusel y afinar copy. ~~Manual:~~ ✅ actualizado Mar 2026 (100 % virtual, §11 secciones, §4.1 imágenes 5 MB, sin calendario/pizarra) — ver `DOCUMENTACION_UNIFICADA.md` **§1.3**.

---

## Pendientes – Ejercicios y vistas

1. **Vista Mis Ejercicios:** ✅ **Terminada (Mar 2026):** Tarjetas con botón + arriba derecha, mismo tamaño, cantidad en grupos musculares; móvil: cuadradas lado a lado; filtros en card; tabla con modal al tocar fila; barra inferior móvil. Ver GUIA_RESPONSIVE.md §5.6.
2. **Mejorar HTML crear y modificar ejercicios:**
   - ~~Formularios acordes al resto de creaciones (estructura, estilos).~~ ✅ **Hecho (Mar 2026):** título compacto, ancho completo, cabecera gradiente "Datos del ejercicio", mismo criterio que crear serie. Colores por módulo ejercicios (violeta #764ba2/#667eea) aplicados.
   - **Pendiente responsive:** ~~Crear ejercicio~~ ✅; ~~modificar ejercicio~~ ✅ (Mar 2026).
   - ~~**Acceso directo** a grupos musculares en crear ejercicio~~ ✅ botón “Crear grupo muscular”.
3. **Grupos musculares:** ~~**Pendiente responsive**~~ ✅ **Hecho (Mar 2026):** lista y formulario editar responsive. Ver GUIA_RESPONSIVE §5.6.
4. **Categorías de rutinas:** ✅ **ABM implementado (Mar 2026):** entidad `Categoria`; categorías del sistema + propias del profesor; lista en `/profesor/mis-categorias`; crear, editar, eliminar; selección en crear/editar rutina. Ver DOCUMENTACION_UNIFICADA §1 y §5.
5. **Mejorar modal que muestra el ejercicio:** ~~En la lista de Mis Ejercicios.~~ ✅ **Hecho (Mar 2026):** modal con cabecera gradiente, botón cerrar, badge lavanda/violeta, imagen en contenedor redondeado, alineado con series y rutinas. En la hoja de rutina (alumno): botón "Ver video" solo si hay URL. Pendiente si hay otro modal de ejercicio en otras vistas por revisar.

---

## Verificar / reparar

### Modificar ejercicio – módulo imagen y botón Guardar
- ~~**Problema:** El formulario terminaba en "Grupos musculares"; no se mostraban la sección de imagen ni los botones. Consola: `ERR_INCOMPLETE_CHUNKED_ENCODING`.~~ ✅ **Reparado (Mar 2026):** `urlImagenActual` y `returnUrlEditar` calculados en el controlador; expresiones Thymeleaf simplificadas con `#strings.isEmpty()`. Ver CHANGELOG [2026-03-23] fix.

### Scroll vertical en vista de progresos (móvil)
- **Pendiente:** Verificar que el scroll **vertical** se active al superar los 5 registros en la vista de progresos del alumno, solo en móvil. Actualmente no funciona correctamente (se activa scroll horizontal en lugar de vertical).

### Eliminar usuario y rutinas asignadas

- **Problema:** Al eliminar todos los usuarios y luego ir a "Rutinas asignadas", al abrir una rutina aparecía que no se podía ver porque solo se pueden ver rutinas asignadas a usuarios (al no existir el usuario, la rutina quedaba huérfana).
- **Lógica a seguir:** Al **eliminar un usuario**, deben **eliminarse también todas sus rutinas asignadas** (activas e inactivas). Así, si se eliminan todos los usuarios, no debe haber rutinas asignadas.
- **Implementado:** En `UsuarioService.eliminarUsuario` se eliminan las rutinas del alumno con `rutinaService.eliminarRutina(id)` en lugar de solo desasignarlas (antes se hacía `setUsuario(null)`).
- **Backup de alumnos:** El sistema de backup está **terminado** (marzo 2026). Si se quiere **mantener el historial del usuario** antes de eliminarlo, usar **Administrar sistema → Sistema de backups** → **Guardar backup** (JSON de alumnos en servidor). Ver [DOCUMENTACION_UNIFICADA.md](DOCUMENTACION_UNIFICADA.md) §2.

---

## Scripts de datos de prueba (scripts/BD/)

Scripts SQL para cargar datos de prueba y testear la app:

| Script | Contenido |
|--------|-----------|
| 00_limpiar_datos_prueba.sql | (Opcional) Elimina todos los datos de prueba |
| 01_usuarios_prueba.sql | 10 alumnos con todos los campos completos |
| 02_progresos_prueba.sql | Progresos variables (0, 1, 2 o 4 por alumno) |
| 03_series_prueba.sql | 10 series (3–4 ejercicios c/u, reps/peso/tiempo variados) |
| 04_rutinas_prueba.sql | 5 rutinas plantilla basadas en las series |

**Orden:** 01 → 02 → 03 → 04. Requiere BD creada, profesor y ejercicios predeterminados. Ver `scripts/BD/README.md`.

---

## Paginación / límite de registros en panel del profesor

- **Pendiente:** Definir una **cantidad máxima de registros** en las vistas del panel del profesor (Mis Alumnos, Mis Series, Mis Rutinas, Asignaciones, etc.). Si hay muchos registros (ej. 50 o más), la lista no termina y es poco óptimo, especialmente en celular.
- **Para probar:** Usar los scripts en `scripts/BD/` que generan 10 alumnos, progresos, 10 series y 5 rutinas.
- **Opciones a evaluar:** paginación, scroll infinito, o límite por defecto (ej. últimas 20) con "Ver más" o búsqueda.

---

## Calendario / historial de rutinas enviadas (más adelante)

- **Pendiente:** Crear un calendario (o módulo similar) para **historial de rutinas enviadas** a alumnos (fechas de envío, qué rutina, a quién). No es el calendario actual de asistencia presencial; es nuevo y orientado a uso 100 % virtual.

---

## Sistema de backup — terminado (actualizado Mar 2026)

- **Dónde:** Administrar sistema → **Sistema de backups** (`/profesor/backup`).
- **En disco:** ZIP (contenido) y JSON (alumnos) bajo `migimvirtual.backups.dir` (defecto `backup/contenido` y `backup/alumnos`). Máx. **2** archivos por tipo.
- **Restaurar:** Reemplazo total (no “agregar” desde la UI). Export de rutinas/series **solo del profesor del panel** (`profesorId` en manifest v1.2).
- **Alumnos:** JSON con mediciones y progresos; sin Excel en esta pantalla (el servicio Excel puede existir aparte).
- **Ubuntu / servidor:** No hace falta crear `backup` antes: se crea al primer “Guardar backup”. Recomendado: ruta absoluta en `application-*.properties` y permisos de escritura — ver [servidor/DESPLIEGUE-SERVIDOR.md](servidor/DESPLIEGUE-SERVIDOR.md) §6.6.
- **Resumen técnico:** [DOCUMENTACION_UNIFICADA.md](DOCUMENTACION_UNIFICADA.md) §2.
- **Interfaz (Mar 2026):** En la pantalla de backups, bloques con **marco violeta** (contenido/ZIP) y **marco azul** (alumnos/JSON). Estilos en `style.css` para que funcionen también con **fragmento** en Administración (`?fragment=1`). Ver [DOCUMENTACION_UNIFICADA.md](DOCUMENTACION_UNIFICADA.md) §1.2.

---

## Módulo Administración del sistema — cierre UX (Mar 2026) — ✅ Completado

- **Estado:** Módulo **Administrar sistema** dado por cerrado a nivel UX coherente con el resto del panel (responsive previo + diferenciación visual).
- **Backups:** Marcos de color contenido vs alumnos (`backup.html` + `style.css`, clase raíz `.admin-backup-fragment`).
- **Usuarios del sistema:** Marcos de color perfil (“Mi usuario” / “Mi perfil”) vs listado de usuarios (`.admin-usuarios-sistema-root`, sectores `usuarios-sistema-sector-cuenta` / `usuarios-sistema-sector-listado`).
- **Documentación:** §1.2 y **§1.3** (manual Mar 2026) en [DOCUMENTACION_UNIFICADA.md](DOCUMENTACION_UNIFICADA.md), [PALETA_COLORES.md](PALETA_COLORES.md), [GUIA_RESPONSIVE.md](GUIA_RESPONSIVE.md) §5.7, manual en app **§10.2 / §10.3** (pública / backups), `CHANGELOG.md` [2026-03-14] y [2026-03-22].

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

- ~~**Continuar con creación de rutinas** (crear, modificar, panel, hoja).~~ ✅ **Hecho (Mar 2026):** Ver CHANGELOG [2026-03-17] y GUIA_RESPONSIVE §5.3. Panel Rutinas con columna Acciones en escritorio; editar rutina mismo layout que crear; hoja responsive y header en dos filas; volver a tab Rutinas desde editar.
- ~~**Mañana: mejorar responsive de asignación de rutinas:**~~ ✅ **Hecho (Mar 2026):** Tabla con búsqueda, modal de detalle, botón deseleccionar, enlace Ver corregido; móvil: columnas simplificadas (Nombre, Categoría, Series), click en fila abre modal, scroll al asignar. Ver CHANGELOG [2026-03-18] y GUIA_RESPONSIVE §5.4.
- ~~**Pestaña Asignaciones – vistas y responsive:**~~ ✅ **Hecho (Mar 2026):** Filtro con card y Limpiar (estructura igual a Series); móvil: columna Acciones oculta, fila clickeable abre modal con detalle y acciones (sin WhatsApp). Botón Limpiar de Mis Alumnos en verde claro. Ver CHANGELOG [2026-03-20] y GUIA_RESPONSIVE §5.5.
- **Barra inferior de navegación (accesos directos):** ✅ **Hecho (Mar 2026):** Inicio → página pública; Manual → manual de usuario; Consultas → mensajes formulario contacto; Más → config. Fragmento `bottom-nav.html` usado en todas las plantillas del panel. Ver CHANGELOG [2026-03-16] feat(ui) accesos rápidos.
- **Siguiente:** **Ejercicios** — ~~vista~~ ✅ terminada; ~~grupos musculares (lista + editar)~~ ✅ responsive; ~~crear ejercicio~~ ✅; ~~modificar ejercicio~~ ✅ paleta naranja + responsive Mar 2026.
- **Siguiente sesión:** ~~Terminar módulo de administración~~ ✅ **Mar 2026:** UX administración cerrada (sectores de color, docs). ~~**Manual de usuario**~~ ✅ **Mar 2026:** revisión completa + §1.3 en DOCUMENTACION_UNIFICADA.

---

## Próximos pasos (orden sugerido)

1. ~~**Mejorar front con responsividad** (panel del profesor).~~ ✅ **Hecho (Mar 2026):** login, navbar compacto, dashboard 6 tarjetas en móvil, tabla alumnos (acción, celular icono, ver solo ojo, asignar desde detalle), footer una fila. Ver GUIA_RESPONSIVE.md §5 y CHANGELOG [2026-03-15].
2. ~~**Vista de ficha del alumno – mejoras para móvil.**~~ ✅ **Hecho (Mar 2026):** contenedor, título y subtítulo adaptados; header (Volver/Eliminar) a ancho completo; tarjetas en 1 columna; bloque alumno-info apilado; botones de tabla con área táctil mínima 38px; breakpoints 991px y 575px. **Vista del alumno terminada:** modal progreso al tocar registro (móvil), modal confirmar eliminar progreso (estilo borrar alumno), botón Guardar notas, Eliminar usuario debajo de todo, barra inferior móvil igual al panel, formato fecha dd/MM/yy. **Rutinas asignadas (Entorno 2):** tabla Fecha|Nombre|Categorías|Estado; fila clickeable abre modal con tarjeta y acciones (Modificar, Ver, Copiar, Pausar/Activar, WhatsApp). Ver CHANGELOG [2026-03-19]. ~~**Scroll en tablas / progreso (móvil)**~~ ✅ **Mar 2026:** `mgv-scroll-*` en listados + `.progreso-scroll-mobile` en progreso; ver GUIA_RESPONSIVE §5.8 y CHANGELOG [2026-03-15].
3. ~~**Módulo de series (vista responsive).**~~ ✅ **Hecho (Mar 2026):** ver sección "Módulo de Series" más arriba. Opcional: revisar editar serie y flujos restantes.
4. ~~**Módulo de rutinas (creación, modificar, panel, hoja, asignar).**~~ ✅ **Hecho (Mar 2026):** CHANGELOG [2026-03-17], [2026-03-18].
5. ~~**Vista Mis Ejercicios.**~~ ✅ **Hecho (Mar 2026):** Tarjetas con + arriba derecha, mismo tamaño, cantidad grupos musculares; móvil cuadradas; filtros; modal al tocar fila; barra inferior. Ver GUIA_RESPONSIVE.md §5.6 y CHANGELOG [2026-03-21]. ~~Grupos musculares (lista + editar).~~ ~~Crear ejercicio (paleta + responsive).~~ ~~Modificar ejercicio (responsive / alinear con crear).~~ ✅ Módulo Ejercicios responsive completado.
6. ~~**Sistema de backup**~~ — Actualizado Mar 2026 (servidor, restauración total, export por profesor). Ver § “Sistema de backup” arriba y DOCUMENTACION_UNIFICADA §2.
7. ~~**Modificar el manual del usuario**~~ ✅ **Mar 2026:** `manual-usuario.html` alineado a app virtual; **§1.3** en DOCUMENTACION_UNIFICADA (profesor, §4.1 imágenes, §10 administración).

---

## Pendientes – Módulo de administración y manual

- **Terminar módulo de administración:** Responsive mejorado (textos cortos, dropdown móvil funcional, tabla consultas). Revisar vistas restantes, alineación y flujos en móvil.
- ~~**Revisar manual de usuario**~~ ✅ Mar 2026 — ver §1.3 y `CHANGELOG` [2026-03-14].

---

## Página pública y admin página pública — gimnasio virtual (recordatorio)

**Contexto:** La **página pública** (`/`, `/planes`) y el bloque de **configuración en administración de página pública** (`/profesor/pagina-publica`) fueron pensados en origen para un **gimnasio físico** (administración presencial): campos y textos tipo **dirección**, **días y horarios**, teléfono fijo, etc.

**Objetivo:** Adaptar todo al modelo **gimnasio virtual** (entrenamiento online / planes digitales):

1. **Página pública**  
   - Revisar secciones y textos: lo que implique **local físico**, **horarios de sala** o **contacto presencial** no debería ser obligatorio o debería eliminarse/sustituirse (ej. enfocar en WhatsApp, redes, valor del servicio virtual).  
   - Ajustar landing y `/planes` para que el mensaje y los datos mostrados reflejen **virtual** y no “administración de gimnasio” clásica.

2. **Administración de la página pública**  
   - Tras definir qué muestra la pública, **simplificar o reorganizar** el formulario de configuración (quitar campos que ya no apliquen, renombrar ayudas, validaciones).  
   - Mantener coherencia con el formulario de contacto (ej. ya se usa “Celular” en `/planes`).

**Documentación de lo ya hecho (Mar 2026):** Ver `DOCUMENTACION_UNIFICADA.md` §1.1 (WhatsApp en modal consulta, marca MiGymVirtual, consultas auto-leídas en móvil, acciones en fila en tabla, flechas orden en tarjetas móvil, etc.).

---

## Hecho recientemente — resumen rápido (Mar 2026)

- Marca **MiGymVirtual** en UI y mensajes; consulta → **WhatsApp** desde modal; consulta móvil **auto “vista”**; tabla consultas **Visto + Eliminar** en una línea; **Celular** en formulario `/planes`; menú admin móvil con **rayitas**; **orden de planes** con flechas en la tarjeta (móvil), no en el modal.
- **Manual de usuario** (`/profesor/manual`): revisión 100 % virtual, enfoque profesor, **§4.1** imágenes (5 MB), administración **§10**; documentado en **DOCUMENTACION_UNIFICADA §1.3**.
- **Cierre app (Mar 2026):** scroll interno en tablas de listado móvil (`mgv-scroll-*`, GUIA_RESPONSIVE §5.8); scripts BD ampliados + `06_asignaciones_prueba.sql`; índice **INDICE_DOCUMENTACION.md**; ver **CHANGELOG [2026-03-15]** y **COMMIT_RELEASE_MAR2026.md**.

---

*Última actualización: Marzo 2026 — doc sincronizada con manual (§1.3). AYUDA_MEMORIA y PLAN_DE_DESARROLLO se unificaron en un solo documento.*
