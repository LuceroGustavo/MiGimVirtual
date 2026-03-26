# Open Graph, previews en WhatsApp y URL pública detrás de Nginx

**Estado (seguimiento):** si al compartir enlaces en WhatsApp **no** se ve el logo de MiGymVirtual o sigue apareciendo material viejo (otro sitio / sin imagen), usar este documento para revisar despliegue, Nginx y caché. Los cambios de código apuntan a generar `og:image` y `og:url` con **HTTPS** y **host público**, no con `http://…:8081`.

**Relacionado:** [DESPLIEGUE-DONWEB-MIGIMVIRTUAL.md](DESPLIEGUE-DONWEB-MIGIMVIRTUAL.md) §10 (Nginx), §5 (`.env.production`).

---

## 1. Problema que se intentó corregir

- La app en el VPS escucha en **`8081`**; **Nginx** termina HTTPS y hace `proxy_pass` a `http://127.0.0.1:8081`.
- Sin tratar cabeceras `Forwarded`, Spring puede armar URLs absolutas como **`http://` + host + `:8081`**. Los crawlers de Meta/WhatsApp piden la página por **`https://migimvirtual.detodoya.com.ar`** y deben poder descargar **`og:image`** desde una URL **pública y coherente** (JPEG liviano `https://…/img/og-share-migymvirtual.jpg`; el PNG del navbar es pesado y WhatsApp a menudo **no** muestra la miniatura si `og:image` supera ~300–600 KB).
- **Mattfuncional en 8080** es otra app: no se “mezcla” en runtime, pero **caché** de previews o URLs mal generadas pueden hacer que el usuario vea logo o datos que no corresponden a MiGymVirtual.

---

## 2. Cambios implementados en el código (referencia)

| Qué | Dónde |
|-----|--------|
| Confianza en cabeceras del proxy (`X-Forwarded-Proto`, etc.) | `application-donweb.properties`: `server.forward-headers-strategy=framework` |
| URL base pública HTTPS por defecto en **donweb** (si no hay env) | `migimvirtual.public-base-url=${MIGIMVIRTUAL_PUBLIC_BASE_URL:https://migimvirtual.detodoya.com.ar}` — evita `og:image` con `http://…:8081` cuando el crawler no refleja bien los forwarded headers |
| Resolución centralizada de la base URL para OG | `com.migimvirtual.config.PublicBaseUrlResolver` — si el `Host` es `migimvirtual.detodoya.com.ar`, fuerza `https://…`; si `migimvirtual.public-base-url` o la env `MIGIMVIRTUAL_PUBLIC_BASE_URL` vienen como `http://migimvirtual.detodoya.com.ar`, se normalizan a **https** (evita `og:url` en HTTP cuando la canonical es HTTPS) |
| Imagen OG liviana (WhatsApp) + dimensiones | `com.migimvirtual.config.OpenGraphBrandLogo` (`/img/og-share-migymvirtual.jpg`, 512×512, `image/jpeg`); navbar sigue usando `mgvirtual_logo1.png` |
| Meta extra `og:image:secure_url`, `width`, `height`, `type`, `alt` | Fragmento `fragments/open-graph-image-meta.html` incluido en `index-publica`, `planes-publica`, `verRutina` |
| Uso del resolvedor + logo | `PortalControlador` (landing `/` y `/planes`), `RutinaControlador` (`/rutinas/hoja/{token}`), `ProfesorController` (vista privada de rutina que reutiliza OG) |
| Meta tags OG en plantillas públicas | `index-publica.html`, `planes-publica.html`; hoja pública `verRutina.html` |
| Endpoint `/status` comprobando existencia del logo en classpath | `PortalControlador` (recurso `static/img/mgvirtual_logo1.png`) |

**Imagen en previews (WhatsApp/Meta):** **`og-share-migymvirtual.jpg`** (peso bajo). El logo grande del sitio sigue siendo **`mgvirtual_logo1.png`** en la UI.

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

1. **Ver HTML:** abrir en el navegador (idealmente sesión incógnito) `https://migimvirtual.detodoya.com.ar/` y **ver código fuente** (Ctrl+U). Buscar `og:image`: debe ser `https://migimvirtual.detodoya.com.ar/img/og-share-migymvirtual.jpg` (sin `:8081`).
2. **Probar la imagen:** abrir esa URL de imagen directamente en el navegador; debe devolver **200** y el JPEG.
3. **Depurador Meta:** [Sharing Debugger](https://developers.facebook.com/tools/debug/) → pegar la URL de la home o de una hoja `/rutinas/hoja/{token}` → “Scrape Again” para forzar refresco de caché.

---

## 6. Advertencia `fb:app_id` en el depurador

Meta puede mostrar *“Falta la propiedad fb:app_id”*. **No es obligatoria** para la vista previa de enlace (título, descripción, imagen). Solo hace falta si usás funciones que dependen de una app de Facebook. Podés ignorarla para MiGymVirtual o añadir más adelante un `<meta property="fb:app_id" content="...">` si creás una app en developers.facebook.com.

---

## 7. WhatsApp y caché

- Los previews **se cachean**. Tras cambiar OG o desplegar, puede tardar o requerir el depurador de Meta y **volver a pegar el enlace** en un chat de prueba.
- No confundir con el favicon del teléfono: lo que importa para la tarjeta es `og:image` y que la URL sea accesible públicamente por HTTPS.

---

## 8. Pendiente / revisión posterior

- [ ] Desplegar JAR con `OpenGraphBrandLogo`, fragmento `open-graph-image-meta` y default `migimvirtual.public-base-url` en **donweb** (`git pull`, `mvn package`, reinicio).
- [ ] Confirmar `proxy_set_header` en el `server` real del subdominio (no solo la plantilla del repo).
- [ ] [Sharing Debugger](https://developers.facebook.com/tools/debug/) → “Scrape Again” y volver a pegar el enlace en WhatsApp (caché).

---

*Documento pensado para retomar el tema cuando haya tiempo de revisar producción línea por línea.*
