-- =============================================================================
-- Script 4: 5 rutinas plantilla (basadas en las series del script 03)
-- Ejecutar después de 03_series_prueba.sql
-- Cada rutina incluye 2 o 3 series de las creadas en el script anterior.
-- =============================================================================

USE migimvirtual;

SET @profesor_id = (SELECT id FROM profesor LIMIT 1);

-- IDs de categorías del sistema (creadas por DataInitializer al arrancar la app)
SET @cat_fuerza = (SELECT id FROM categoria WHERE nombre = 'FUERZA' AND profesor_id IS NULL LIMIT 1);
SET @cat_cardio = (SELECT id FROM categoria WHERE nombre = 'CARDIO' AND profesor_id IS NULL LIMIT 1);
SET @cat_flexibilidad = (SELECT id FROM categoria WHERE nombre = 'FLEXIBILIDAD' AND profesor_id IS NULL LIMIT 1);

-- Obtener IDs de las series plantilla creadas en script 03 (por nombre para no depender de IDs fijos)
SET @serie_pecho = (SELECT id FROM serie WHERE nombre = 'Pecho y Tríceps - Básico' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_piernas = (SELECT id FROM serie WHERE nombre = 'Piernas - Fuerza' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_espalda = (SELECT id FROM serie WHERE nombre = 'Espalda y Bíceps' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_full = (SELECT id FROM serie WHERE nombre = 'Full Body - Circuito' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_hombros = (SELECT id FROM serie WHERE nombre = 'Hombros - Desarrollo' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_cardio = (SELECT id FROM serie WHERE nombre = 'Cardio + Core' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_upper = (SELECT id FROM serie WHERE nombre = 'Upper Body - Empuje' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_lower = (SELECT id FROM serie WHERE nombre = 'Lower Body - Glúteos' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_elong = (SELECT id FROM serie WHERE nombre = 'Elongación y Movilidad' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_mixto = (SELECT id FROM serie WHERE nombre = 'Mixto - Alta Intensidad' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);

-- Rutina 1: Fuerza completa (Pecho + Piernas + Espalda)
INSERT INTO rutina (nombre, descripcion, estado, fecha_creacion, fecha_modificacion, es_plantilla, creador, token_publico, usuario_id, profesor_id)
VALUES ('Rutina Fuerza Completa', 'Rutina de 3 días para desarrollo muscular general.', 'ACTIVA', NOW(), NOW(), 1, 'ADMIN', 'test_rutina1_abc123456789', NULL, @profesor_id);
SET @r1 = LAST_INSERT_ID();
INSERT INTO rutina_categoria (rutina_id, categoria_id) VALUES (@r1, @cat_fuerza);
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 0, descripcion, 0, creador, repeticiones_serie, id, @r1, @profesor_id FROM serie WHERE id = @serie_pecho;
SET @r1s1 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r1s1, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_pecho;
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 1, descripcion, 0, creador, repeticiones_serie, id, @r1, @profesor_id FROM serie WHERE id = @serie_piernas;
SET @r1s2 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r1s2, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_piernas;
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 2, descripcion, 0, creador, repeticiones_serie, id, @r1, @profesor_id FROM serie WHERE id = @serie_espalda;
SET @r1s3 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r1s3, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_espalda;

-- Rutina 2: Full body (Full Body + Cardio)
INSERT INTO rutina (nombre, descripcion, estado, fecha_creacion, fecha_modificacion, es_plantilla, creador, token_publico, usuario_id, profesor_id)
VALUES ('Full Body + Cardio', 'Circuito completo con cardio. Ideal para días cortos.', 'ACTIVA', NOW(), NOW(), 1, 'ADMIN', 'test_rutina2_def456789012', NULL, @profesor_id);
SET @r2 = LAST_INSERT_ID();
INSERT INTO rutina_categoria (rutina_id, categoria_id) VALUES (@r2, @cat_fuerza), (@r2, @cat_cardio);
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 0, descripcion, 0, creador, repeticiones_serie, id, @r2, @profesor_id FROM serie WHERE id = @serie_full;
SET @r2s1 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r2s1, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_full;
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 1, descripcion, 0, creador, repeticiones_serie, id, @r2, @profesor_id FROM serie WHERE id = @serie_cardio;
SET @r2s2 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r2s2, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_cardio;

-- Rutina 3: Upper + Lower (Upper + Lower)
INSERT INTO rutina (nombre, descripcion, estado, fecha_creacion, fecha_modificacion, es_plantilla, creador, token_publico, usuario_id, profesor_id)
VALUES ('Upper Body + Lower Body', 'Split clásico superior/inferior.', 'ACTIVA', NOW(), NOW(), 1, 'ADMIN', 'test_rutina3_ghi789012345', NULL, @profesor_id);
SET @r3 = LAST_INSERT_ID();
INSERT INTO rutina_categoria (rutina_id, categoria_id) VALUES (@r3, @cat_fuerza);
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 0, descripcion, 0, creador, repeticiones_serie, id, @r3, @profesor_id FROM serie WHERE id = @serie_upper;
SET @r3s1 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r3s1, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_upper;
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 1, descripcion, 0, creador, repeticiones_serie, id, @r3, @profesor_id FROM serie WHERE id = @serie_lower;
SET @r3s2 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r3s2, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_lower;

-- Rutina 4: Fuerza + Elongación (Hombros + Elongación)
INSERT INTO rutina (nombre, descripcion, estado, fecha_creacion, fecha_modificacion, es_plantilla, creador, token_publico, usuario_id, profesor_id)
VALUES ('Hombros y Movilidad', 'Desarrollo de hombros con elongación final.', 'ACTIVA', NOW(), NOW(), 1, 'ADMIN', 'test_rutina4_jkl012345678', NULL, @profesor_id);
SET @r4 = LAST_INSERT_ID();
INSERT INTO rutina_categoria (rutina_id, categoria_id) VALUES (@r4, @cat_fuerza), (@r4, @cat_flexibilidad);
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 0, descripcion, 0, creador, repeticiones_serie, id, @r4, @profesor_id FROM serie WHERE id = @serie_hombros;
SET @r4s1 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r4s1, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_hombros;
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 1, descripcion, 0, creador, repeticiones_serie, id, @r4, @profesor_id FROM serie WHERE id = @serie_elong;
SET @r4s2 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r4s2, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_elong;

-- Rutina 5: alta intensidad (Mixto + Pecho + Espalda)
INSERT INTO rutina (nombre, descripcion, estado, fecha_creacion, fecha_modificacion, es_plantilla, creador, token_publico, usuario_id, profesor_id)
VALUES ('Alta Intensidad - Push Pull', 'Rutina avanzada de empuje y tirón.', 'ACTIVA', NOW(), NOW(), 1, 'ADMIN', 'test_rutina5_mno345678901', NULL, @profesor_id);
SET @r5 = LAST_INSERT_ID();
INSERT INTO rutina_categoria (rutina_id, categoria_id) VALUES (@r5, @cat_fuerza), (@r5, @cat_cardio);
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 0, descripcion, 0, creador, repeticiones_serie, id, @r5, @profesor_id FROM serie WHERE id = @serie_mixto;
SET @r5s1 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r5s1, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_mixto;
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 1, descripcion, 0, creador, repeticiones_serie, id, @r5, @profesor_id FROM serie WHERE id = @serie_pecho;
SET @r5s2 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r5s2, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_pecho;
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 2, descripcion, 0, creador, repeticiones_serie, id, @r5, @profesor_id FROM serie WHERE id = @serie_espalda;
SET @r5s3 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r5s3, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_espalda;

SELECT CONCAT('Insertadas ', COUNT(*), ' rutinas plantilla.') AS resultado FROM rutina WHERE es_plantilla = 1 AND usuario_id IS NULL AND profesor_id = @profesor_id;
