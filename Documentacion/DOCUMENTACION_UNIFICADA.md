# Documentación unificada – Referencias y resúmenes

Contenido importante reunido de los documentos del proyecto. Para contexto: [LEEME_PRIMERO.md](LEEME_PRIMERO.md), [AYUDA_MEMORIA.md](AYUDA_MEMORIA.md), [INDICE_DOCUMENTACION.md](INDICE_DOCUMENTACION.md) y [PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md](PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md).

---

## 0.1 Paleta de colores por módulo

| Módulo | Color | Notas |
|--------|-------|-------|
| Alumnos | Verde | Pastel y derivaciones |
| Series | Violeta / rosa viejo | Pastel y derivaciones |
| Rutinas | Amarillo | Pastel y derivaciones |
| Asignaciones | Azul / celeste | Pastel y derivaciones |
| Ejercicios | Naranja | Pastel y derivaciones |
| Administrar el sistema | Gris (menú); sectores violeta/azul en backups y usuarios | Pastel y derivaciones; marcos §1.2 y [PALETA_COLORES.md](PALETA_COLORES.md) |

**Detalle:** Ver [PALETA_COLORES.md](PALETA_COLORES.md).

---

## 0. Evolución MiGymVirtual (realizada)

- **Proyecto:** Nombre MiGymVirtual; paquete `com.MiGymVirtual`; clase principal `MiGymVirtualApplication`.
- **Base de datos:** `MiGymVirtual` (URL en `application*.properties`); se crea al arrancar si no existe; tablas con `ddl-auto=update`.
- **Credenciales de desarrollo:** `profesor@migymvirtual.com` / `profesor`; `lucerogustavosi@gmail.com` / `Qbasic.1977`.
- **Marca:** Navbar con logo `mgvirtual_logo1.png` y fondo `fondo-navbar.png`. Página pública: carrusel con videos `video_mgvirtual_inicio_escritorio.mp4` (escritorio) y `Video_mgvirtual_inicio_movil.mp4` (móvil).
- **Servidor:** Menú `./MiGymVirtual`; scripts `reset_db_MiGymVirtual.sql` / `reset_db_MiGymVirtual.sh`; variables `MiGymVirtual_DB_USER`, `MiGymVirtual_DB_PASSWORD`.

---

## 1. Resumen de lo implementado

- **Panel único:** Profesor (roles DEVELOPER, ADMIN, AYUDANTE). Sin panel alumno ni admin separado.
- **Ejercicios:** Predeterminados 1–60 desde `uploads/ejercicios/`; ABM; grupos musculares como entidad (`GrupoMuscular`); formularios y modal Ver alineados con series/rutinas. **Módulo Ejercicios responsive completado (Mar 2026):** vista Mis Ejercicios, crear ejercicio, modificar ejercicio, grupos musculares (lista + editar); paleta naranja; barra inferior móvil.
- **Series y rutinas:** ABM; asignación rutina → alumno; enlace por token `/rutinas/hoja/{token}`; Copiar enlace y WhatsApp desde ficha alumno; orden de series; modificar rutina con tres bloques (Detalles, Series en rutina, Añadir más). **ABM de categorías de rutinas implementado (Mar 2026):** entidad `Categoria`; categorías del sistema (FUERZA, CARDIO, FLEXIBILIDAD, FUNCIONAL, HIIT) + propias del profesor; lista en `/profesor/mis-categorias`; crear, editar, eliminar; selección en crear/editar rutina.
- **Alumnos:** Solo ficha (sin login). Estado ACTIVO/INACTIVO; filtros por nombre y estado. Al eliminar alumno se borran mediciones y rutinas asignadas. Tarjeta "Progreso del alumno" con historial de registros (crear, editar, eliminar). **Vista del alumno terminada (Mar 2026):** responsive móvil, modal progreso al tocar registro, modal confirmar eliminar progreso, botón Guardar notas, Eliminar usuario debajo de todo, barra inferior móvil, formato fecha dd/MM/yy. **Scroll móvil (Mar 2026):** tablas de lectura con `mgv-scroll-panel` / `mgv-scroll-embed` + scroll interno en ≤991px; progreso con `.progreso-scroll-mobile` (altura fija en móvil). Ver [GUIA_RESPONSIVE.md](GUIA_RESPONSIVE.md) §5.8.
- **Página pública:** Landing `/`, Planes `/planes`, consultas; administración en `/profesor/pagina-publica`. **Mar 2026:** config unificada en BD (redes, email, eslogan, mapa opcional), textos virtual-first — ver §1.1 bis.
- **Manual del usuario:** HTML en `/profesor/manual` (pie del panel, embebido en Administrar sistema); índice Mar 2026: acceso, panel, alumnos, ejercicios (incl. formatos imagen y 5 MB), grupos, series, rutinas, categorías, progreso, administración (usuarios + página pública + backups), resumen. Sin calendario/asistencias/pizarra (app 100 % virtual). Detalle de la última actualización: **§1.3**.

