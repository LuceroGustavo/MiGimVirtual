# Plan de desarrollo – MiGimVirtual

**Objetivo:** Transformar la base Mattfuncional en **MiGimVirtual**, app 100 % virtual (sin asistencias presenciales, sin pizarra/sala TV, sin gestión de alumnos presenciales). Mantener ejercicios, series, rutinas, hoja por token y hacer todo responsive.

**Referencia:** [BASE_PARA_APP_VIRTUAL.md](BASE_PARA_APP_VIRTUAL.md), [LEEME_PRIMERO.md](LEEME_PRIMERO.md).

---

## Orden de fases (resumen)

| Fase | Contenido | Estado |
|------|-----------|--------|
| **0** | Subplan: listado detallado de lo que se elimina (módulos, clases, rutas, templates) | Pendiente |
| **1** | Nueva base de datos `mgvirtual` y configuración | Pendiente |
| **2** | Renombrar todo Mattfuncional → MiGimVirtual (paquete, pom, títulos, docs) | Pendiente |
| **3** | Ejecutar eliminaciones según subplan (calendario, pizarra, sala, simplificar alumnos) | Pendiente |
| **4** | Continuar: responsive, ajustes UX, pruebas, documentación | Pendiente |

**Por qué este orden:** Definir el subplan (Fase 0) primero evita renombrar o migrar código que después vamos a borrar. La base nueva (Fase 1) se crea limpia para MiGimVirtual. El renombre (Fase 2) deja el proyecto con identidad única. En Fase 3 se elimina todo lo que no es virtual y en Fase 4 se cierra responsive y mejoras.

---

## Fase 0 – Subplan: qué eliminar o simplificar

Checklist para marcar al ejecutar la Fase 3. Todo lo que se elimina es por enfoque 100 % virtual.

### 0.1 Calendario y asistencias (eliminar)

| Tipo | Archivo / elemento | Notas |
|------|--------------------|--------|
| **Controlador** | `CalendarioController.java` | Eliminar clase completa. Rutas: `/calendario/**`, `semanal/profesor/{id}`. |
| **Servicios** | `CalendarioService.java`, `AsistenciaService.java`, `CalendarioExcepcionService.java`, `SlotConfigService.java` | Eliminar. |
| **Entidades** | `Asistencia.java`, `CalendarioExcepcion.java`, `DiaHorarioAsistencia.java`, `SlotConfig.java` | Eliminar. |
| **Repositorios** | `AsistenciaRepository.java`, `CalendarioExcepcionRepository.java`, `SlotConfigRepository.java` | Eliminar. |
| **DTOs** | `CalendarioSemanalDTO.java`, `AsistenciaVistaDTO.java` | Eliminar. |
| **Enum** | `TipoAsistencia.java` | Eliminar si solo se usa en asistencia. |
| **Templates** | `calendario/semanal-profesor.html`, `calendario/semanal.html` | Eliminar. |
| **Rutas / menú** | En `ProfesorController`, `SecurityConfig`, navbar: enlaces a calendario, rutas `/calendario/**` | Quitar referencias. |
| **Otros** | `UsuarioService`, `DepuracionService`, `AlumnoExportService`, `AlumnoJsonBackupService`: métodos o bloques que usen Asistencia/CalendarioExcepcion/SlotConfig | Simplificar o eliminar (ej. depuración de asistencias). |
| **DataInitializer** | Creación de `SlotConfig`, datos de calendario/asistencia | Quitar. |

### 0.2 Pizarra digital y sala TV (eliminar)

| Tipo | Archivo / elemento | Notas |
|------|--------------------|--------|
| **Controladores** | `PizarraController.java`, `SalaController.java` | Eliminar. Rutas: `/profesor/pizarra/**`, `/sala/**`. |
| **Servicio** | `PizarraService.java` | Eliminar. |
| **Entidades** | `Pizarra.java`, `PizarraColumna.java`, `PizarraItem.java`, `PizarraTrabajo.java`, `SalaTransmision.java` | Eliminar. |
| **Repositorios** | `PizarraRepository.java`, `PizarraColumnaRepository.java`, `PizarraItemRepository.java`, `PizarraTrabajoRepository.java`, `SalaTransmisionRepository.java` | Eliminar. |
| **DTO** | `PizarraEstadoDTO.java` | Eliminar. |
| **Templates** | `profesor/pizarra-lista.html`, `profesor/pizarra-editor.html`, `profesor/pizarra-nueva.html`, `sala/sala.html`, `sala/sala-pin.html`, `sala/sala-error.html` | Eliminar. |
| **Rutas / seguridad** | `SecurityConfig`: `.requestMatchers("/sala/**").permitAll()` y rutas de pizarra; navbar y `ProfesorController`: enlaces a pizarra/sala | Quitar. |

### 0.3 Alumnos presenciales (simplificar / reorientar)

| Tipo | Archivo / elemento | Acción |
|------|--------------------|--------|
| **Usuario / ficha** | `Usuario.java`: campos día/horario, tipo asistencia, etc. | Decidir: eliminar campos de presencial o dejar para uso virtual (ej. “tipo de plan”). |
| **Vistas** | `profesor/alumno-detalle.html`, `profesor/nuevoalumno.html`, lista alumnos: columna “Presente”, día/horario, excepciones de calendario | Quitar columna Presente, bloques de día/horario y excepciones; simplificar a ficha virtual. |
| **Servicios** | `UsuarioService`, `MedicionFisicaService`: lógica de presente/ausente, excepciones | Quitar o simplificar. |
| **DepuracionService** | Tarjeta “Registro de asistencias e inasistencias” y método asociado | Eliminar; mantener solo depuración de rutinas asignadas si se desea. |
| **AdminPanelController** | Ruta y vista de depuración de asistencias | Quitar. |

