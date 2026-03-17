# Mejoras: Crear Nueva Rutina y flujo de series

Documentación de las mejoras realizadas en la pantalla **Crear Nueva Rutina** y en la navegación entre crear rutina, ver serie y editar serie.

---

## 1. Responsive y layout móvil

- **Viewport** añadido en `crearRutina.html` para correcto escalado en móvil.
- **Columnas:** en pantallas pequeñas el contenido se apila; la sección "Seleccionar Series" aparece primero (`order-1` en móvil) y "Información de la Rutina" después (`order-2`).
- **Márgenes:** en móvil se redujeron márgenes y padding del contenedor (alineado con la página Crear Serie):
  - Clase `crear-rutina-container` con `padding-left/right: 0.3rem` en viewports &lt; 768px.
  - `.main-container` con `margin: 4px 0` y `padding: 1rem 0.3rem` en móvil.
- **Tabla de series:** en pantallas muy pequeñas se reduce el tamaño de fuente y se controla el desbordamiento de la columna Descripción.

---

## 2. Vista de series: de tarjetas a tabla

- **Antes:** Las series se mostraban en tarjetas grandes (nombre, descripción, ejercicios, Ver serie, Modificar, Vueltas). Con muchas series (ej. 50) era poco práctico.
- **Ahora:**
  - **Tabla** con columnas: Agregar (+) | Nombre | N° Ej. | Descripción (oculta en móvil) | Acción (solo icono “Ver detalle”).
  - **Buscador** “Buscar por nombre” que filtra las filas en tiempo real.
  - **Selector en la fila:** botón “+” para agregar la serie a la rutina sin abrir el modal.
  - En **móvil** la columna Descripción se oculta (clase `col-descripcion-movil`); la descripción se muestra en el modal de detalle.

---

## 3. Modal “Ver detalle” de serie

- Al hacer clic en el icono de ojo se abre un **modal** con una tarjeta de la serie.
- **Contenido de la tarjeta:**
  - Nombre, **Descripción** (con etiqueta “Descripción:”), lista de ejercicios (badges).
  - Sin campo “Vueltas” en el modal; las vueltas solo se editan en la lista “Series seleccionadas”.
- **Estilo:** Fondo y acentos en tonos pastel violeta (#ede7f6, #7e57c2, #b39ddb) coherentes con el resto de la app.
- **Botones:** Ver serie | Modificar | Agregar (a la rutina), en una sola fila; en móvil se mantienen alineados y sin ocupar todo el ancho.
- **Cierre:** Solo con la “X” en la esquina superior derecha (sin botón “Cerrar” en el pie).

---

## 4. Series seleccionadas (orden en la rutina)

- Lista ordenada con: nombre de la serie, **Vueltas**, Subir/Bajar y **Eliminar** (icono papelera).
- **Nombre largo:** El nombre tiene ancho flexible con `text-truncate` y `min-width: 0`; los controles (Vueltas, Subir, Bajar, Eliminar) tienen `flex-shrink: 0` para que no bajen de línea y el icono de eliminar quede siempre en la misma fila.
- **Leyenda en móvil:** En pantallas pequeñas solo se muestra el texto corto “Seleccionar series”; en escritorio se muestra la leyenda completa con instrucciones.

---

## 5. Resaltado de filas seleccionadas

- Cada vez que una serie se agrega o se quita de la lista “Series seleccionadas”, se actualiza el estado visual de la **tabla** de series disponibles.
- Las filas de las series que están en la lista se marcan con la clase `fila-serie-seleccionada` (fondo gris).
- Al eliminar una serie de la selección, su fila deja de estar resaltada.
- La sincronización se hace por **ID** en string para evitar discrepancias entre Thymeleaf y JavaScript.

---

## 6. Navegación: volver a Crear Rutina

### Editar serie desde Crear Rutina

- El enlace “Modificar serie” del modal de detalle apunta a  
  `/series/editar/{id}?volver=/rutinas/crear`.
- El controlador **SerieController** (`GET /editar/{id}`) recibe el parámetro `volver` y lo expone en el modelo como `volverUrl`.
- La vista **crearSerie.html** incluye un `<input type="hidden" id="volverUrl">` con ese valor cuando existe.
- Tras guardar con éxito en modo edición, el script redirige a `volverUrl` si está definido; en caso contrario, a `/profesor/dashboard?tab=series`.
- **Resultado:** Si el profesor entró a editar desde Crear Rutina, al guardar vuelve a **Crear Rutina**.

### Ver serie desde Crear Rutina

- El enlace “Ver serie” del modal apunta a  
  `/series/ver/{id}?volver=/rutinas/crear`.
- El controlador **SerieController** (`GET /ver/{id}`) recibe el parámetro `volver` y lo pasa al modelo.
- En **verSerie.html**:
  - Si existe `volver`: el botón de volver muestra el texto **“Volver”** y el `href` es el valor de `volver` (p. ej. `/rutinas/crear`).
  - Si no existe `volver`: el botón muestra **“Volver al panel”** y enlaza al dashboard del profesor.
- **Resultado:** Si el usuario abrió “Ver serie” desde Crear Rutina, puede volver con un solo clic a **Crear Rutina**.

---

## Archivos modificados

| Archivo | Cambios principales |
|---------|----------------------|
| `templates/rutinas/crearRutina.html` | Responsive, tabla de series, modal detalle, lista con eliminar, estilos nombre largo, leyenda móvil, enlaces con `?volver=/rutinas/crear`, `syncSelectedRows()` con IDs en string |
| `templates/series/crearSerie.html` | Input oculto `volverUrl`, redirección tras guardar según `volverUrl` |
| `templates/series/verSerie.html` | Enlace “Volver” / “Volver al panel” condicional según parámetro `volver` |
| `controladores/SerieController.java` | Parámetro `volver` en `GET /editar/{id}` y `GET /ver/{id}`, atributos `volverUrl` y `volver` en el modelo |

---

## Notas técnicas

- El **POST** a `/rutinas/crear-plantilla` no cambió: se siguen enviando `selectedSeries` y `repeticiones_{id}`.
- La tabla de series en crear rutina usa `data-serie-id`, `data-serie-nombre`, `data-serie-descripcion`, `data-serie-reps` y `data-ejercicios-nombres` (nombres separados por `|`) para el modal y para la sincronización de filas seleccionadas.
