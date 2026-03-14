-- Reset completo de la base migimvirtual (borra todos los datos).
-- Ejecutar como usuario con permiso DROP/CREATE DATABASE (p. ej. root o migimvirtual_user si tiene privilegios).
-- Uso: mysql -u root -p < reset_db_migimvirtual.sql
-- O desde el script: bash scripts/servidor/reset_db_migimvirtual.sh

DROP DATABASE IF EXISTS migimvirtual;
CREATE DATABASE migimvirtual CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
