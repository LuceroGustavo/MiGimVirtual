# Contexto del proyecto – Leé esto primero

**Uso:** Si trabajás desde otra PC (o abrís el repo de nuevo), leé este archivo primero y después los que necesites. Sirve para que vos o la IA de Cursor tengan contexto rápido del proyecto.

---

## 0. Cómo ingresar (acceso rápido)

| Entorno | URL | Login |
|---------|-----|-------|
| **Producción** | http://detodoya.com.ar | `/login` — requiere correo y contraseña |
| **Local** | http://localhost:8080 | `/login` — mismo flujo |
| **Producción MiGymVirtual (HTTPS)** | https://migimvirtual.detodoya.com.ar/ | Ver [servidor/DESPLIEGUE-DONWEB-MIGIMVIRTUAL.md](servidor/DESPLIEGUE-DONWEB-MIGIMVIRTUAL.md) |
| **Servidor (IP, puerto app)** | http://149.50.144.53:8081/ | Mismo documento (puerto **8081**; Mattfuncional usa **8080** en el mismo VPS) |

**Páginas públicas (sin login):** `/` (landing), `/planes`, `/publica`, `/demo`.

**Credenciales de desarrollo** (creadas por `DataInitializer` al arrancar):

| Usuario | Correo | Contraseña |
|---------|--------|------------|
| Profesor/Admin | profesor@migymvirtual.com | profesor |
| Developer | lucerogustavosi@gmail.com | Qbasic.1977 |

Tras iniciar sesión se redirige al **Panel del profesor** (`/profesor/{id}`).

---

## 1. Qué es este proyecto

- **Nombre:** MiGymVirtual (evolución desde Mattfuncional).
- **Qué hace:** App para un profesor/entrenador: gestiona **alumnos** (ficha, sin login), **ejercicios**, **series** y **rutinas** (asignación por alumno, enlace público por token). Orientación 100 % virtual: sin calendario, asistencias ni pizarra en sala (eliminados Mar 2026).
- **Quién usa:** Roles **DEVELOPER** (super admin), **ADMIN** y **AYUDANTE**. No hay panel alumno ni panel admin separado.
- **Stack:** Spring Boot, Thymeleaf, MySQL, Bootstrap. Código Java bajo `src/main/java/com/migimvirtual/` (controladores, servicios, entidades, repositorios, config, dto, enums).

**Nombres (marca vs técnico):** el producto se llama **MiGymVirtual** (textos, UI y nombre visible en `pom.xml`). La **carpeta del repo** en el disco puede ser `Migimvirtual` u otro nombre; no afecta al código. En Java y Maven se usa **minúsculas** en paquetes y artefactos (`com.migimvirtual`, `migimvirtual`) por convención; el contenido y la marca siguen siendo MiGymVirtual.

---

## 2. Dónde está cada cosa en el código

| Área | Dónde mirar |
|------|-------------|
| **Panel profesor** | `controladores/ProfesorController.java`, templates `profesor/*.html` |
| **Ejercicios** | `Exercise`, `ExerciseService`, `ExerciseCargaDefaultOptimizado`, `ExerciseRepository`, `ExerciseController` |
| **Series** | `Serie`, `SerieEjercicio`, `SerieService`, `SerieController`, `SerieRepository` |
| **Rutinas** | `Rutina`, `RutinaService`, `RutinaControlador`, `RutinaRepository`; hoja pública en `RutinaControlador` + template por token |
| **Alumnos (usuarios)** | `Usuario`, `UsuarioService`, `UsuarioRepository`; ficha en `ProfesorController` + `alumno-detalle.html` |
| **Grupos musculares** | `GrupoMuscular`, `GrupoMuscularService`, rutas en `ProfesorController` (`/profesor/mis-grupos-musculares`) |
| **Seguridad** | `SecurityConfig.java`, `CustomAuthenticationSuccessHandler` |

---

## 3. Estructura de la documentación (todo en `Documentacion/`)

| Archivo | Para qué sirve |
|---------|-----------------|
| **INDICE_DOCUMENTACION.md** | Mapa de **toda** la documentación (raíz + `Documentacion/` + `scripts/BD/`). Empezar aquí si buscás un tema concreto. |
| **LEEME_PRIMERO.md** (este) | Contexto del proyecto, acceso, evolución realizada, dónde está cada cosa. |
| **PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md** | Plan de desarrollo: fases (BD, renombre, eliminaciones virtual, responsive), subplan de lo que se quita. |
| **SUBPLAN_DESARROLLO_MODULOS.md** | Avances por módulo (Alumnos → Series → Rutinas → Asignaciones → Ejercicios → Administrar); checklist y notas. |
| **ELIMINACION_CALENDARIO_Y_PIZARRA_MAR2026.md** | Detalle de la eliminación de calendario, asistencias y pizarra online (Mar 2026). |
| **DOCUMENTACION_UNIFICADA.md** | Resúmenes: lo implementado, backup en servidor (§2), manual (§1.3), **§1.4 datos de prueba SQL**, **§1.6 plan unificado commit Mar 2026**, referencias técnicas. |
| **GUIA_RESPONSIVE.md** | Responsive del panel; **§5.8** scroll en tablas móvil (`mgv-scroll-panel` / `mgv-scroll-embed`). |
| **PALETA_COLORES.md** | Colores por módulo + **sectores de Administración** (backup contenido/alumnos, usuarios perfil/listado). Tonos pastel y derivaciones. |
| **AYUDA_MEMORIA.md** | Lista rápida de pendientes (ejercicios/vistas), eliminar alumno, backup terminado. |

