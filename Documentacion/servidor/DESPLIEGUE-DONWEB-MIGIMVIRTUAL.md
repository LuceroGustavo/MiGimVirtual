# Despliegue MiGymVirtual en Donweb (VPS)

Documentación de referencia para **producción** en el servidor Donweb: misma máquina que otras apps (p. ej. Mattfuncional), **puerto propio 8081**, dominio por subdominio, menú bash de gestión y acceso por **SSH** o **VNC**.

**Repositorio GitHub:** [https://github.com/LuceroGustavo/MiGimVirtual](https://github.com/LuceroGustavo/MiGimVirtual)

**Perfil Spring en servidor:** `donweb` (`--spring.profiles.active=donweb`)

---

## 1. Cómo encaja en el VPS (dos apps, dos puertos)

| Aplicación     | Puerto en el VPS | Uso típico |
|----------------|------------------|------------|
| Mattfuncional  | **8080**         | Sitio principal `detodoya.com.ar` (Nginx → 8080) |
| **MiGymVirtual** | **8081**       | Subdominio `migimvirtual.detodoya.com.ar` (Nginx → 8081) |

**Regla:** MiGymVirtual **nunca** debe arrancar pensando que el puerto es 8080 en este servidor, o chocará con Matt. El puerto efectivo lo define la variable de entorno **`MIGIMVIRTUAL_APP_PORT=8081`** (ver §5).

---

## 2. Datos del servidor Donweb

| Dato | Valor |
|------|--------|
| **IP pública** | `149.50.144.53` |
| **SSH** | `ssh -p 5638 root@149.50.144.53` |
| **Usuario** | `root` (o el que uses en el VPS) |
| **Sistema operativo** | Ubuntu 24.04 (ej. hostname `vps-5469468-x`) |
| **Panel** | Donweb → Cloud / VPS → Consola VNC o SSH |

**Autenticación SSH recomendada:** clave pública en el servidor (`~/.ssh/authorized_keys`), clave privada en la PC. Así no hace falta contraseña en cada conexión. Referencia genérica de claves: documentación del proyecto o `Detodoya.com/documentacion/servidor/Configuracion-SSH-Donweb.md` si está en el workspace.

---

## 3. URLs públicas

| URL | Descripción |
|-----|-------------|
| **https://migimvirtual.detodoya.com.ar/** | Entrada principal (HTTPS vía Let’s Encrypt + Nginx) |
| **http://149.50.144.53:8081/** | Acceso directo por IP y puerto (HTTP a Tomcat embebido; útil para pruebas) |

**DNS (Donweb → Zona DNS de `detodoya.com.ar`):** registro tipo **A** cuyo nombre sea el subdominio (p. ej. `migimvirtual` o `migimvirtual.detodoya.com.ar` según el formulario) apuntando a **`149.50.144.53`**. Los registros de **correo** (MX, `mail`, etc.) no deben modificarse para esto.

**Certificado TLS:** emitido con **Certbot** para el host `migimvirtual.detodoya.com.ar`; renovación automática habitual de Let’s Encrypt.

---

## 4. Directorio y artefactos en el servidor

| Ruta | Contenido |
|------|-----------|
| **`/root/migimvirtual/`** | Clon del repo Git; aquí se ejecuta `./migimvirtual` |
| **`/root/migimvirtual/target/migimvirtual-0.0.1-SNAPSHOT.jar`** | JAR generado por Maven tras compilar |
| **`/root/migimvirtual/logs/migimvirtual.log`** | Log de la app si se arranca con el menú / `nohup` |
| **`/root/migimvirtual/uploads/`** | Imágenes de ejercicios (según `application-donweb.properties`) |
| **`/root/migimvirtual/.env.production`** | Variables de entorno **solo en el servidor** (no versionar). Ver §5. Permisos recomendados: `chmod 600` |

**Menú launcher (varias apps):**

| Ruta | Función |
|------|---------|
| **`/root/menu.sh`** | **Recomendado.** Menú 1 = Mattfuncional, 2 = MiGymVirtual; abre el script nativo de cada proyecto |
| **`/root/deploy-all-apps.sh`** | Mismo comportamiento (llama a `menu.sh`); nombre antiguo por compatibilidad |

**Uso típico en VNC (desde cualquier carpeta):** `bash /root/menu.sh`  
Si estás en `/root` y ya tiene permiso de ejecución: `./menu.sh`

**Nombre antiguo:** `deploy-all-apps.sh` (con **guiones** entre `all` y `apps`). Si se escribe `deploy-allapps.sh` (sin guión), el archivo no existe.

---

## 5. Variables de entorno y puerto 8081

### 5.1 Archivo `/root/migimvirtual/.env.production` (recomendado en VPS)

Creá o editá este archivo en el servidor (valores de ejemplo; la contraseña es la que definas en MySQL):

```bash
export MIGIMVIRTUAL_APP_PORT=8081
export MIGIMVIRTUAL_DB_USER=migimvirtual_user
export MIGIMVIRTUAL_DB_PASSWORD='(contraseña del usuario MySQL)'
```

```bash
chmod 600 /root/migimvirtual/.env.production
```

El script **`migimvirtual`** (en la raíz del repo) debe **cargar primero** `.env.production` y **después** asignar `APP_PORT`, para que no quede fijado en 8080 por error. Si tras un `git pull` el menú vuelve a fallar por puerto, comprobar que el script del repo mantenga ese orden (`.env.production` + `~/.bashrc` → luego `APP_PORT`).

### 5.2 Alternativa: `~/.bashrc`

También se pueden exportar las mismas variables al final de `~/.bashrc`. Ojo: en **shells no interactivos**, el `.bashrc` de Ubuntu a veces hace `return` antes de llegar al final; por eso **`.env.production` en la carpeta del proyecto** es más fiable para scripts y `nohup`.

### 5.3 Contraseña de BD y Mattfuncional

En los `application-*.properties` de Spring **no** suele ir la contraseña de producción en claro: se usan variables (`MIGIMVIRTUAL_DB_PASSWORD`, etc.). En el mismo VPS puede reutilizarse la misma política que Matt (`MATT_DB_PASSWORD` en el servidor); el usuario MySQL de MiGymVirtual es distinto (`migimvirtual_user`) pero la contraseña puede coincidir o no según decisión de administración.

---

## 6. Base de datos MySQL

- **Base de datos:** `migimvirtual`
- **Usuario típico:** `migimvirtual_user` con permisos solo sobre esa base.
- **Conexión:** `localhost:3306` desde la app en el VPS.
- Esquema: Hibernate `ddl-auto=update` en perfil `donweb` (ajustes en `src/main/resources/application-donweb.properties`).

**Reset controlado (borra todos los datos):** desde la raíz del proyecto en el servidor:

```bash
bash scripts/servidor/reset_db_migimvirtual.sh
```

(Requiere `MIGIMVIRTUAL_DB_PASSWORD` o archivo `.migimvirtual_db_password` según indique el script.)

---

## 7. Script de menú `migimvirtual` (gestión desde consola)

Ubicación: **`/root/migimvirtual/migimvirtual`** (ejecutar como `./migimvirtual` estando en esa carpeta).

Debe ser **ejecutable:**

```bash
chmod +x /root/migimvirtual/migimvirtual
```

### Opciones habituales del menú

| Opción | Acción |
|--------|--------|
| 1 | Parar aplicación |
| 2 | Actualizar código (`git pull`) |
| 3 | Compilar (`mvnw` / Maven) |
| 4 | Iniciar aplicación |
| 5 | Despliegue completo (1+2+3+4) |
| 6 | Estado del sistema |
| 7 | Logs |
| 8 | Reiniciar |
| 9 | Información del proyecto |
| 10 | Espacio en disco |
| 11 | Borrar base de datos (peligroso) |
| 12 | Salir |

Tras **parar** la app, para volver a levantarla usá **4** o **5**. Si aparece **“Puerto 8080 ocupado”**, el menú está usando el puerto equivocado: revisá §5 y `.env.production`.

---

## 8. Acceso por consola VNC (Donweb)

1. Panel Donweb → tu VPS → **Consola VNC** (o cliente noVNC en el navegador).
2. Iniciá sesión como **`root`** (si pide contraseña, la de root del VPS).
3. En VNC **casi no hay copiar/pegar**: escribí los comandos con cuidado.

### 8.1 Abrir solo el menú de MiGymVirtual

```bash
cd /root/migimvirtual
```

```bash
./migimvirtual
```

Si dice permiso denegado:

```bash
chmod +x /root/migimvirtual/migimvirtual
```

### 8.2 Abrir el menú general (elegir Matt o MiGym)

```bash
bash /root/menu.sh
```

(O desde `/root` con `chmod +x menu.sh` una vez: `./menu.sh`.)

Elegí **2** para MiGymVirtual (internamente hace `bash ./migimvirtual` en la carpeta del proyecto).

**Equivalente antiguo:** `bash /root/deploy-all-apps.sh` (redirige al mismo menú).

### 8.3 Mattfuncional con `screen` (referencia cruzada)

Matt suele documentarse con **`screen -r mattfuncional`** tras `./iniciar-menu.sh`. MiGymVirtual en este despliegue **no depende** de ese flujo: alcanza con `./migimvirtual` en `/root/migimvirtual`. Si en el futuro se unifica con `screen`, actualizar esta sección.

---

## 9. Acceso por SSH desde la PC

**Sesión interactiva:**

```bash
ssh -p 5638 root@149.50.144.53
```

**Comando remoto único (ejemplo):**

```bash
ssh -p 5638 root@149.50.144.53 "cd /root/migimvirtual && git pull && ./mvnw -q package -DskipTests"
```

(Adaptar según necesidad; el menú interactivo no se puede “opción 5” así sin herramientas adicionales.)

**Subir archivos:**

```bash
scp -P 5638 archivo_local root@149.50.144.53:/ruta/remota/
```

---

## 10. Nginx (proxy inverso)

- El tráfico **HTTPS** hacia `migimvirtual.detodoya.com.ar` lo termina **Nginx** y se reenvía a **`http://127.0.0.1:8081`**.
- Archivo de referencia en el repo (plantilla / histórico): `Documentacion/servidor/nginx-detodoya.conf` (el del subdominio puede vivir como `migimvirtual.detodoya.com.ar` en `sites-available` en el servidor; Certbot suele inyectar bloques `ssl`).

Cabeceras típicas: `Host`, `X-Real-IP`, `X-Forwarded-For`, `X-Forwarded-Proto` (importante para que Spring Security y enlaces HTTPS se comporten bien).

**Tamaño de subida:** si subís ZIP grandes, en Nginx puede hacer falta `client_max_body_size` (p. ej. `50M`) en el `server` del subdominio.

---

## 11. Backups en disco (panel Administrar sistema)

Los backups (contenido ZIP, alumnos JSON, etc.) se guardan según la propiedad Spring **`migimvirtual.backups.dir`** en el perfil activo (ver `application-donweb.properties`).

- **No suele ser obligatorio** crear la carpeta `backup` a mano: al primer “Guardar backup” el servicio puede crear `backup/contenido` y `backup/alumnos` (o la estructura equivalente según versión).
- En **producción** conviene definir una **ruta absoluta** en el servidor y asegurar **permisos de escritura** para el usuario que ejecuta la app (`root` o usuario dedicado).

Comportamiento funcional y límites (rotación, restauración): **`Documentacion/DOCUMENTACION_UNIFICADA.md` §2**.

---

## 12. Memoria RAM en VPS pequeños (p. ej. 2 GB)

Dos aplicaciones Spring Boot en paralelo consumen mucha RAM. Es razonable limitar heap en el arranque, p. ej. **`-Xmx384m`** o similar en el menú / script de inicio, y monitorear con `free -h` y logs.

---

## 13. Checklist rápido de problemas

| Síntoma | Qué revisar |
|---------|-------------|
| Puerto 8080 ocupado al iniciar MiGymVirtual | `MIGIMVIRTUAL_APP_PORT` y orden de carga en `migimvirtual` (§5); `.env.production` |
| `No such file` al ejecutar launcher | Usar **`bash /root/menu.sh`** o nombre correcto **`deploy-all-apps.sh`** (con guiones) |
| `mattfuncional` / `migimvirtual` no ejecuta | `chmod +x` sobre el script |
| HTTPS con error de certificado en el subdominio | DNS A correcto; Certbot emitido para ese `server_name` |
| BD “Access denied” | Usuario/clave en `.env.production` y usuario MySQL creado en la base `migimvirtual` |
| Script con errores raros de sintaxis tras editar desde Windows | Fin de línea CRLF: en Linux `sed -i 's/\r$//' migimvirtual` |

---

## 14. Usuarios iniciales de la aplicación (solo referencia)

Tras el primer arranque, el `DataInitializer` crea usuarios de gestión (p. ej. administrador / developer). Las contraseñas **no** deben documentarse en texto plano en este archivo: están en código o configuración segura del proyecto. Para soporte, revisar logs del arranque o `DataInitializer` en el código.

---

## 15. Resumen una línea para IA / contexto

**MiGymVirtual en Donweb:** repo en `/root/migimvirtual`, perfil **`donweb`**, puerto **`8081`**, variables en **`.env.production`**, MySQL **`migimvirtual`**, URL pública **https://migimvirtual.detodoya.com.ar**, VPS **`149.50.144.53`**, SSH puerto **`5638`**, menú **`./migimvirtual`**, launcher multi-app **`bash /root/menu.sh`**, convive con Matt en **8080**.

---

**Última actualización del documento:** marzo 2026 (despliegue real en Donweb + HTTPS + launcher).
