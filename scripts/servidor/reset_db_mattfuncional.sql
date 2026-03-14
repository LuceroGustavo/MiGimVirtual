-- Reset completo de la base mattfuncional (borra todos los datos).
-- Ejecutar como usuario con permiso DROP/CREATE DATABASE (p. ej. root o mattfuncional_user si tiene privilegios).
-- Uso: mysql -u root -p < reset_db_mattfuncional.sql
-- O desde el script: bash scripts/servidor/reset_db_mattfuncional.sh

DROP DATABASE IF EXISTS mattfuncional;
CREATE DATABASE mattfuncional CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
