-- =============================================================================
-- Script 2: Progresos del alumno (registro_progreso)
-- Ejecutar después de 01_usuarios_prueba.sql
-- Distribución: algunos alumnos con 0, otros con 1, 2 o 4 progresos.
-- =============================================================================

USE migimvirtual;

-- Alumno 1 (test_alumno_1): 4 progresos
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 10 DAY, 'BRAZOS, PECHO', 'Buen avance en press banca. Subí 2 kg.'
FROM usuario WHERE correo = 'test_alumno_1@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 25 DAY, 'PIERNAS, ESPALDA', 'Sentadillas con buena técnica. Sin molestias.'
FROM usuario WHERE correo = 'test_alumno_1@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 40 DAY, 'CARDIO, BRAZOS', '30 min cardio. Curl con mancuernas 12 kg.'
FROM usuario WHERE correo = 'test_alumno_1@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 55 DAY, 'PECHO, TRÍCEPS', 'Fondos en paralelas. Ligera molestia en hombro derecho.'
FROM usuario WHERE correo = 'test_alumno_1@migimvirtual.test' LIMIT 1;

-- Alumno 2 (test_alumno_2): 2 progresos
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 5 DAY, 'PIERNAS', 'Rodilla bien. Caminata 20 min sin dolor.'
FROM usuario WHERE correo = 'test_alumno_2@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 20 DAY, 'CARDIO, ELONGACIÓN', 'Bici estática 25 min. Estiramientos al final.'
FROM usuario WHERE correo = 'test_alumno_2@migimvirtual.test' LIMIT 1;

-- Alumno 3 (test_alumno_3): 1 progreso
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 7 DAY, 'ESPALDA, BRAZOS, ELONGACIÓN', 'Rutina completa. Presión arterial normal post-entreno.'
FROM usuario WHERE correo = 'test_alumno_3@migimvirtual.test' LIMIT 1;

-- Alumno 4 (test_alumno_4): 4 progresos
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 2 DAY, 'PECHO, HOMBROS', 'Press plano 60 kg x 8. Buen día.'
FROM usuario WHERE correo = 'test_alumno_4@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 9 DAY, 'PIERNAS', 'Sentadilla 80 kg x 6. Subiendo carga.'
FROM usuario WHERE correo = 'test_alumno_4@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 16 DAY, 'ESPALDA, BÍCEPS', 'Dominadas 8 rep. Remo 50 kg.'
FROM usuario WHERE correo = 'test_alumno_4@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 23 DAY, 'CARDIO', 'HIIT 15 min. Manteniendo ritmo.'
FROM usuario WHERE correo = 'test_alumno_4@migimvirtual.test' LIMIT 1;

-- Alumno 5 (test_alumno_5): 2 progresos
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 3 DAY, 'ESPALDA, CORE', 'Plancha 45 seg. Lumbar sin dolor.'
FROM usuario WHERE correo = 'test_alumno_5@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 14 DAY, 'ELONGACIÓN, PIERNAS', 'Estiramientos y movilidad. Mejor postura.'
FROM usuario WHERE correo = 'test_alumno_5@migimvirtual.test' LIMIT 1;

-- Alumno 6 (test_alumno_6): 1 progreso
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 6 DAY, 'CARDIO', 'Caminata 30 min. Glucemia pre: 110.'
FROM usuario WHERE correo = 'test_alumno_6@migimvirtual.test' LIMIT 1;

-- Alumno 7 (test_alumno_7): 0 progresos (ninguno)

-- Alumno 8 (test_alumno_8): 2 progresos (estaba activo antes de baja)
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 45 DAY, 'PIERNAS, ELONGACIÓN', 'Movilidad de cadera. Sin dolor.'
FROM usuario WHERE correo = 'test_alumno_8@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 60 DAY, 'BRAZOS, ESPALDA', 'Bandas elásticas. Fuerza mantenida.'
FROM usuario WHERE correo = 'test_alumno_8@migimvirtual.test' LIMIT 1;

-- Alumno 9 (test_alumno_9): 1 progreso
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 1 DAY, 'PECHO, TRÍCEPS, HOMBROS', 'Rutina de definición. 3 series por ejercicio.'
FROM usuario WHERE correo = 'test_alumno_9@migimvirtual.test' LIMIT 1;

-- Alumno 10 (test_alumno_10): 0 progresos (ninguno)

SELECT CONCAT('Insertados progresos. Total en registro_progreso: ', COUNT(*), '.') AS resultado
FROM registro_progreso r
JOIN usuario u ON r.usuario_id = u.id
WHERE u.correo LIKE 'test_alumno_%@migimvirtual.test';
