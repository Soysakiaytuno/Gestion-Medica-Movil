
package com.example.appmedica.repository

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

        if (correo.isBlank()) throw ErrorOperacion("Ingresa tu correo electrónico.")
        if (contrasena.isBlank()) throw ErrorOperacion("Ingresa tu contraseña.")

        // 1. Autenticar las credenciales con Firebase
        val resultado = auth.signInWithEmailAndPassword(
            correo.trim(),
            contrasena
        ).await()

        try {
            val usuario = resultado.user
                ?: throw ErrorOperacion("No se pudo iniciar sesión. Vuelve a intentarlo.")

            // 2. Consultar el perfil utilizando el UID autenticado
            val documento = firestore.collection("perfiles")
                .document(usuario.uid)
                .get()
                .await()

            if (!documento.exists()) {
                throw ErrorOperacion("Tu cuenta aún no está habilitada. Contacta al responsable de la aplicación.")
            }

            // 3. Convertir el rol de Firestore a nuestro enum
            val rol = when (documento.getString("rol")) {
                "MEDICO" -> RolUsuario.MEDICO
                "PACIENTE" -> RolUsuario.PACIENTE
                else -> throw ErrorOperacion("Tu cuenta necesita ser revisada antes de ingresar. Contacta al responsable.")
            }

            if (documento.getBoolean("activo") != true) {
                throw ErrorOperacion("Tu cuenta está desactivada. Contacta al responsable de la aplicación.")
            }

            val nombre = documento.getString("nombre")
                ?.takeIf { it.isNotBlank() }
                ?: throw ErrorOperacion("Faltan datos de tu cuenta. Contacta al responsable para completarlos.")

            val correoPerfil = documento.getString("correo")
                ?.takeIf { it.isNotBlank() }
                ?: throw ErrorOperacion("Faltan datos de tu cuenta. Contacta al responsable para completarlos.")

            val pacienteId = documento.getString("pacienteId")
                ?.takeIf { it.isNotBlank() }

            if (rol == RolUsuario.PACIENTE && pacienteId == null) {
                throw ErrorOperacion("Tu cuenta aún no está vinculada a una ficha de paciente. Contacta al responsable.")
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

}
