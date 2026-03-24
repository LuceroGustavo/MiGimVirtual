# Índice de documentación – MiGymVirtual

Orden sugerido para **contexto** (IA o nueva PC): [LEEME_PRIMERO.md](LEEME_PRIMERO.md) → [DOCUMENTACION_UNIFICADA.md](DOCUMENTACION_UNIFICADA.md) → [servidor/DESPLIEGUE-DONWEB-MIGIMVIRTUAL.md](servidor/DESPLIEGUE-DONWEB-MIGIMVIRTUAL.md) (si tocás producción Donweb) → [CHANGELOG.md](../CHANGELOG.md) (últimas entradas).

---

## Raíz del repositorio

| Archivo | Contenido |
|---------|-----------|
| [README.md](../README.md) | Qué es el proyecto, punteros a `Documentacion/` y backups |
| [CHANGELOG.md](../CHANGELOG.md) | Historial de cambios versionado por fecha |

---

## Documentación principal (`Documentacion/`)

| Archivo | Uso |
|---------|-----|
| **LEEME_PRIMERO.md** | Acceso (URLs, credenciales), mapa del código, estado del proyecto |
| **COMMIT_MAR2026_UNIFICADO.md** | Mensaje y `git add` sugeridos para el commit del plan §1.6 |
| **DOCUMENTACION_UNIFICADA.md** | Resumen implementado, §1 pública/admin (**§1.5** post-cierre, **§1.6** commit unificado Mar 2026), §2 backup, manual §1.3, referencias |
| **AYUDA_MEMORIA.md** | Pendientes cortos, recordatorios, próximos pasos |
| **GUIA_RESPONSIVE.md** | Entornos, breakpoints, módulos del panel (§5.x), **scroll tablas móvil §5.8** |
| **PALETA_COLORES.md** | Colores por módulo y sectores Administración |
| **PLAN_DE_DESARROLLO_MIGIMVIRTUAL.md** | Plan por fases (nombre puede variar según versión del archivo) |
| **SUBPLAN_DESARROLLO_MODULOS.md** | Checklist por módulo |
| **ELIMINACION_CALENDARIO_Y_PIZARRA_MAR2026.md** | Histórico eliminación calendario / pizarra |
| **CAMBIOS_PANEL_ALUMNO_Y_ASISTENCIAS_MAR2026.md** | Cambios panel alumno (histórico) |
| **servidor/DESPLIEGUE-DONWEB-MIGIMVIRTUAL.md** | **Producción Donweb:** puerto 8081, SSH/VNC, DNS, Nginx HTTPS, scripts, `.env.production` |
| **servidor/DESPLIEGUE-SERVIDOR.md** | Puntero al documento anterior (compatibilidad) |

---

## Scripts SQL (`scripts/BD/`)

| Archivo | Rol |
|---------|-----|
| **README.md** | Orden de ejecución, requisitos, tablas (fuente principal en inglés/español) |
| **LEEME_BD.txt** | Orden corto en español (MySQL Workbench) |
| `00_limpiar_datos_prueba.sql` | Opcional: borrar datos de prueba |
| `01` … `06` | Usuarios, progresos, series, rutinas, (opc.) reparación, **asignaciones** |

Detalle de volúmenes y tokens: ver **README.md** en esa carpeta.

---

## Otros

| Ruta | Notas |
|------|--------|
| `docs/MEJORAS-CREAR-RUTINA.md` | Notas de mejora crear rutina |
| `CHANGELOG.md` | Ver entrada **[2026-03-15]** cierre app + scroll + BD |

---

*Actualizado: Marzo 2026 — reorganización documentación para commit de cierre.*
