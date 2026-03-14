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
| Profesor/Admin | profesor@mattfuncional.com | profesor |
| Developer | developer@mattfuncional.com | Qbasic.1977.mattfuncional |

Tras iniciar sesión se redirige al **Panel del profesor** (`/profesor/{id}`).

---

## 1. Qué es este proyecto

- **Nombre:** Mattfuncional (evolución de MiGym).
- **Qué hace:** App para un profesor/entrenador: gestiona **alumnos** (ficha, sin login), **ejercicios**, **series**, **rutinas** (asignación por alumno, enlace público por token), **calendario semanal** con asistencia (presente/ausente) y **progreso** (modal en ficha del alumno).
- **Quién usa:** Roles **DEVELOPER** (super admin), **ADMIN** y **AYUDANTE**. No hay panel alumno ni panel admin separado.
- **Stack:** Spring Boot, Thymeleaf, MySQL, Bootstrap. Código en `src/main/java/com/mattfuncional/` (controladores, servicios, entidades, repositorios, config, dto, enums).

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

Quedan **cuatro archivos** principales más la carpeta de servidor:

| Archivo | Para qué sirve |
|---------|-----------------|
| **LEEME_PRIMERO.md** (este) | Contexto del proyecto, acceso, dónde está cada cosa en el código. |
| **AYUDA_MEMORIA.md** | Lista rápida de pendientes de ejercicios/vistas; nota sobre eliminar alumno y backup. Sistema de backup terminado (resumen). |
| **PLAN_DE_DESARROLLO_UNIFICADO.md** | Visión, fases, checklist y pendientes detallados (único plan de desarrollo). |
| **DOCUMENTACION_UNIFICADA.md** | Resúmenes: lo implementado, backup y exportación, Excel alumnos, despliegue, manual (índice), referencias técnicas. |
| **BASE_PARA_APP_VIRTUAL.md** | Contexto para fork: app 100 % virtual (quitar asistencias, pizarra, alumnos presenciales; 100 % responsive). |

**Carpeta `servidor/`:** Despliegue en VPS (SSH, Nginx, menú). Ver `servidor/DESPLIEGUE-SERVIDOR.md` y `servidor/nginx-detodoya.conf`.

**En la raíz del proyecto:** `CHANGELOG.md` – historial general de la app.

---

## 4. Resumen rápido de estado (para la IA)

- **Implementado:** Ejercicios, series, rutinas, grupos musculares, alumnos (sin login), calendario y asistencia, pizarra y sala TV, página pública, manual en `/profesor/manual`, sistema de backup (ZIP, JSON y Excel alumnos). Ver DOCUMENTACION_UNIFICADA.md.
- **Pendiente:** Depuración de datos antiguos (archivar/eliminar asistencia de más de 12 meses). Ver PLAN_DE_DESARROLLO_UNIFICADO.md.

---

## 5. Frase para dar contexto a la IA desde otra PC

- *"Leé Documentacion/LEEME_PRIMERO.md y Documentacion/DOCUMENTACION_UNIFICADA.md para tener contexto del proyecto."*
- O: *"Para entender el proyecto: Documentacion/LEEME_PRIMERO.md y después PLAN_DE_DESARROLLO_UNIFICADO.md."*

---

*Última actualización: Marzo 2026.*
