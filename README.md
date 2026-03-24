# MiGymVirtual

App de gestión de gimnasio virtual: ejercicios, series, rutinas y hoja por token. Panel único de profesor (evolución desde Mattfuncional).

**Documentación:** En la carpeta **`Documentacion/`**:

1. **`Documentacion/INDICE_DOCUMENTACION.md`** – Mapa de toda la documentación (empezar aquí si buscás un tema).
2. **`Documentacion/LEEME_PRIMERO.md`** – Contexto del proyecto, acceso y dónde está cada cosa en el código.
3. **`Documentacion/PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md`** (u homónimo según versión) – Plan de desarrollo.
4. **`Documentacion/DOCUMENTACION_UNIFICADA.md`** – Resumen implementado, backup (§2), datos de prueba SQL (§1.4).
5. **`CHANGELOG.md`** (raíz) – Historial; última entrada relevante **[2026-03-15]** (cierre: scroll móvil, BD prueba, docs).

**Scripts SQL de prueba:** `scripts/BD/README.md` — orden `00` (opcional) → `01` … `06`.

**Backups y despliegue Ubuntu / Donweb:** `Documentacion/DOCUMENTACION_UNIFICADA.md` §2; despliegue completo (puerto 8081, SSH, VNC, Nginx): **`Documentacion/servidor/DESPLIEGUE-DONWEB-MIGIMVIRTUAL.md`**. Carpeta `backup` en servidor: ver ese documento y §2 de la documentación unificada.

**Stack:** Spring Boot, Thymeleaf, MySQL, Bootstrap. Código en `src/main/java/com/migimvirtual/`.
