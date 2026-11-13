# Tarea 5 Integración de citas

La base recibida en `1dd79f7` implementaba las tareas anteriores para pacientes,
pero todavía no contenía citas. Este cambio agrega la persistencia mínima de
citas y su flujo de demostración sobre la arquitectura existente. Conserva el
registro, la búsqueda, el historial y la paleta de pacientes.

## Flujo y decisiones

- Inicio → Agenda de citas → Médico demo: crear, consultar, editar pendientes y
  eliminar con confirmación.
- Paciente demo: seleccionar un paciente registrado, consultar sus propuestas y
  aceptar o rechazar las pendientes. No ofrece acciones para crear, editar o eliminar.
- Una propuesta respondida no se edita ni se vuelve a responder. El médico puede
  eliminarla o crear otra propuesta. La actualización condicional en SQL evita
  sobrescribir respuestas anteriores.
- Los campos son obligatorios; fecha y hora deben existir y estar en el futuro.
- Room es la fuente de datos; Repository → ViewModel/StateFlow → Compose refleja
  los cambios. La próxima consulta del inicio usa la agenda guardada.
- La migración 1 → 2 añade `citas`, su índice y relación con `pacientes`, sin
  borrar ni reconstruir la tabla de pacientes. Se conserva el esquema 1.
- La selección de rol y paciente es una simulación local, no autenticación ni
  autorización segura. No sincroniza dispositivos.
- No incluye el filtro por estado (Tarea 6), Retrofit (Tarea 7) ni recetas.

## Verificación

Resultado en esta intervención: APK compilado; seis pruebas unitarias aprobadas
(cinco nuevas y la prueba de ejemplo existente); APK de pruebas Android
compilado; lint con cero errores y 21 advertencias en archivos preexistentes
(dependencias, recursos y manifiesto); `git diff --check` sin incidencias.

Desde `AppMovil-GestionMedica`:

```powershell
.\gradlew.bat :app:assembleDebug --no-configuration-cache
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebugAndroidTest :app:lintDebug --no-configuration-cache
```

Las cinco pruebas de `ValidacionCitaTest` cubren campos vacíos, fechas inexistentes,
años bisiestos, formatos, horas inválidas y fechas pasadas. Las pruebas Android
`CitaRepositoryTest` cubren CRUD, respuestas válidas e inválidas, emisiones Flow,
reapertura de la base y migración conservando pacientes y diagnósticos.
`FlujoCitasTest` recorre creación inválida/válida, edición, cambio de rol,
aceptación, recreación de actividad y eliminación con confirmación.

Ejecución Android y revisión visual: **NO VERIFICADO** en esta intervención;
`adb devices -l` no encontró dispositivos conectados. Compilar las pruebas no
equivale a ejecutarlas. Con un emulador o teléfono conectado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest --no-configuration-cache
```

Para verificar manualmente, registrar una cita futura desde Médico demo,
editarla, pasar a Paciente demo y seleccionar el mismo paciente. Aceptarla o
rechazarla y comprobar que ya no ofrece responder. Cerrar por completo y abrir
la aplicación: la cita y su estado deben seguir guardados. Volver a Médico demo
y probar conservar/eliminar en el diálogo de confirmación. Comprobar también
que un segundo paciente no ve las propuestas del primero.

La recreación de actividad del test de interfaz no sustituye el cierre del
proceso; la prueba de repositorio sí cierra y vuelve a abrir el archivo SQLite.
