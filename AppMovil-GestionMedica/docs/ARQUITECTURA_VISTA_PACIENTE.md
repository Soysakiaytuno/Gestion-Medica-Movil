# Arquitectura y Documentación Técnica: Vista del Paciente (`RolUsuario.PACIENTE`)

Este documento describe el estado actual de **AppMovil-GestionMedica**, la arquitectura consolidada tras la incorporación de la vista del paciente y la justificación técnica de cada decisión de diseño.

---

## 1. Estado Actual del Proyecto

La aplicación sigue una arquitectura **MVVM + Repository + Room + Jetpack Compose Navigation**, con autenticación basada en **Firebase Auth** y perfiles de rol en **Cloud Firestore** (`perfiles/{uid}`), combinada con persistencia clínica local en **Room (`AppDatabase` v2)**.

### Roles y capacidades actuales

| Capacidad / Pantalla | `RolUsuario.MEDICO` | `RolUsuario.PACIENTE` |
| :--- | :--- | :--- |
| **Pantalla de inicio** | `InicioScreen` (buscador, accesos rápidos, próxima cita global) | `InicioPacienteScreen` (saludo, próxima cita propia, resumen de historial) |
| **Agenda / Citas** | `AgendaCitasScreen` ("Tu Agenda de Consultas", puede crear citas) | `AgendaCitasScreen` ("Mis Citas", solo sus citas, sin botón "Crear cita") |
| **Detalle de cita** | `DetalleCitaScreen` (ver, editar si pendiente, eliminar) | `DetalleCitaScreen` (ver si le pertenece, **Aceptar** / **Rechazar** si `PENDIENTE`) |
| **Historial médico** | `HistorialMedicoScreen` de cualquier paciente | `HistorialMedicoScreen` únicamente de su propio `pacienteId` (solo lectura) |
| **Buscar / Registrar paciente** | Habilitado (`BuscarPacienteScreen`, `RegistrarPacienteScreen`) | **Bloqueado** (oculto en UI y protegido en navegación) |
| **Catálogo de medicamentos** | Habilitado (`CatalogoMedicamentosScreen`) | **Bloqueado** (oculto en UI y protegido en navegación) |

---

## 2. Arquitectura y Flujo de Datos

```mermaid
flowchart TD
    Auth["AuthRepository (Firebase Auth + Firestore perfiles/{uid})"] -->|SesionUsuario + PerfilUsuario| Main["MainActivity"]
    Main -->|BarraSesion + AppNavigation| Nav["AppNavigation (startDestination según rol)"]

    Nav -->|rol == MEDICO| MedHome["InicioScreen"]
    Nav -->|rol == PACIENTE| Launch["LaunchedEffect: asegurarPacienteParaPerfil(perfil)"]
    Launch --> PacHome["InicioPacienteScreen"]

    PacHome -->|Mis citas / Próxima cita| Citas["AgendaCitasScreen (filtrada por pacienteId)"]
    Citas -->|Click en cita| Detalle["DetalleCitaScreen (Aceptar / Rechazar)"]
    PacHome -->|Mi historial médico| Hist["HistorialMedicoScreen (solo su pacienteId)"]

    subgraph Capa de Datos Local (Room)
        CitasVM["CitasViewModel"] --> CitaRepo["CitaRepository"] --> CitaDao["CitaDao"]
        Nav --> PacRepo["PacienteRepository"] --> PacDao["PacienteDao"]
    end
```

### Componentes clave incorporados o actualizados

1. **Capa de datos local (`data/local`, `repository`)**:
   - `CitaDao.observarProxima(pacienteId: String? = null)` y `CitaRepository.observarProxima(pacienteId: String? = null)`: filtran la próxima cita activa (`estado != 'RECHAZADA'` y fecha/hora futura) por `(:pacienteId IS NULL OR pacienteId = :pacienteId)`.
   - `PacienteDao.existeId(id: String)` y `PacienteRepository.asegurarPacienteParaPerfil(perfil: PerfilUsuario)`: garantizan de forma transaccional que el `pacienteId` autenticado exista en la tabla `pacientes` de Room.
2. **Capa de presentación (`viewmodel`, `ui/screens`)**:
   - `CitasViewModel.resumenProxima`: consulta la próxima cita pasando `perfil.pacienteId` cuando `perfil.rol == RolUsuario.PACIENTE`, y muestra `"No tienes próximas citas."` cuando no hay resultados.
   - `InicioPacienteScreen.kt`: interfaz dedicada para el paciente con la misma paleta (`AzulPrincipal`, `AzulOscuro`, `AzulClaro`, `AzulPildora`, `FondoBlanco`, `TextoOscuro`) y lenguaje visual que `InicioScreen`.
   - `AgendaCitasScreen` (`CitasScreen.kt`): adapta título (`"Mis Citas"`) y oculta acciones exclusivas de médico (`"Crear cita"` y aviso de registro de paciente).
3. **Navegación y punto de entrada (`navigation`, `MainActivity.kt`)**:
   - `MainActivity.kt`: sustituye la pantalla temporal de `RolUsuario.PACIENTE` por `BarraSesion(titulo = "Área del paciente", ...)` + `AppNavigation(pacientes, citas, estado.sesion.perfil)`.
   - `AppNavigation.kt`: selecciona `INICIO_PACIENTE` o `INICIO` como `startDestination` según el rol, ejecuta la sincronización preventiva en Room y aplica guardas de acceso en rutas restringidas.

---

## 3. Justificación Técnica de las Decisiones

