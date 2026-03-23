-- =============================================================================
-- Script 0 (opcional): Limpiar datos de prueba
-- Ejecutar si querés eliminar los datos de prueba y volver a cargar.
-- Orden: primero rutinas (y sus series copiadas), luego series plantilla,
--        progresos y usuarios.
-- =============================================================================

USE migimvirtual;

-- Desactivar Safe Updates para permitir DELETE con JOIN (MySQL Workbench lo bloquea por defecto)
SET SQL_SAFE_UPDATES = 0;

SET @profesor_id = (SELECT id FROM profesor LIMIT 1);

-- Eliminar rutinas plantilla de prueba (y sus series por CASCADE o manual)
-- Primero: desvincular categorías (tabla rutina_categoria)
DELETE rc FROM rutina_categoria rc
JOIN rutina r ON rc.rutina_id = r.id
WHERE r.token_publico LIKE 'test_rutina%' AND r.profesor_id = @profesor_id;

DELETE se FROM serie_ejercicio se
JOIN serie s ON se.serie_id = s.id
JOIN rutina r ON s.rutina_id = r.id
WHERE r.token_publico LIKE 'test_rutina%' AND r.profesor_id = @profesor_id;

DELETE s FROM serie s
JOIN rutina r ON s.rutina_id = r.id
WHERE r.token_publico LIKE 'test_rutina%' AND r.profesor_id = @profesor_id;

DELETE FROM rutina WHERE token_publico LIKE 'test_rutina%' AND profesor_id = @profesor_id;

-- Eliminar series plantilla de prueba (creadas por script 03)
DELETE se FROM serie_ejercicio se
JOIN serie s ON se.serie_id = s.id
WHERE s.es_plantilla = 1 AND s.rutina_id IS NULL AND s.profesor_id = @profesor_id
  AND s.nombre IN (
    'Pecho y Tríceps - Básico', 'Piernas - Fuerza', 'Espalda y Bíceps', 'Full Body - Circuito',
    'Hombros - Desarrollo', 'Cardio + Core', 'Upper Body - Empuje', 'Lower Body - Glúteos',
    'Elongación y Movilidad', 'Mixto - Alta Intensidad',
    'Core - Estabilidad', 'Prensa y Gemelos', 'Antebrazo y Agarre', 'HIIT - Tabata',
    'Movilidad Cadera', 'Empuje Inclinado', 'Tirón Remo', 'Calentamiento General',
    'Enfriamiento Activo', 'Superserie Brazos'
  );

DELETE FROM serie WHERE es_plantilla = 1 AND rutina_id IS NULL AND profesor_id = @profesor_id
  AND nombre IN (
    'Pecho y Tríceps - Básico', 'Piernas - Fuerza', 'Espalda y Bíceps', 'Full Body - Circuito',
    'Hombros - Desarrollo', 'Cardio + Core', 'Upper Body - Empuje', 'Lower Body - Glúteos',
    'Elongación y Movilidad', 'Mixto - Alta Intensidad',
    'Core - Estabilidad', 'Prensa y Gemelos', 'Antebrazo y Agarre', 'HIIT - Tabata',
    'Movilidad Cadera', 'Empuje Inclinado', 'Tirón Remo', 'Calentamiento General',
    'Enfriamiento Activo', 'Superserie Brazos'
  );

-- Rutinas asignadas de prueba (tokens test_asign_*) — antes de borrar usuarios
DELETE se FROM serie_ejercicio se
JOIN serie s ON se.serie_id = s.id
JOIN rutina r ON s.rutina_id = r.id
WHERE r.token_publico LIKE 'test_asign_%' AND r.profesor_id = @profesor_id;

DELETE s FROM serie s
JOIN rutina r ON s.rutina_id = r.id
WHERE r.token_publico LIKE 'test_asign_%' AND r.profesor_id = @profesor_id;

DELETE rc FROM rutina_categoria rc
JOIN rutina r ON rc.rutina_id = r.id
WHERE r.token_publico LIKE 'test_asign_%' AND r.profesor_id = @profesor_id;

DELETE FROM rutina WHERE token_publico LIKE 'test_asign_%' AND profesor_id = @profesor_id;

-- Eliminar progresos de usuarios de prueba
DELETE rp FROM registro_progreso rp
JOIN usuario u ON rp.usuario_id = u.id
WHERE u.correo LIKE 'test_alumno_%@migimvirtual.test';

-- Eliminar usuarios de prueba (si tienen rutinas asignadas, eliminarlas antes)
-- Primero: desvincular categorías de rutinas asignadas a alumnos de prueba
DELETE rc FROM rutina_categoria rc
JOIN rutina rr ON rc.rutina_id = rr.id
JOIN usuario u ON rr.usuario_id = u.id
WHERE u.correo LIKE 'test_alumno_%@migimvirtual.test';

DELETE rr FROM rutina rr
JOIN usuario u ON rr.usuario_id = u.id
WHERE u.correo LIKE 'test_alumno_%@migimvirtual.test';

DELETE FROM usuario WHERE correo LIKE 'test_alumno_%@migimvirtual.test';

-- Restaurar Safe Updates
SET SQL_SAFE_UPDATES = 1;

SELECT 'Datos de prueba eliminados.' AS resultado;
