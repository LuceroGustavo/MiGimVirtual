# Despliegue de Mattfuncional en el servidor Donweb

Guía para desplegar y gestionar la aplicación **Mattfuncional** en el VPS de Donweb. Incluye acceso por **SSH con clave** (recomendado desde tu PC), por **SSH con contraseña** y por **Consola VNC** (cuando PowerShell está bloqueado).

**Repositorio:** https://github.com/LuceroGustavo/Mattfuncional

---

## 1. Datos del servidor

| Dato   | Valor |
|--------|--------|
| IP     | `149.50.144.53` |
| Puerto aplicación | **8080** |
| SSH    | `ssh -p 5638 root@149.50.144.53` |
| SO     | Ubuntu 24.04 |

**URL de la aplicación (una vez desplegada):**  
`http://149.50.144.53:8080`

---

## 2. Acceso por SSH desde tu PC (recomendado: con clave, sin contraseña)

**Forma recomendada** para entrar al servidor desde tu computadora (PowerShell, Cursor, etc.):

- Se usa **SSH con clave** (clave pública en el servidor, clave privada en tu PC).
- **No pide contraseña** en cada conexión: podés ejecutar comandos directamente y, si usás Cursor u otra herramienta que corra comandos en tu terminal, también puede ejecutar órdenes en el servidor sin que tengas que escribir la contraseña.
- Evita confusiones entre “quién entra” y “quién ejecuta”: desde tu PC, cualquier comando `ssh -p 5638 root@149.50.144.53 "comando"` se ejecuta en el servidor si la clave está configurada.

**Conectar (sesión interactiva):**

```bash
ssh -p 5638 root@149.50.144.53
```

**Ejecutar un comando sin abrir sesión** (útil para scripts o para que la IA ejecute tareas en el servidor):

```bash
ssh -p 5638 root@149.50.144.53 "cd /root/mattfuncional && ./mattfuncional"
```

Si todavía no tenés clave configurada y querés hacerlo, generá un par en tu PC y agregá la clave pública al servidor en `~/.ssh/authorized_keys` (documentación estándar de SSH).

---

## 3. Acceso cuando no podés usar PowerShell/SSH (Consola VNC)

Si en tu trabajo PowerShell está bloqueado, podés gestionar el servidor desde la **Consola VNC** que ofrece Donweb:

1. Entrá al panel de Donweb → tu servidor (detodoya.com / vps-5469468-x).
2. Hacé clic en **Consola VNC** (botón verde).
3. En la consola **no se puede copiar/pegar**; tenés que escribir los comandos a mano.

### 3.1 Entrar al menú de Mattfuncional (primera vez o si no existe la sesión)

Escribí en este orden (cada línea y Enter):

```text
cd /root/mattfuncional
```

```text
./iniciar-menu.sh
```

Si la sesión ya existía, te dirá algo como: *"La sesión 'mattfuncional' ya existe. Para entrar al menú ejecutá: screen -r mattfuncional"*. En ese caso pasá al paso 3.2.

Si se creó nueva, también te pedirá que entres con el comando del paso 3.2.

### 3.2 Conectarte a la sesión del menú

Escribí **exactamente** (con **espacio** entre `screen` y `-r`):

```text
screen -r mattfuncional
```

Importante: no escribas `screen-r` junto; tiene que ser `screen`, espacio, `-r`, espacio, `mattfuncional`.

### 3.3 Despliegue completo desde el menú

En el menú **MATTFUNCIONAL - MENÚ DE GESTIÓN** elegí la opción **5** (Despliegue completo).  
Eso hace: parar app → actualizar código (git pull) → compilar → iniciar.

Cuando termine, la app queda en: **http://149.50.144.53:8080**

### 3.4 Salir del menú sin cerrarlo

Para salir de la consola pero dejar el menú y la app corriendo:  
**Ctrl+A**, soltá, y después **D** (detach).  
La próxima vez que entres por VNC podés volver con: `screen -r mattfuncional`.

---

## 4. Opciones del menú (1-11)

| Opción | Acción |
|--------|--------|
| 1 | Parar aplicación Mattfuncional |
| 2 | Actualizar código (git pull) |
| 3 | Compilar aplicación Mattfuncional |
| 4 | Iniciar aplicación Mattfuncional |
| 5 | **Despliegue completo** (1 → 2 → 3 → 4) |
| 6 | Ver estado del sistema |
| 7 | Ver logs de la aplicación |
| 8 | Reiniciar aplicación Mattfuncional |
| 9 | Información del proyecto |
| 10 | Ver espacio en disco |
| 11 | Salir |

*Nota: No se modificará por ahora el menú del servidor (sin instalación de Workbench ni scripts adicionales).*

---

## 5. Acceso a la aplicación (usuario developer)

Una vez desplegada, entrá en el navegador a:

**http://149.50.144.53:8080**

Para el **usuario developer** (creado automáticamente al iniciar la app):

- **Usuario (correo):** `developer@mattfuncional.com`
- **Contraseña:** definida en el proyecto (ver `DataInitializer.java` o documentación interna).  
  Si no la cambiaste, es la que está en ese archivo (no se documenta aquí por seguridad).

