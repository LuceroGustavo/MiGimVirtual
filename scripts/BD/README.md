# Scripts de datos de prueba - MiGimVirtual

Scripts SQL para cargar datos de prueba en la base de datos. **Ejecutar en el orden indicado.**

## Requisitos previos

- Base de datos `migimvirtual` creada y con tablas (la app debe haber arrancado al menos una vez).
- Ejercicios predeterminados cargados (60 ejercicios del sistema).
- Al menos un profesor existente (creado por DataInitializer: profesor@migymvirtual.com).

## Orden de ejecución

1. **01_usuarios_prueba.sql** – 10 alumnos de prueba
2. **02_progresos_prueba.sql** – Progresos asignados a los alumnos
3. **03_series_prueba.sql** – 10 series de ejercicios (usa ejercicios predeterminados)
4. **04_rutinas_prueba.sql** – 5 rutinas plantilla (usa las series creadas en paso 3)

**Opcional:** `00_limpiar_datos_prueba.sql` – elimina todos los datos de prueba antes de volver a cargar.

**Reparación:** `05_reparar_serie_ejercicios_nulos.sql` – elimina registros de serie_ejercicio con exercise_id NULL (si las series fallan al editar o la vista está vacía).

## Cómo ejecutar

```bash
# Desde la raíz del proyecto, con MySQL en localhost:
mysql -u root -p migimvirtual < scripts/BD/01_usuarios_prueba.sql
mysql -u root -p migimvirtual < scripts/BD/02_progresos_prueba.sql
mysql -u root -p migimvirtual < scripts/BD/03_series_prueba.sql
mysql -u root -p migimvirtual < scripts/BD/04_rutinas_prueba.sql
```

O desde MySQL Workbench / DBeaver: abrir cada archivo y ejecutarlo en orden.

## Datos creados

| Script | Cantidad | Descripción |
|--------|----------|-------------|
| 01 | 10 usuarios | Alumnos con todos los campos del formulario completos |
| 02 | Variable | 0–4 progresos por alumno (algunos sin progresos) |
| 03 | 10 series | Cada una con 3–4 ejercicios, reps/peso/tiempo variados |
| 04 | 5 rutinas | Plantillas que usan las series del script 03 |

## Notas

- Los alumnos de prueba usan correos `test_alumno_N@migimvirtual.test` (N = 1 a 10).
- Los alumnos no tienen contraseña (no inician sesión; se gestionan desde el panel del profesor).
