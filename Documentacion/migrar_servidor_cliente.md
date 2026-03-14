# Plan: Migrar Mattfuncional al servidor del cliente

Este documento reúne los pasos y puntos a tener en cuenta para migrar la aplicación Mattfuncional al servidor que compre el cliente. El cliente comprará un servidor **igual al nuestro** (mismo tipo de VPS con Ubuntu). Aquí se describen las características de nuestro servidor actual y los pasos de migración.

**Documentación de referencia:**

- [DESPLIEGUE-SERVIDOR.md](servidor/DESPLIEGUE-SERVIDOR.md) – Despliegue y gestión en el VPS (acceso SSH, menú, Nginx, MySQL, problemas frecuentes).
- [DOCUMENTACION_UNIFICADA.md](DOCUMENTACION_UNIFICADA.md) – Resumen de lo implementado, backup, despliegue, manual.
- [CHANGELOG.md](../CHANGELOG.md) – Historial de cambios del proyecto.
- [servidor/nginx-detodoya.conf](servidor/nginx-detodoya.conf) – Ejemplo de configuración Nginx (adaptar al dominio del cliente).

---

## 1. Características del servidor actual (referencia para compra del cliente)

Servidor de referencia: **VPS Donweb** donde corre Mattfuncional (detodoya.com.ar).

| Dato | Valor (nuestro servidor) |
|------|---------------------------|
| **Sistema operativo** | Ubuntu 24.04 LTS |
| **Acceso** | SSH (usuario `root` o usuario con sudo) |
| **Puerto SSH** | 5638 (en Donweb puede ser distinto por VPS; el cliente puede tener otro, ej. 22) |
| **IP** | 149.50.144.53 (la del cliente será otra) |
| **Aplicación** | Spring Boot (JAR); escucha en puerto **8080** |
| **Base de datos** | MySQL 8 (MariaDB compatible); BD `mattfuncional`, usuario `mattfuncional_user` |
| **Proxy inverso** | Nginx (puertos 80/443 → proxy a 8080) |
| **Certificado HTTPS** | Let's Encrypt (Certbot) – opcional pero recomendado |
| **Java** | OpenJDK 17 o superior (requerido para compilar y ejecutar el JAR) |
| **Memoria JVM** | -Xmx512m -Xms256m (script `mattfuncional`) |
| **Carpeta de la app** | `/root/mattfuncional` (clon del repo) |
| **Uploads** | `/root/mattfuncional/uploads` (ejercicios, avatares, etc.) |
| **Logs** | `logs/mattfuncional.log` dentro del proyecto |
| **Menú de gestión** | Script `./mattfuncional` en la raíz del proyecto; sesión `screen` opcional |

Requisitos mínimos recomendados para el servidor del cliente:

- **RAM:** 1 GB mínimo; 2 GB recomendado si hay muchos usuarios concurrentes.
- **Disco:** 20 GB mínimo (sistema + app + MySQL + uploads y backups).
- **Conexión:** Red estable para SSH y tráfico web.

---

## 2. Checklist previo a la migración

- [ ] Cliente tiene (o va a contratar) un VPS con **Ubuntu 24.04** (o 22.04 LTS).
- [ ] Cliente tiene (o va a registrar) un **dominio** que apuntará al servidor (ej. `midominio.com`).
- [ ] Repositorio de Mattfuncional accesible (GitHub público o acceso para el cliente/equipo).
- [ ] Decidir credenciales de BD y usuario developer en el servidor del cliente (no reutilizar las de producción actual).
- [ ] Tener a mano: [DESPLIEGUE-SERVIDOR.md](servidor/DESPLIEGUE-SERVIDOR.md) y este plan.

---

## 3. Pasos para la migración al servidor del cliente

### 3.1 Preparar el servidor (primera vez)

1. **Acceder por SSH** al VPS del cliente (puerto según proveedor, ej. `ssh -p 22 root@IP_DEL_CLIENTE` o `ssh root@IP_DEL_CLIENTE`).

2. **Actualizar el sistema:**
   ```bash
   apt update && apt upgrade -y
   ```

3. **Instalar Java 17+ y Maven (si no usa solo el wrapper):**
   ```bash
   apt install -y openjdk-17-jdk-headless git
   ```
   (El proyecto usa `./mvnw`, pero el JDK es necesario para compilar y ejecutar el JAR.)

