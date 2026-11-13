package com.example.appmedica.viewmodel

import android.util.Patterns
import android.database.SQLException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmedica.model.Paciente
import com.example.appmedica.repository.PacienteRepository
import com.example.appmedica.repository.ResultadoRegistroPaciente
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlinx.coroutines.launch

data class RegistroPacienteFormulario(
    val nombre: String = "",
    val apellidoPaterno: String = "",
    val apellidoMaterno: String = "",
    val ci: String = "",
    val fechaNacimiento: String = "",
    val email: String = "",
    val celular: String = ""
)

class RegistroViewModel(private val repository: PacienteRepository) : ViewModel() {
    var formulario by mutableStateOf(RegistroPacienteFormulario())
        private set

    var mensajeError by mutableStateOf("")
        private set

    var guardando by mutableStateOf(false)
        private set

    fun actualizarFormulario(cambio: (RegistroPacienteFormulario) -> RegistroPacienteFormulario) {
        formulario = cambio(formulario)
    }

    fun guardar(alGuardar: () -> Unit) {
        if (guardando) return
        val paciente = validarFormulario() ?: return
        guardando = true
        viewModelScope.launch {
            try {
                when (repository.registrar(paciente)) {
                    ResultadoRegistroPaciente.GUARDADO -> alGuardar()
                    ResultadoRegistroPaciente.CI_DUPLICADO -> mensajeError = "Ya existe un paciente con ese CI."
                    ResultadoRegistroPaciente.EMAIL_DUPLICADO -> mensajeError = "Ya existe un paciente con ese email."
                }
            } catch (_: SQLException) {
                mensajeError = "No se pudo guardar el paciente. Intenta nuevamente."
            } finally {
                guardando = false
            }
        }
    }

    private fun validarFormulario(): Paciente? = with(formulario) {
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
        } else if (ciLimpio.length !in 7..8 || ciLimpio.any { it !in '0'..'9' }) {
            mensajeError = "El CI debe tener 7 u 8 dígitos, sin signos ni espacios internos."
        } else if (celularLimpio.length < 8 || celularLimpio.any { it !in '0'..'9' }) {
            mensajeError = "El celular debe tener al menos 8 dígitos, sin signos ni espacios internos."
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailLimpio).matches()) {
            mensajeError = "El formato del email no es válido (ej. nombre@correo.com)."
        } else if (fecha == null) {
            mensajeError = "Formato de fecha inválido. Usa AAAA-MM-DD (ej. 2006-05-29)."
        } else if (fecha.isAfter(LocalDate.now())) {
            mensajeError = "La fecha de nacimiento no puede ser futura."
        } else {
            mensajeError = ""
            return@with Paciente(
                nombre = nombre.trim(),
                apellidoPaterno = apellidoPaterno.trim(),
                apellidoMaterno = apellidoMaterno.trim(),
                ci = ciLimpio,
                fechaNacimiento = fecha,
                email = emailLimpio,
                celular = celularLimpio,
                diagnosticos = emptyList()
            )
        }
        null
    }

    private fun parsearFecha(texto: String): LocalDate? {
        return try {
            LocalDate.parse(texto.trim())
        } catch (e: DateTimeParseException) {
            null
        }
    }
}