**Carpeta `servidor/`:** Despliegue Donweb / VPS — guía principal **[servidor/DESPLIEGUE-DONWEB-MIGIMVIRTUAL.md](servidor/DESPLIEGUE-DONWEB-MIGIMVIRTUAL.md)** (SSH, VNC, puerto 8081, scripts, DNS, HTTPS). `servidor/DESPLIEGUE-SERVIDOR.md` redirige al mismo tema. Plantilla Nginx: `servidor/nginx-detodoya.conf`.

**Carpeta `scripts/BD/`:** Datos de prueba MySQL — ver **`scripts/BD/README.md`** (orden 00→06) y **`LEEME_BD.txt`** (resumen en español).

**En la raíz:** `CHANGELOG.md` – historial de la app (**[2026-03-15]** cierre: scroll móvil + BD + documentación).

---

## 4. Evolución ya realizada (MiGymVirtual)

- **Base de datos:** BD MySQL `migimvirtual` (nombre en minúsculas en `application*.properties`). MySQL crea la BD si no existe; Hibernate crea/actualiza tablas con `ddl-auto=update`.
- **Código:** Paquete `com.migimvirtual`; clase principal `MigimvirtualApplication`; `pom.xml`: `groupId`/`artifactId` `com.migimvirtual` / `migimvirtual`, nombre visible del proyecto **MiGymVirtual**; credenciales de desarrollo `profesor@migymvirtual.com` y `lucerogustavosi@gmail.com`.
- **Marca e interfaz:** Navbar con logo `mgvirtual_logo1.png` y fondo `fondo-navbar.png`; página pública (carrusel) con videos `video_mgvirtual_inicio_escritorio.mp4` (escritorio) y `Video_mgvirtual_inicio_movil.mp4` (móvil).
- **Servidor:** Menú bash `./migimvirtual` en la raíz del repo; scripts `scripts/servidor/reset_db_migimvirtual.sql` y `reset_db_migimvirtual.sh`; en VPS, variables típicas `MIGIMVIRTUAL_DB_USER`, `MIGIMVIRTUAL_DB_PASSWORD`, etc. (ver `servidor/DESPLIEGUE-DONWEB-MIGIMVIRTUAL.md`).

**Pendiente según plan:** Fase 0 (subplan detallado), Fase 4 (responsive, UX). Fase 3 (eliminar calendario, pizarra, sala, simplificar alumnos) realizada en Mar 2026. Ver ELIMINACION_CALENDARIO_Y_PIZARRA_MAR2026.md y PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md.

---

## 5. Resumen rápido de estado (para la IA)

- **Implementado:** Ejercicios, series, rutinas, grupos musculares, alumnos (sin login), **página pública** (landing + planes con config BD unificada, redes y contacto virtual; ver DOCUMENTACION_UNIFICADA §1.1 bis), **manual de usuario** en `/profesor/manual` (actualizado Mar 2026: enfoque profesor, 11 secciones, §4.1 formatos imagen y 5 MB — ver **§1.3**), sistema de **backup en servidor** (ZIP contenido + JSON alumnos, restauración total, máx. 2 archivos/tipo; export rutinas/series por profesor del panel). **Administrar sistema:** UX de cierre Mar 2026 — sectores con marco de color (backups contenido vs alumnos; usuarios perfil vs listado), estilos en `style.css` para vista con `?fragment=1`. **Listados en móvil (Mar 2026):** scroll interno en tablas de lectura (`mgv-scroll-panel` / `mgv-scroll-embed`, ver GUIA_RESPONSIVE §5.8). **Scripts SQL de prueba** ampliados (`scripts/BD/`, §1.4 DOCUMENTACION_UNIFICADA). Calendario, asistencias y pizarra/sala eliminados (Mar 2026); app 100 % virtual. Ver DOCUMENTACION_UNIFICADA.md §1.2, **§1.3**, **§1.4**, §2, DESPLIEGUE-SERVIDOR §6.6 (carpeta backup en Ubuntu) y ELIMINACION_CALENDARIO_Y_PIZARRA_MAR2026.md.
- **Pendiente:** Refinamientos opcionales de producto (página pública / copy — AYUDA_MEMORIA). Ver PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md.
- **Cierre Mar 2026:** Documentación reorganizada (`INDICE_DOCUMENTACION.md`, `scripts/BD/README.md`, `CHANGELOG` [2026-03-15]). **Commit unificado actual:** [COMMIT_MAR2026_UNIFICADO.md](COMMIT_MAR2026_UNIFICADO.md) (§1.6).
- **Post-cierre (mismo mes):** mejoras de **arranque en dev** y **UX Usuarios del sistema** en móvil — **§1.5**; **limpieza logging SLF4J**, **confirmaciones móvil**, **fix fragment navbar** y **commit unificado** — **§1.6** + [COMMIT_MAR2026_UNIFICADO.md](COMMIT_MAR2026_UNIFICADO.md). `CHANGELOG` [2026-03-16], [2026-03-23], **[2026-03-24]**.

---

## 6. Frase para dar contexto a la IA desde otra PC

- *"Leé Documentacion/LEEME_PRIMERO.md y Documentacion/DOCUMENTACION_UNIFICADA.md para tener contexto del proyecto."*
- *"Plan de desarrollo: Documentacion/PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md."*

---

*Última actualización: Marzo 2026 — §1.6 plan unificado (commit), confirmaciones móvil, fix navbar Thymeleaf, CHANGELOG [2026-03-24].*
