
# Configuración de perfiles en Firebase

## Autenticación
- Firebase Authentication con correo y contraseña.
- Dos cuentas creadas manualmente: médico y paciente.
- Sin registro público desde la aplicación.

## Cloud Firestore
Colección: `perfiles`

Cada documento utiliza el UID de Firebase Authentication.

### Médico
- nombre: string
- correo: string
- rol: "MEDICO"
- activo: true

### Paciente
- nombre: string
- correo: string
- rol: "PACIENTE"
- activo: true
- pacienteId: "paciente-josue-01"

## Seguridad
Las reglas están guardadas en `firestore.rules`.
Solo se permite leer el perfil propio.
Las escrituras desde la aplicación están prohibidas.

Los perfiles se administran desde Firebase Console.

No almacenar contraseñas ni tokens en el repositorio.
