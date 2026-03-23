# Scripts SQL — datos de prueba (MiGymVirtual / migimvirtual)

Scripts para cargar **alumnos**, **progresos**, **series plantilla**, **rutinas plantilla** y **rutinas asignadas** en MySQL. Ver también **`LEEME_BD.txt`** (resumen en español).

## Requisitos

- Base de datos **`migimvirtual`** (nombre según `application.properties`) creada; tablas generadas (la app debe haber arrancado al menos una vez).
- **Ejercicios** en tabla `exercise` (carga automática al iniciar, p. ej. 60 predeterminados).
- Al menos un **profesor** (p. ej. `DataInitializer`).
- **Categorías** del sistema: FUERZA, CARDIO, FLEXIBILIDAD, etc. (inicialización al arrancar).

Sin ejercicios, el script `03` puede insertar filas inconsistentes en `serie_ejercicio`.

## Orden de ejecución

| Orden | Archivo | Descripción |
|------:|---------|-------------|
| 0 (opc.) | `00_limpiar_datos_prueba.sql` | Elimina datos de prueba (`test_rutina*`, series plantilla de prueba, `test_asign_*`, usuarios `test_alumno_*`, progresos asociados) |
| 1 | `01_usuarios_prueba.sql` | **20** alumnos (`test_alumno_1` … `test_alumno_20@migimvirtual.test`) |
| 2 | `02_progresos_prueba.sql` | Registros en `registro_progreso` (varios por alumno; volumen extra para probar UI) |
| 3 | `03_series_prueba.sql` | **20** series plantilla con ejercicios |
| 4 | `04_rutinas_prueba.sql` | **12** rutinas plantilla (`token_publico` `test_rutina*`) |
| 5 | `06_asignaciones_prueba.sql` | Procedimiento almacenado + **rutinas asignadas** a alumnos (`test_asign_*`) |
| — | `05_reparar_serie_ejercicios_nulos.sql` | **Solo reparación:** borra `serie_ejercicio` con `exercise_id` NULL |

**No** ejecutar `06` antes de `04`. Tras limpiar con `00`, volver a ejecutar `01` → `06`.

## Cómo ejecutar

**MySQL Workbench / DBeaver:** abrir cada archivo en orden y ejecutar (el `06` usa `DELIMITER` y procedimiento; ejecutar el archivo completo).

**CLI:**

```bash
mysql -u root -p migimvirtual < scripts/BD/01_usuarios_prueba.sql
# ... idem 02, 03, 04, 06
```

## Resumen de datos

| Script | Cantidad / notas |
|--------|------------------|
| 01 | 20 usuarios alumno de prueba |
| 02 | Varios `registro_progreso` por alumno |
| 03 | 20 series plantilla (`serie` + `serie_ejercicio`) |
| 04 | 12 rutinas plantilla + categorías + series copiadas en cada rutina |
| 06 | ~18 rutinas **asignadas** (copias con `usuario_id`, token `test_asign_%`) |

Tokens de prueba:

- Plantillas: `test_rutina1_…` … `test_rutina12_…`
- Asignadas: `test_asign_…`

---

*Actualizado Mar 2026 — alineado con scripts ampliados y `06_asignaciones_prueba.sql`.*
