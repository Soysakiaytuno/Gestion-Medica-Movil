package com.example.appmedica.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

enum class EstadoCita(val etiqueta: String) {
    PENDIENTE("Pendiente"), ACEPTADA("Aceptada"), RECHAZADA("Rechazada")
}

enum class RolDemo { MEDICO, PACIENTE }

data class Cita(
    val id: String,
    val pacienteId: String,
    val fecha: String,
    val hora: String,
    val motivo: String,
    val estado: EstadoCita = EstadoCita.PENDIENTE
)

data class FormularioCita(
    val id: String? = null,
    val pacienteId: String = "",
    val fecha: String = "",
    val hora: String = "",
    val motivo: String = ""
)

fun validarCita(formulario: FormularioCita, ahora: LocalDateTime = LocalDateTime.now()): String? {
    if (formulario.pacienteId.isBlank() || formulario.fecha.isBlank() ||
        formulario.hora.isBlank() || formulario.motivo.isBlank()) {
        return "Selecciona un paciente y completa todos los campos."
    }
    if (!Regex("\\d{4}-\\d{2}-\\d{2}").matches(formulario.fecha.trim())) {
        return "Usa una fecha válida con formato AAAA-MM-DD."
    }
    if (!Regex("\\d{2}:\\d{2}").matches(formulario.hora.trim())) {
        return "Usa una hora válida con formato HH:mm (24 horas)."
    }
    val fecha = try { LocalDate.parse(formulario.fecha.trim()) } catch (_: DateTimeParseException) {
        return "La fecha indicada no existe."
    }
    val hora = try { LocalTime.parse(formulario.hora.trim()) } catch (_: DateTimeParseException) {
        return "La hora indicada no existe."
    }
    if (!LocalDateTime.of(fecha, hora).isAfter(ahora)) return "La cita debe ser en una fecha y hora futuras."
    return null
}
