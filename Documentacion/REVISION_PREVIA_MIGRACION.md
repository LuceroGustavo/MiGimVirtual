# Revisión previa a migración – archivos y estructura

Revisión de duplicados y archivos innecesarios. Se eliminaron los archivos indicados en §2; **README.md** y **CHANGELOG.md** se dejan en la raíz (conviene para GitHub y convención).

---

## 1. Estructura actual del proyecto

```
Mattfuncional/
├── pom.xml, README.md, CHANGELOG.md, .gitignore, .gitattributes
├── mvnw, mvnw.cmd, .mvn/
├── mattfuncional          ← script menú servidor (bash)
├── .dockerignore
├── Documentacion/         ← documentación del proyecto (incl. servidor/)
├── scripts/servidor/      ← reset_db_mattfuncional.sh, reset_db_mattfuncional.sql
└── src/
    ├── main/java/... (controladores, servicios, entidades, config, etc.)
    └── main/resources/
        ├── application*.properties
        ├── static/ (css, js, img)
        └── templates/
```

**Documentacion/** (se mantiene íntegra):

- AYUDA_MEMORIA.md  
- DOCUMENTACION_UNIFICADA.md  
- LEEME_PRIMERO.md  
- PENDIENTES_FINALES.md  
- PLAN_DE_DESARROLLO_UNIFICADO.md  
- servidor/DESPLIEGUE-SERVIDOR.md  
- servidor/nginx-detodoya.conf  

No hay duplicados dentro de Documentacion.

---

## 2. Archivos que podrías eliminar o revisar (aviso, no eliminados)

| Archivo / carpeta | Motivo | Recomendación |
|-------------------|--------|----------------|
| **src/main/resources/templates/sala/sala.html.backup** | Copia de respaldo de `sala.html`. La versión en uso es `sala.html`. | **Eliminar** si ya no lo usás (queda solo `sala.html`). |
| **tatus** (en raíz) | Parece un typo de "status"; no es un comando estándar ni un script del proyecto. | **Eliminar** si no tiene uso (o renombrar a `status` si era intencional). |
| **debug.ps1** (raíz) | Script de depuración local (PowerShell). | Opcional: eliminar si no lo usás, o dejar si lo usás para depurar. |
| **debug.bat** (raíz) | Script de depuración local (Windows). | Igual que debug.ps1. |
| **tarea_actual.md** (raíz) | Notas de tarea/commit. | Opcional: eliminar si ya está todo commiteado; si lo usás como recordatorio, dejalo. |

No se encontraron otros `.bak`, `.old` o `.orig` en el repo (salvo el `.backup` de sala).

---

## 3. Carpetas/archivos que no conviene tocar

- **target/** – Generado por Maven; no se versiona (está en .gitignore). No eliminar del repo (ya está ignorado).
- **Documentacion/** – Toda la carpeta se mantiene para la migración y referencia.
- **scripts/servidor/** – Contiene scripts usados en el servidor (reset BD); la documentación los referencia.
- **mattfuncional** (raíz) – Script del menú de gestión en el servidor; necesario para despliegue.

---

## 4. Referencias en documentación a archivos que no están en el repo

En **Documentacion/servidor/DESPLIEGUE-SERVIDOR.md** se mencionan:

- **scripts/servidor/iniciar-menu.sh** – No existe en el repo. En su lugar, en el servidor se usa el script de raíz **`./mattfuncional`** (y sesión `screen -r mattfuncional`). Conviene actualizar la doc para indicar que el menú se ejecuta con `./mattfuncional` desde la raíz del proyecto.
- **limpiar_duplicados_slot_config.sql**, **consultar_duplicados_usuario.sql**, **alter_consulta_email_nullable.sql** – Citados en “Solución de problemas” pero no están en `scripts/servidor/`. Opciones: (a) crearlos y subirlos al repo según lo que describe DESPLIEGUE-SERVIDOR.md, o (b) actualizar la doc indicando que esos scripts se crean bajo demanda cuando haga falta.

En **CHANGELOG** y **tarea_actual.md** se menciona **COMMIT_PENDIENTE.md** y **Documentacion/MANUAL-USUARIO.md** / **AVANCES_DEL_APP.md**; no están en el árbol actual (el manual está como `profesor/manual-usuario.html`). No es necesario “eliminar” nada; solo tener en cuenta que esas rutas en la doc pueden estar desactualizadas.

---

## 5. Resumen de acciones sugeridas (solo después de tu OK)

1. **Eliminar** (si estás de acuerdo):  
   `src/main/resources/templates/sala/sala.html.backup`  
   `tatus` (raíz)

2. **Opcional:**  
   `debug.ps1`, `debug.bat`, `tarea_actual.md` – según si los seguís usando.

3. **No eliminar:**  
   Ningún archivo de Documentacion; ni `mattfuncional`, ni `scripts/servidor/`.

4. **Documentación:**  
   Actualizar DESPLIEGUE-SERVIDOR.md para que el menú sea `./mattfuncional` (y no `iniciar-menu.sh`), y aclarar qué hacer con los SQL de problemas (crearlos o documentar que son bajo demanda). Eso se puede hacer dentro del plan **migrar_servidor_cliente.md**.

Cuando confirmes qué querés borrar, se puede ejecutar solo eso y dejar el resto como está.
