package com.example.appmedica.viewmodel

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.appmedica.db.DBClass
import com.example.appmedica.model.Diagnostico
import com.example.appmedica.model.Paciente
import java.time.LocalDate

/**
 * ViewModel encargado de consultar el object DBClass tal cual está definido,
 * sin modificar MainActivity ni DBClass.
 */
class HistorialViewModel : ViewModel() {

    var pacienteActual by mutableStateOf<Paciente?>(null)
        private set

    var listaDiagnosticos by mutableStateOf<List<Diagnostico>>(emptyList())
        private set

    init {
        registrarPacientePruebaSiNoExiste()
    }

    /**
     * Registra el paciente de prueba en DBClass usando DBClass.agregar()
     * únicamente si aún no existe en la base de datos simulada.
     */
    @SuppressLint("NewApi")
    private fun registrarPacientePruebaSiNoExiste() {
        if (DBClass.getPaciente("Josue") == null) {
            DBClass.agregar(
                Paciente(
                    nombre = "Josue",
                    apellidoPaterno = "Balbontin",
                    apellidoMaterno = "Perez",
                    ci = "8493021",
                    email = "josue.balbontin@email.com",
                    celular = "70012345",
                    diagnosticos = listOf(
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
                    ),
                    fechaNacimiento = LocalDate.of(1999, 5, 14)
                )
            )
        }
    }

    /**
     * Obtiene los datos del paciente exclusivamente usando los métodos de DBClass
     * (DBClass.getPaciente y DBClass.getPacientes).
     */
    fun cargarDatosHistorial(paciente: Paciente?) {
        val pacienteDeDB = if (paciente != null) {
            DBClass.getPaciente(paciente.nombre) ?: paciente
        } else {
            DBClass.getPaciente("Josue") ?: DBClass.getPacientes().firstOrNull()
        }

        pacienteActual = pacienteDeDB
        listaDiagnosticos = pacienteDeDB?.diagnosticos ?: emptyList()
    }
}
