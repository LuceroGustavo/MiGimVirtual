# Guía: diseño responsivo en MiGimVirtual

Resumen para entender **cómo funciona** el diseño responsivo, **qué consume recursos** y **qué se recomienda** en este proyecto.

---

## 1. ¿Vista de escritorio “igual” y en móvil “distinta”? ¿Consume más?

**Respuesta corta: no consume más.** Se usa **una sola página (un solo HTML)** y **un solo CSS** que cambia según el ancho de pantalla. El navegador carga la misma cantidad de recursos (una solicitud, un DOM). Lo que cambia es **qué reglas CSS se aplican** en cada tamaño, no “otra versión” del sitio.

- **No** son dos sitios (uno para desktop y otro para móvil).
- **Sí** es el mismo HTML con clases y media queries que hacen que el layout se adapte (columnas que pasan a una sola, menús que se pliegan, tablas que se desplazan, etc.).

Por eso: **misma vista en escritorio que ya tenés + comportamiento adaptado en móvil = mismo consumo**. No hay “doble versión” ni servidor extra.

---

## 2. ¿Qué se recomienda: mobile-first o desktop-first?

Las dos formas son válidas:

| Enfoque | Idea | Cuándo suele usarse |
|--------|------|----------------------|
| **Desktop-first** | Diseñás para pantalla grande y con `@media (max-width: ...)` adaptás para pantallas chicas. | Cuando la versión principal es la de escritorio (como en tu caso). |
| **Mobile-first** | Diseñás primero para móvil y con `@media (min-width: ...)` agregás espacio/columnas para pantallas grandes. | Cuando la prioridad es móvil o querés forzar simplicidad en móvil. |

**Para MiGimVirtual:** tiene sentido **mantener desktop como referencia** (como ahora) y **ir agregando reglas para pantallas chicas**. No hace falta reescribir todo en mobile-first. Si en móvil algo se ve mal, se ajusta con media queries o clases de Bootstrap; el escritorio puede quedar igual.

**Sobre “si está pensado para celular, ¿no sería malo para escritorio?”:** No. “Pensado para móvil” solo significa que el diseño base funciona en pantalla chica. En pantallas grandes ese mismo diseño se **expande** (más columnas, más espacio). No implica que escritorio quede peor; con buenos breakpoints ambos quedan bien.

---

## 3. Tecnologías que te ayudan (y que ya usás)

- **Bootstrap 5**  
  Ya lo usás. Trae:
  - **Grid:** `container`, `row`, `col-*`, `col-sm-*`, `col-md-*`, `col-lg-*`, `col-xl-*`. En móvil las columnas se apilan; a partir de cierto ancho se ponen en fila.
  - **Breakpoints:**  
    `sm` 576px, `md` 768px, `lg` 992px, `xl` 1200px, `xxl` 1400px.
  - **Utilidades:**  
    `d-none d-md-block` (oculto en móvil, visible desde tablet),  
    `d-md-none` (visible solo en móvil),  
    `w-100` en imágenes,  
    `table-responsive` para tablas con scroll horizontal en móvil.

- **Viewport**  
  En las plantillas ya tenés algo como:  
  `<meta name="viewport" content="width=device-width, initial-scale=1.0">`  
  Eso es necesario para que el móvil no “zoom out” y el responsive funcione bien.

- **CSS: Flexbox y Grid**  
  Bootstrap ya los usa por dentro. Si en algún bloque necesitás alinear o repartir espacio, podés usar `d-flex`, `flex-wrap`, o en casos puntuales CSS Grid. No hace falta otra tecnología nueva.

- **Media queries**  
  En Bootstrap ya vienen en el framework. Si necesitás algo muy concreto en `style.css` o en un `<style>` de una plantilla, podés usar por ejemplo:
  - `@media (max-width: 767px) { ... }` → solo pantallas menores a 768px (móvil/tablet chica).
  - `@media (min-width: 992px) { ... }` → solo escritorio.

No hace falta cambiar de stack: **Bootstrap + media queries + mismo HTML** es suficiente y está pensado para esto.

---

## 4. Buenas prácticas rápidas

