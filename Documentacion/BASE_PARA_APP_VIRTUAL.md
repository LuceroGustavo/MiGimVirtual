# Base para app 100 % virtual (fork de Mattfuncional)

**Uso:** Cuando abras un **nuevo chat** para crear una app derivada de Mattfuncional enfocada en lo **virtual** (sin asistencias presenciales, sin pizarra digital, sin alumnos presenciales, 100 % responsive), dale a la IA este archivo como primer contexto. Después puede profundizar en LEEME_PRIMERO.md y DOCUMENTACION_UNIFICADA.md.

---

## 0. Nombre y carpeta del nuevo proyecto

- **Nombre de la app:** **MiGymVirtual** (así, con mayúsculas en M, G y V — para interfaz, documentación y marca).
- **Carpeta del proyecto:** **`migymvirtual`** (todo en minúsculas — para carpeta, repositorio y URLs). Todo el contenido de Mattfuncional se copia dentro de esa carpeta. La IA debe trabajar ahí y, cuando corresponda, renombrar paquetes/títulos de Mattfuncional a MiGymVirtual.

---

## 1. Proyecto de origen (Mattfuncional)

- **Qué es:** App para profesor/entrenador: gestión de alumnos (ficha, sin login), ejercicios, series, rutinas (asignación por alumno, enlace por token), calendario semanal con asistencia (presente/ausente), progreso, pizarra/sala TV, página pública.
- **Stack:** Spring Boot, Thymeleaf, MySQL, Bootstrap. Código en `src/main/java/com/mattfuncional/` y plantillas en `src/main/resources/templates/`.
- **Roles:** DEVELOPER, ADMIN, AYUDANTE. Un solo panel (profesor).

---

## 2. Estructura rápida del código (para modificar / quitar cosas)

| Área | Ubicación principal |
|------|----------------------|
| Panel profesor / dashboard | `ProfesorController.java`, `profesor/*.html` |
| Ejercicios | `Exercise*`, `ExerciseController`, `ejercicios/`, `profesor/ejercicios-lista.html` |
| Series | `Serie*`, `SerieController`, `series/*.html` |
| Rutinas | `Rutina*`, `RutinaControlador`, `rutinas/*.html` |
| Alumnos (fichas) | `Usuario`, `UsuarioService`, `ProfesorController` (alumnos), `profesor/alumno-detalle.html`, `nuevoalumno.html` |
| **Calendario y asistencia** | `CalendarioController`, `AsistenciaService`, `Asistencia`, `SlotConfig`, `calendario/semanal.html` |
| **Pizarra y sala TV** | `PizarraController`, `PizarraService`, `SalaController`, `profesor/pizarra-*.html`, `sala/sala.html` |
| Grupos musculares | `GrupoMuscular*`, rutas en `ProfesorController` (`/profesor/mis-grupos-musculares`) |
| Página pública | `PublicoController`, `PortalControlador`, `index-publica.html`, `planes-publica.html` |
| Seguridad / login | `SecurityConfig.java`, `CustomAuthenticationSuccessHandler` |

---

## 3. Objetivo de la nueva app (virtual)

- **Enfocada en uso 100 % virtual:** sin asistencia presencial, sin pizarra digital, sin gestión de alumnos presenciales.
- **100 % responsive** para uso en móvil y escritorio.

---

## 4. Qué quitar o dejar de usar en el fork virtual

| Funcionalidad | Qué tocar |
|---------------|-----------|
| **Asistencias** (presente/ausente, calendario semanal) | Quitar o simplificar: `CalendarioController`, `AsistenciaService`, `Asistencia`, `SlotConfig`, templates de calendario/asistencia, menú y rutas relacionadas. |
| **Pizarra digital / sala TV** | Quitar: `PizarraController`, `PizarraService`, `SalaController`, pizarra-*.html, sala/sala.html, rutas `/sala/*`, `/profesor/pizarra*`. |
| **Alumnos presenciales** (ficha, día/horario, presente) | Quitar o reorientar: fichas de alumno con día/horario, columna “Presente”, lógica de asistencia en ficha. Si se mantienen “alumnos” o “usuarios”, que sean para uso virtual (ej. usuarios que acceden por enlace/token a rutinas). |

---

## 5. Qué mantener o priorizar en el fork virtual

- **Ejercicios, series y rutinas** (núcleo de la app).
- **Rutinas por enlace/token** (hoja pública `/rutinas/hoja/{token}`) — ideal para uso virtual.
- **Grupos musculares** (si aplica).
- **Página pública** (landing, planes, consultas) — adaptable a producto virtual.
- **Login y roles** (profesor/admin) según necesidad.
- **Responsive:** revisar/mejorar estilos y layouts en todas las vistas para 100 % responsive (móvil primero si se desea).

---

## 6. Documentación a leer después (en este repo)

| Archivo | Para qué |
|---------|----------|
| **LEEME_PRIMERO.md** | Contexto completo, acceso (URLs, credenciales), dónde está cada cosa en el código. |
| **DOCUMENTACION_UNIFICADA.md** | Resumen de lo implementado, backup, despliegue, manual, referencias técnicas. |
| **PLAN_DE_DESARROLLO_UNIFICADO.md** | Visión, fases y checklist (por si se reutilizan criterios). |

---

## 7. Frase para el nuevo chat

Podés copiar algo así al abrir el chat del nuevo proyecto:

*“Voy a partir del proyecto Mattfuncional para hacer la app **MiGymVirtual** (todo el código está en la carpeta **migymvirtual**). Leé primero Documentacion/BASE_PARA_APP_VIRTUAL.md para el objetivo y el nombre del proyecto; después LEEME_PRIMERO.md y DOCUMENTACION_UNIFICADA.md para la estructura. En MiGymVirtual hay que quitar: asistencias (calendario/presente/ausente), pizarra digital y sala TV, y la parte de alumnos presenciales. Mantener ejercicios, series, rutinas (y hoja por token), y hacer todo 100 % responsive.”*

---

*Creado para uso como contexto único al iniciar un fork orientado a app virtual.*