### 3.1. Unificación de `AppNavigation` frente a crear un grafo separado
- **Decisión**: Reutilizar `AppNavigation` eligiendo `startDestination` dinámicamente (`INICIO_PACIENTE` vs `INICIO`) en lugar de duplicar un `PacienteNavigation.kt`.
- **Justificación**:
  - `AgendaCitasScreen`, `DetalleCitaScreen` e `HistorialMedicoScreen` ya comparten la misma lógica de visualización y repositorios. Separar grafos habría duplicado rutas, argumentos (`cita/{citaId}`, `historial_medico/{pacienteId}`) y manejo de `NavController`.
  - Se evita el acoplamiento inseguro añadiendo guardas explícitas *fail-closed* (`RutaRestringida`) en las 5 rutas exclusivas del médico (`INICIO`, `BUSCAR_PACIENTE`, `REGISTRAR_PACIENTE`, `CATALOGO_MEDICAMENTOS`, `formulario_cita`) **antes** de instanciar sus ViewModels, y validando en `HISTORIAL_MEDICO/{pacienteId}` que un paciente solo pueda consultar su propio `perfil.pacienteId`.

### 3.2. Clave de `CitasViewModel` compuesta por `uid`, `rol` y `pacienteId`
- **Decisión**: Instanciar `CitasViewModel` con `key = "citas-${perfil.uid}-${perfil.rol.name}-${perfil.pacienteId.orEmpty()}"`.
- **Justificación**: Evita reutilizar accidentalmente un `StateFlow` o caché de agenda entre sesiones distintas o si un perfil cambiara de rol/identificador asociado, manteniendo el aislamiento de datos por usuario.

### 3.3. Sincronización preventiva en Room (`asegurarPacienteParaPerfil`) con `withTransaction`
- **Decisión**: Al iniciar sesión como `PACIENTE`, `AppNavigation` invoca `repository.asegurarPacienteParaPerfil(perfil)` dentro de `database.withTransaction`.
- **Justificación**:
  - El proyecto utiliza un esquema híbrido: autenticación y rol en Firebase/Firestore, pero citas e historiales en Room local (`AppDatabase`, que precarga `paciente-josue-01`).
  - La tabla `citas` tiene una restricción `FOREIGN KEY(pacienteId) REFERENCES pacientes(id) ON DELETE RESTRICT`. Si un usuario autenticado con rol `PACIENTE` tiene un `pacienteId` válido en Firestore que aún no existe en la base local (por ejemplo, una cuenta nueva o tras limpiar datos locales), consultar su historial o recibir citas fallaría.
  - Al verificar `dao.existeId(pacienteId)` dentro de una transacción, **se preservan intactos los datos y diagnósticos reales** de pacientes ya existentes (como `paciente-josue-01`), y solo se inserta un registro base cuando no existe, generando valores únicos seguros para `ci` y `email` (`generarCiDisponible`, `generarEmailDisponible`) para no violar los índices únicos de Room.

### 3.4. Parametrización de `observarProxima(pacienteId: String?)` en SQL
- **Decisión**: Extender la consulta existente en `CitaDao` con `(:pacienteId IS NULL OR pacienteId = :pacienteId)` en vez de crear una segunda consulta casi idéntica.
- **Justificación**: Mantiene una única fuente de verdad SQL para determinar la próxima cita activa (`estado != 'RECHAZADA'` y fecha/hora futura ordenada cronológicamente), evitando divergencias futuras entre la vista del médico (`pacienteId = null`) y la del paciente (`pacienteId = perfil.pacienteId`).

### 3.5. Defensa en profundidad para el control de acceso por rol
- **Decisión**: Aplicar restricciones en tres capas (UI, Navegación y Repositorio/DAO).
- **Justificación**:
  1. **UI**: oculta botones y accesos que no corresponden al paciente (`InicioPacienteScreen`, `AgendaCitasScreen`, `DetalleCitaScreen`).
  2. **Navegación**: bloquea rutas de médico y accesos cruzados por `pacienteId` en `AppNavigation`.
  3. **Repositorio / DAO**: `CitaRepository.guardar` y `eliminar` validan `rol == RolUsuario.MEDICO`; `CitaRepository.responder` valida `rol == RolUsuario.PACIENTE` y `CitaDao.responderPendiente` restringe en SQL `WHERE id = :id AND pacienteId = :pacienteId AND estado = 'PENDIENTE'`. Esto garantiza integridad local incluso si una pantalla invocara una acción indebida.

---

## 4. Cobertura de Pruebas y Verificación

- **Pruebas unitarias (`./gradlew testDebugUnitTest`)**: `6/6` aprobadas (`ExampleUnitTest`, `ValidacionCitaTest`).
- **Pruebas instrumentadas (`app/src/androidTest/`)**:
  - `CitaRepositoryTest`: verifica que `observarProxima(pacienteId)` devuelve únicamente la próxima cita del paciente solicitado y no la de otros pacientes.
  - `PacienteRepositoryTest`: verifica que `asegurarPacienteParaPerfil` crea el registro cuando no existe, preserva registros existentes (como `paciente-josue-01`), y no actúa para `RolUsuario.MEDICO` ni `pacienteId` vacío.
  - `InicioPacienteScreenTest`: valida saludo personalizado, tarjeta de próxima cita (con cita y vacía), resumen del historial, accesos rápidos y ausencia de acciones de médico.
  - `CitasScreenTest`: valida el título `"Mis Citas"` y la ausencia del botón `"Crear cita"` para `RolUsuario.PACIENTE` frente a `RolUsuario.MEDICO`.
  - **Ejecución en emulador `Medium_Phone` (API 37)**: `21/21` pruebas aprobadas en la suite dirigida (con `FlujoPacientesTest` fallando de forma preexistente al inicio de la tarea por requerir autenticación previa en `MainActivity`).
