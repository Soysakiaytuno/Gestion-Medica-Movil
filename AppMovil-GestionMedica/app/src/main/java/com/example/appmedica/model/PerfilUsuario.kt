
package com.example.appmedica.model

data class PerfilUsuario(
    val uid: String,
    val nombre: String,
    val correo: String,
    val rol: RolUsuario,
    val activo: Boolean,
    val pacienteId: String? = null
)
