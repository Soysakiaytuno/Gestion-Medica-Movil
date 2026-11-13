package com.example.appmedica.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appmedica.repository.PacienteRepository
import kotlinx.coroutines.flow.flowOf

class HistorialViewModel(repository: PacienteRepository, pacienteId: String?) : ViewModel() {
    val paciente = pacienteId?.let(repository::observarPaciente) ?: flowOf(null)
}
