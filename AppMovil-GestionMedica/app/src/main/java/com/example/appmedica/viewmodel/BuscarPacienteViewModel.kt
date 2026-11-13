package com.example.appmedica.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.appmedica.model.Paciente
import com.example.appmedica.repository.PacienteRepository

class BuscarPacienteViewModel(repository: PacienteRepository) : ViewModel() {
    val pacientes = repository.pacientes

    var query by mutableStateOf("")
        private set

    fun onQueryChange(nuevoTexto: String) {
        query = nuevoTexto
    }

    fun pacientesFiltrados(todos: List<Paciente>): List<Paciente> {
        val busqueda = query.trim()
        if (busqueda.isEmpty()) return todos

        return todos.filter {
            "${it.nombre} ${it.apellidoPaterno} ${it.apellidoMaterno}"
                .contains(busqueda, ignoreCase = true)
        }
    }
}
