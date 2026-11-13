# AppMedica · Verificación de predefensa ISW 234

Tarea 8 · 13 de noviembre de 2026 · Rama `crud-citas` · Base `0219b18`

La matriz usa los criterios de la Tarea 8 y la especificación del proyecto. La guía docente se menciona en la planificación y su archivo no está en el repositorio.

## Matriz de cumplimiento

| Requisito | Estado | Evidencia |
| --- | --- | --- |
| Compilar e instalar el APK debug | CUMPLE | **Ejecutado.** `assembleDebug` terminó correctamente. Se instaló el APK y se abrió `MainActivity` en `Medium_Phone`, Android 17. |
| Dependencias de la variante debug | CUMPLE | **Ejecutado.** Gradle resolvió `debugRuntimeClasspath` y completó el empaquetado y la comprobación de clases duplicadas. **Estático.** Se revisaron el catálogo y las dependencias directas de Room, Retrofit, Lifecycle y Navigation Compose. |
| Navegación multipantalla | CUMPLE | **Ejecutado.** `FlujoPacientesTest`, `FlujoCitasTest` y la prueba manual de openFDA recorrieron Inicio, alta, búsqueda/listado, historial de pacientes, formulario/listado/detalle de citas y catálogo. Se comprobó el regreso entre destinos. **Estático.** `AppNavigation.kt` define el `NavHost`, `navigate()` y `popBackStack()`. |
| Alta, búsqueda y actualización del listado de pacientes | CUMPLE | **Ejecutado.** Se registró un paciente ficticio, apareció en la búsqueda activa al regresar y su CI coincidió en el historial abierto desde ambos listados. |
| CRUD de citas y roles demo | CUMPLE | **Ejecutado.** Las pruebas crearon y editaron una cita pendiente, aceptaron y rechazaron propuestas, verificaron las restricciones del rol Paciente y comprobaron la cancelación y confirmación del borrado. |
| Persistencia tras cerrar y reabrir AppMedica | CUMPLE | **Ejecutado.** Se detuvo el proceso con `adb shell am force-stop com.example.appmedica`, se comprobó que no seguía activo y se relanzó. Un paciente nuevo reapareció en la búsqueda. Una cita nueva reapareció en la agenda con su paciente, motivo y estado pendiente. |
| Integridad entre pacientes y citas | CUMPLE | **Ejecutado.** Room rechazó una cita con paciente inexistente e impidió borrar un paciente referenciado. Se pudo eliminar el paciente después de borrar su cita. La migración 1 → 2 conservó el paciente y sus diagnósticos. **Estático.** La clave foránea usa `ON DELETE RESTRICT`. |
| Formularios y mensajes de validación | CUMPLE | **Ejecutado.** La UI mostró los mensajes para campos vacíos, CI con signo y celular con espacio. Las pruebas unitarias rechazaron fechas inexistentes, horas inválidas y citas en el pasado. El repositorio rechazó CI y correo duplicados. |
| Identificadores y registros inexistentes | CUMPLE | **Ejecutado en Room/Repository.** Las consultas por ID inexistente devolvieron ausencia y editar, responder o eliminar una cita inexistente fue rechazado. **Estático.** Las pantallas de detalle e historial manejan el resultado ausente. |
| Flow, StateFlow y reactividad | CUMPLE | **Estático.** Los DAO exponen `Flow`, los ViewModels derivan `StateFlow` y los destinos de Compose recogen el estado con `collectAsStateWithLifecycle()`. Los listados derivan sus datos de Room. **Ejecutado.** El alta y las respuestas se reflejaron sin recargar manualmente. |
| Filtros SQL por estado | CUMPLE | **Estático.** `CitaDao.observarFiltradas` usa un `WHERE` parametrizado por estado y paciente. **Ejecutado.** Se probaron Todas, Pendientes, Aceptadas y Rechazadas. Al rechazar una pendiente salió del filtro y su detalle siguió mostrando el estado correcto. |
| Retrofit, GET y visualización remota | CUMPLE | **Estático.** Se revisaron la URL base, `ApiService`, DTO, permiso INTERNET y repositorio remoto. **Ejecutado.** La app mostró el código NDC `0283-1820` devuelto por `GET https://api.fda.gov/drug/ndc.json?limit=10`. |
| Carga, error y reintento de la API | CUMPLE | **Ejecutado.** Compose mostró el estado de carga en una prueba aislada. En el emulador, sin Wi‑Fi ni datos, el catálogo mostró el error y Reintentar. Al restaurar la red y pulsar el botón se cargó el catálogo y se pudo volver a Inicio. |
| Separación de responsabilidades y paleta | CUMPLE | **Estático.** Se revisaron las capas UI, navegación, ViewModel, Repository, Room y Retrofit, y el uso de la paleta compartida. **Ejecutado.** Se usaron las pantallas en el emulador. |
| Teléfono físico y errores HTTP específicos | NO VERIFICADO | La ejecución se hizo en `Medium_Phone`. Se provocó un fallo real de conexión. No se forzaron respuestas HTTP 429/5xx ni JSON inválido del proveedor. |

## Ejecución

Desde `AppMovil-GestionMedica/`, con el AVD iniciado:

```powershell
$env:GRADLE_USER_HOME = Join-Path $env:USERPROFILE '.gradle'
.\gradlew.bat assembleDebug testDebugUnitTest connectedDebugAndroidTest --offline --console=plain
.\gradlew.bat :app:dependencies --configuration debugRuntimeClasspath --offline --console=plain
```

Resultado final: `BUILD SUCCESSFUL`, 6 pruebas unitarias y 9 instrumentadas, sin fallos ni omisiones. Se añadieron casos para integridad referencial, IDs inexistentes y la interfaz de carga de openFDA. La revisión no encontró defectos de producción que bloquearan la predefensa.

Para repetir el arranque en frío, registra un paciente y una cita con un motivo único. Detén el proceso con `adb shell am force-stop com.example.appmedica` y vuelve a lanzar `adb shell am start -W -n com.example.appmedica/.MainActivity`. Busca el paciente y abre el detalle de la cita.

Para repetir la prueba de red, desactiva Wi‑Fi y datos del emulador, abre el catálogo desde Inicio y espera el mensaje de error. Restaura la red, pulsa **Reintentar** y confirma que aparecen códigos NDC y que **Volver** regresa a Inicio.

## Guion de demostración

1. Abre Inicio. Registra un paciente ficticio, búscalo por nombre y abre su historial.
2. En **Agenda de citas → Médico demo → Crear cita**, intenta guardar el formulario vacío. Selecciona al paciente e ingresa fecha y hora futuras y un motivo. Guarda, abre el detalle y edita el motivo.
3. Regresa a la agenda, selecciona **Pendientes**, cambia a **Paciente demo** y elige al mismo paciente. Acepta la cita y vuelve. Comprueba que desaparece de **Pendientes**, aparece en **Aceptadas** y sigue visible en **Todas**. Repite con otra cita para demostrar **Rechazadas**.
4. Detén AppMedica, vuelve a abrirla y comprueba el paciente y las citas guardadas. En **Médico demo**, elimina una cita de prueba y verifica el listado.
5. Abre **Catálogo de medicamentos · openFDA**. Muestra el nombre y el código NDC. Repite el acceso con la red desactivada, restáurala y pulsa **Reintentar**.
6. En el código, muestra `CitaEntity`, `CitaDao`, `CitaRepository`, `CitasViewModel`, `AppNavigation` y `ApiService`.
