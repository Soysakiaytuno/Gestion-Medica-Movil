package com.example.appmedica.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmedica.model.Paciente
import com.example.appmedica.repository.PacienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class BuscarPacienteViewModel(repository: PacienteRepository) : ViewModel() {
    private val consulta = MutableStateFlow("")
    val query = consulta.asStateFlow()
    val pacientes: StateFlow<List<Paciente>> = repository.pacientes
        .combine(consulta) { todos, texto ->
            val busqueda = texto.trim()
            if (busqueda.isEmpty()) todos else todos.filter { paciente ->
                "${paciente.nombre} ${paciente.apellidoPaterno} ${paciente.apellidoMaterno}"
                    .contains(busqueda, ignoreCase = true)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(nuevoTexto: String) {
        consulta.value = nuevoTexto
    }
}
