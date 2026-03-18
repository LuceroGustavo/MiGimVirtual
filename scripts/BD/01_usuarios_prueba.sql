-- =============================================================================
-- Script 1: 10 usuarios (alumnos) de prueba
-- Ejecutar después de que la app haya creado las tablas y el profesor inicial.
-- Los alumnos tienen todos los campos del formulario de alta completos.
-- =============================================================================

USE migimvirtual;

-- Obtener el ID del profesor (creado por DataInitializer)
SET @profesor_id = (SELECT id FROM profesor LIMIT 1);

-- Si no hay profesor, no insertar (evitar error)
INSERT INTO usuario (
    nombre, edad, sexo, peso, password, rol, avatar, correo,
    notas_profesor, objetivos_personales, restricciones_medicas, celular,
    estado_alumno, fecha_alta, fecha_baja, historial_estado, fecha_inicio, profesor_id
) VALUES
-- Alumno 1
('María García López', 28, 'F', 65.5, NULL, 'ALUMNO', '/img/avatar1.png', 'test_alumno_1@migimvirtual.test',
 'Alumna muy comprometida. Preferir entrenar por la mañana.', 'Ganar masa muscular y definir abdomen.', 'Ninguna. Sin restricciones.',
 '+54 11 4567-8901', 'ACTIVO', CURDATE() - INTERVAL 180 DAY, NULL, 'ALTA|2024-09-18', CURDATE() - INTERVAL 180 DAY, @profesor_id),
-- Alumno 2
('Carlos Rodríguez Pérez', 35, 'M', 82.0, NULL, 'ALUMNO', '/img/avatar2.png', 'test_alumno_2@migimvirtual.test',
 'Trabaja de noche, prefiere entrenar tarde.', 'Bajar peso y mejorar resistencia cardiovascular.', 'Problemas de rodilla izquierda. Evitar impacto.',
 '+54 11 5678-9012', 'ACTIVO', CURDATE() - INTERVAL 120 DAY, NULL, 'ALTA|2024-11-15', CURDATE() - INTERVAL 120 DAY, @profesor_id),
-- Alumno 3
('Ana Martínez Fernández', 42, 'F', 58.3, NULL, 'ALUMNO', '/img/avatar3.png', 'test_alumno_3@migimvirtual.test',
 'Madre de dos hijos. Horario flexible.', 'Tonificar y mantener flexibilidad. Prevenir osteoporosis.', 'Hipertensión controlada con medicación.',
 '+54 11 6789-0123', 'ACTIVO', CURDATE() - INTERVAL 90 DAY, NULL, 'ALTA|2024-12-17', CURDATE() - INTERVAL 90 DAY, @profesor_id),
-- Alumno 4
('Diego Sánchez Ruiz', 22, 'M', 75.0, NULL, 'ALUMNO', '/img/avatar4.png', 'test_alumno_4@migimvirtual.test',
 'Estudiante universitario. Entrena 4 veces por semana.', 'Aumentar fuerza y volumen. Preparar para competencia amateur.',
 'Ninguna.', '+54 11 7890-1234', 'ACTIVO', CURDATE() - INTERVAL 60 DAY, NULL, 'ALTA|2025-01-15', CURDATE() - INTERVAL 60 DAY, @profesor_id),
-- Alumno 5
('Laura Torres González', 31, 'F', 62.0, NULL, 'ALUMNO', '/img/avatar5.png', 'test_alumno_5@migimvirtual.test',
 'Trabaja de oficina. Sedentaria hasta hace 3 meses.', 'Perder 5 kg y mejorar postura. Reducir dolor lumbar.',
 'Lumbalgia crónica. Evitar ejercicios de alto impacto.',
 '+54 11 8901-2345', 'ACTIVO', CURDATE() - INTERVAL 45 DAY, NULL, 'ALTA|2025-01-30', CURDATE() - INTERVAL 45 DAY, @profesor_id),
-- Alumno 6
('Fernando López Díaz', 48, 'M', 88.5, NULL, 'ALUMNO', '/img/avatar6.png', 'test_alumno_6@migimvirtual.test',
 'Ejecutivo. Viaja poco. Poco tiempo disponible.', 'Mantener salud cardiovascular. Controlar presión arterial.',
 'Diabetes tipo 2. Controlar glucemia antes de entrenar intenso.',
 '+54 11 9012-3456', 'ACTIVO', CURDATE() - INTERVAL 200 DAY, NULL, 'ALTA|2024-08-25', CURDATE() - INTERVAL 200 DAY, @profesor_id),
-- Alumno 7
('Valentina Romero Castro', 19, 'F', 55.0, NULL, 'ALUMNO', '/img/avatar7.png', 'test_alumno_7@migimvirtual.test',
 'Deportista. Hace natación además.', 'Mejorar rendimiento en natación. Fuerza en tren superior.',
 'Ninguna.', '+54 11 0123-4567', 'ACTIVO', CURDATE() - INTERVAL 30 DAY, NULL, 'ALTA|2025-02-15', CURDATE() - INTERVAL 30 DAY, @profesor_id),
-- Alumno 8
('Ricardo Morales Vega', 55, 'M', 90.0, NULL, 'ALUMNO', '/img/avatar8.png', 'test_alumno_8@migimvirtual.test',
 'Jubilado. Mucho tiempo libre.', 'Movilidad articular. Prevenir sarcopenia. Socializar.',
 'Artrosis de cadera. Evitar sentadillas profundas.',
 '+54 11 1234-5678', 'INACTIVO', CURDATE() - INTERVAL 250 DAY, CURDATE() - INTERVAL 30 DAY,
 'ALTA|2024-07-15|BAJA|2025-02-15', CURDATE() - INTERVAL 250 DAY, @profesor_id),
-- Alumno 9
('Sofía Herrera Mendoza', 26, 'F', 60.0, NULL, 'ALUMNO', '/img/avatar1.png', 'test_alumno_9@migimvirtual.test',
 'Empleada en gimnasio. Conoce bien el entrenamiento.', 'Definición muscular. Preparar para competencia de fitness.',
 'Alergia al polvo. Entrenar en ambiente limpio.',
 '+54 11 2345-6789', 'ACTIVO', CURDATE() - INTERVAL 15 DAY, NULL, 'ALTA|2025-03-01', CURDATE() - INTERVAL 15 DAY, @profesor_id),
-- Alumno 10
('Pablo Jiménez Silva', 38, 'M', 78.0, NULL, 'ALUMNO', '/img/avatar2.png', 'test_alumno_10@migimvirtual.test',
 'Entrenador personal. Quiere complementar su propio entrenamiento.', 'Mantener fuerza y movilidad. Prevenir lesiones por sobrecarga.',
 'Ninguna.', '+54 11 3456-7890', 'ACTIVO', CURDATE() - INTERVAL 100 DAY, NULL, 'ALTA|2024-12-05', CURDATE() - INTERVAL 100 DAY, @profesor_id);

SELECT CONCAT('Insertados ', ROW_COUNT(), ' usuarios de prueba.') AS resultado;
