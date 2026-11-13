package com.example.appmedica.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmedica.model.Paciente
import com.example.appmedica.repository.PacienteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class HistorialViewModel(repository: PacienteRepository, pacienteId: String?) : ViewModel() {
    val paciente: StateFlow<Paciente?> = (pacienteId?.let(repository::observarPaciente) ?: flowOf(null))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
