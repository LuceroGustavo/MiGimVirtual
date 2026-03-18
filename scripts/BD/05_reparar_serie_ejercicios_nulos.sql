-- =============================================================================
-- Script 5 (reparación): Eliminar serie_ejercicio con exercise_id NULL
-- Ejecutar si las series tienen ejercicios "rotos" (exercise_id NULL) que causan
-- error al editar o vista vacía. Esto elimina solo esos registros huérfanos.
-- Las series quedarán con menos ejercicios; si quedan en 0, podés eliminarlas.
-- =============================================================================

USE migimvirtual;

SET SQL_SAFE_UPDATES = 0;

-- Eliminar serie_ejercicio donde exercise_id es NULL
DELETE FROM serie_ejercicio WHERE exercise_id IS NULL;

SET SQL_SAFE_UPDATES = 1;

SELECT CONCAT('Eliminados ', ROW_COUNT(), ' registros de serie_ejercicio con exercise_id NULL.') AS resultado;
