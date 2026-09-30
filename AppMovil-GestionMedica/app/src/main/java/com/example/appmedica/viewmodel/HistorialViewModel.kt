package com.example.appmedica.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.appmedica.db.DBClass
import com.example.appmedica.model.Diagnostico
import com.example.appmedica.model.Paciente

/**
 * ViewModel encargado de consultar el object DBClass.
 * Obtiene el paciente real seleccionado sin ejecutar scripts automáticos
 * ni sobreescribir pacientes recién registrados.
 */
class HistorialViewModel : ViewModel() {

    var pacienteActual by mutableStateOf<Paciente?>(null)
        private set

    var listaDiagnosticos by mutableStateOf<List<Diagnostico>>(emptyList())
        private set

    /**
     * Carga los datos del paciente buscando en DBClass por su ID o usando el paciente recibido.
     * Si no se especifica paciente ni ID, toma el primero disponible en DBClass.
     */
    fun cargarDatosHistorial(paciente: Paciente?, pacienteId: String? = null) {
        val idABuscar = paciente?.id ?: pacienteId

        val pacienteDeDB = when {
            idABuscar != null -> DBClass.getPacientePorId(idABuscar) ?: DBClass.getPaciente(idABuscar) ?: paciente
            paciente != null -> DBClass.getPaciente(paciente.nombre) ?: paciente
            else -> DBClass.getPacientes().firstOrNull()
        }

        pacienteActual = pacienteDeDB
        listaDiagnosticos = pacienteDeDB?.diagnosticos ?: emptyList()
    }
}