### 1.1 Página pública y administración — mejoras UX (Mar 2026)

- **Marca:** Texto visible unificado como **MiGymVirtual** (antes “MiGimVirtual”) en plantillas HTML, `spring.application.name`, mensajes de WhatsApp desde el modal de consulta, `pom.xml`, scripts y documentación. Paquete Java y nombre de BD (`com.migimvirtual`, `migimvirtual`) sin cambiar.
- **Modal consulta (móvil / admin página pública):** Tarjeta de teléfono abre **WhatsApp** (`wa.me` + heurística Argentina); estilos hover `.modal-consulta-wa-activo`. Texto prefijado del mensaje menciona MiGymVirtual.
- **Consultas — móvil:** Al abrir el modal de detalle, la consulta se **marca como vista automáticamente** (POST); se quitó el botón “Marcar como visto” del modal. En **escritorio** sigue el botón “Visto” en la tabla.
- **Consultas — escritorio (tabla):** Columna **Acciones** con **Visto** y **Eliminar** en **una sola fila** (flex + `publica-consultas-acciones` en `pagina-publica-admin.css`).
- **Formulario contacto en `/planes`:** Placeholder y textos de validación: **“Celular”** en lugar de “Teléfono” (`planes-publica.html`). El campo sigue enviándose como `telefono` en el backend.
- **Administración del sistema (móvil):** Menú desplegable de secciones: icono de **cuatro rayitas horizontales** a la derecha (en lugar del chevron/triángulo), indicando más opciones (`administracion.html`).
- **Planes (móvil) en admin página pública:** **Subir / Bajar** orden del plan con **flechas en la tarjeta** (columna derecha); mismo POST que escritorio. **Quitado** del modal de detalle del plan. Tap en la parte izquierda de la fila sigue abriendo el modal (detalle, Editar, Eliminar).
- **Estilos:** `pagina-publica-admin.css` (consultas, modal WhatsApp, planes móvil, acciones tabla).

### 1.1 bis Página pública — modelo gimnasio virtual (Mar 2026) — ✅ Implementado

La landing (`/`) y **Planes** (`/planes`) comparten la misma **configuración en BD** (`configuracion_pagina_publica`), cargada vía **`ConfiguracionPaginaPublicaService.rellenarModeloPaginaPublica(Model)`** desde `PortalControlador` (ya no hay WhatsApp/Instagram/dirección hardcodeados en `index-publica.html`).

| Tema | Detalle |
|------|---------|
| **Nuevas claves** | `url_mapa`, `tiktok`, `youtube`, `facebook`, `linkedin`, `twitter`, `email_contacto`, `eslogan` |
| **Opcional virtual** | Dirección y mapa vacíos si el servicio es 100 % online; el pie solo muestra ubicación si hay texto o URL de mapa |
| **Redes** | Cada ícono/enlace en contacto y pie solo aparece si hay valor guardado |
| **Celular / WhatsApp** | Un solo número en el admin (guardado en `telefono` y `whatsapp`); enlaces `wa.me` y botón flotante usan `getNumeroWhatsAppPublico()` (prioridad celular) |
| **Textos** | Carrusel y sección “Por qué…” en `index-publica.html` + lista “Servicios” en `planes-publica.html` orientados a entrenamiento online |
| **Defaults (filas nuevas)** | `asegurarConfigInicial`: dirección vacía, horarios tipo consulta online, eslogan sugerido; instalaciones existentes conservan valores hasta que el admin guarde |

**Planes de ejemplo (BD nueva):** `PlanPublicoService.asegurarPlanesIniciales()` crea cuatro planes **virtuales** (Esencial, Progreso, Intensivo, Premium online). Si ya hay filas en `plan_publico`, no se modifica nada — editar desde el admin.

**Admin:** `profesor/pagina-publica-admin.html` — bloque “Configuración de datos” ampliado. **Manual:** `/profesor/manual` §10.2 (página pública).

**Mantenimiento:** `PortalControlador` debe importar `java.util.List` (uso en `/status` con `List<Exercise>`).