---

## 6. Primera vez: preparar el servidor

Solo hace falta hacerlo una vez (o si reinstalás el servidor).

### 6.1 Base de datos MySQL

Crear la base y el usuario (por ejemplo desde SSH o desde la misma consola):

```bash
mysql -u root -p
```

Dentro de MySQL:

```sql
CREATE DATABASE IF NOT EXISTS mattfuncional
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'mattfuncional_user'@'localhost' IDENTIFIED BY 'Matt2026';
GRANT ALL PRIVILEGES ON mattfuncional.* TO 'mattfuncional_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 6.2 Clonar el repositorio

```bash
cd /root
git clone https://github.com/LuceroGustavo/Mattfuncional.git mattfuncional
cd mattfuncional
```

Si el repo es privado, configurá token o SSH antes.

### 6.3 Variables de entorno para la base de datos

Para que el menú pueda iniciar la app correctamente:

```bash
echo 'export MATT_DB_USER=mattfuncional_user' >> ~/.bashrc
echo 'export MATT_DB_PASSWORD=Matt2026' >> ~/.bashrc
source ~/.bashrc
```

(Sustituí `Matt2026` si usaste otra contraseña en MySQL.)

### 6.4 Permisos de ejecución

```bash
chmod +x /root/mattfuncional/mattfuncional
chmod +x /root/mattfuncional/scripts/servidor/iniciar-menu.sh
```

### 6.5 Directorio de uploads (recomendado para ver ejercicios e imágenes)

El perfil `donweb` usa por defecto `/root/mattfuncional/uploads`. Crear la carpeta en el servidor:

```bash
mkdir -p /root/mattfuncional/uploads
```

La app crea automáticamente `uploads/ejercicios` al iniciar. Si las imágenes de ejercicios predeterminados (1.webp … 60.webp) no están, los ejercicios se crean igual pero sin imagen.

---

## 7. Uso por SSH con contraseña (alternativa)

Si entrás con usuario y contraseña (no tenés clave configurada):

```bash
ssh -p5638 root@149.50.144.53
cd /root/mattfuncional
./iniciar-menu.sh
screen -r mattfuncional
```

Luego en el menú elegí la opción que necesites (por ejemplo **5** para despliegue completo).

---

## 8. Archivos relacionados

| Archivo | Descripción |
|---------|-------------|
| `mattfuncional` (raíz del proyecto) | Script del menú de gestión (ejecutar como `./mattfuncional`). |
| `scripts/servidor/iniciar-menu.sh` | Crea la sesión `screen` con el menú; después se entra con `screen -r mattfuncional`. |
| `src/main/resources/application-donweb.properties` | Perfil Spring para Donweb (puerto por defecto 8081; el menú usa 8080 con `MATT_APP_PORT`). |
| `Documentacion/servidor/nginx-detodoya.conf` | Configuración Nginx para el dominio detodoya.com.ar (proxy al puerto 8080). Copiar a `/etc/nginx/sites-available/detodoya.com.ar` en el servidor. |

---

## 8.1 Modificar límite de subida (client_max_body_size)

Para permitir subir archivos ZIP grandes (backup de ejercicios con imágenes), Nginx debe tener `client_max_body_size` configurado. El archivo `nginx-detodoya.conf` del repo ya incluye `client_max_body_size 50M;` dentro del bloque `server` HTTPS.

**Si necesitás aplicarlo o cambiarlo en el servidor:**

1. **Por SSH** (con clave configurada):
   ```bash
   ssh -p 5638 root@149.50.144.53
   ```
   Luego en el servidor:
   ```bash
   # Ver si ya está configurado
   grep -n client_max_body_size /etc/nginx/sites-available/detodoya.com.ar

   # Si no está o querés cambiarlo, editar:
   nano /etc/nginx/sites-available/detodoya.com.ar
   # Dentro del bloque server { ... } que tiene proxy_pass, agregar (o modificar):
   #   client_max_body_size 50M;   # o 100M para backups más grandes

   # Probar y recargar
   nginx -t && systemctl reload nginx
   ```

2. **Copiar desde tu PC** (si modificaste el .conf en el repo):
   ```bash
   scp -P 5638 Documentacion/servidor/nginx-detodoya.conf root@149.50.144.53:/etc/nginx/sites-available/detodoya.com.ar
   ssh -p 5638 root@149.50.144.53 "nginx -t && systemctl reload nginx"
   ```

3. **Por Consola VNC** (si no podés usar SSH): entrá al menú, y en una terminal del servidor ejecutá los mismos comandos `nano`, `nginx -t` y `systemctl reload nginx`.

---

## 9. Resumen rápido (Consola VNC)

1. Donweb → Consola VNC.
2. `cd /root/mattfuncional`
3. `./iniciar-menu.sh`
4. `screen -r mattfuncional` (con espacio entre `screen` y `-r`).
5. Opción **5** para despliegue completo.
6. Abrir en el navegador: **http://149.50.144.53:8080**
7. Salir del menú sin cerrar: **Ctrl+A**, luego **D**.

---

## 10. Vincular el dominio detodoya.com.ar a la app

El dominio **detodoya.com.ar** en Donweb (DOMINIOS & DNS) ya está apuntando al mismo VPS (`149.50.144.53`). Eso solo hace que, al escribir `detodoya.com.ar` en el navegador, la petición llegue al servidor. Para que **esa petición llegue a Mattfuncional** (y no a otro servicio o a nada), en el **servidor** tenés que tener configurado un **proxy inverso** (Nginx o Apache) que escuche en el puerto 80 (y opcionalmente 443 para HTTPS) y reenvíe las peticiones al puerto donde corre la app (8080 u 8081).

### Qué suele faltar cuando “no se vincula” el dominio

| Qué | Dónde | Comentario |
|-----|--------|------------|
| **DNS** | Panel Donweb → DOMINIOS & DNS | Ya está: detodoya.com.ar → 149.50.144.53. |
| **Proxy inverso (Nginx/Apache)** | **Dentro del VPS** (Ubuntu) | Es lo que suele faltar: que el tráfico a `detodoya.com.ar` se envíe al puerto de la app. |
| **Puerto de la app** | Menú / variables de entorno | La app debe estar escuchando en un puerto fijo (ej. 8080). El proxy debe apuntar a ese mismo puerto. |

### Ejemplo con Nginx (en el servidor)

En el repo hay un archivo listo para copiar: **`Documentacion/servidor/nginx-detodoya.conf`**.

Si en el servidor usás **Nginx**:

1. Instalar Nginx (si no está):  
   `apt update && apt install -y nginx`

2. Copiar el sitio al servidor (desde tu PC, con la clave SSH configurada):  
   `scp -P 5638 Documentacion/servidor/nginx-detodoya.conf root@149.50.144.53:/etc/nginx/sites-available/detodoya.com.ar`

   O crear a mano el archivo `/etc/nginx/sites-available/detodoya.com.ar` con el mismo contenido (proxy a `http://127.0.0.1:8080` y headers `Host`, `X-Real-IP`, etc.).