1. **Contenedores:** Usar `container` o `container-fluid` y dentro `row` + `col-*` con breakpoints (`col-12 col-md-6 col-lg-4`, etc.) para que en móvil sea una columna y en desktop varias.
2. **Tablas:** Envolver en `<div class="table-responsive">` para que en móvil hagan scroll horizontal y no rompan el layout.
3. **Botones y enlaces:** En móvil conviene que los toques sean cómodos (área mínima ~44px de alto); a veces `btn-sm` en desktop y `btn` en móvil, o padding extra en pantallas chicas.
4. **Menús / navegación:** En desktop pueden ser una barra horizontal; en móvil un menú colapsable (hamburguesa) o lista vertical. Mismo HTML, distintas clases según breakpoint.
5. **Evitar anchos fijos en px** en elementos que deban adaptarse (mejor `max-width`, `%`, o clases de Bootstrap).

---

## 5. Panel del profesor – mejoras realizadas (Mar 2026)

Responsividad del panel del profesor terminada para móvil (breakpoint 991px / 992px). Escritorio sin cambios.

| Área | En móvil (≤ 991px) |
|------|---------------------|
| **Login** | Viewport, padding y tamaños reducidos; botón táctil. |
| **Navbar** | Una fila: logo + MiGimVirtual (link al panel), sobre, nombre, Salir. Sin Volver, Ir a mi Panel, correo ni avatar. Logo lleva a `/profesor/dashboard` si es profesor. |
| **Dashboard** | 6 tarjetas como pantalla principal; al tocar Alumnos/Series/Rutinas/Asignaciones se abre la pestaña. Sin "Volver al inicio" (logo = volver). Contenido de pestañas con mismo ancho que tarjetas (480px). Tarjetas más altas (150px), iconos y texto un poco más grandes. |
| **Tabla Mis Alumnos** | Columna "Acción"; celular = icono + popover (solo móvil); Ver = solo ícono ojo; Asignar rutina oculto (se hace desde detalle del alumno). |
| **Footer** | Una fila: MiGimVirtual + lucerogustavosi. Sin año. Compacto. |

**Archivos:** login.html, fragments/navbar.html, fragments/footer.html, footer.css, profesor/dashboard.html. Detalle en CHANGELOG [2026-03-15] feat(ui): responsividad del panel del profesor.

---

## 5.1 Ficha del alumno – mejoras móvil (Mar 2026)

| Área | En móvil (≤ 991px) |
|------|---------------------|
| **Contenedor** | Márgenes y padding reducidos; container-fluid con px-2 en móvil. |
| **Título y estado** | hero-title 1.5rem; hero-subtitle 0.95rem; btn-estado-ficha más compacto. |
| **Header (Volver/Eliminar)** | Botones a ancho completo, apilados verticalmente. |
| **Tarjetas (stats-grid)** | 1 columna; stat-item con padding reducido; stat-number 1.5rem. |
| **Bloque alumno-info** | Columnas apiladas; padding 1rem. |
| **Tabla rutinas** | **Entorno 2:** Columnas Fecha, Nombre, Categorías (truncadas), Estado. Fila clickeable abre modal con tarjeta de datos y acciones (Modificar, Ver, Copiar, Pausar/Activar, WhatsApp). Escritorio: tabla completa con botones. |
| **≤ 575px** | Contenedor más compacto; título 1.35rem; botones de sección rutinas a ancho completo. |

**Archivo:** profesor/alumno-detalle.html. Ver CHANGELOG [2026-03-19].

---

## 5.2 Módulo de Series – vista responsive (Mar 2026) – ✅ Completado

> **Nota:** La vista responsive del módulo de series está completada. Opcional: revisar vista editar serie si difiere de crear.

### Vista detalle de serie (`series/verSerie.html`)

| Área | En móvil (≤ 991px) |
|------|---------------------|
| **Grid ejercicios** | 1 columna (apilados); tablet 2 cols; desktop 3 cols. |
| **Contenedor** | Sin min-width; padding reducido. |
| **Título y tarjetas** | Fuente y alturas reducidas. |
| **Flecha volver** | Botón "Volver al panel" visible solo en móvil, enlaza a `/profesor/dashboard`. |
| **Footer** | Incluido (footer.css) para consistencia con el panel. |
| **Badge vueltas / ejercicios** | "Vueltas" en verde (#7ee787); "ejercicios" en naranja. |

### Panel del profesor – pestaña Series (`profesor/dashboard.html`)

| Comportamiento | Descripción |
|----------------|-------------|
| **Modal al tocar serie** | En móvil, al tocar una fila se abre modal con Ver, Editar, Eliminar. |
| **Botón Ver** | En móvil: cierra el modal y navega en la misma pestaña. En escritorio: abre en pestaña nueva (`target="_blank"`). |

### Formulario crear/modificar serie (`series/crearSerie.html`)

| Área | En móvil (≤ 991px) |
|------|---------------------|
| **Título** | Fuente reducida a 1.35rem (antes 2.1rem). |
| **Filtros ejercicios** | Búsqueda + selector + botón **Limpiar** en la misma fila (5+4+3 columnas). |
| **Orden de paneles** | 1) Ejercicios disponibles → 2) Ejercicios en esta serie (tabla) → 3) Nombre, descripción, vueltas y Guardar. |
| **Tabla ejercicios** | Envuelta en `table-responsive` para scroll horizontal. |