### 1.2 Panel Administración — sectores visuales y CSS global (Mar 2026) — ✅ Cierre módulo

Para **diferenciar** bloques dentro de **Administrar sistema** (vista embebida con `?fragment=1` y página completa), se añadieron **marcos de color** y fondo suave, alineados con la paleta del proyecto:

| Sección | Contorno / criterio | Clases HTML (resumen) |
|---------|---------------------|------------------------|
| **Sistema de backups — contenido** (ZIP: ejercicios, rutinas plantilla, series, etc.) | Violeta `#7e57c2` (familia `#5e35b1`) | `.admin-backup-fragment .backup-sector-contenido` |
| **Sistema de backups — alumnos** (JSON) | Azul `#039be5` | `.admin-backup-fragment .backup-sector-alumnos` |
| **Usuarios del sistema — perfil** (“Mi usuario” / “Mi perfil”) | Mismo criterio violeta que backup contenido | `.admin-usuarios-sistema-root .usuarios-sistema-sector-cuenta` |
| **Usuarios del sistema — listado** (tabla, vista DEVELOPER) | Mismo criterio azul que backup alumnos | `.admin-usuarios-sistema-root .usuarios-sistema-sector-listado` |

**Técnico:** Los estilos viven en **`src/main/resources/static/style.css`** (no solo en `<style>` del `<head>` de cada plantilla), porque al cargar un **fragmento Thymeleaf** (`profesor/backup :: contenido`, `profesor/usuarios-sistema :: contenido`) el navegador **no recibe** el `<head>` de esa página: el shell es `administracion.html`, que ya enlaza `style.css`. Sin esto, los contornos no se aplicaban en el panel.

**Backend (backup):** En `ExerciseZipBackupService`, el borrado previo para restauración “suplantar” se encapsuló en un método privado (`ejecutarBorradoPrevioSuplantar`) para claridad transaccional y mejor compatibilidad con analizadores/lambdas.

**Referencia UX responsive:** [GUIA_RESPONSIVE.md](GUIA_RESPONSIVE.md) §5.7.

**Pendiente de producto (ver AYUDA_MEMORIA):** Refinar **contenidos** de la página pública (imágenes del carrusel `/img/publica/`, textos finos, FAQ opcional). La base técnica “virtual” y la configuración administrable ya están (§1.1 bis).
- **Backup (terminado / actualizado Mar 2026):** Guardado en servidor, restauración total, export acotado al profesor del panel. Ver sección 2 y despliegue Ubuntu en [servidor/DESPLIEGUE-SERVIDOR.md](servidor/DESPLIEGUE-SERVIDOR.md) (carpeta `backup`).
- **Depuración de datos:** Módulo eliminado en Mar 2026 (ya no existe en Administración).

### 1.3 Manual del usuario — últimos cambios (Mar 2026)

- **Audiencia:** texto orientado al **profesor** y a la **cuenta administradora** de uso habitual (despliegue con un solo usuario operativo); no se mencionan roles técnicos AYUDANTE/DEVELOPER en el manual.
- **Estructura:** **11 secciones** numeradas; eliminada la sección duplicada “Usuarios del sistema” como capítulo aparte — el tema queda en **§10.1** (dentro de Administración).
- **Ejercicios §4.1:** formatos admitidos por extensión **JPEG/JPG, PNG, GIF, WebP, BMP**; tamaño máximo **5 MB** por archivo (`ImagenServicio`). Recomendaciones en el manual: WebP (calidad/peso), GIF (animación), PNG (transparencia).
- **Referencias cruzadas en docs:** página pública **§10.2**; backups **§10.3** (actualizar textos antiguos que citaban §13.x).
- **Historial:** `CHANGELOG.md` [2026-03-14] (revisión 100 % virtual + repaso profesor + §4.1).

### 1.4 Datos de prueba en MySQL (scripts `scripts/BD/`) — Mar 2026

Para poblar **alumnos**, **progresos**, **series plantilla**, **rutinas plantilla** y **rutinas asignadas** en desarrollo o demos, usar los scripts SQL en orden: **`00`** (opcional, limpiar) → **`01`** → **`02`** → **`03`** → **`04`** → **`06`**. `05` es solo reparación si hay `serie_ejercicio` con `exercise_id` NULL.

