-- =============================================================================
-- Script 2: Progresos del alumno (registro_progreso)
-- Ejecutar después de 01_usuarios_prueba.sql
-- Distribución: varios alumnos con 0–12 progresos (alumno 1 con más filas para probar scroll).
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

-- Alumno 1: más registros (probar scroll en ficha)
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 70 DAY, 'CORE', 'Plancha lateral 30 seg.'
FROM usuario WHERE correo = 'test_alumno_1@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 85 DAY, 'HOMBROS', 'Press militar con mancuernas.'
FROM usuario WHERE correo = 'test_alumno_1@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 100 DAY, 'PIERNAS', 'Zancadas con peso corporal.'
FROM usuario WHERE correo = 'test_alumno_1@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 115 DAY, 'CARDIO', 'Elíptica 25 min zona 2.'
FROM usuario WHERE correo = 'test_alumno_1@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 130 DAY, 'ESPALDA', 'Jalón al pecho 40 kg.'
FROM usuario WHERE correo = 'test_alumno_1@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 145 DAY, 'BRAZOS, CORE', 'Circuito bíceps + plancha.'
FROM usuario WHERE correo = 'test_alumno_1@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 160 DAY, 'PECHO', 'Aperturas con mancuernas.'
FROM usuario WHERE correo = 'test_alumno_1@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 175 DAY, 'PIERNAS, GLÚTEOS', 'Hip thrust 3x12.'
FROM usuario WHERE correo = 'test_alumno_1@migimvirtual.test' LIMIT 1;

-- Alumnos 11–20: 2–3 progresos cada uno
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 5 DAY, 'PIERNAS', 'Bandas y glúteos.'
FROM usuario WHERE correo = 'test_alumno_11@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 18 DAY, 'CORE', 'Abdominales en fitball.'
FROM usuario WHERE correo = 'test_alumno_11@migimvirtual.test' LIMIT 1;

INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 4 DAY, 'CARDIO', 'Trote suave 5 km.'
FROM usuario WHERE correo = 'test_alumno_12@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 12 DAY, 'PIERNAS', 'Estabilidad tobillo.'
FROM usuario WHERE correo = 'test_alumno_12@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 22 DAY, 'ELONGACIÓN', 'Estiramientos post-carrera.'
FROM usuario WHERE correo = 'test_alumno_12@migimvirtual.test' LIMIT 1;

INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 3 DAY, 'FULL BODY', 'Rutina corta en casa.'
FROM usuario WHERE correo = 'test_alumno_13@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 14 DAY, 'CARDIO', 'HIIT 12 min.'
FROM usuario WHERE correo = 'test_alumno_13@migimvirtual.test' LIMIT 1;

INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 6 DAY, 'CARDIO, PIERNAS', 'Caminata inclinada.'
FROM usuario WHERE correo = 'test_alumno_14@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 19 DAY, 'CORE', 'Planchas y vacío.'
FROM usuario WHERE correo = 'test_alumno_14@migimvirtual.test' LIMIT 1;

INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 2 DAY, 'PECHO, HOMBROS', 'Press plano 50 kg.'
FROM usuario WHERE correo = 'test_alumno_15@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 11 DAY, 'ESPALDA', 'Dominadas asistidas.'
FROM usuario WHERE correo = 'test_alumno_15@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 24 DAY, 'CORE', 'Ab wheel ruedas.'
FROM usuario WHERE correo = 'test_alumno_15@migimvirtual.test' LIMIT 1;

INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 4 DAY, 'ESPALDA, CUELLO', 'Movilidad y remo.'
FROM usuario WHERE correo = 'test_alumno_16@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 15 DAY, 'CORE', 'Bird dog y cat-cow.'
FROM usuario WHERE correo = 'test_alumno_16@migimvirtual.test' LIMIT 1;

INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 3 DAY, 'PIERNAS, GLÚTEOS', 'Sentadilla sumo.'
FROM usuario WHERE correo = 'test_alumno_17@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 16 DAY, 'ELONGACIÓN', 'Estiramientos de cadera.'
FROM usuario WHERE correo = 'test_alumno_17@migimvirtual.test' LIMIT 1;

INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 4 DAY, 'CARDIO', 'Bici 20 min.'
FROM usuario WHERE correo = 'test_alumno_18@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 9 DAY, 'PIERNAS', 'Prensa 3x15.'
FROM usuario WHERE correo = 'test_alumno_18@migimvirtual.test' LIMIT 1;

INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 2 DAY, 'CORE, PELVIS', 'Kegel y hipopresivos.'
FROM usuario WHERE correo = 'test_alumno_19@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 10 DAY, 'BRAZOS', 'Mancuernas livianas.'
FROM usuario WHERE correo = 'test_alumno_19@migimvirtual.test' LIMIT 1;

INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 1 DAY, 'FULL BODY', 'Técnica con barra vacía.'
FROM usuario WHERE correo = 'test_alumno_20@migimvirtual.test' LIMIT 1;
INSERT INTO registro_progreso (usuario_id, fecha, grupos_musculares, observaciones)
SELECT id, CURDATE() - INTERVAL 7 DAY, 'PIERNAS', 'Sentadilla goblet.'
FROM usuario WHERE correo = 'test_alumno_20@migimvirtual.test' LIMIT 1;

SELECT CONCAT('Insertados progresos. Total en registro_progreso: ', COUNT(*), '.') AS resultado
FROM registro_progreso r
JOIN usuario u ON r.usuario_id = u.id
WHERE u.correo LIKE 'test_alumno_%@migimvirtual.test';
