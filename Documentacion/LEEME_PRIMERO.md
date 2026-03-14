# Contexto del proyecto – Leé esto primero

**Uso:** Si trabajás desde otra PC (o abrís el repo de nuevo), leé este archivo primero y después los que necesites. Sirve para que vos o la IA de Cursor tengan contexto rápido del proyecto.

---

## 0. Cómo ingresar (acceso rápido)

| Entorno | URL | Login |
|---------|-----|-------|
| **Producción** | http://detodoya.com.ar | `/login` — requiere correo y contraseña |
| **Local** | http://localhost:8080 | `/login` — mismo flujo |
| **Servidor (IP)** | http://149.50.144.53:8080 | Ver [servidor/DESPLIEGUE-SERVIDOR.md](servidor/DESPLIEGUE-SERVIDOR.md) |

**Páginas públicas (sin login):** `/` (landing), `/planes`, `/publica`, `/demo`.

**Credenciales de desarrollo** (creadas por `DataInitializer` al arrancar):

| Usuario | Correo | Contraseña |
|---------|--------|------------|
| Profesor/Admin | profesor@migimvirtual.com | profesor |
| Developer | developer@migimvirtual.com | Qbasic.1977.migimvirtual |

Tras iniciar sesión se redirige al **Panel del profesor** (`/profesor/{id}`).

---

## 1. Qué es este proyecto

- **Nombre:** MiGimVirtual (evolución desde Mattfuncional).
- **Qué hace:** App para un profesor/entrenador: gestiona **alumnos** (ficha, sin login), **ejercicios**, **series**, **rutinas** (asignación por alumno, enlace público por token), **calendario semanal** con asistencia (presente/ausente) y **progreso** (modal en ficha del alumno).
- **Quién usa:** Roles **DEVELOPER** (super admin), **ADMIN** y **AYUDANTE**. No hay panel alumno ni panel admin separado.
- **Stack:** Spring Boot, Thymeleaf, MySQL, Bootstrap. Código en `src/main/java/com/migimvirtual/` (controladores, servicios, entidades, repositorios, config, dto, enums).

---

## 2. Dónde está cada cosa en el código

| Área | Dónde mirar |
|------|-------------|
| **Panel profesor** | `controladores/ProfesorController.java`, templates `profesor/*.html` |
| **Ejercicios** | `Exercise`, `ExerciseService`, `ExerciseCargaDefaultOptimizado`, `ExerciseRepository`, `ExerciseController` |
| **Series** | `Serie`, `SerieEjercicio`, `SerieService`, `SerieController`, `SerieRepository` |
| **Rutinas** | `Rutina`, `RutinaService`, `RutinaControlador`, `RutinaRepository`; hoja pública en `RutinaControlador` + template por token |
| **Alumnos (usuarios)** | `Usuario`, `UsuarioService`, `UsuarioRepository`; ficha en `ProfesorController` + `alumno-detalle.html` |
| **Calendario y asistencia** | `CalendarioController`, `CalendarioService`, `AsistenciaService`, `Asistencia`, `DiaHorarioAsistencia`, `SlotConfig`; template `semanal-profesor.html` |
| **Grupos musculares** | `GrupoMuscular`, `GrupoMuscularService`, rutas en `ProfesorController` (`/profesor/mis-grupos-musculares`) |
| **Seguridad** | `SecurityConfig.java`, `CustomAuthenticationSuccessHandler` |

---

## 3. Estructura de la documentación (todo en `Documentacion/`)

| Archivo | Para qué sirve |
|---------|-----------------|
| **LEEME_PRIMERO.md** (este) | Contexto del proyecto, acceso, evolución realizada, dónde está cada cosa. |
| **PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md** | Plan de desarrollo: fases (BD, renombre, eliminaciones virtual, responsive), subplan de lo que se quita. |
| **DOCUMENTACION_UNIFICADA.md** | Resúmenes: lo implementado, backup, Excel alumnos, despliegue, manual (índice), referencias técnicas. |
| **AYUDA_MEMORIA.md** | Lista rápida de pendientes (ejercicios/vistas), eliminar alumno, backup terminado. |

**Carpeta `servidor/`:** Despliegue en VPS (SSH, Nginx, menú). Ver `servidor/DESPLIEGUE-SERVIDOR.md` y `servidor/nginx-detodoya.conf`.

**En la raíz:** `CHANGELOG.md` – historial de la app.

---

## 4. Evolución ya realizada (MiGimVirtual)

- **Base de datos:** Nueva BD `migimvirtual`; en `application*.properties` la URL apunta a `migimvirtual`. MySQL crea la BD si no existe; Hibernate crea/actualiza tablas con `ddl-auto=update`.
- **Renombre completo:** Paquete `com.mattfuncional` → `com.migimvirtual`; clase principal `MigimvirtualApplication`; `pom.xml` (groupId/artifactId/name) MiGimVirtual; credenciales de desarrollo `profesor@migimvirtual.com` y `developer@migimvirtual.com`.
- **Marca e interfaz:** Navbar con logo `mgvirtual_logo1.png` y fondo `fondo-navbar.png`; página pública (carrusel) con videos `video_mgvirtual_inicio_escritorio.mp4` (escritorio) y `Video_mgvirtual_inicio_movil.mp4` (móvil).
- **Servidor:** Script de menú `./migimvirtual` en la raíz; scripts `scripts/servidor/reset_db_migimvirtual.sql` y `reset_db_migimvirtual.sh`; variables de entorno `MIGIMVIRTUAL_DB_USER`, `MIGIMVIRTUAL_DB_PASSWORD`.

**Pendiente según plan:** Fase 0 (subplan detallado), Fase 3 (eliminar calendario, pizarra, sala, simplificar alumnos), Fase 4 (responsive, UX). Ver PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md.

---

## 5. Resumen rápido de estado (para la IA)

- **Implementado:** Ejercicios, series, rutinas, grupos musculares, alumnos (sin login), calendario y asistencia, pizarra y sala TV, página pública, manual en `/profesor/manual`, sistema de backup (ZIP, JSON y Excel alumnos). Ver DOCUMENTACION_UNIFICADA.md.
- **Pendiente:** Eliminaciones para app 100 % virtual (calendario, pizarra, sala, simplificar alumnos) y responsive. Ver PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md.

---

## 6. Frase para dar contexto a la IA desde otra PC

- *"Leé Documentacion/LEEME_PRIMERO.md y Documentacion/DOCUMENTACION_UNIFICADA.md para tener contexto del proyecto."*
- *"Plan de desarrollo: Documentacion/PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md."*

---

*Última actualización: Marzo 2026.*