**Archivos:** series/verSerie.html, series/crearSerie.html, profesor/dashboard.html.

### Completado

- Vista responsive de detalle de serie, dashboard (pestaña Series) y formulario crear/modificar serie. Badge de vueltas en verde en la cabecera de la serie.
- Opcional: vista **editar serie** (si difiere de crear) y otros flujos.

---

## 5.3 Módulo de Rutinas – vista responsive (Mar 2026) – ✅ Completado

> **Nota:** Panel Rutinas (escritorio con Acciones, móvil con modal), formulario modificar rutina alineado con crear rutina, y hoja de rutina responsive con header en dos filas.

### Panel del profesor – pestaña Rutinas (`profesor/dashboard.html`)

| Contexto | Comportamiento |
|----------|----------------|
| **Escritorio (≥992px)** | Columna **Acciones** con Ver, Editar, Eliminar; no se abre modal al hacer clic en la fila. |
| **Móvil (<992px)** | Sin columna Acciones; al tocar la fila se abre modal con Ver, Editar, Eliminar. |
| **URL al cambiar pestaña** | Se actualiza con `?tab=rutinas` (u otra) para que el botón Atrás vuelva a la pestaña correcta. |

### Formulario modificar rutina (`rutinas/editarRutina.html`)

- Mismo layout que **Crear rutina**: columna info rutina (izq), series en la rutina + tabla disponibles (der), barra inferior móvil, responsive.
- Enlace **“Volver al panel (Rutinas)”** a `/profesor/dashboard?tab=rutinas`.

### Hoja de rutina (`rutinas/verRutina.html`)

| Área | En móvil |
|------|----------|
| **Vista profesor y enlace compartido** | Misma plantilla; `esVistaEscritorio = false` para vista profesor, grid 1 columna. |
| **Grid de ejercicios** | 1 columna (≤767px); nombres hasta 2 líneas; sin overflow horizontal. |
| **Header** | Fila 1: logo + MiGimVirtual \| fecha. Fila 2: nombre de la rutina a ancho completo, centrado, con word-wrap para nombres largos. |

**Archivos:** profesor/dashboard.html, rutinas/editarRutina.html, rutinas/verRutina.html, ProfesorController.java.

---

## 5.4 Asignar rutina – vista responsive (Mar 2026) – ✅ Completado

> **Nota:** Refactor completo: tabla con búsqueda, modal de detalle, diferenciación por entorno (escritorio vs móvil).

### Vista asignar rutina (`profesor/asignar-rutina.html`)

| Área | Descripción |
|------|-------------|
| **Tabla general** | Reemplaza grid de tarjetas; columnas: Seleccionar, Nombre, Series, Categoría, Acción. Búsqueda por nombre. |
| **Modal detalle** | Ver, Modificar, Seleccionar. Enlace "Ver" apunta a `/profesor/rutinas/ver/{id}` (vista privada, no hoja pública). |
| **Deselección** | Cruz (×) junto al badge "X seleccionada(s)" para quitar la selección. |
| **Navegación** | Botón "Volver al detalle del alumno" (no al dashboard). |
| **Notas** | Módulo siempre visible entre alumno y selección de rutinas. |

### Entorno 2 (móvil ≤991px)

| Área | Comportamiento |
|------|----------------|
| **Columnas tabla** | Solo Nombre, Categoría, Series (Seleccionar y Acción ocultas). |
| **Abrir modal** | Click en la fila completa para abrir el modal (como en series). |
| **Cerrar y scroll** | Al seleccionar rutina y cerrar el modal, scroll automático al botón asignar. |
| **Lista colapsa** | Al seleccionar una rutina, la tabla y búsqueda se ocultan; resumen breve visible. Cruz (×) para deseleccionar y volver a la lista. |

### Archivos
profesor/asignar-rutina.html, ProfesorController.java.

