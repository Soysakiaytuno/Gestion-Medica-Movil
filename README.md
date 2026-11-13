# App de Gestión Médica

AppMedica es una demostración académica para ISW 234. Usa datos ficticios para pacientes y citas.

## Ejecutar en Android Studio

1. Abre la carpeta `AppMovil-GestionMedica/` como proyecto Gradle. La configuración del proyecto usa Android SDK 37 y JDK 25 para el daemon de Gradle.
2. Inicia el emulador `Medium_Phone` o un dispositivo compatible con Android API 26 o posterior. Selecciona la configuración `app` y pulsa **Run**.
3. Desde PowerShell, en `AppMovil-GestionMedica/`, puedes compilar y ejecutar las pruebas con:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest connectedDebugAndroidTest
```

El APK debug se genera en `app/build/outputs/apk/debug/app-debug.apk`. El catálogo openFDA requiere conexión a Internet.

## Demostración

1. Desde Inicio, registra un paciente ficticio con CI de 7 u 8 dígitos y correo único. Búscalo y abre su historial. El paciente precargado Josue Balbontin tiene diagnósticos de muestra.
2. En **Agenda de citas → Médico demo**, intenta guardar una cita vacía para mostrar la validación. Después selecciona al paciente y guarda una fecha futura, una hora y un motivo. Abre el detalle y edita el motivo.
3. En **Paciente demo**, selecciona al mismo paciente y acepta o rechaza una propuesta pendiente. Muestra cómo cambia el listado al seleccionar **Pendientes**, **Aceptadas**, **Rechazadas** y **Todas**.
4. Detén y vuelve a abrir AppMedica. Busca el paciente y la cita en sus listados para demostrar la persistencia de Room.
5. Desde Inicio abre **Catálogo de medicamentos · openFDA**. Muestra los datos recibidos por Retrofit. Para demostrar el error, vuelve a abrir el catálogo sin conexión, restaura la red y pulsa **Reintentar**.

Consulta [PREDEFENSA_CHECKLIST.md](PREDEFENSA_CHECKLIST.md) para ver la matriz y los pasos de verificación.

[Diseño](https://www.figma.com/design/u9zwv9qMRvq12o8gPDTP3s/Sin-t%C3%ADtulo?node-id=0-1&t=clzOFUhKkY7PA9cA-1)

[Informe del Primer Avance](https://docs.google.com/document/d/1NH08ApnyL7F5uHz0nlIjuuh1x5oSxe_G3dIds0iCsCs/edit?usp=sharing)