### 0.4 Referencias cruzadas a revisar

- **ProfesorController:** enlaces en dashboard/navbar a Calendario, Pizarra, Sala; redirecciones que apunten a `/calendario` o `/profesor/pizarra`.
- **NavbarModelAdvice / fragments/navbar.html:** ítems de menú Calendario, Pizarra.
- **CustomAuthenticationSuccessHandler:** solo si redirige a algo de calendario/pizarra.
- **Backup / export:** `ExerciseZipBackupService`, `AlumnoJsonBackupService`, `AlumnoExportService`: quitar referencias a Asistencia, CalendarioExcepcion, SlotConfig, Pizarra, Sala en export/import si las hay.
- **Manual de usuario:** `profesor/manual-usuario.html`: quitar secciones Calendario, Pizarra, Sala, Presente.

---

## Fase 1 – Nueva base de datos `mgvirtual`

1. **Crear en MySQL** la base `mgvirtual` (vacía o con script mínimo si se prefiere).
2. **Configuración:**
   - En `application.properties` (y perfiles `application-dev.properties`, etc.): cambiar `jdbc:mysql://.../mattfuncional?...` por `.../mgvirtual?...`.
   - Dejar el resto de configuración JPA igual (`ddl-auto=update` para que Hibernate cree/actualice el esquema según entidades que queden tras Fase 3).
3. **Scripts:** Si existen `scripts/servidor/reset_db_mattfuncional.sql` (y similares), crear versión `reset_db_migimvirtual.sql` o equivalente para `mgvirtual`, y actualizar referencias en documentación.
4. **No migrar datos** de mattfuncional a mgvirtual; arrancar limpio con la app ya recortada (tras Fase 3) para que el esquema coincida con el código.

**Nota:** Si se prefiere que la BD se llame `migimvirtual` en lugar de `mgvirtual`, basta usar ese nombre en la URL y en los scripts.

---

## Fase 2 – Renombrar Mattfuncional → MiGimVirtual

1. **Paquete Java:** `com.mattfuncional` → `com.migimvirtual` (refactor en IDE: rename package; actualizar todos los imports).
2. **pom.xml:** `groupId` y `artifactId` a `com.migimvirtual` y `migimvirtual`; `name` y `description` a MiGimVirtual.
3. **Clase principal:** `MattfuncionalApplication.java` → `MigimvirtualApplication.java` (o `MiGimVirtualApplication.java`) y mismo en `ServletInitializer` si referencia la aplicación.
4. **application.properties:** `spring.application.name=MiGimVirtual`; propiedades con prefijo `mattfuncional.*` → `migimvirtual.*` (y actualizar código que las use).
5. **Títulos y marca:** En templates (navbar, login, footer), reemplazar "Mattfuncional" / "Mat Funcional" por "MiGimVirtual".
6. **Documentación:** README.md, LEEME_PRIMERO.md, DOCUMENTACION_UNIFICADA.md, este plan: actualizar nombre del proyecto y referencias a paquete/BD.
7. **Scripts y carpeta raíz:** Renombrar `mattfuncional` (script servidor) a `migimvirtual` si se sigue usando, y referencias en Documentacion/servidor.

---

## Fase 3 – Ejecutar eliminaciones (según subplan Fase 0)

- Ir tildando en el subplan cada ítem a medida que se elimina o simplifica.
- Orden sugerido: (1) eliminar controladores y rutas, (2) servicios, (3) entidades y repositorios, (4) DTOs y enums, (5) templates, (6) limpiar SecurityConfig, navbar, ProfesorController, DataInitializer, DepuracionService, backup/export y manual.
- Tras cada bloque, compilar y arrancar la app para detectar referencias rotas.

---

## Fase 4 – Continuar

- **Responsive:** Revisar todas las vistas (ejercicios, series, rutinas, hoja por token, alumnos, panel, página pública) para 100 % responsive (móvil primero si se desea).
- **UX:** Ajustes de textos, mensajes y flujos para contexto virtual.
- **Pruebas:** Login, CRUD ejercicios/series/rutinas, asignación y hoja por token.
- **Documentación:** Actualizar LEEME_PRIMERO, DOCUMENTACION_UNIFICADA y CHANGELOG con el estado MiGimVirtual.
- **Opcional:** Depuración de rutinas asignadas, backup/export sin asistencias/pizarra; manual de usuario actualizado.

---

## Resumen de tu enfoque vs este plan

| Tu punto | Ajuste / mejora |
|----------|------------------|
| 1. Nueva BD `mgvirtual` | Incluido en Fase 1. Sin migración de datos; BD nueva para esquema limpio tras eliminar módulos. |
| 2. Renombrar todo Mattfuncional → MiGimVirtual | Fase 2, con detalle de paquete, pom, properties, títulos y docs. |
| 3. Subplan con métodos a eliminar | Fase 0: subplan detallado (controladores, servicios, entidades, repos, templates, rutas) para marcar al ejecutar Fase 3. |
| 4. Después continuar | Fase 4: responsive, UX, pruebas y documentación. |

**Optimización:** Hacer el subplan (Fase 0) **antes** del renombre y de la BD evita tocar código que después se borra. La BD nueva se crea cuando ya está claro qué entidades quedan (después de Fase 3); si se quiere tener la BD desde Fase 1, se puede crear vacía y dejar que `ddl-auto=update` la actualice tras las eliminaciones.

*Última actualización: Marzo 2026.*