| Contenido | Detalle |
|-----------|---------|
| Alumnos | 20 usuarios `test_alumno_1` … `@migimvirtual.test` |
| Series | 20 plantillas con ejercicios del catálogo |
| Rutinas | 12 plantillas (`token_publico` `test_rutina*`) |
| Asignaciones | Script **06** con procedimiento almacenado; tokens `test_asign_*` |
| Progresos | Varios registros por alumno (incluye volumen para probar scroll en ficha) |

**Fuente de verdad:** `scripts/BD/README.md` y `scripts/BD/LEEME_BD.txt`. **Índice general:** [INDICE_DOCUMENTACION.md](INDICE_DOCUMENTACION.md).

---

## 2. Backup y exportación

**Estado:** Terminado y revisado (marzo 2026). Acceso: **Administrar sistema → Sistema de backups** (`/profesor/backup`).

### 2.0 Comportamiento actual (Mar 2026)

| Aspecto | Detalle |
|---------|---------|
| **Almacenamiento** | Los archivos **no se descargan al navegador** por defecto: se guardan en disco bajo la raíz configurada (`migimvirtual.backups.dir`, por defecto `backup/`), con subcarpetas `contenido/` (ZIP) y `alumnos/` (JSON). |
| **Rotación** | Máximo **2** archivos por tipo; al guardar un tercero se elimina el más antiguo (por nombre con fecha). |
| **Restaurar** | **Reemplazo total** (“foto del día”): al restaurar contenido o alumnos se borran los datos actuales de ese ámbito y se cargan los del snapshot. |
| **Contenido (ZIP)** | Incluye catálogo global de **ejercicios** + imágenes, **grupos** y **categorías** (archivo `categorias.json`, manifest v1.2 con `profesorId`). **Rutinas plantilla y series** solo del **profesor del panel** (mismo criterio que «Mis rutinas» / «Mis series»), para no mezclar otros profesores ni inflar el conteo al restaurar. |
| **Alumnos (JSON)** | Datos personales, **mediciones** y **registros de progreso** (`RegistroProgreso`). Sin asistencias, sin rutinas asignadas ni otras asignaciones. Versión JSON `1.1`. |
| **Excel desde backup** | El botón “Exportar a Excel” en la pantalla de backup fue **eliminado**; el servicio `AlumnoExportService` puede seguir existiendo para otros usos si aplica. |

**Configuración:** `application.properties` → `migimvirtual.backups.dir=backup` (relativo al directorio de trabajo de la JVM al arrancar, o ruta absoluta recomendada en producción).

**Clases principales:** `BackupStorageService` (listar, guardar, leer, rotar), `AdminPanelController` (POST `guardar-contenido`, `guardar-alumnos`, `restaurar-contenido`, `restaurar-alumnos`), `ExerciseZipBackupService` (export/import ZIP, `exportarEjerciciosAZip(Long profesorId)`, `importarSnapshotCompletoDesdeZipBytes`), `AlumnoJsonBackupService`, `CategoriaService` (`ensureCategoriaExiste`, `eliminarCategoriasDelProfesor` en restore con `categorias.json`).

**Historial de cambios:** `CHANGELOG.md` — **[2026-03-22]** (cierre UX Administración, sectores de color, `style.css` + fragmentos); **[2026-03-21]** (backup en servidor, fix export por `profesorId`, manifest v1.2).

**Despliegue Ubuntu / VPS:** No hace falta crear la carpeta `backup` a mano antes del primer uso: la app crea `backup/contenido` y `backup/alumnos` al primer guardado. Recomendaciones de permisos y ruta absoluta: [servidor/DESPLIEGUE-SERVIDOR.md](servidor/DESPLIEGUE-SERVIDOR.md) (sección carpeta backup).

**Manual en la app:** `/profesor/manual` §10.3 Sistema de backups (texto alineado con este flujo).

**Interfaz (Mar 2026):** Marcos de color en la pantalla de backups (contenido vs alumnos). Ver §1.2.

---

## 2.1 Depuración de datos (eliminado Mar 2026)

El módulo "Depuración de datos" fue eliminado por completo en marzo 2026. No existe en el menú de Administración ni en la aplicación (servicio, controlador, plantilla y referencias eliminados).

### 2.2 Modales y avisos unificados (confirmaciones y alertas)

**Estado:** Completado (febrero 2026). En toda la app las confirmaciones y avisos usan modales con estilo MiGymVirtual (cabecera morada `.modal-confirmar-header`, pie `.modal-confirmar-footer` en `style.css`), reemplazando `alert()` y `confirm()` nativos del navegador.

**Vistas con modal de confirmación y/o alerta:**

