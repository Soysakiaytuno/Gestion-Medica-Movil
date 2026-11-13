package com.example.appmedica.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.appmedica.data.local.PacientesEnMemoria
import com.example.appmedica.model.Paciente

class HistorialViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val paciente: Paciente? = savedStateHandle.get<String>(PACIENTE_ID)
        ?.let(PacientesEnMemoria::getPacientePorId)

    companion object {
        const val PACIENTE_ID = "pacienteId"
    }
}
