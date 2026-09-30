package com.example.appmedica.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.appmedica.model.Diagnostico
import com.example.appmedica.model.Paciente

/**
 * ViewModel encargado de gestionar y proveer la información del paciente
 * y su lista de diagnósticos para la pantalla de Historial Médico,
 * aplicando separación de responsabilidades (MVVM).
 */
class HistorialViewModel : ViewModel() {

    var pacienteActual by mutableStateOf<Paciente?>(null)
        private set

    var listaDiagnosticos by mutableStateOf<List<Diagnostico>>(emptyList())
        private set

    /**
     * Carga los datos del paciente recibido o inicializa datos de demostración
     * si aún no se ha seleccionado un paciente desde la navegación, extrayendo
     * su lista de diagnósticos para mostrarlos en la interfaz.
     */
    fun cargarDatosHistorial(paciente: Paciente?) {
        val pacienteResuelto = paciente ?: obtenerPacientePorDefecto()
        pacienteActual = pacienteResuelto
        listaDiagnosticos = obtenerDiagnosticosDePaciente(pacienteResuelto)
    }

    /**
     * Obtiene de forma controlada la lista de diagnósticos asociados al paciente.
     */
    fun obtenerDiagnosticosDePaciente(paciente: Paciente): List<Diagnostico> {
        return if (paciente.diagnosticos.isNotEmpty()) {
            paciente.diagnosticos
        } else {
            obtenerDiagnosticosDePrueba()
        }
    }

    private fun obtenerPacientePorDefecto(): Paciente {
        return Paciente(
            nombre = "Josue",
            apellidoPaterno = "Balbontin",
            apellidoMaterno = "Perez",
            ci = "8493021",
            email = "josue.balbontin@email.com",
            celular = "70012345",
            diagnosticos = obtenerDiagnosticosDePrueba()
        )
    }

    private fun obtenerDiagnosticosDePrueba(): List<Diagnostico> {
        return listOf(
            Diagnostico(
                fecha = "04/10/2026",
                titulo = "Asma",
                descripcion = "El paciente presenta Tos insistente\n\nla enfermedad es tos\n\nse le receta tal cosa\n\nporque desinflama el pulmon"
            ),
            Diagnostico(
                fecha = "18/08/2026",
                titulo = "Bronquitis Aguda",
                descripcion = "El paciente acude por dificultad respiratoria leve\n\nse detecta inflamación bronquial\n\nse receta inhalador cada 8 horas"
            )
        )
    }
}