| Vista | Confirmación | Alerta (éxito/error/info) |
|-------|--------------|---------------------------|
| Panel Administración (backup, usuarios-sistema, pagina-publica-admin) | Sí | Sí |
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

**Resumen:** App en VPS Donweb. Acceso SSH: `ssh -p 5638 root@149.50.144.53`. Aplicación en puerto 8080. Si PowerShell está bloqueado, usar Consola VNC de Donweb y menú `./MiGymVirtual` / `screen -r MiGymVirtual`. **Backups en disco:** ver §2 y en despliegue la sección **Carpeta `backup` (Ubuntu / producción)** — creación automática al primer guardado; opcional ruta absoluta y permisos. **Nginx:** el flujo de backup actual es por formularios en el panel (no subida masiva por proxy en muchos casos); si en el futuro se suben archivos grandes por HTTP, mantener `client_max_body_size` en Nginx; ejemplo en `servidor/nginx-detodoya.conf`.

**Detalle completo:** [servidor/DESPLIEGUE-SERVIDOR.md](servidor/DESPLIEGUE-SERVIDOR.md) (acceso SSH, Consola VNC, menú, Nginx, reinicio, uploads, carpeta backup).

---

## 4. Manual del usuario – Índice de secciones

El manual en la app (`/profesor/manual`) incluye:

1. Acceso al sistema  
2. Panel del profesor (tarjetas, pestañas, responsive)  
3. Alumnos (lista, formulario, ficha, últimas rutinas, notas, progreso)  
4. Ejercicios (predeterminados vs propios, permisos; §4.1 formatos de imagen y tamaño máx. 5 MB)  
5. Grupos musculares  
6. Series  
7. Rutinas (plantillas, categorías obligatorias, asignación, enlaces, nota/reseña)  
8. Categorías de rutinas (`/profesor/mis-categorias`)  
9. Progreso del alumno (modal fecha + grupos + observaciones)  
10. Administración del sistema (§10.1 usuarios, §10.2 página pública, §10.3 backups) — redactado para profesor/cuenta administradora; sin sección duplicada de “usuarios”  
11. Resumen rápido  

*(No incluye calendario, asistencias ni pizarra TV — eliminados; ver `ELIMINACION_CALENDARIO_Y_PIZARRA_MAR2026.md`.)*

---

## 5. Referencias técnicas (una línea)

| Tema | Resumen |
|------|--------|
| **Grupos musculares** | Entidad `GrupoMuscular`; sistema + por profesor; ABM en `/profesor/mis-grupos-musculares`; ejercicios con `@ManyToMany`. |
| **Categorías de rutinas** | Entidad `Categoria`; sistema (5) + por profesor; ABM en `/profesor/mis-categorias`; rutinas con `@ManyToMany`; selección en crear/editar rutina. |
| **Asistencia en calendario** | *(Eliminado Mar 2026.)* |
| **Pizarra / sala TV** | *(Eliminado Mar 2026.)* |
| **Página pública** | Fase 8. Landing `/`, Planes `/planes`, consultas; hero con video/carrusel; administración en panel. |
| **Ejercicios predeterminados** | `ExerciseCargaDefaultOptimizado.asegurarEjerciciosPredeterminados()`; imágenes en `uploads/ejercicios/` (1.webp–60.webp). |
| **Imágenes ejercicio (subida)** | `ImagenServicio`: máx. **5 MB**; extensiones `png`, `gif`, `bmp`, `webp`, `jpg`/`jpeg`; GIF/WebP sin re-encode para no perder animación (`ImageOptimizationService`). |
| **Backups en servidor** | `BackupStorageService` + `migimvirtual.backups.dir`; ZIP contenido (`ExerciseZipBackupService`, `profesorId` en export) y JSON alumnos; máx. 2 archivos/tipo; ver §2 y DESPLIEGUE-SERVIDOR (carpeta backup). |
| **Restricción AYUDANTE** | No puede acceder a "Administrar sistema"; redirección y mensaje si intenta entrar a `/profesor/administracion`. |
| **Eliminar alumno** | `UsuarioService.eliminarUsuario`: borra asistencias, mediciones, excepciones, rutinas asignadas; luego el usuario. |
| **Depuración de datos** | `DepuracionService`; panel en `/profesor/depuracion`; elimina asistencias o rutinas asignadas anteriores a una fecha elegida. |

---

*Última actualización: Marzo 2026 — §1.3 manual del usuario (profesor, §4.1 imágenes 5 MB, §10 administración unificada). §2 Backup. §1.1 página pública/admin. Depuración eliminada (§2.1).*
