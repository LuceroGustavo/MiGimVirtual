#!/bin/bash
# Reset de la base de datos mattfuncional en el servidor (borra TODOS los datos).
# Ejecutar desde la raíz del proyecto: bash scripts/servidor/reset_db_mattfuncional.sh
# Requiere: MySQL cliente, usuario con permiso DROP/CREATE DATABASE (root o mattfuncional_user según permisos).

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$APP_DIR"

# Cargar contraseña como en el menú mattfuncional
[ -f "$HOME/.bashrc" ] && source "$HOME/.bashrc"
[ -z "$MATT_DB_PASSWORD" ] && [ -f "$APP_DIR/.matt_db_password" ] && export MATT_DB_PASSWORD=$(cat "$APP_DIR/.matt_db_password" | tr -d '\n\r')

MYSQL_USER="${MATT_DB_USER:-mattfuncional_user}"
MYSQL_PASS="$MATT_DB_PASSWORD"
DB_NAME="mattfuncional"

echo "=============================================="
echo "  RESET BASE DE DATOS: $DB_NAME"
echo "=============================================="
echo "Esto BORRARÁ TODOS los datos (usuarios, alumnos, rutinas, series, etc.)."
echo "La aplicación recreará las tablas al arrancar (spring.jpa.hibernate.ddl-auto=update)."
echo ""
read -p "Escriba SI en mayúsculas para confirmar: " CONFIRM
if [ "$CONFIRM" != "SI" ]; then
    echo "Cancelado."
    exit 0
fi

if [ -z "$MYSQL_PASS" ]; then
    echo "Error: MATT_DB_PASSWORD no está definida. Exportala o crea $APP_DIR/.matt_db_password"
    exit 1
fi

echo "Ejecutando DROP DATABASE y CREATE DATABASE..."
mysql -u "$MYSQL_USER" -p"$MYSQL_PASS" -e "DROP DATABASE IF EXISTS $DB_NAME; CREATE DATABASE $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

if [ $? -eq 0 ]; then
    echo "Base de datos reseteada correctamente."
    echo "Reinicia la aplicación (opción 8 del menú o bash mattfuncional 8) para que Hibernate cree las tablas."
else
    echo "Error. Si el usuario $MYSQL_USER no tiene permiso DROP DATABASE, ejecuta como root:"
    echo "  mysql -u root -p < $SCRIPT_DIR/reset_db_mattfuncional.sql"
    exit 1
fi
