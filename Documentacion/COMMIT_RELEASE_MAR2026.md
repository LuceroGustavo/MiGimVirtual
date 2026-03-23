# Sugerencia de commit — cierre Mar 2026

## Mensaje sugerido

```
chore(release): cierre app — scroll tablas móvil, BD prueba ampliada, docs

- CSS: mgv-scroll-panel / mgv-scroll-embed (≤991px) en listados del panel
- SQL: 20 alumnos, 20 series, 12 rutinas, 06 asignaciones; 00/LEEME actualizados
- Docs: INDICE_DOCUMENTACION, §1.4 y §5.8, README scripts/BD, CHANGELOG [2026-03-15]
```

## Rutas típicas a incluir (ajustar según tu `git status`)

Desde la raíz del repo del proyecto **Migimvirtual**:

```bash
git add CHANGELOG.md README.md
git add Documentacion/
git add scripts/BD/
git add src/main/resources/static/style.css
git add src/main/resources/templates/profesor/dashboard.html
git add src/main/resources/templates/profesor/ejercicios-lista.html
git add src/main/resources/templates/profesor/asignar-rutina.html
git add src/main/resources/templates/profesor/usuarios-sistema.html
git add src/main/resources/templates/profesor/alumno-detalle.html
git status
git commit -m "chore(release): cierre app — scroll tablas móvil, BD prueba ampliada, docs"
```

Si tu repositorio Git solo incluye la carpeta del proyecto, ejecutá los comandos dentro de esa carpeta.

## Referencia

- Detalle técnico: `CHANGELOG.md` **[2026-03-15]**
- Índice de documentación: `Documentacion/INDICE_DOCUMENTACION.md`