4. **Instalar MySQL:**
   ```bash
   apt install -y mysql-server
   mysql_secure_installation
   ```
   Crear base y usuario (sustituir contraseña por una segura):
   ```bash
   mysql -u root -p
   ```
   ```sql
   CREATE DATABASE IF NOT EXISTS mattfuncional
     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER IF NOT EXISTS 'mattfuncional_user'@'localhost' IDENTIFIED BY 'CONTRASEÑA_SEGURA';
   GRANT ALL PRIVILEGES ON mattfuncional.* TO 'mattfuncional_user'@'localhost';
   FLUSH PRIVILEGES;
   EXIT;
   ```

5. **Variables de entorno para la BD** (ajustar contraseña):
   ```bash
   echo 'export MATT_DB_USER=mattfuncional_user' >> ~/.bashrc
   echo 'export MATT_DB_PASSWORD=CONTRASEÑA_SEGURA' >> ~/.bashrc
   source ~/.bashrc
   ```

6. **Clonar el repositorio:**
   ```bash
   cd /root
   git clone https://github.com/LuceroGustavo/Mattfuncional.git mattfuncional
   cd mattfuncional
   ```
   Si el repo es privado, configurar token o clave SSH en el servidor.

7. **Permisos de ejecución del menú:**
   ```bash
   chmod +x /root/mattfuncional/mattfuncional
   ```

8. **Carpeta de uploads:**
   ```bash
   mkdir -p /root/mattfuncional/uploads
   ```

9. **Perfil Spring en servidor:** El script `mattfuncional` usa el perfil `donweb`. En el servidor del cliente se puede seguir usando `application-donweb.properties`; si el cliente tiene otro path de uploads o puerto, crear un nuevo perfil (ej. `application-cliente.properties`) y cambiar en el script la variable `SPRING_PROFILE` o usar variables de entorno.

   En `application-donweb.properties` las rutas son:
   - `mattfuncional.uploads.dir=/root/mattfuncional/uploads`
   - Puerto por defecto en ese perfil es 8081; el script `mattfuncional` fuerza `MATT_APP_PORT=8080` y `-Dserver.port=$APP_PORT`. Dejar 8080 para coincidir con Nginx.

### 3.2 Primera compilación y arranque

1. **Compilar:**
   ```bash
   cd /root/mattfuncional
   ./mvnw clean package -DskipTests -q
   ```

2. **Iniciar con el menú:**
   ```bash
   ./mattfuncional
   ```
   Elegir opción **4** (Iniciar aplicación) o **5** (Despliegue completo si ya hay código actualizado).

3. **Comprobar:** Desde el servidor `curl -I http://127.0.0.1:8080` debe devolver HTTP 200 (o 302 si redirige a login).

### 3.3 Nginx y dominio del cliente

1. **Instalar Nginx:**
   ```bash
   apt install -y nginx
   ```

2. **Configuración de sitio:** Usar como base el archivo [servidor/nginx-detodoya.conf](servidor/nginx-detodoya.conf). Crear en el servidor del cliente un archivo, por ejemplo `/etc/nginx/sites-available/mattfuncional-cliente`, con:
   - `server_name` = dominio del cliente (ej. `app.cliente.com`).
   - `proxy_pass http://127.0.0.1:8080;`
   - `client_max_body_size 50M;` (para subir backups ZIP y JSON).
   - Si usan HTTPS con Let's Encrypt, después de obtener el certificado agregar bloques `listen 443 ssl` y las rutas a `fullchain.pem` y `privkey.pem`.

3. **Activar sitio y recargar Nginx:**
   ```bash
   ln -sf /etc/nginx/sites-available/mattfuncional-cliente /etc/nginx/sites-enabled/
   nginx -t && systemctl reload nginx
   ```

4. **DNS:** En el panel del proveedor de dominio, apuntar el dominio (A) a la IP del VPS del cliente.

5. **HTTPS (recomendado):**
   ```bash
   apt install -y certbot python3-certbot-nginx
   certbot --nginx -d dominio-del-cliente.com
   ```
   Certbot ajusta Nginx automáticamente. Luego revisar que `client_max_body_size 50M;` siga en el bloque `server` que hace proxy a 8080.

