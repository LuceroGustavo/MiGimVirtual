-- =============================================================================
-- Script 4: 12 rutinas plantilla (basadas en las series del script 03)
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
SET @serie_core = (SELECT id FROM serie WHERE nombre = 'Core - Estabilidad' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_prensa = (SELECT id FROM serie WHERE nombre = 'Prensa y Gemelos' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_antebrazo = (SELECT id FROM serie WHERE nombre = 'Antebrazo y Agarre' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_hiit = (SELECT id FROM serie WHERE nombre = 'HIIT - Tabata' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_mov_cad = (SELECT id FROM serie WHERE nombre = 'Movilidad Cadera' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_emp_incl = (SELECT id FROM serie WHERE nombre = 'Empuje Inclinado' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_tiron = (SELECT id FROM serie WHERE nombre = 'Tirón Remo' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_calent = (SELECT id FROM serie WHERE nombre = 'Calentamiento General' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_enfri = (SELECT id FROM serie WHERE nombre = 'Enfriamiento Activo' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);
SET @serie_super = (SELECT id FROM serie WHERE nombre = 'Superserie Brazos' AND es_plantilla = 1 AND rutina_id IS NULL LIMIT 1);

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

-- Rutina 6: Core + Cardio
INSERT INTO rutina (nombre, descripcion, estado, fecha_creacion, fecha_modificacion, es_plantilla, creador, token_publico, usuario_id, profesor_id)
VALUES ('Core y Cardio Express', 'Abdominales + trabajo cardiovascular.', 'ACTIVA', NOW(), NOW(), 1, 'ADMIN', 'test_rutina6_klm789012345678', NULL, @profesor_id);
SET @r6 = LAST_INSERT_ID();
INSERT INTO rutina_categoria (rutina_id, categoria_id) VALUES (@r6, @cat_fuerza), (@r6, @cat_cardio);
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 0, descripcion, 0, creador, repeticiones_serie, id, @r6, @profesor_id FROM serie WHERE id = @serie_core;
SET @r6s1 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r6s1, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_core;
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 1, descripcion, 0, creador, repeticiones_serie, id, @r6, @profesor_id FROM serie WHERE id = @serie_cardio;
SET @r6s2 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r6s2, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_cardio;

-- Rutina 7: HIIT + Movilidad
INSERT INTO rutina (nombre, descripcion, estado, fecha_creacion, fecha_modificacion, es_plantilla, creador, token_publico, usuario_id, profesor_id)
VALUES ('HIIT y Movilidad', 'Intervalos + cadera.', 'ACTIVA', NOW(), NOW(), 1, 'ADMIN', 'test_rutina7_nop234567890123', NULL, @profesor_id);
SET @r7 = LAST_INSERT_ID();
INSERT INTO rutina_categoria (rutina_id, categoria_id) VALUES (@r7, @cat_cardio), (@r7, @cat_flexibilidad);
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 0, descripcion, 0, creador, repeticiones_serie, id, @r7, @profesor_id FROM serie WHERE id = @serie_hiit;
SET @r7s1 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r7s1, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_hiit;
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 1, descripcion, 0, creador, repeticiones_serie, id, @r7, @profesor_id FROM serie WHERE id = @serie_mov_cad;
SET @r7s2 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r7s2, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_mov_cad;

-- Rutina 8: Empuje inclinado + Tirón
INSERT INTO rutina (nombre, descripcion, estado, fecha_creacion, fecha_modificacion, es_plantilla, creador, token_publico, usuario_id, profesor_id)
VALUES ('Push Pull Inclinado', 'Empuje en banco + remo.', 'ACTIVA', NOW(), NOW(), 1, 'ADMIN', 'test_rutina8_qrs345678901234', NULL, @profesor_id);
SET @r8 = LAST_INSERT_ID();
INSERT INTO rutina_categoria (rutina_id, categoria_id) VALUES (@r8, @cat_fuerza);
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 0, descripcion, 0, creador, repeticiones_serie, id, @r8, @profesor_id FROM serie WHERE id = @serie_emp_incl;
SET @r8s1 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r8s1, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_emp_incl;
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 1, descripcion, 0, creador, repeticiones_serie, id, @r8, @profesor_id FROM serie WHERE id = @serie_tiron;
SET @r8s2 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r8s2, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_tiron;

-- Rutina 9: Calentamiento + Enfriamiento
INSERT INTO rutina (nombre, descripcion, estado, fecha_creacion, fecha_modificacion, es_plantilla, creador, token_publico, usuario_id, profesor_id)
VALUES ('Sesión Suave Completa', 'Entrada y salida.', 'ACTIVA', NOW(), NOW(), 1, 'ADMIN', 'test_rutina9_tuv456789012345', NULL, @profesor_id);
SET @r9 = LAST_INSERT_ID();
INSERT INTO rutina_categoria (rutina_id, categoria_id) VALUES (@r9, @cat_flexibilidad);
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 0, descripcion, 0, creador, repeticiones_serie, id, @r9, @profesor_id FROM serie WHERE id = @serie_calent;
SET @r9s1 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r9s1, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_calent;
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 1, descripcion, 0, creador, repeticiones_serie, id, @r9, @profesor_id FROM serie WHERE id = @serie_enfri;
SET @r9s2 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r9s2, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_enfri;

-- Rutina 10: Prensa + Piernas (plantilla)
INSERT INTO rutina (nombre, descripcion, estado, fecha_creacion, fecha_modificacion, es_plantilla, creador, token_publico, usuario_id, profesor_id)
VALUES ('Piernas Máquina', 'Prensa + trabajo de piernas.', 'ACTIVA', NOW(), NOW(), 1, 'ADMIN', 'test_rutina10_wxy567890123456', NULL, @profesor_id);
SET @r10 = LAST_INSERT_ID();
INSERT INTO rutina_categoria (rutina_id, categoria_id) VALUES (@r10, @cat_fuerza);
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 0, descripcion, 0, creador, repeticiones_serie, id, @r10, @profesor_id FROM serie WHERE id = @serie_prensa;
SET @r10s1 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r10s1, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_prensa;
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 1, descripcion, 0, creador, repeticiones_serie, id, @r10, @profesor_id FROM serie WHERE id = @serie_piernas;
SET @r10s2 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r10s2, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_piernas;

-- Rutina 11: Superserie + Antebrazo
INSERT INTO rutina (nombre, descripcion, estado, fecha_creacion, fecha_modificacion, es_plantilla, creador, token_publico, usuario_id, profesor_id)
VALUES ('Brazos y Agarre', 'Volumen de brazos.', 'ACTIVA', NOW(), NOW(), 1, 'ADMIN', 'test_rutina11_zab678901234567', NULL, @profesor_id);
SET @r11 = LAST_INSERT_ID();
INSERT INTO rutina_categoria (rutina_id, categoria_id) VALUES (@r11, @cat_fuerza);
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 0, descripcion, 0, creador, repeticiones_serie, id, @r11, @profesor_id FROM serie WHERE id = @serie_super;
SET @r11s1 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r11s1, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_super;
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 1, descripcion, 0, creador, repeticiones_serie, id, @r11, @profesor_id FROM serie WHERE id = @serie_antebrazo;
SET @r11s2 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r11s2, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_antebrazo;

-- Rutina 12: Full Body + Core
INSERT INTO rutina (nombre, descripcion, estado, fecha_creacion, fecha_modificacion, es_plantilla, creador, token_publico, usuario_id, profesor_id)
VALUES ('Circuito Full + Core', 'Cuerpo completo con abdominales.', 'ACTIVA', NOW(), NOW(), 1, 'ADMIN', 'test_rutina12_cde789012345678', NULL, @profesor_id);
SET @r12 = LAST_INSERT_ID();
INSERT INTO rutina_categoria (rutina_id, categoria_id) VALUES (@r12, @cat_fuerza), (@r12, @cat_cardio);
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 0, descripcion, 0, creador, repeticiones_serie, id, @r12, @profesor_id FROM serie WHERE id = @serie_full;
SET @r12s1 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r12s1, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_full;
INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
SELECT nombre, 1, descripcion, 0, creador, repeticiones_serie, id, @r12, @profesor_id FROM serie WHERE id = @serie_core;
SET @r12s2 = LAST_INSERT_ID();
INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
SELECT @r12s2, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = @serie_core;

SELECT CONCAT('Insertadas ', COUNT(*), ' rutinas plantilla.') AS resultado FROM rutina WHERE es_plantilla = 1 AND usuario_id IS NULL AND profesor_id = @profesor_id;
