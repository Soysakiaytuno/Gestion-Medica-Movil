package com.example.appmedica.viewmodel

import android.database.SQLException
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmedica.model.Paciente
import com.example.appmedica.repository.PacienteRepository
import com.example.appmedica.repository.ResultadoRegistroPaciente
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

data class RegistroPacienteEstado(
    val formulario: RegistroPacienteFormulario = RegistroPacienteFormulario(),
    val mensajeError: String = "",
    val guardando: Boolean = false
)

class RegistroViewModel(private val repository: PacienteRepository) : ViewModel() {
    private val estadoMutable = MutableStateFlow(RegistroPacienteEstado())
    val estado = estadoMutable.asStateFlow()

    fun actualizarFormulario(cambio: (RegistroPacienteFormulario) -> RegistroPacienteFormulario) {
        estadoMutable.update { it.copy(formulario = cambio(it.formulario)) }
    }

    fun guardar(alGuardar: () -> Unit) {
        if (estado.value.guardando) return
        val paciente = validarFormulario(estado.value.formulario) ?: return
        estadoMutable.update { it.copy(guardando = true, mensajeError = "") }
        viewModelScope.launch {
            try {
                when (repository.registrar(paciente)) {
                    ResultadoRegistroPaciente.GUARDADO -> alGuardar()
                    ResultadoRegistroPaciente.CI_DUPLICADO -> mostrarError("Ya existe un paciente con ese CI.")
                    ResultadoRegistroPaciente.EMAIL_DUPLICADO -> mostrarError("Ya existe un paciente con ese email.")
                }
            } catch (_: SQLException) {
                mostrarError("No se pudo guardar el paciente. Intenta nuevamente.")
            } finally {
                estadoMutable.update { it.copy(guardando = false) }
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        estadoMutable.update { it.copy(mensajeError = mensaje) }
    }

    private fun validarFormulario(formulario: RegistroPacienteFormulario): Paciente? = with(formulario) {
        val fecha = parsearFecha(fechaNacimiento)
        val ciLimpio = ci.trim()
        val celularLimpio = celular.trim()
        val emailLimpio = email.trim()

        if (
            nombre.isBlank() || apellidoPaterno.isBlank() ||
            apellidoMaterno.isBlank() || ciLimpio.isBlank() ||
            fechaNacimiento.isBlank() || email.isBlank() || celularLimpio.isBlank()
        ) {
            mostrarError("Todos los campos son obligatorios.")
        } else if (ciLimpio.length !in 7..8 || ciLimpio.any { it !in '0'..'9' }) {
            mostrarError("El CI debe tener 7 u 8 dígitos, sin signos ni espacios internos.")
        } else if (celularLimpio.length < 8 || celularLimpio.any { it !in '0'..'9' }) {
            mostrarError("El celular debe tener al menos 8 dígitos, sin signos ni espacios internos.")
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailLimpio).matches()) {
            mostrarError("El formato del email no es válido (ej. nombre@correo.com).")
        } else if (fecha == null) {
            mostrarError("Formato de fecha inválido. Usa AAAA-MM-DD (ej. 2006-05-29).")
        } else if (fecha.isAfter(LocalDate.now())) {
            mostrarError("La fecha de nacimiento no puede ser futura.")
        } else {
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