### 3.4 Datos iniciales y usuario developer

- La aplicación crea datos iniciales (usuario developer, grupos, etc.) al arrancar si la BD está vacía (ver `DataInitializer.java`). Las credenciales del usuario developer están en ese código (o en configuración); documentarlas en un lugar seguro para el cliente y, si hace falta, cambiar la contraseña tras el primer acceso.

### 3.5 Backup y restauración en el servidor del cliente

- **Exportar desde el servidor actual:** Usar el panel de Mattfuncional (Administración → Backup) para exportar ejercicios (ZIP), alumnos (JSON) y, si aplica, Excel de alumnos.
- **Subir al servidor del cliente:** Por SCP o por la interfaz de backup/importación una vez la app esté corriendo en el cliente.
- **Importar en el cliente:** Administración → Backup → Importar ZIP / Importar alumnos JSON según corresponda.
- Si el cliente ya tiene un backup de BD MySQL completo, se puede restaurar en el nuevo servidor antes de arrancar la app (o con la app parada) y luego iniciar la app.

---

## 4. Mantenimiento y actualizaciones en el servidor del cliente

- **Actualizar código y redesplegar:** Entrar por SSH, `cd /root/mattfuncional`, ejecutar `./mattfuncional` y elegir **5** (Despliegue completo): para app → git pull → compilar → iniciar.
- **Ver logs:** Opción **7** del menú o `tail -f /root/mattfuncional/logs/mattfuncional.log`.
- **Reiniciar solo la app:** Opción **8** del menú.

---

## 5. Diferencias cliente vs. nuestro servidor (a adaptar)

| Aspecto | Nuestro servidor | Servidor del cliente |
|---------|-------------------|----------------------|
| IP | 149.50.144.53 | IP asignada por el proveedor del cliente |
| Puerto SSH | 5638 | Suele ser 22 u otro indicado por el proveedor |
| Dominio | detodoya.com.ar | Dominio que el cliente registre/apunte |
| Certificado SSL | Let's Encrypt para detodoya.com.ar | Certbot para el dominio del cliente |
| Contraseña BD | (interna) | Definir una nueva y segura |
| Usuario developer | developer@mattfuncional.com | Mismo o otro; contraseña cambiar si se desea |

El archivo **nginx-detodoya.conf** es de ejemplo; en el cliente se sustituye `detodoya.com.ar` por el dominio del cliente y las rutas del certificado SSL por las que genere Certbot para ese dominio.

---

## 6. Documentación a entregar o referenciar al cliente

- [LEEME_PRIMERO.md](LEEME_PRIMERO.md) – Cómo ingresar a la app (URLs, credenciales).
- [DOCUMENTACION_UNIFICADA.md](DOCUMENTACION_UNIFICADA.md) – Resumen de funcionalidades y despliegue.
- [servidor/DESPLIEGUE-SERVIDOR.md](servidor/DESPLIEGUE-SERVIDOR.md) – Detalle de acceso SSH, menú, Nginx, solución de problemas.
- Este plan: **migrar_servidor_cliente.md** – Para que el equipo o el cliente siga los pasos de migración.

Opcional: generar un PDF o documento único “Manual de instalación en servidor” que resuma los apartados 1–5 de este plan más los comandos esenciales de DESPLIEGUE-SERVIDOR.md.

---

## 7. Resumen rápido (orden sugerido)

1. Contratar VPS Ubuntu 24.04 y tener IP y acceso SSH.
2. Instalar Java 17, MySQL, Git; crear BD y usuario; variables `MATT_DB_*`.
3. Clonar repo en `/root/mattfuncional`; `chmod +x mattfuncional`; `mkdir uploads`.
4. Compilar con `./mvnw clean package -DskipTests`; iniciar con `./mattfuncional` (opción 4 o 5).
5. Instalar y configurar Nginx (proxy a 8080, `client_max_body_size 50M`); apuntar dominio (A) a la IP.
6. Opcional: Certbot para HTTPS.
7. Comprobar acceso por dominio; cambiar contraseña developer si corresponde.
8. Si hace falta: exportar datos del servidor actual e importar en el cliente por el panel de Backup.

---

*Última actualización: Febrero 2026. Características del servidor actual extraídas de DESPLIEGUE-SERVIDOR.md y script `mattfuncional`.*