3. Activar el sitio y recargar Nginx (en el servidor):

   ```bash
   ln -sf /etc/nginx/sites-available/detodoya.com.ar /etc/nginx/sites-enabled/
   nginx -t && systemctl reload nginx
   ```

Después de eso, **http://detodoya.com.ar** debería mostrar la app. (Para HTTPS hace falta después un certificado, por ejemplo con Certbot/Let's Encrypt.)

**Estado (Feb 2026):** Este sitio ya está configurado en el VPS; el dominio detodoya.com.ar apunta a Mattfuncional en el puerto 8080.

### Si usás Apache

Con **Apache** necesitás `mod_proxy` y `mod_proxy_http` habilitados, y un VirtualHost con `ServerName detodoya.com.ar` y `ProxyPass / http://127.0.0.1:8080/`. Si querés, se puede agregar un ejemplo completo en esta misma sección.

### Comprobar en el servidor

- Que la app esté corriendo: `curl -I http://127.0.0.1:8080` (o el puerto que uses).
- Que Nginx/Apache esté escuchando en 80: `ss -tlnp | grep :80` o `netstat -tlnp | grep :80`.
- Que el virtual host use `server_name detodoya.com.ar` (Nginx) o `ServerName detodoya.com.ar` (Apache) y que el `proxy_pass` apunte al mismo puerto de la app.

---

## 11. Solución de problemas

### Error "Query did not return a unique result: 2 results were returned" en el calendario

Puede deberse a **duplicados en `slot_config`** o a **varios usuarios con el mismo correo** en la tabla `usuario`. El código ya usa `findFirst` / `LIMIT 1` para no fallar, pero conviene limpiar datos:

1. **Ver logs en vivo** (opción **12** del menú) para confirmar el error.
2. **Duplicados en slot_config:**
   ```bash
   mysql -u mattfuncional_user -p mattfuncional < /root/mattfuncional/scripts/servidor/limpiar_duplicados_slot_config.sql
   ```
3. **Duplicados en usuario (mismo correo):** Consultar primero (solo lectura):
   ```bash
   mysql -u mattfuncional_user -p mattfuncional < /root/mattfuncional/scripts/servidor/consultar_duplicados_usuario.sql
   ```
   En la app, en **Usuarios del sistema** (logueado como developer) se muestra un aviso amarillo si hay correos duplicados. Resolver desde la interfaz (eliminar o unificar) o en BD con cuidado.
4. **Error "Column 'email' cannot be null" en formulario de consulta (/planes):** La tabla `consulta` requiere que `email` permita NULL (para consultas solo con teléfono). Ejecutar:
   ```bash
   mysql -u mattfuncional_user -p mattfuncional < /root/mattfuncional/scripts/servidor/alter_consulta_email_nullable.sql
   ```
5. **Despliegue completo** (opción 5) para aplicar la versión actualizada.

---

**Última actualización:** Febrero 2026.  
Se documentó el acceso por **SSH con clave (sin contraseña)** como forma recomendada desde tu PC; con la clave configurada, Cursor u otras herramientas pueden ejecutar comandos en el servidor sin pedir contraseña.
