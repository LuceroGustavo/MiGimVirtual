# Open Graph, previews en WhatsApp y URL pública detrás de Nginx

**Estado (seguimiento):** si al compartir enlaces en WhatsApp **no** se ve el logo de MiGymVirtual o sigue apareciendo material viejo (otro sitio / sin imagen), usar este documento para revisar despliegue, Nginx y caché. Los cambios de código apuntan a generar `og:image` y `og:url` con **HTTPS** y **host público**, no con `http://…:8081`.

**Relacionado:** [DESPLIEGUE-DONWEB-MIGIMVIRTUAL.md](DESPLIEGUE-DONWEB-MIGIMVIRTUAL.md) §10 (Nginx), §5 (`.env.production`).

---

## 1. Problema que se intentó corregir

- La app en el VPS escucha en **`8081`**; **Nginx** termina HTTPS y hace `proxy_pass` a `http://127.0.0.1:8081`.
- Sin tratar cabeceras `Forwarded`, Spring puede armar URLs absolutas como **`http://` + host + `:8081`**. Los crawlers de Meta/WhatsApp piden la página por **`https://migimvirtual.detodoya.com.ar`** y deben poder descargar **`og:image`** desde una URL **pública y coherente** (normalmente `https://…/img/mgvirtual_logo1.png`).
- **Mattfuncional en 8080** es otra app: no se “mezcla” en runtime, pero **caché** de previews o URLs mal generadas pueden hacer que el usuario vea logo o datos que no corresponden a MiGymVirtual.

---

## 2. Cambios implementados en el código (referencia)

| Qué | Dónde |
|-----|--------|
| Confianza en cabeceras del proxy (`X-Forwarded-Proto`, etc.) | `application-donweb.properties`: `server.forward-headers-strategy=framework` |
| URL base pública opcional por variable de entorno | `migimvirtual.public-base-url=${MIGIMVIRTUAL_PUBLIC_BASE_URL:}` (mismo archivo) |
| Resolución centralizada de la base URL para OG | `com.migimvirtual.config.PublicBaseUrlResolver` |
| Uso del resolvedor + imagen **`/img/mgvirtual_logo1.png`** (mismo logo que el navbar) | `PortalControlador` (landing `/` y `/planes`), `RutinaControlador` (`/rutinas/hoja/{token}`), `ProfesorController` (vista privada de rutina que reutiliza OG) |
| Meta tags OG en plantillas públicas | `index-publica.html`, `planes-publica.html`; hoja pública `verRutina.html` |
| Endpoint `/status` comprobando existencia del logo en classpath | `PortalControlador` (ruta `/img/mgvirtual_logo1.png`) |

**Imagen social:** siempre **`mgvirtual_logo1.png`** (no `logo.png` ni `logo matt.jpeg`).

---

## 3. Variables en el servidor (opcional pero útil)

En `/root/migimvirtual/.env.production` (sin barra final en la URL):

```bash
export MIGIMVIRTUAL_PUBLIC_BASE_URL=https://migimvirtual.detodoya.com.ar
```

- Usar si **aun** con `forward-headers-strategy=framework` las URLs generadas no son HTTPS/host correctos (proxy intermedio, CDN, etc.).
- Tras editar: reiniciar la app para que Spring vuelva a leer el entorno (según cómo cargue el script `migimvirtual` / `.env.production`).

---

## 4. Nginx: comprobar en el VPS

En el `server` del subdominio `migimvirtual.detodoya.com.ar`, el bloque `location` que hace proxy a **8081** debería incluir al menos:

```nginx
proxy_set_header Host $host;
proxy_set_header X-Forwarded-Proto $scheme;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
```

Guardar, `nginx -t`, `systemctl reload nginx`.

---

## 5. Cómo verificar sin WhatsApp

1. **Ver HTML:** abrir en el navegador (idealmente sesión incógnito) `https://migimvirtual.detodoya.com.ar/` y **ver código fuente** (Ctrl+U). Buscar `og:image`: debe ser `https://migimvirtual.detodoya.com.ar/img/mgvirtual_logo1.png` (sin `:8081`).
2. **Probar la imagen:** abrir esa URL de imagen directamente en el navegador; debe devolver **200** y el PNG.
3. **Depurador Meta:** [Sharing Debugger](https://developers.facebook.com/tools/debug/) → pegar la URL de la home o de una hoja `/rutinas/hoja/{token}` → “Scrape Again” para forzar refresco de caché.

---

## 6. WhatsApp y caché

- Los previews **se cachean**. Tras cambiar OG o desplegar, puede tardar o requerir el depurador de Meta y **volver a pegar el enlace** en un chat de prueba.
- No confundir con el favicon del teléfono: lo que importa para la tarjeta es `og:image` y que la URL sea accesible públicamente por HTTPS.

---

## 7. Pendiente / revisión posterior

- [ ] Confirmar en el servidor que el JAR desplegado incluye estos cambios (`git pull`, build, reinicio).
- [ ] Confirmar `proxy_set_header` en el `server` real del subdominio (no solo la plantilla del repo).
- [ ] Si sigue fallando: fijar `MIGIMVIRTUAL_PUBLIC_BASE_URL`, reiniciar, repetir §5.
- [ ] Limpiar caché en el depurador de Meta y reintentar WhatsApp.

---

*Documento pensado para retomar el tema cuando haya tiempo de revisar producción línea por línea.*
