
package com.example.appmedica.repository

import com.example.appmedica.model.IdTokenInfo
import com.example.appmedica.model.PerfilUsuario
import com.example.appmedica.model.RolUsuario
import com.example.appmedica.model.SesionUsuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): SesionUsuario {

        require(correo.isNotBlank()) {
            "El correo no puede estar vacío"
        }

        require(contrasena.isNotBlank()) {
            "La contraseña no puede estar vacía"
        }

        // 1. Autenticar las credenciales con Firebase
        val resultado = auth.signInWithEmailAndPassword(
            correo.trim(),
            contrasena
        ).await()

        try {
            val usuario = resultado.user
                ?: error("No se pudo obtener el usuario autenticado")

            // 2. Consultar el perfil utilizando el UID autenticado
            val documento = firestore.collection("perfiles")
                .document(usuario.uid)
                .get()
                .await()

            check(documento.exists()) {
                "El usuario no tiene un perfil registrado"
            }

            // 3. Convertir el rol de Firestore a nuestro enum
            val rol = when (documento.getString("rol")) {
                "MEDICO" -> RolUsuario.MEDICO
                "PACIENTE" -> RolUsuario.PACIENTE
                else -> error("El perfil tiene un rol inválido")
            }

            check(documento.getBoolean("activo") == true) {
                "El perfil está desactivado"
            }

            val nombre = documento.getString("nombre")
                ?.takeIf { it.isNotBlank() }
                ?: error("El perfil no tiene nombre")

            val correoPerfil = documento.getString("correo")
                ?.takeIf { it.isNotBlank() }
                ?: error("El perfil no tiene correo")

            val pacienteId = documento.getString("pacienteId")
                ?.takeIf { it.isNotBlank() }

            check(rol != RolUsuario.PACIENTE || pacienteId != null) {
                "El paciente no tiene un identificador asociado"
            }

            // 4. Construir el perfil
            val perfil = PerfilUsuario(
                uid = usuario.uid,
                nombre = nombre,
                correo = correoPerfil,
                rol = rol,
                activo = true,
                pacienteId = pacienteId
            )

            // 5. Devolver la sesión autenticada
            return SesionUsuario(
                perfil = perfil,
                correoVerificado = usuario.isEmailVerified
            )

        } catch (excepcion: Exception) {
            // No conservar una sesión sin perfil válido
            auth.signOut()
            throw excepcion
        }
    }

    // 6. Cerrar la sesión del usuario en Firebase
    fun cerrarSesion() {
        auth.signOut()
    }

    // 7. Consultar los metadatos del JWT
    suspend fun obtenerInfoToken(): IdTokenInfo {
        val usuario = auth.currentUser
            ?: error("No hay una sesión iniciada")

        val resultadoToken = usuario.getIdToken(false).await()

        return IdTokenInfo(
            emitidoEnSegundos = resultadoToken.issuedAtTimestamp,
            expiraEnSegundos = resultadoToken.expirationTimestamp
        )
    }
}
