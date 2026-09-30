package com.example.appmedica.viewmodel

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.appmedica.db.DBClass
import com.example.appmedica.model.Paciente
import java.time.LocalDate
import java.time.format.DateTimeParseException

class RegistroViewModel : ViewModel() {

    var nombre by mutableStateOf("")
    var apellidoPaterno by mutableStateOf("")
    var apellidoMaterno by mutableStateOf("")
    var ci by mutableStateOf("")
    var fechaNacimiento by mutableStateOf("")
    var email by mutableStateOf("")
    var celular by mutableStateOf("")

    var mensajeError by mutableStateOf("")

    fun onGuardar(onGuardarPaciente: (Paciente) -> Unit) {
        val fecha = parsearFecha(fechaNacimiento)
        val ciLimpio = ci.trim()
        val celularLimpio = celular.trim()
        val emailLimpio = email.trim()

        if (
            nombre.isBlank() || apellidoPaterno.isBlank() ||
            apellidoMaterno.isBlank() || ciLimpio.isBlank() ||
            fechaNacimiento.isBlank() || email.isBlank() || celularLimpio.isBlank()
        ) {
            mensajeError = "Todos los campos son obligatorios."
        } else if (ciLimpio.toLongOrNull() == null || celularLimpio.toLongOrNull() == null) {
            mensajeError = "El CI y el Celular deben contener solo números."
        } else if (ciLimpio.length != 8) {
            // NUEVA REGLA: CI exactamente 8 dígitos
            mensajeError = "El CI debe tener exactamente 8 números."
        } else if (celularLimpio.length < 8) {
            // NUEVA REGLA: Celular de 8 o más dígitos
            mensajeError = "El número de celular debe tener al menos 8 números."
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailLimpio).matches()) {
            // NUEVA VALIDACIÓN: formato de email
            mensajeError = "El formato del email no es válido (ej. nombre@correo.com)."
        } else if (fecha == null) {
            mensajeError = "Formato de fecha inválido. Usa AAAA-MM-DD (ej. 2006-05-29)."
        } else if (fecha.isAfter(LocalDate.now())) {
            mensajeError = "La fecha de nacimiento no puede ser futura."
        } else if (DBClass.existeCi(ciLimpio)) {
            // NUEVA VALIDACIÓN: CI duplicado
            mensajeError = "Ya existe un paciente con ese CI."
        } else if (DBClass.existeEmail(emailLimpio)) {
            // NUEVA VALIDACIÓN: email duplicado
            mensajeError = "Ya existe un paciente con ese email."
        } else {
            mensajeError = ""
            onGuardarPaciente(
                Paciente(
                    nombre = nombre.trim(),
                    apellidoPaterno = apellidoPaterno.trim(),
                    apellidoMaterno = apellidoMaterno.trim(),
                    ci = ciLimpio,
                    fechaNacimiento = fecha,
                    email = emailLimpio,
                    celular = celularLimpio,
                    diagnosticos = emptyList()
                )
            )
        }
    }

    private fun parsearFecha(texto: String): LocalDate? {
        return try {
            LocalDate.parse(texto.trim())
        } catch (e: DateTimeParseException) {
            null
        }
    }
}