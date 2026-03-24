-- =============================================================================
-- Script 6: Rutinas asignadas a alumnos (copias desde plantillas test_rutina*)
-- Ejecutar después de 04_rutinas_prueba.sql (y con usuarios de 01).
-- Crea copias con es_plantilla=0, usuario_id y series clonadas (como la app).
--
-- Requiere: MySQL 5.7+ / 8.x (procedimiento almacenado).
-- En Workbench: ejecutar el script completo (incluye DELIMITER).
-- =============================================================================

USE migimvirtual;

SET SQL_SAFE_UPDATES = 0;

DROP PROCEDURE IF EXISTS mgv_asignar_rutina_plantilla;

DELIMITER $$

CREATE PROCEDURE mgv_asignar_rutina_plantilla(
  IN p_token_plantilla VARCHAR(64),
  IN p_correo_alumno VARCHAR(128),
  IN p_token_asign VARCHAR(32),
  IN p_nota VARCHAR(2000)
)
BEGIN
  DECLARE v_tpl BIGINT DEFAULT NULL;
  DECLARE v_uid BIGINT DEFAULT NULL;
  DECLARE v_asig BIGINT;
  DECLARE v_done INT DEFAULT 0;
  DECLARE v_serie_id BIGINT;
  DECLARE v_new_serie BIGINT;
  DECLARE cur CURSOR FOR SELECT id FROM serie WHERE rutina_id = v_tpl ORDER BY orden ASC;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

  SET v_tpl = (SELECT id FROM rutina WHERE token_publico = p_token_plantilla LIMIT 1);
  SET v_uid = (SELECT id FROM usuario WHERE correo = p_correo_alumno LIMIT 1);

  IF v_tpl IS NULL OR v_uid IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Plantilla o alumno no encontrado (revisá token y correo).';
  END IF;

  INSERT INTO rutina (nombre, descripcion, estado, fecha_creacion, fecha_modificacion, es_plantilla, creador, token_publico, usuario_id, profesor_id, nota_para_alumno)
  SELECT nombre, descripcion, 'ACTIVA', NOW(), NOW(), 0, creador, p_token_asign, v_uid, profesor_id, p_nota
  FROM rutina WHERE id = v_tpl LIMIT 1;
  SET v_asig = LAST_INSERT_ID();

  INSERT INTO rutina_categoria (rutina_id, categoria_id)
  SELECT v_asig, categoria_id FROM rutina_categoria WHERE rutina_id = v_tpl;

  SET v_done = 0;
  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO v_serie_id;
    IF v_done THEN
      LEAVE read_loop;
    END IF;
    INSERT INTO serie (nombre, orden, descripcion, es_plantilla, creador, repeticiones_serie, plantilla_id, rutina_id, profesor_id)
    SELECT nombre, orden, descripcion, 0, creador, repeticiones_serie, id, v_asig, profesor_id FROM serie WHERE id = v_serie_id;
    SET v_new_serie = LAST_INSERT_ID();
    INSERT INTO serie_ejercicio (serie_id, exercise_id, valor, unidad, peso, orden)
    SELECT v_new_serie, exercise_id, valor, unidad, peso, orden FROM serie_ejercicio WHERE serie_id = v_serie_id;
  END LOOP;
  CLOSE cur;
END$$

DELIMITER ;

-- Plantillas: test_rutina1_abc123456789 … test_rutina12_cde789012345678
-- Tokens asignación: máx. 32 caracteres (columna token_publico); prefijo test_asgnNN_

