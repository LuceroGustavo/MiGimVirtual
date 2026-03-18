-- =============================================================================
-- Script 3: 10 series de ejercicios (plantillas)
-- Ejecutar después de tener ejercicios predeterminados (60) cargados.
-- Cada serie tiene 3 o 4 ejercicios con reps, peso o tiempo variados.
-- IMPORTANTE: Requiere que existan ejercicios en la tabla 'exercise'.
-- =============================================================================

USE migimvirtual;

-- Validación: debe haber ejercicios cargados (aborta si la tabla está vacía)
SET @_exercise_count = (SELECT COUNT(*) FROM exercise);
-- Si @_exercise_count = 0, el script fallará al insertar (exercise_id NULL). Ejecute primero la carga de ejercicios desde la app.

SET @profesor_id = (SELECT id FROM profesor LIMIT 1);

-- Variables con IDs de ejercicios (predeterminados: profesor_id IS NULL)
-- Fallback: si no hay predeterminados, usa cualquier ejercicio disponible
SET @e1  = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 0),  (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 0));
SET @e2  = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 1),  (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 1));
SET @e3  = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 2),  (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 2));
SET @e4  = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 3),  (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 3));
SET @e5  = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 4),  (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 4));
SET @e6  = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 5),  (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 5));
SET @e7  = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 6),  (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 6));
SET @e8  = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 7),  (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 7));
SET @e9  = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 8),  (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 8));
SET @e10 = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 9),  (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 9));
SET @e11 = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 10), (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 10));
SET @e12 = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 11), (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 11));
SET @e13 = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 12), (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 12));
SET @e14 = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 13), (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 13));
SET @e15 = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 14), (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 14));
SET @e16 = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 15), (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 15));
SET @e17 = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 16), (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 16));
SET @e18 = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 17), (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 17));
SET @e19 = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 18), (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 18));
SET @e20 = COALESCE((SELECT id FROM exercise WHERE profesor_id IS NULL ORDER BY id LIMIT 1 OFFSET 19), (SELECT id FROM exercise ORDER BY id LIMIT 1 OFFSET 19));

-- Serie 1: Pecho y tríceps (3 ejercicios)
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, rutina_id, profesor_id)
VALUES ('Pecho y Tríceps - Básico', 0, 'Rutina de empuje para principiantes.', 1, 'ADMIN', 2, NULL, @profesor_id);
SET @s1 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden) VALUES
(@s1, @e1, 12, 'reps', 10, 0), (@s1, @e2, 10, 'reps', 15, 1), (@s1, @e3, 15, 'reps', 5, 2);

-- Serie 2: Piernas (4 ejercicios)
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, rutina_id, profesor_id)
VALUES ('Piernas - Fuerza', 0, 'Enfocado en cuádriceps y glúteos.', 1, 'ADMIN', 1, NULL, @profesor_id);
SET @s2 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden) VALUES
(@s2, @e4, 10, 'reps', 20, 0), (@s2, @e5, 12, 'reps', 15, 1), (@s2, @e6, 15, 'reps', 10, 2), (@s2, @e7, 20, 'reps', NULL, 3);

-- Serie 3: Espalda y bíceps (3 ejercicios)
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, rutina_id, profesor_id)
VALUES ('Espalda y Bíceps', 0, 'Tirón y curl.', 1, 'ADMIN', 2, NULL, @profesor_id);
SET @s3 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden) VALUES
(@s3, @e8, 8, 'reps', 25, 0), (@s3, @e9, 12, 'reps', 12, 1), (@s3, @e10, 45, 'seg', NULL, 2);

-- Serie 4: Full body (4 ejercicios)
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, rutina_id, profesor_id)
VALUES ('Full Body - Circuito', 0, 'Circuito corto para días ocupados.', 1, 'ADMIN', 3, NULL, @profesor_id);
SET @s4 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden) VALUES
(@s4, @e11, 30, 'seg', NULL, 0), (@s4, @e12, 12, 'reps', 8, 1), (@s4, @e13, 10, 'reps', 20, 2), (@s4, @e14, 15, 'reps', 5, 3);

-- Serie 5: Hombros (3 ejercicios)
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, rutina_id, profesor_id)
VALUES ('Hombros - Desarrollo', 0, 'Press y elevaciones.', 1, 'ADMIN', 1, NULL, @profesor_id);
SET @s5 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden) VALUES
(@s5, @e15, 10, 'reps', 12, 0), (@s5, @e16, 12, 'reps', 6, 1), (@s5, @e17, 15, 'reps', 4, 2);

-- Serie 6: Cardio y core (4 ejercicios)
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, rutina_id, profesor_id)
VALUES ('Cardio + Core', 0, 'Mezcla de cardio y abdominales.', 1, 'ADMIN', 2, NULL, @profesor_id);
SET @s6 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden) VALUES
(@s6, @e18, 5, 'min', NULL, 0), (@s6, @e19, 45, 'seg', NULL, 1), (@s6, @e20, 20, 'reps', NULL, 2), (@s6, @e1, 30, 'seg', NULL, 3);

-- Serie 7: Upper body (3 ejercicios)
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, rutina_id, profesor_id)
VALUES ('Upper Body - Empuje', 0, 'Pecho, hombros y tríceps.', 1, 'ADMIN', 1, NULL, @profesor_id);
SET @s7 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden) VALUES
(@s7, @e2, 8, 'reps', 20, 0), (@s7, @e3, 10, 'reps', 10, 1), (@s7, @e4, 12, 'reps', 8, 2);

-- Serie 8: Lower body (4 ejercicios)
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, rutina_id, profesor_id)
VALUES ('Lower Body - Glúteos', 0, 'Enfoque en glúteos y piernas.', 1, 'ADMIN', 2, NULL, @profesor_id);
SET @s8 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden) VALUES
(@s8, @e5, 12, 'reps', 15, 0), (@s8, @e6, 15, 'reps', 12, 1), (@s8, @e7, 20, 'reps', NULL, 2), (@s8, @e8, 10, 'reps', 25, 3);

-- Serie 9: Elongación (3 ejercicios)
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, rutina_id, profesor_id)
VALUES ('Elongación y Movilidad', 0, 'Estiramientos y movilidad articular.', 1, 'ADMIN', 1, NULL, @profesor_id);
SET @s9 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden) VALUES
(@s9, @e9, 60, 'seg', NULL, 0), (@s9, @e10, 45, 'seg', NULL, 1), (@s9, @e11, 90, 'seg', NULL, 2);

-- Serie 10: Mixto intenso (4 ejercicios)
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, rutina_id, profesor_id)
VALUES ('Mixto - Alta Intensidad', 0, 'Combinación de fuerza y resistencia.', 1, 'ADMIN', 2, NULL, @profesor_id);
SET @s10 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden) VALUES
(@s10, @e12, 10, 'reps', 18, 0), (@s10, @e13, 12, 'reps', 15, 1), (@s10, @e14, 3, 'min', NULL, 2), (@s10, @e15, 15, 'reps', 8, 3);

SELECT CONCAT('Insertadas ', COUNT(*), ' series plantilla.') AS resultado FROM serie WHERE es_plantilla = 1 AND rutina_id IS NULL AND profesor_id = @profesor_id;