### Entornos
- **Entorno 1 (escritorio):** Tabla completa con botones + y ojo; botón deseleccionar visible.
- **Entorno 2 (móvil):** Tabla simplificada; fila clickeable; barra inferior móvil.

---

## 5.5 Pestaña Asignaciones – vista responsive (Mar 2026) – ✅ Completado

> **Nota:** Vistas y responsividad del módulo de asignaciones (Rutinas Asignadas) en el panel del profesor finalizadas. Filtro alineado con Series/Rutinas; móvil con modal al tocar fila.

### Panel del profesor – pestaña Asignaciones (`profesor/dashboard.html`)

| Contexto | Comportamiento |
|----------|----------------|
| **Filtro (ambos entornos)** | Card debajo del título, misma estructura que Series/Rutinas. Input "Filtrar por alumno..." y botón "Limpiar" en la misma fila. Botón Limpiar con tono claro (#c8e6c9) coherente con el tema Alumnos. |
| **Escritorio (≥992px)** | Tabla completa: Usuario, Rutina, Estado, Fecha, Acciones (Ver, Copiar enlace, Editar, Pausar/Activar, Eliminar). |
| **Móvil (<992px)** | Columna **Acciones** oculta. Al tocar una fila se abre modal con detalle de la rutina asignada. |
| **Modal móvil** | Tarjeta con Alumno, Fecha, Rutina, Categorías, Series, Estado, Reseña. Acciones: Modificar, Ver, Copiar enlace, Pausar/Activar, Eliminar (sin WhatsApp, a diferencia del detalle del alumno). |

### Archivos
profesor/dashboard.html.

### Completado
- Filtro por alumno con card y botón Limpiar (estructura igual a Series).
- Entorno 2: columna Acciones oculta; fila clickeable abre modal con detalle y acciones.
- Consistencia de colores en botón Limpiar (verde claro en Alumnos, violeta en Series, etc.).

---

## 5.6 Módulo de Ejercicios – vista Mis Ejercicios responsive (Mar 2026) – ✅ Completado

> **Nota:** La vista **Mis Ejercicios** (`/profesor/mis-ejercicios`) está terminada. Pendiente responsive: crear ejercicio, modificar ejercicio, crear grupos musculares, modificar grupos musculares.

### Vista Mis Ejercicios (`profesor/ejercicios-lista.html`)

| Área | Descripción |
|------|-------------|
| **Tarjetas (escritorio)** | Dos tarjetas del mismo tamaño (260px): "Total disponibles" (naranja) con cantidad de ejercicios; "Grupos musculares" (verde) con cantidad de grupos. Botón + en esquina superior derecha (como dashboard Alumnos/Series/Rutinas). Contenido centrado: icono, número, etiqueta. |
| **Tarjetas (móvil ≤991px)** | Cuadradas, una al lado de la otra; gap reducido; iconos y fuentes escalados. |
| **Filtros** | Card con búsqueda, selector de grupo y botón Limpiar (naranja pastel). |
| **Tabla** | Entorno 2: columnas #, Tipo, Imagen, Acciones ocultas; fila clickeable abre modal con detalle, imagen y botones Editar/Eliminar. |
| **Barra inferior móvil** | Inicio, Alumnos, Rutinas, Más. |

### Pendiente (responsive)

- **Crear ejercicio** (`/profesor/mis-ejercicios/nuevo`)
- **Modificar ejercicio** (`/profesor/mis-ejercicios/editar/{id}`)
- **Crear grupos musculares** (`/profesor/mis-grupos-musculares`)
- **Modificar grupos musculares**

### Archivos

profesor/ejercicios-lista.html.

---

## 6. Orden sugerido para otras vistas (opcional)

1. Revisar que **todas** las plantillas del panel tengan la meta **viewport** (ya está en varias).
2. **Tablas restantes:** series, rutinas y asignaciones ya tienen `table-responsive` y comportamiento móvil (modal al tocar fila). Opcional: vista en cards en otras tablas.
3. **Formularios:** que los campos no queden con ancho fijo; usar `col-12 col-md-*` en móvil.
---

*Documento de referencia para responsividad. Panel del profesor completado Mar 2026. Módulo de series completado Mar 2026. Módulo de rutinas (panel, editar, hoja, asignar) completado Mar 2026. Pestaña Asignaciones completada Mar 2026. Vista Mis Ejercicios completada Mar 2026. Pendiente: responsive de crear ejercicio, modificar ejercicio, crear grupos musculares, modificar grupos musculares.*