CALL mgv_asignar_rutina_plantilla('test_rutina1_abc123456789', 'test_alumno_1@migimvirtual.test', 'test_asgn01_abcdefghij1234567890', 'Plan fuerza — seguimos con 3 series por bloque.');
CALL mgv_asignar_rutina_plantilla('test_rutina2_def456789012', 'test_alumno_2@migimvirtual.test', 'test_asgn02_abcdefghij1234567891', 'Cardio + full body para bajar impacto en rodilla.');
CALL mgv_asignar_rutina_plantilla('test_rutina3_ghi789012345', 'test_alumno_3@migimvirtual.test', 'test_asgn03_abcdefghij1234567892', 'Split superior/inferior — ajustar cargas según PA.');
CALL mgv_asignar_rutina_plantilla('test_rutina4_jkl012345678', 'test_alumno_4@migimvirtual.test', 'test_asgn04_abcdefghij1234567893', 'Hombros + movilidad antes de competencia.');
CALL mgv_asignar_rutina_plantilla('test_rutina5_mno345678901', 'test_alumno_5@migimvirtual.test', 'test_asgn05_abcdefghij1234567894', 'Alta intensidad — cuidar lumbar en remo.');
CALL mgv_asignar_rutina_plantilla('test_rutina6_klm789012345678', 'test_alumno_6@migimvirtual.test', 'test_asgn06_abcdefghij1234567895', 'Core y cardio suave por diabetes.');
CALL mgv_asignar_rutina_plantilla('test_rutina1_abc123456789', 'test_alumno_7@migimvirtual.test', 'test_asgn07_abcdefghij1234567896', 'Misma base que María — volumen acuático.');
CALL mgv_asignar_rutina_plantilla('test_rutina8_qrs345678901234', 'test_alumno_9@migimvirtual.test', 'test_asgn08_abcdefghij1234567897', 'Push/pull inclinado para definición.');
CALL mgv_asignar_rutina_plantilla('test_rutina10_wxy567890123456', 'test_alumno_11@migimvirtual.test', 'test_asgn09_abcdefghij1234567898', 'Énfasis piernas en máquina.');
CALL mgv_asignar_rutina_plantilla('test_rutina7_nop234567890123', 'test_alumno_12@migimvirtual.test', 'test_asgn10_abcdefghij1234567899', 'HIIT corto + cadera para running.');
CALL mgv_asignar_rutina_plantilla('test_rutina11_zab678901234567', 'test_alumno_13@migimvirtual.test', 'test_asgn11_abcdefghij1234567900', 'Brazos y agarre — progresar cada 2 semanas.');
CALL mgv_asignar_rutina_plantilla('test_rutina12_cde789012345678', 'test_alumno_14@migimvirtual.test', 'test_asgn12_abcdefghij1234567901', 'Circuito completo + core.');
CALL mgv_asignar_rutina_plantilla('test_rutina5_mno345678901', 'test_alumno_15@migimvirtual.test', 'test_asgn13_abcdefghij1234567902', 'Intensidad alta — descansos 90s.');
CALL mgv_asignar_rutina_plantilla('test_rutina9_tuv456789012345', 'test_alumno_16@migimvirtual.test', 'test_asgn14_abcdefghij1234567903', 'Sesión suave — priorizar cuello.');
CALL mgv_asignar_rutina_plantilla('test_rutina2_def456789012', 'test_alumno_17@migimvirtual.test', 'test_asgn15_abcdefghij1234567904', 'Full body + cardio para danza.');
CALL mgv_asignar_rutina_plantilla('test_rutina3_ghi789012345', 'test_alumno_18@migimvirtual.test', 'test_asgn16_abcdefghij1234567905', 'Split clásico — controlar presión.');
CALL mgv_asignar_rutina_plantilla('test_rutina4_jkl012345678', 'test_alumno_19@migimvirtual.test', 'test_asgn17_abcdefghij1234567906', 'Movilidad + hombros post-parto.');
CALL mgv_asignar_rutina_plantilla('test_rutina1_abc123456789', 'test_alumno_20@migimvirtual.test', 'test_asgn18_abcdefghij1234567907', 'Fuerza general — técnica primero.');

DROP PROCEDURE IF EXISTS mgv_asignar_rutina_plantilla;

SET SQL_SAFE_UPDATES = 1;

SELECT CONCAT('Rutinas asignadas de prueba: ', COUNT(*), '.') AS resultado
FROM rutina
WHERE token_publico LIKE 'test_asgn_%';
